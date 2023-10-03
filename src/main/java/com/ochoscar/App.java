package com.ochoscar;

import com.ochoscar.operations.CopyFiles;
import com.ochoscar.operations.DeleteFiles;
import com.ochoscar.operations.DeleteIncomplete;

/**
 * Common operations with files
 * @author ochoscar
 */
public class App {

    public static void main(String[] args) {
        // Recover params from command line
        String operation = "";
        boolean argsError = false;
        if(args.length > 0) {
            operation = args[0];
            if(operation.equals("copy") && args.length != 2) {
                argsError = true;
            }
        } else {
            argsError = true;
        }

        if(argsError) {
            System.out.println("Usage: java -jar <operation> <path>");
            System.out.println("For operation copy you need pass source folder");
            System.out.println("For operation delete you need pass source folder");
        }

        if(operation.equals("copy")) {
            System.out.println("************************************");
            System.out.println("Starting copy operation");
            CopyFiles.execute(args);
        } else if(operation.equals("delete")) {
            System.out.println("************************************");
            System.out.println("Starting delete operation");
            DeleteFiles.execute(args);
        } else if(operation.equals("delete_incomplete")) {
            System.out.println("************************************");
            System.out.println("Starting delete incomplete operation");
            DeleteIncomplete.execute(args);
        }
    }

}
