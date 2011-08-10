package com.pomortsev.wadltestgen.config;

/**
 * Apigee Source configuration
 */
public class Source {
    /**
     * The application name, used to construct an endpoint of the form:
     *      https://{appName}-api.apigee.com/v1
     */
    public String appName;
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    /**
     * A username associated with the Source application, used to authenticate and retrieve a Smartkey
     */
    public String username;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /**
     * A password for the given user
     */
    public String password;
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
