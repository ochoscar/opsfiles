package com.ochoscar.operations.similarity;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class VideoFingerprintExtractor {

    private static final ThreadLocal<PerceptiveHash> HASHER =
            ThreadLocal.withInitial(() -> new PerceptiveHash(32));

    private static final double TARGET_INTERVAL_SECONDS = 30.0;
    private static final int MIN_FRAMES = 10;
    private static final int MAX_FRAMES = 60;

    public static double getDuration(Path videoPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toAbsolutePath().toString());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            output = reader.readLine();
        }

        int exitCode = process.waitFor();
        if (exitCode != 0 || output == null || output.isBlank()) {
            throw new RuntimeException(
                    "ffprobe fallo para: " + videoPath
                            + " (exit code: " + exitCode + ")");
        }

        return Double.parseDouble(output.trim());
    }

    public static ExtendedFingerprint extract(Path videoPath) throws Exception {
        double duration = getDuration(videoPath);
        int frameCount = frameCountFor(duration);
        double frameInterval = duration / (frameCount + 1);

        double[] timestamps = new double[frameCount];
        for (int i = 0; i < frameCount; i++) {
            timestamps[i] = frameInterval * (i + 1);
        }

        List<Hash> hashes = batchExtractFrames(videoPath, timestamps);
        if (hashes.isEmpty()) {
            throw new RuntimeException(
                    "No se extrajeron frames para: " + videoPath);
        }
        return new ExtendedFingerprint(hashes, duration);
    }

    private static int frameCountFor(double duration) {
        int target = (int) Math.round(duration / TARGET_INTERVAL_SECONDS);
        return Math.max(MIN_FRAMES, Math.min(MAX_FRAMES, target));
    }

    private static List<Hash> batchExtractFrames(Path videoPath, double[] timestamps)
            throws Exception {
        Path tempDir = Files.createTempDirectory("frames_");
        try {
            StringBuilder selectExpr = new StringBuilder();
            for (int i = 0; i < timestamps.length; i++) {
                if (i > 0) selectExpr.append("+");
                selectExpr.append(String.format(
                        "between(t\\,%s\\,%s)",
                        formatTimestamp(timestamps[i]),
                        formatTimestamp(timestamps[i] + 0.04)));
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-threads", "1",
                    "-i", videoPath.toAbsolutePath().toString(),
                    "-vf", "select='" + selectExpr + "'",
                    "-vsync", "vfr",
                    "-frames:v", String.valueOf(timestamps.length),
                    "-y",
                    tempDir.resolve("frame_%03d.png").toAbsolutePath().toString());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            ArrayDeque<String> tail = new ArrayDeque<>();
            try (var reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    tail.addLast(line);
                    if (tail.size() > 10) tail.removeFirst();
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException(
                        "ffmpeg fallo extrayendo frames de: " + videoPath
                                + " (exit code: " + exitCode + ")\n"
                                + String.join("\n", tail));
            }

            List<Hash> hashes = new ArrayList<>();
            try (Stream<Path> files = Files.list(tempDir)) {
                List<Path> frames = files
                        .filter(p -> p.toString().endsWith(".png"))
                        .sorted(Comparator.comparing(Path::getFileName))
                        .toList();

                PerceptiveHash hasher = HASHER.get();
                for (Path frame : frames) {
                    BufferedImage image = ImageIO.read(frame.toFile());
                    if (image != null) {
                        hashes.add(hasher.hash(image));
                    }
                }
            }

            return hashes;
        } finally {
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); }
                            catch (Exception ignored) {}
                        });
            }
        }
    }

    private static String formatTimestamp(double seconds) {
        return String.format(Locale.ROOT, "%.3f", seconds);
    }
}
