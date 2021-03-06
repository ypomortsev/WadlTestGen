package com.pomortsev.wadltestgen.util;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Various utility methods; mainly used by the generator when recreating the template file tree
 */
public class Utils {
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void copyStaticFiles(File inputDir, File outputDir) throws IOException {
        File[] files = inputDir.listFiles(new StaticFileFilter());

        for (File file : files) {
            File outFile = new File(outputDir, file.getName());

            if (!file.isDirectory()) {
                outputDir.mkdirs();
                copyFile(file, outFile);
            } else {
                copyStaticFiles(file, outFile);
            }
        }
    }
}
