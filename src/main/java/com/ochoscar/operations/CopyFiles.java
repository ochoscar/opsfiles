package com.ochoscar.operations;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CopyFiles {

    public static void execute(String args[]) {
        String source = args[1];
        File file = new File(source);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        List<String> dirs = Arrays.asList(directories);
        Collections.sort(dirs);

        List<String> filesToMove = new ArrayList<>();
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
                    filesToMove.add(source + "/" + dir + "/" + strFile);
                    System.out.println("Added file to processing: " + source + "/" + dir + "/" + strFile);
                }
            }
        });

        for(String forMove : filesToMove) {
            System.out.println("Moving: " + forMove + " ... to " + source + "/" + forMove.substring(forMove.lastIndexOf("/") + 1));
            try {
                FileUtils.moveFile(new File(forMove), new File(source + "/" + forMove.substring(forMove.lastIndexOf("/") + 1)));
            } catch (Exception ex) {
                System.out.println("Error moving files");
                ex.printStackTrace();
            }
        }
    }

}
