package com.ochoscar.operations.similarity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VideoDuplicateFinder {

    private static final double SIMILARITY_THRESHOLD = 0.90;
    private static final double MIN_DURATION_SECONDS = 30.0;

    private record ScoredPair(VideoMetadata meta, double score) {}

    public static void execute(String[] args) {
        try {
            String source = null;
            boolean exactMode = false;
            int threads = Math.min(Runtime.getRuntime().availableProcessors(), 16);
            for (int i = 1; i < args.length; i++) {
                if ("--exact".equals(args[i])) {
                    exactMode = true;
                } else if ("--threads".equals(args[i]) && i + 1 < args.length) {
                    threads = Math.max(1, Integer.parseInt(args[++i]));
                } else if (source == null) {
                    source = args[i];
                }
            }
            if (source == null) {
                System.err.println("Falta el path de origen");
                return;
            }
            System.out.println("Hilos para extraccion: " + threads);

            Path root = Paths.get(source);

            List<Path> videos = Files.walk(root)
                    .filter(Files::isRegularFile)
                    .filter(VideoUtils::isVideo)
                    .toList();

            System.out.println("Videos encontrados: " + videos.size());

            // =============================
            // FASE 1 - DUPLICADOS EXACTOS (opcional)
            // =============================

            if (exactMode) {
                System.out.println("\n=== FASE 1: SHA256 ===");
                Progress p1 = new Progress("SHA256", videos.size());
                Map<String, List<Path>> shaGroups = new HashMap<>();

                for (Path video : videos) {
                    String sha = HashUtils.sha256(video);
                    shaGroups.computeIfAbsent(sha, k -> new ArrayList<>())
                            .add(video);
                    p1.tick();
                }

                System.out.println("\n=== DUPLICADOS EXACTOS ===");
                shaGroups.values()
                        .stream()
                        .filter(g -> g.size() > 1)
                        .forEach(group -> {
                            System.out.println("\nGrupo:");
                            group.forEach(System.out::println);
                        });
            } else {
                System.out.println("(Fase 1 SHA256 omitida; use --exact para activarla)");
            }

            // ===================================
            // FASE 2 - METADATA Y FINGERPRINTS
            // ===================================

            System.out.println("\n=== FASE 2: METADATA ===");
            Progress p2a = new Progress("metadata", videos.size());
            List<VideoMetadata> metadataList = new ArrayList<>();

            for (Path video : videos) {
                try {
                    double duration = VideoFingerprintExtractor.getDuration(video);
                    if (duration < MIN_DURATION_SECONDS) {
                        p2a.tick();
                        continue;
                    }
                    metadataList.add(
                            new VideoMetadata(video, Files.size(video), duration));
                } catch (Exception e) {
                    p2a.newline();
                    System.err.println("Error obteniendo metadata de "
                            + video + ": " + e.getMessage());
                }
                p2a.tick();
            }

            System.out.println("\n=== FASE 2: FINGERPRINTS ===");
            Progress p2b = new Progress("fingerprint", metadataList.size());
            Map<Path, ExtendedFingerprint> fingerprints = new ConcurrentHashMap<>();

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(metadataList.size());

            for (VideoMetadata metadata : metadataList) {
                executor.submit(() -> {
                    try {
                        fingerprints.put(
                                metadata.path(),
                                VideoFingerprintExtractor.extract(metadata.path()));
                    } catch (Exception e) {
                        p2b.newline();
                        System.err.println("Error procesando "
                                + metadata.path() + ": " + e.getMessage());
                    } finally {
                        p2b.tick();
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            // ===================================
            // FASE 3 - AGRUPAR POR SIMILITUD
            // ===================================

            System.out.println("\n=== FASE 3: COMPARACION ===");
            List<VideoMetadata> withFingerprint = metadataList.stream()
                    .filter(m -> fingerprints.containsKey(m.path()))
                    .toList();

            Progress p3 = new Progress("comparing", withFingerprint.size());
            List<Set<Path>> similarGroups = new ArrayList<>();
            Set<Path> visited = new HashSet<>();

            for (int i = 0; i < withFingerprint.size(); i++) {

                VideoMetadata metaI = withFingerprint.get(i);
                Path current = metaI.path();

                if (visited.contains(current)) {
                    p3.tick();
                    continue;
                }

                Set<Path> group = new HashSet<>();
                group.add(current);

                List<VideoMetadata> remaining = withFingerprint.subList(
                        i + 1, withFingerprint.size());

                ExtendedFingerprint fpI = fingerprints.get(current);

                List<ScoredPair> scores = remaining.parallelStream()
                        .filter(metaJ -> !visited.contains(metaJ.path()))
                        .map(metaJ -> {
                            ExtendedFingerprint fpJ = fingerprints.get(metaJ.path());
                            double sim = fpI.similarity(fpJ);
                            return new ScoredPair(metaJ, sim);
                        })
                        .toList();

                for (ScoredPair sp : scores) {
                    Path candidate = sp.meta().path();
                    if (visited.contains(candidate)) continue;

                    if (sp.score() >= SIMILARITY_THRESHOLD) {
                        group.add(candidate);
                        visited.add(candidate);
                    }
                }

                if (group.size() > 1) {
                    similarGroups.add(group);
                    visited.add(current);
                }

                p3.tick();
            }

            System.out.println("\n=== VIDEOS SIMILARES ===");

            Map<Path, VideoMetadata> metaByPath = new HashMap<>();
            for (VideoMetadata m : metadataList) {
                metaByPath.put(m.path(), m);
            }

            int groupNumber = 1;
            for (Set<Path> group : similarGroups) {
                System.out.println("\nGrupo " + groupNumber++);
                group.stream()
                        .sorted(Comparator.comparingDouble(
                                (Path p) -> metaByPath.get(p).duration()).reversed())
                        .forEach(p -> {
                            VideoMetadata m = metaByPath.get(p);
                            double mb = m.size() / (1024.0 * 1024.0);
                            System.out.printf(Locale.ROOT,
                                    "  [%7.1fs | %8.1f MB] %s%n",
                                    m.duration(), mb, p);
                        });
            }

        } catch (Exception e) {
            System.err.println("Error en la operacion de similitud: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
