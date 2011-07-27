package com.pomortsev.wadltestgen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.xmlbeans.*;

import net.java.dev.wadl.x2009.x02.ApplicationDocument;
import net.java.dev.wadl.x2009.x02.ApplicationDocument.Application;
import net.java.dev.wadl.x2009.x02.MethodDocument.Method;
import net.java.dev.wadl.x2009.x02.ParamDocument.Param;
import net.java.dev.wadl.x2009.x02.RequestDocument.Request;
import net.java.dev.wadl.x2009.x02.ResourceDocument.Resource;
import net.java.dev.wadl.x2009.x02.ResourceTypeDocument.ResourceType;
import net.java.dev.wadl.x2009.x02.ResourcesDocument.Resources;


/**
 * Created by IntelliJ IDEA.
 * User: tyler
 * Date: 7/27/11
 * Time: 2:27 PM
 *
 */
public class WadlParser {


    // Field Declarations
    private File wadl;

    //URLs to combing representing every level
    private String resourcesURL;
    // Console does not use resource path private String resourceURL;
    private String methodURL;
    //Arrays of base resources and resources inside the former
    private Resources[] resources;
    private Resource[] resource;
    //Arrays of parameters at each level
    private Param[] resourceParams;
    private Param[] methodParams;
    /* HashMaps to store resource level parameters in
     * and arrays to retrieve the value after example URL
     * is found. Separated query and template styles.
     */
    private ArrayList<String> formatArray;
    private HashMap<String, String> format;
    private ArrayList<String> queryArray;
    private HashMap<String,String> query;
    private ArrayList<String> paramArray;
    private HashMap<String,String> param;

    private Method[] methods;

    private Scanner input;


    public WadlParser(File wadl)
    {
        this.wadl = wadl;
        this.input = new Scanner(System.in);

    }

    public void printResources() throws IOException, XmlException, InterruptedException
    {
        //Verifies WADL follows Schema
        ApplicationDocument appDoc = ApplicationDocument.Factory.parse(wadl);

        Application app = appDoc.getApplication();
        this.resources = app.getResourcesArray();

        //Get Base resources and loop through making sure to add URL
        for (int i = 0; i < resources.length; i++)
        {
            this.resourcesURL = new String(resources[i].getBase());

            //Get resources located in Base resource and Loop through
            this.resource = resources[i].getResourceArray();

            for (int j = 0; j < resource.length; j++)
            {
                // Init/Clear Arrays and HashMaps
                this.formatArray = new ArrayList<String>();
                this.queryArray = new ArrayList<String>();
                this.paramArray = new ArrayList<String>();
                this.param = new HashMap<String,String>();
                this.query = new HashMap<String,String>();
                this.format = new HashMap<String,String>();

                if (resource[j].getParamArray() != null)
                {
                    this.resourceParams = resource[j].getParamArray();
                    //Check style and put in appropriate array and HashMap
                    for (int k = 0; k < resourceParams.length; k++)
                    {
                        if (resourceParams[k].getRequired())
                        {
                            if(resourceParams[k].getStyle().toString().equals("template"))
                            {
                                this.formatArray.add(resourceParams[k].getName());
                                if (resourceParams[k].isSetDefault())
                                {
                                    this.format.put(resourceParams[k].getName(), resourceParams[k].getDefault());
                                }
                                else
                                {
                                    System.out.println("Input value for required " + resourceParams[k].getName() + " template parameter in " + resource[j].getPath() + " resource:");
                                    this.format.put(resourceParams[k].getName(), this.input.next());
                                }
                            }
                            else if (resourceParams[k].getStyle().toString().equals("query"))
                            {
                                this.queryArray.add(resourceParams[k].getName());
                                if (resourceParams[k].isSetDefault())
                                {
                                    this.query.put(resourceParams[k].getName(), resourceParams[k].getDefault());
                                }
                                else
                                {
                                    System.out.println("Input value for required " + resourceParams[k].getName() + " query parameter in " + resource[j].getPath() + " resource:");
                                    this.query.put(resourceParams[k].getName(), this.input.next());
                                }
                            }
                        }
                    }
                }
                //Still looking at an individual resource, going through methods now
                this.methods = resource[j].getMethodArray();
                for (int l = 0; l < methods.length; l++)
                {
                    methodURL = new String(methods[l].getExample().getUrl());

                    /* Replace Template Parameters inside of URL
                     * Must be enclosed in {}
                     */
                    for (int m = 0; m < formatArray.size(); m++)
                        methodURL = new String(methodURL.replaceFirst("\\{" + formatArray.get(m) + "\\}", format.get(formatArray.get(m))));


                    //Add required query parameters to methodURL
                    for (int n = 0; n < queryArray.size(); n++)
                    {
                        if (n == 0)
                            methodURL = new String(methodURL.concat("?"));
                        else
                            methodURL = new String(methodURL.concat("&"));

                        methodURL = new String(methodURL.concat(queryArray.get(n) + "=" + query.get(queryArray.get(n))));
                    }
                    if (methods[l].getRequest() != null)
                    {
                        if (methods[l].getRequest().getParamArray() != null)
                        {
                            this.methodParams = methods[l].getRequest().getParamArray();

                            for (int o = 0; o < methodParams.length; o++)
                            {
                                if (methodParams[o].getRequired())
                                {
                                    this.paramArray.add(methodParams[o].getName());
                                    if (methodParams[o].isSetDefault())
                                    {
                                        this.param.put(methodParams[o].getName(), methodParams[o].getDefault());
                                    }
                                    else if (methodParams[o].isSetFixed())
                                    {
                                        this.param.put(methodParams[o].getName(), methodParams[o].getFixed());
                                    }
                                    else
                                    {
                                        System.out.println("Input value for required " + methodParams[o].getName() + " query parameter in " + methods[l].getId() + " method:");
                                        this.param.put(methodParams[o].getName(), this.input.next());
                                    }
                                }
                            }
                            for (int p = 0; p < paramArray.size(); p++)
                            {
                                if (queryArray.size() == 0 && p == 0)
                                    methodURL = new String(methodURL.concat("?"));
                                else
                                    methodURL = new String(methodURL.concat("&"));

                                methodURL = new String(methodURL.concat(paramArray.get(p) + "=" + param.get(paramArray.get(p))));
                            }
                        }
                    }
                    System.out.print(methods[l].getName() + " ");
                    System.out.println(resourcesURL + methodURL);

                }
            }
        }
    }
}
