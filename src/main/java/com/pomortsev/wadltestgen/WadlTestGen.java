package com.pomortsev.wadltestgen;

import com.beust.jcommander.JCommander;
import com.pomortsev.wadltestgen.config.CLIParameters;
import com.pomortsev.wadltestgen.config.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

        try {
            WadlParser parse = new WadlParser((new File(cfg.paths.wadlFile)));
            parse.printResources();
        }  catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // process templates

        Map<String, Object> tree = new HashMap<String, Object>();

        try {
            File templateDirectory = new File(cfg.paths.templateDirectory);
            File outputDirectory = new File(cfg.paths.outputDirectory);

            outputDirectory.mkdirs();

            // copy static non-template files from template dir

            Utils.copyStaticFiles(templateDirectory, outputDirectory);

            // process templates

            freemarker.template.Configuration tplCfg = new freemarker.template.Configuration();
            tplCfg.setDirectoryForTemplateLoading(templateDirectory);
            tplCfg.setObjectWrapper(new DefaultObjectWrapper());

            File testDir = new File(outputDirectory, "js/tests");
            testDir.mkdirs();

            OutputStreamWriter testOutput = new OutputStreamWriter(
                    new FileOutputStream(new File(testDir, "test.js")));

            File wadlFile = new File("temp");


            tree.put("wadl", freemarker.ext.dom.NodeModel.parse(wadlFile));
            tree.put("tpl", cfg.tpl);

            Template tpl = tplCfg.getTemplate("test.js.ftl");
            tpl.process(tree, testOutput);
            wadlFile.delete();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
