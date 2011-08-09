package com.pomortsev.wadltestgen.proxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.*;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ListeningHandler implements NHttpServiceHandler {
    private final ConnectingIOReactor connectingIOReactor;
    private final HttpProcessor httpProcessor;
    private final HttpResponseFactory responseFactory;
    private final ConnectionReuseStrategy connStrategy;
    private final HttpParams params;

    public ListeningHandler(
            final ConnectingIOReactor connectingIOReactor,
            final HttpProcessor httpProcessor,
            final HttpResponseFactory responseFactory,
            final ConnectionReuseStrategy connStrategy,
            final HttpParams params) {
        super();
        this.connectingIOReactor = connectingIOReactor;
        this.httpProcessor = httpProcessor;
        this.connStrategy = connStrategy;
        this.responseFactory = responseFactory;
        this.params = params;
    }

    public void connected(final NHttpServerConnection conn) {
        System.out.println(conn + " [client->proxy] conn open");

        ProxyTask proxyTask = new ProxyTask();

        synchronized (proxyTask) {
            // Initialize connection state

            HttpContext context = conn.getContext();
            context.setAttribute(ProxyTask.ATTRIB, proxyTask);

            // this might not be the completely right thing to do here
            proxyTask.setClientState(ConnState.CONNECTED);
        }
    }

    public void requestReceived(final NHttpServerConnection conn) {
        System.out.println(conn + " [client->proxy] request received");

        HttpContext context = conn.getContext();
        ProxyTask proxyTask = (ProxyTask) context.getAttribute(ProxyTask.ATTRIB);

        synchronized (proxyTask) {
            ConnState connState = proxyTask.getClientState();
            if (connState != ConnState.IDLE && connState != ConnState.CONNECTED) {
                throw new IllegalStateException("Illegal client connection state: " + connState);
            }

            try {
                HttpRequest request = conn.getHttpRequest();

                System.out.println(conn + " [client->proxy] >> " + request.getRequestLine());

                ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
                if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
                    // Downgrade protocol version if greater than HTTP/1.1
                    ver = HttpVersion.HTTP_1_1;
                }

                // parse request line to get host

                String requestUri = StringUtils.removeStart(request.getRequestLine().getUri(), "/");
                String targetHostAndPort = ProxyUtils.parseHostAndPort(requestUri);
                String requestPath = ProxyUtils.stripHost(requestUri);

                String targetHostName = StringUtils.substringBefore(targetHostAndPort, ":");
                int targetPort = ProxyUtils.parsePort(targetHostAndPort);

                HttpHost targetHost = new HttpHost(targetHostName, targetPort);

                // make a modified copy of the request

                HttpRequest newRequest = new BasicHttpRequest(
                        request.getRequestLine().getMethod(),
                        requestPath);

                newRequest.setParams(request.getParams());
                newRequest.setHeaders(request.getAllHeaders());

                // Update connection state
                proxyTask.setTarget(targetHost);
                proxyTask.setRequest(newRequest);
                proxyTask.setClientState(ConnState.REQUEST_RECEIVED);
                proxyTask.setClientIOControl(conn);

                // connect

                InetSocketAddress address = new InetSocketAddress(
                                    targetHost.getHostName(),
                                    targetHost.getPort());

                this.connectingIOReactor.connect(
                        address,
                        null,
                        proxyTask,
                        null);

                // See if the client expects a 100-Continue
                if (request instanceof HttpEntityEnclosingRequest) {
                    if (((HttpEntityEnclosingRequest) request).expectContinue()) {
                        HttpResponse ack = this.responseFactory.newHttpResponse(
                                ver,
                                HttpStatus.SC_CONTINUE,
                                context);
                        conn.submitResponse(ack);
                    }
                } else {
                    // No request content expected. Suspend client input
                    conn.suspendInput();
                }

                // If there is already a connection to the origin server
                // make sure origin output is active
                if (proxyTask.getOriginIOControl() != null) {
                    proxyTask.getOriginIOControl().requestOutput();
                }
            } catch (IOException ex) {
                shutdownConnection(conn);
            } catch (HttpException ex) {
                shutdownConnection(conn);
            }
        }
    }

    public void inputReady(final NHttpServerConnection conn, final ContentDecoder decoder) {
        System.out.println(conn + " [client->proxy] input ready");

        HttpContext context = conn.getContext();
        ProxyTask proxyTask = (ProxyTask) context.getAttribute(ProxyTask.ATTRIB);

        synchronized (proxyTask) {
            ConnState connState = proxyTask.getClientState();
            if (connState != ConnState.REQUEST_RECEIVED
                    && connState != ConnState.REQUEST_BODY_STREAM) {
                throw new IllegalStateException("Illegal client connection state: " + connState);
            }

            try {
                ByteBuffer dst = proxyTask.getInBuffer();

                int bytesRead = decoder.read(dst);
                System.out.println(conn + " [client->proxy] " + bytesRead + " bytes read");
                System.out.println(conn + " [client->proxy] " + decoder);

                if (!dst.hasRemaining()) {
                    // Input buffer is full. Suspend client input
                    // until the origin handler frees up some space in the buffer
                    conn.suspendInput();
                }

                // If there is some content in the input buffer make sure origin
                // output is active
                if (dst.position() > 0) {
                    if (proxyTask.getOriginIOControl() != null) {
                        proxyTask.getOriginIOControl().requestOutput();
                    }
                }

                if (decoder.isCompleted()) {
                    System.out.println(conn + " [client->proxy] request body received");
                    // Update connection state
                    proxyTask.setClientState(ConnState.REQUEST_BODY_DONE);
                    // Suspend client input
                    conn.suspendInput();
                } else {
                    proxyTask.setClientState(ConnState.REQUEST_BODY_STREAM);
                }
            } catch (IOException ex) {
                shutdownConnection(conn);
            }
        }
    }

    public void responseReady(final NHttpServerConnection conn) {
        System.out.println(conn + " [client<-proxy] response ready");

        HttpContext context = conn.getContext();
        ProxyTask proxyTask = (ProxyTask) context.getAttribute(ProxyTask.ATTRIB);

        synchronized (proxyTask) {
            ConnState connState = proxyTask.getClientState();

            if (connState == ConnState.IDLE) {
                // Response not available
                return;
            }

            if (connState != ConnState.REQUEST_RECEIVED && connState != ConnState.REQUEST_BODY_DONE) {
                throw new IllegalStateException("Illegal client connection state: " + connState);
            }

            try {
                HttpRequest request = proxyTask.getRequest();
                HttpResponse response = proxyTask.getResponse();

                if (response == null) {
                    throw new IllegalStateException("HTTP request is null");
                }

                // Remove hop-by-hop headers
                response.removeHeaders(HTTP.CONTENT_LEN);
                response.removeHeaders(HTTP.TRANSFER_ENCODING);
                response.removeHeaders(HTTP.CONN_DIRECTIVE);
                response.removeHeaders("Keep-Alive");
                response.removeHeaders("Proxy-Authenticate");
                response.removeHeaders("Proxy-Authorization");
                response.removeHeaders("TE");
                response.removeHeaders("Trailers");
                response.removeHeaders("Upgrade");

                response.addHeader("Access-Control-Allow-Origin","*");

                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));

                // Close client connection if the connection to the target
                // is no longer active / open
                if (proxyTask.getOriginState().compareTo(ConnState.CLOSING) >= 0) {
                    response.addHeader(HTTP.CONN_DIRECTIVE, "Close");
                }

                // Pre-process HTTP request
                context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
                context.setAttribute(ExecutionContext.HTTP_REQUEST, request);
                this.httpProcessor.process(response, context);

                conn.submitResponse(response);

                proxyTask.setClientState(ConnState.RESPONSE_SENT);

                System.out.println(conn + " [client<-proxy] << " + response.getStatusLine());

                if (!canResponseHaveBody(request, response)) {
                    conn.resetInput();
                    if (!this.connStrategy.keepAlive(response, context)) {
                        System.out.println(conn + " [client<-proxy] close connection");
                        proxyTask.setClientState(ConnState.CLOSING);
                        conn.close();
                    } else {
                        // Reset connection state
                        proxyTask.reset();
                        conn.requestInput();
                        // Ready to deal with a new request
                    }
                }
            } catch (IOException ex) {
                shutdownConnection(conn);
            } catch (HttpException ex) {
                shutdownConnection(conn);
            }
        }
    }

    private boolean canResponseHaveBody(final HttpRequest request, final HttpResponse response) {
        if (request != null && "HEAD".equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }

        int status = response.getStatusLine().getStatusCode();
        return status >= HttpStatus.SC_OK
                && status != HttpStatus.SC_NO_CONTENT
                && status != HttpStatus.SC_NOT_MODIFIED
                && status != HttpStatus.SC_RESET_CONTENT;
    }

    public void outputReady(final NHttpServerConnection conn, final ContentEncoder encoder) {
        System.out.println(conn + " [client<-proxy] output ready");

        HttpContext context = conn.getContext();
        ProxyTask proxyTask = (ProxyTask) context.getAttribute(ProxyTask.ATTRIB);

        synchronized (proxyTask) {
            ConnState connState = proxyTask.getClientState();
            if (connState != ConnState.RESPONSE_SENT && connState != ConnState.RESPONSE_BODY_STREAM) {
                throw new IllegalStateException("Illegal client connection state: " + connState);
            }

            HttpResponse response = proxyTask.getResponse();
            if (response == null) {
                throw new IllegalStateException("HTTP request is null");
            }

            try {
                ByteBuffer src = proxyTask.getOutBuffer();
                src.flip();

                int bytesWritten = encoder.write(src);
                System.out.println(conn + " [client<-proxy] " + bytesWritten + " bytes written");
                System.out.println(conn + " [client<-proxy] " + encoder);

                src.compact();

                if (src.position() == 0) {
                    if (proxyTask.getOriginState() == ConnState.RESPONSE_BODY_DONE) {
                        encoder.complete();
                    } else {
                        // Input output is empty. Wait until the origin handler
                        // fills up the buffer
                        conn.suspendOutput();
                    }
                }

                // Update connection state
                if (encoder.isCompleted()) {
                    System.out.println(conn + " [proxy] response body sent");
                    proxyTask.setClientState(ConnState.RESPONSE_BODY_DONE);
                    if (!this.connStrategy.keepAlive(response, context)) {
                        System.out.println(conn + " [client<-proxy] close connection");
                        proxyTask.setClientState(ConnState.CLOSING);
                        conn.close();
                    } else {
                        // Reset connection state
                        proxyTask.reset();
                        conn.requestInput();
                        // Ready to deal with a new request
                    }
                } else {
                    proxyTask.setClientState(ConnState.RESPONSE_BODY_STREAM);
                    // Make sure origin input is active
                    proxyTask.getOriginIOControl().requestInput();
                }
            } catch (IOException ex) {
                shutdownConnection(conn);
            }
        }
    }

    public void closed(final NHttpServerConnection conn) {
        System.out.println(conn + " [client->proxy] conn closed");

        HttpContext context = conn.getContext();
        ProxyTask proxyTask = (ProxyTask) context.getAttribute(ProxyTask.ATTRIB);

        if (proxyTask != null) {
            synchronized (proxyTask) {
                proxyTask.setClientState(ConnState.CLOSED);
            }
        }
    }

    public void exception(final NHttpServerConnection conn, final HttpException httpex) {
        System.out.println(conn + " [client->proxy] HTTP error: " + httpex.getMessage());

        if (conn.isResponseSubmitted()) {
            shutdownConnection(conn);
            return;
        }

        HttpContext context = conn.getContext();

        try {
            HttpResponse response = this.responseFactory.newHttpResponse(
                    HttpVersion.HTTP_1_0, HttpStatus.SC_BAD_REQUEST, context);

            response.setParams(new DefaultedHttpParams(this.params, response.getParams()));
            response.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);

            // Pre-process HTTP request
            context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
            context.setAttribute(ExecutionContext.HTTP_REQUEST, null);
            this.httpProcessor.process(response, context);

            conn.submitResponse(response);

            conn.close();
        } catch (IOException ex) {
            shutdownConnection(conn);
        } catch (HttpException ex) {
            shutdownConnection(conn);
        }
    }

    public void exception(final NHttpServerConnection conn, final IOException ex) {
        shutdownConnection(conn);
        System.out.println(conn + " [client->proxy] I/O error: " + ex.getMessage());
    }

    public void timeout(final NHttpServerConnection conn) {
        System.out.println(conn + " [client->proxy] timeout");
        closeConnection(conn);
    }

    private void shutdownConnection(final NHttpConnection conn) {
        try {
            conn.shutdown();
        } catch (IOException ignore) {}
    }

    private void closeConnection(final NHttpConnection conn) {
        try {
            conn.close();
        } catch (IOException ignore) {}
    }
}
