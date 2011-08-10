package com.pomortsev.wadltestgen.config;

/**
 * API configuration that is mainly used in the templates to create correct tests
 */
public class Api {
    public enum Type {
        PLAIN, SOURCE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    /**
     * The type of API this WADL is describing. Currently the two options
     * are "PLAIN" and "SOURCE". The former uses a regular XHR call (with an optional
     * proxy) to call the API, and the latter uses the Apigee Source library.
     */
    public Type type = Type.PLAIN;
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    /**
     * Proxy settings for plain-type APIs to get around same origin policy restrictions
     */
    public Proxy proxy;
    public Proxy getProxy() { return proxy; }
    public void setProxy(Proxy proxy) { this.proxy = proxy; }

    /**
     * Apigee Source application configuration
     */
    public Source source;
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
}
