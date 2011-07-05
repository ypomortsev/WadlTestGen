package com.pomortsev.wadltestgen;

import java.io.*;
import java.nio.channels.FileChannel;

public class Utils {
    public static class NonTemplateFileFilter implements FileFilter {
        public boolean accept(File file) {
            return !file.getName().endsWith("ftl") && !file.isHidden();
        }
    }

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
        File[] files = inputDir.listFiles(new NonTemplateFileFilter());

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
