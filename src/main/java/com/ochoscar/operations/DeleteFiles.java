package com.ochoscar.operations;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class DeleteFiles {

    public static void execute(String args[]) {
        String source = args[1];
        File file = new File(source);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        List<String> dirs = new ArrayList<String>(Arrays.asList(directories));
        dirs.add("");
        Collections.sort(dirs);


        Set<String> filesToDelete = new HashSet<>();
        dirs.stream().forEach(dir -> {
            List<String> filesIntoDir = new ArrayList<>();
            File filesPath = new File(source + "/" +  dir);
            String[] files = filesPath.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isFile();
                }
            });

            if(files != null && files.length > 0) {
                for(String strFile : files) {
                    String currentPath = source + "/" + dir + (dir.equals("") ? "" : "/") + strFile;
                    filesIntoDir.add(currentPath);
                    //System.out.println("Added file to processing: " + source + "/" + dir + "/" + strFile);
                }
            }

            System.out.println("*********************************");
            System.out.println("Starting search repeated files...");
            System.out.println(dir);

            Collections.sort(filesIntoDir);
            for(String currentItem : filesIntoDir) {
                try {
                    String currentName = currentItem.substring(0, currentItem.lastIndexOf("_"));
                    int currentRes = Integer.parseInt(currentItem.substring(currentItem.lastIndexOf("_") + 1, currentItem.lastIndexOf("p.mp4")));

                    for (String findItem : filesIntoDir) {
                        try {
                            int findRes = Integer.parseInt(findItem.substring(findItem.lastIndexOf("_") + 1, findItem.lastIndexOf("p.mp4")));

                            if (findItem.startsWith(currentName) && currentRes < findRes) {
                                System.out.println("Added to delete: " + currentItem);
                                filesToDelete.add(currentItem);
                                currentRes = findRes;

                            }
                        } catch(Exception e) {}
                    }
                } catch(Exception e) {}
            }
        });

        int count = 0;
        long size = 0;
        for(String forDelete : filesToDelete) {
            System.out.println("Deleting: " + forDelete);
            try {
                File deleteFile = new File(forDelete);
                size += deleteFile.length();
                count++;
                //FileUtils.forceDelete(deleteFile);
            } catch (Exception ex) {
                System.out.println("Error deleting file");
                ex.printStackTrace();
            }
        }
        System.out.println("Total files deleted: " + count);
        System.out.println("Total bytes deleted: " + size + " bytes");
        System.out.println("Total bytes deleted: " + (size / 1024)  + " kbytes");
        System.out.println("Total bytes deleted: " + (size / (1024 * 1024))  + " mbytes");
        System.out.println("Total bytes deleted: " + (size / (1024 * 1024 * 1024))  + " gbytes");
    }

}
