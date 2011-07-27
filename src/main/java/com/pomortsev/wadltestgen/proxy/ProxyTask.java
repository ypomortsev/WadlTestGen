package com.pomortsev.wadltestgen.proxy;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ProxyTask {
    public static final String ATTRIB = "nhttp.proxy-task";

    private final ByteBuffer inBuffer;
    private final ByteBuffer outBuffer;

    private HttpHost target;

    private IOControl originIOControl;
    private IOControl clientIOControl;

    private ConnState originState;
    private ConnState clientState;

    private HttpRequest request;
    private HttpResponse response;

    public ProxyTask() {
        super();
        this.originState = ConnState.IDLE;
        this.clientState = ConnState.IDLE;
        this.inBuffer = ByteBuffer.allocateDirect(10240);
        this.outBuffer = ByteBuffer.allocateDirect(10240);
    }

    public ByteBuffer getInBuffer() {
        return this.inBuffer;
    }

    public ByteBuffer getOutBuffer() {
        return this.outBuffer;
    }

    public HttpHost getTarget() {
        return this.target;
    }

    public void setTarget(final HttpHost target) {
        this.target = target;
    }

    public HttpRequest getRequest() {
        return this.request;
    }

    public void setRequest(final HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return this.response;
    }

    public void setResponse(final HttpResponse response) {
        this.response = response;
    }

    public IOControl getClientIOControl() {
        return this.clientIOControl;
    }

    public void setClientIOControl(final IOControl clientIOControl) {
        this.clientIOControl = clientIOControl;
    }

    public IOControl getOriginIOControl() {
        return this.originIOControl;
    }

    public void setOriginIOControl(final IOControl originIOControl) {
        this.originIOControl = originIOControl;
    }

    public ConnState getOriginState() {
        return this.originState;
    }

    public void setOriginState(final ConnState state) {
        this.originState = state;
    }

    public ConnState getClientState() {
        return this.clientState;
    }

    public void setClientState(final ConnState state) {
        this.clientState = state;
    }

    public void reset() {
        this.inBuffer.clear();
        this.outBuffer.clear();
        this.originState = ConnState.IDLE;
        this.clientState = ConnState.IDLE;
        this.request = null;
        this.response = null;
    }

    public void shutdown() {
        if (this.clientIOControl != null) {
            try {
                this.clientIOControl.shutdown();
            } catch (IOException ignore) {}
        }
        if (this.originIOControl != null) {
            try {
                this.originIOControl.shutdown();
            } catch (IOException ignore) {}
        }
    }

}

