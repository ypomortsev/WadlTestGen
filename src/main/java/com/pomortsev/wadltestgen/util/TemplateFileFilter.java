package com.pomortsev.wadltestgen.util;

import java.io.File;
import java.io.FileFilter;

public class TemplateFileFilter implements FileFilter {
    /**
     * Template files end with the extension "ftl", but we ignore
     * those starting with an underscore (in case we want to include a .ftl
     * inside another), and files hidden from the system
     */
    public boolean accept(File file) {
        String filename = file.getName();
        return filename.endsWith("ftl") &&
               !filename.startsWith("_") &&
               !file.isHidden();
    }
}
