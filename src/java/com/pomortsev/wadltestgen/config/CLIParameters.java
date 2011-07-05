package com.pomortsev.wadltestgen.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;

public class CLIParameters {
    @Parameter(names = "-cfg", converter = FileConverter.class)
    public File cfgFile = null;
}
