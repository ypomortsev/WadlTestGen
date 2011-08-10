package com.pomortsev.wadltestgen.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;

/**
 * Command-line parameters for the main WadlTestGen application
 */
public class CLIParameters {
    @Parameter(names = "-cfg",
               description = "YAML configuration file",
               required = true,
               converter = FileConverter.class)
    public File cfgFile = null;
}
