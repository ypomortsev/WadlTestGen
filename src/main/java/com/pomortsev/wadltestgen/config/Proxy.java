package com.pomortsev.wadltestgen.config;

/**
 * Configuration for the optional plain-type request proxy
 */
public class Proxy {
    /**
     * Whether or not the proxy is used to make API calls
     */
    public boolean enabled = false;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Proxy base address; usually local. This is used as a simple prefix for the API
     * calls, like so: http://proxy:8080/http://example.com
     */
    public String base = "http://localhost:8080/";
    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }
}
