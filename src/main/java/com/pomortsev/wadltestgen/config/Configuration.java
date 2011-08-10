package com.pomortsev.wadltestgen.config;

import java.util.Map;

/**
 * The base configuration object that is normally loaded from a YAML file
 */
public class Configuration {
    public Paths paths;
    public Paths getPaths() { return paths; }
    public void setPaths(Paths paths) { this.paths = paths; }

    public Api api;
    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }

    public Map tpl;
    public Map getTpl() { return tpl; }
    public void setTpl(Map tpl) { this.tpl = tpl; }
}
