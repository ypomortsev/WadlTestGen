package com.pomortsev.wadltestgen.config;

public class Proxy {
    public boolean enabled = false;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String base = "http://localhost:8080/";
    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }
}
