package com.ochoscar.operations;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class DeleteIncomplete {

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
            File filesPath = new File(source + "/" +  dir);
            String[] files = filesPath.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isFile();
                }
            });

            if(files != null && files.length > 0) {
                for(String strFile : files) {
                    if(strFile.endsWith(".part")) {
                        String currentPath = source + "/" + dir + (dir.equals("") ? "" : "/") + strFile;
                        filesToDelete.add(currentPath);
                        System.out.println("Added file to delete: " + source + "/" + dir + "/" + strFile);
                    }
                }
            }
        });

        int count = 0;
        long size = 0;
        for(String forDelete : filesToDelete) {
            System.out.println("Deleting: " + forDelete);
            count++;
            try {
                File deleteFile = new File(forDelete);
                size += deleteFile.length();
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
