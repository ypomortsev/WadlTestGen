package com.pomortsev.wadltestgen.proxy;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility functions, based on LittleProxy ProxyUtils.java source.
 */
public class ProxyUtils {
    public static String stripHost(final String uri) {
        if (!uri.startsWith("http")) {
            // It's likely a URI path, not the full URI (i.e. the host is
            // already stripped).
            return uri;
        }
        final String noHttpUri = StringUtils.substringAfter(uri, "://");
        final int slashIndex = noHttpUri.indexOf("/");
        if (slashIndex == -1) {
            return "/";
        }
        return noHttpUri.substring(slashIndex);
    }

    public static String parseHostAndPort(final String uri) {
        final String tempUri;
        if (!uri.startsWith("http")) {
            // Browsers particularly seem to send requests in this form when
            // they use CONNECT.
            tempUri = uri;
        }
        else {
            // We can't just take a substring from a hard-coded index because it
            // could be either http or https.
            tempUri = StringUtils.substringAfter(uri, "://");
        }
        final String hostAndPort;
        if (tempUri.contains("/")) {
            hostAndPort = tempUri.substring(0, tempUri.indexOf("/"));
        }
        else {
            hostAndPort = tempUri;
        }
        return hostAndPort;
    }

    public static int parsePort(final String uri) {
        if (uri.contains(":")) {
            final String portStr = StringUtils.substringAfter(uri, ":");
            return Integer.parseInt(portStr);
        }
        else if (uri.startsWith("http")) {
            return 80;
        }
        else if (uri.startsWith("https")) {
            return 443;
        }
        else {
            // Unsupported protocol -- return 80 for now.
            return 80;
        }
    }

}
