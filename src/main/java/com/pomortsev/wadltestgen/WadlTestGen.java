package com.pomortsev.wadltestgen;

import com.beust.jcommander.JCommander;
import com.pomortsev.wadltestgen.config.CLIParameters;
import com.pomortsev.wadltestgen.config.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The main class that invokes the test generator; meant to be used through a CLI or in a script
 */
public class WadlTestGen {
    public static void main(String[] args) {
        // parse parameters

        CLIParameters params = new CLIParameters();
        JCommander jcommander = new JCommander(params, args);

        // load config

        Yaml yaml = new Yaml(new Constructor(Configuration.class));

        Configuration cfg;
        try {
            cfg = (Configuration) yaml.load(new FileInputStream(params.cfgFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("configuration file not found.");
        }

        // run the test suite generator

        try {
            new Generator(cfg).generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
