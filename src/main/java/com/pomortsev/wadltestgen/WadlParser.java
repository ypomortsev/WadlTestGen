package com.pomortsev.wadltestgen;

import com.apigee.api.wadl.x2010.x07.ExampleType;
import net.java.dev.wadl.x2009.x02.ApplicationDocument;
import net.java.dev.wadl.x2009.x02.ApplicationDocument.Application;
import net.java.dev.wadl.x2009.x02.MethodDocument.Method;
import net.java.dev.wadl.x2009.x02.ParamDocument.Param;
import net.java.dev.wadl.x2009.x02.ParamStyle;
import net.java.dev.wadl.x2009.x02.ResourceDocument.Resource;
import net.java.dev.wadl.x2009.x02.ResourcesDocument.Resources;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;

/**
 * WADL file parser and preprocessor. Creates an XMLBeans structure from the definition
 * and modifies parts of it to aid the templating part of the generator
 */
public class WadlParser {
    private File wadl;

    public WadlParser(File wadl) {
        this.wadl = wadl;
    }

    /**
     * Parses the WADL and returns the (processed) root Application object
     *
     * @return an Application object
     * @throws XmlException
     * @throws IOException
     */
    public Application getApplication() throws XmlException, IOException {
        // Parse the WADL file into an Application object
        ApplicationDocument appDoc = ApplicationDocument.Factory.parse(wadl);
        Application app = appDoc.getApplication();

        for (Resources resources : app.getResourcesArray()) {
            // substitute template parameters into paths inside each Resource
            for (Resource resource : resources.getResourceArray()) {
                templateResourcePaths(resource);
            }
        }

        return app;
    }

    /**
     * Substitutes template parameters into the Resource's path attribute and apigee:example URLs in methods
     *
     * @param resource The resource to process
     */
    protected void templateResourcePaths(Resource resource) {
        String resourcePath = resource.getPath();

        for (Param resourceParam : resource.getParamArray()) {
            // template the resource path
            resourcePath = templatePath(resourcePath, resourceParam);

            // template the apigee:example URLs in methods
            for (Method resourceMethod : resource.getMethodArray()) {
                ExampleType example = resourceMethod.getExample();

                if (example.isSetUrl()) {
                    example.setUrl(templatePath(example.getUrl(), resourceParam));
                }
            }
        }

        resource.setPath(resourcePath);
    }

    /**
     * Substitutes the default value of the param into the given path
     * Note: This method does not URL-encode the default value
     *
     * @param path  the path string (part of an URL)
     * @param param  the parameter to template
     * @return templated path
     */
    protected String templatePath(String path, Param param) {
        // process only template parameters with a default value
        if (param.getStyle().intValue() == ParamStyle.INT_TEMPLATE &&
            (param.isSetDefault() || param.isSetFixed())) {

            // if the parameter is fixed, that value is the default
            String paramDefaultValue = param.isSetFixed() ? param.getFixed() : param.getDefault();

            // substitute into the path
            return path.replace('{' + param.getName() + '}', paramDefaultValue);
        }

        return path;
    }
}
