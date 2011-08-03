package com.pomortsev.wadltestgen;

import com.pomortsev.wadltestgen.config.Configuration;
import com.pomortsev.wadltestgen.util.Utils;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The core of the test suite generator. Takes in a configuration object and
 * creates the test suite accordingly.
 */
public class Generator {
    private Configuration cfg;

    public Generator(Configuration cfg) {
        this.cfg = cfg;
    }

    public void generate() {
        try {
            WadlParser parse = new WadlParser(new File(this.cfg.paths.wadlFile));
            parse.printResources();
        }  catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // process templates

        Map<String, Object> tree = new HashMap<String, Object>();

        try {
            File templateDirectory = new File(this.cfg.paths.templateDirectory);
            File outputDirectory = new File(this.cfg.paths.outputDirectory);

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
            tree.put("tpl", this.cfg.tpl);

            Template tpl = tplCfg.getTemplate("test.js.ftl");
            tpl.process(tree, testOutput);
            wadlFile.delete();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
