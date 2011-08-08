package com.pomortsev.wadltestgen.config;

public class Api {
    public enum Type {
        PLAIN, SOURCE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private Type type = Type.PLAIN;
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
}
