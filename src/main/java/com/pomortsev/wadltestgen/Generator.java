package com.pomortsev.wadltestgen;

import com.pomortsev.wadltestgen.config.Configuration;
import com.pomortsev.wadltestgen.util.TemplateFileFilter;
import com.pomortsev.wadltestgen.util.Utils;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The core of the test suite generator. Takes in a configuration object and
 * creates the test suite accordingly.
 */
public class Generator {
    protected Configuration cfg;

    public Generator(Configuration cfg) {
        this.cfg = cfg;
    }

    protected void processTemplate(File template, File outputFile) throws IOException, TemplateException {
        // initialize freemarker

        freemarker.template.Configuration tplCfg = new freemarker.template.Configuration();
        tplCfg.setDirectoryForTemplateLoading(template.getParentFile());
        tplCfg.setObjectWrapper(new DefaultObjectWrapper());

        // add template variables

        Map<String, Object> tplMap = new HashMap<String, Object>();

        try {
            // process the WADL and add it to the template
            WadlParser wadlParser = new WadlParser(new File(this.cfg.paths.wadlFile));
            tplMap.put("app", wadlParser.getApplication());
        } catch (XmlException e) {
            e.printStackTrace();
        }

        tplMap.put("cfg", this.cfg);

        // write out the template

        Template tpl = tplCfg.getTemplate(template.getName());
        tpl.process(tplMap, new OutputStreamWriter(new FileOutputStream(outputFile)));
    }

    public void processTemplates(File templateDirectory, File outputDirectory) throws IOException, TemplateException {
        File[] templateFiles = templateDirectory.listFiles(new TemplateFileFilter());

        for (File templateFile : templateFiles) {
            if (!templateFile.isDirectory()) {
                outputDirectory.mkdirs();

                File outputFile = new File(
                        outputDirectory,
                        StringUtils.removeEnd(templateFile.getName(), ".ftl"));

                processTemplate(templateFile, outputFile);
            } else {
                File newOutputDirectory = new File(outputDirectory, templateFile.getName());

                processTemplates(templateFile, newOutputDirectory);
            }
        }
    }

    public void generate() throws IOException {
        File templateDirectory = new File(this.cfg.paths.templateDirectory);
        File outputDirectory = new File(this.cfg.paths.outputDirectory);

        // copy static non-template files from template dir

        Utils.copyStaticFiles(templateDirectory, outputDirectory);

        // process templates

        try {
            processTemplates(templateDirectory, outputDirectory);
        } catch (TemplateException e) {
            // errors get logged to the console
        }
    }
}
