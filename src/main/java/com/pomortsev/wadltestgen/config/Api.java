package com.pomortsev.wadltestgen.config;

public class Api {
    public enum Type {
        PLAIN, SOURCE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public Type type = Type.PLAIN;
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Proxy proxy;
    public Proxy getProxy() { return proxy; }
    public void setProxy(Proxy proxy) { this.proxy = proxy; }

    public Source source;
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
}
