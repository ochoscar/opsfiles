package com.ochoscar.operations.similarity;

import java.nio.file.Path;

public record VideoMetadata(Path path, long size, double duration) {
}
