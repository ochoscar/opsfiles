package com.ochoscar;

import com.ochoscar.operations.CopyFiles;
import com.ochoscar.operations.DeleteFiles;
import com.ochoscar.operations.DeleteIncomplete;
import com.ochoscar.operations.similarity.VideoDuplicateFinder;

/**
 * Common operations with files
 * @author ochoscar
 */
public class App {

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        String operation = args[0];

        System.out.println("************************************");

        switch (operation) {
            case "copy" -> {
                System.out.println("Starting copy operation");
                CopyFiles.execute(args);
            }
            case "delete" -> {
                System.out.println("Starting delete operation");
                DeleteFiles.execute(args);
            }
            case "delete_incomplete" -> {
                System.out.println("Starting delete incomplete operation");
                DeleteIncomplete.execute(args);
            }
            case "similarity" -> {
                System.out.println("Starting video similarity operation");
                System.out.println("Requires: ffmpeg and ffprobe in PATH");
                VideoDuplicateFinder.execute(args);
            }
            default -> {
                System.err.println("Unknown operation: " + operation);
                printUsage();
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar opsfiles.jar <operation> <path>");
        System.out.println();
        System.out.println("Operations:");
        System.out.println("  copy               Move files from subdirectories to the source folder");
        System.out.println("  delete             Delete duplicate video files based on resolution");
        System.out.println("  delete_incomplete   Delete incomplete (.part) files");
        System.out.println("  similarity          Find duplicate and similar videos using visual fingerprints");
        System.out.println("                      Flags: --exact        also detect byte-identical duplicates via SHA256");
        System.out.println("                             --threads N    workers for fingerprint extraction (default: min(cores,16))");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar opsfiles.jar copy D:/Videos");
        System.out.println("  java -jar opsfiles.jar delete D:/Videos");
        System.out.println("  java -jar opsfiles.jar delete_incomplete D:/Videos");
        System.out.println("  java -jar opsfiles.jar similarity D:/Videos");
        System.out.println("  java -jar opsfiles.jar similarity D:/Videos --exact");
    }
}
