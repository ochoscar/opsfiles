package com.ochoscar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Programa para realizar operaciones comunes con archivos
 * @author ochoscar
 */
public class App {

    public static void main(String[] args) {
        // Valida los parametros
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
            System.out.println("Usage: java -jar <operation>\nFor operation copy you need pass source and destination folder");
        }

        // En caso que no haya habido error procesa la solicitud
        if(operation.equals("copy")) {
            // Obtiene la lista de directorios a procesar
            String source = args[1];
            //String destination = args[2];
            // Listar las carpetas de la ruta de origen
            File file = new File(source);
            String[] directories = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });
            List<String> dirs = Arrays.asList(directories);
            Collections.sort(dirs);

            // Por cada directorio verifica si el mismo tiene archivos completos y no este vacío para armar otra lista
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
                    //List<String> arrayFiles = Arrays.asList(files);
                    // Solo agrega las rutas que no terminan en .part
                    //if( arrayFiles.size() > 0 && arrayFiles.stream().allMatch(f -> !f.endsWith(".part"))  ) {
                    for(String strFile : files) {
                        filesToMove.add(source + "/" + dir + "/" + strFile);
                        System.out.println("Added file to processing: " + source + "/" + dir + "/" + strFile);
                    }
                }
            });

            // Mueve los directorios marcados de a la carpeta destino
            for(String forMove : filesToMove) {
                System.out.println("Moving: " + forMove + " ... to " + source + "/" + forMove.substring(forMove.lastIndexOf("/") + 1));
                try {
                    FileUtils.moveFile(new File(forMove), new File(source + "/" + forMove.substring(forMove.lastIndexOf("/") + 1)));
                } catch (Exception ex) {
                    System.out.println("Ha ocurrido un error moviendo un archivo");
                    ex.printStackTrace();
                }
            }

        }

    }

}
