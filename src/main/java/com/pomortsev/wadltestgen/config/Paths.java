package com.pomortsev.wadltestgen.config;

/**
 * Configuration for various paths that are needed to generate a test suite
 */
public class Paths {
    /**
     * The path to the WADL file that describes the API
     */
    public String wadlFile;
    public String getWadlFile() { return wadlFile; }
    public void setWadlFile(String wadlFile) { this.wadlFile = wadlFile; }

    /**
     * The directory where the generated test suite is output
     */
    public String outputDirectory;
    public String getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }

    /**
     * The directory containing templates that will be processed by the generator
     * and static files that will be copied as-is
     */
    public String templateDirectory;
    public String getTemplateDirectory() { return templateDirectory; }
    public void setTemplateDirectory(String templateDirectory) { this.templateDirectory = templateDirectory; }
}
