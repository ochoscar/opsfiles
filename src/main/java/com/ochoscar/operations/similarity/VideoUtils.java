package com.ochoscar.operations.similarity;

import java.nio.file.Path;
import java.util.Set;

public class VideoUtils {

    private static final Set<String> VIDEO_EXTENSIONS =
            Set.of(
                    ".mp4",
                    ".avi",
                    ".mkv",
                    ".mov",
                    ".wmv",
                    ".flv",
                    ".m4v");

    public static boolean isVideo(Path path) {
        String name = path
                .getFileName()
                .toString()
                .toLowerCase();
        return VIDEO_EXTENSIONS
                .stream()
                .anyMatch(name::endsWith);
    }
}
