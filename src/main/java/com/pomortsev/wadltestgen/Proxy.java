package com.pomortsev.wadltestgen;

import com.pomortsev.wadltestgen.proxy.ConnectingHandler;
import com.pomortsev.wadltestgen.proxy.ListeningHandler;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultClientIOEventDispatch;
import org.apache.http.impl.nio.DefaultServerIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.NHttpClientHandler;
import org.apache.http.nio.NHttpServiceHandler;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;

/**
 * Rudimentary HTTP/1.1 reverse proxy based on the non-blocking I/O model.
 * <p>
 * Please note the purpose of this application is demonstrate the usage of HttpCore APIs.
 * It is NOT intended to demonstrate the most efficient way of building an HTTP reverse proxy.
 *
 *
 */
public class Proxy {
    public static void main(String[] args) throws Exception {
        HttpParams params = new SyncBasicHttpParams();
        params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1")
            .setParameter(CoreProtocolPNames.USER_AGENT, "HttpComponents/1.1");

        final ConnectingIOReactor connectingIOReactor = new DefaultConnectingIOReactor(1, params);

        final ListeningIOReactor listeningIOReactor = new DefaultListeningIOReactor(1, params);

        // Set up HTTP protocol processor for incoming connections
        HttpProcessor inhttpproc = new ImmutableHttpProcessor(
            new HttpRequestInterceptor[] {
                new RequestContent(),
                new RequestTargetHost(),
                new RequestConnControl(),
                new RequestUserAgent(),
                new RequestExpectContinue()
            }
        );

        // Set up HTTP protocol processor for outgoing connections
        HttpProcessor outhttpproc = new ImmutableHttpProcessor(
            new HttpResponseInterceptor[] {
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
            }
        );

        NHttpClientHandler connectingHandler = new ConnectingHandler(
                inhttpproc,
                new DefaultConnectionReuseStrategy(),
                params);

        NHttpServiceHandler listeningHandler = new ListeningHandler(
                connectingIOReactor,
                outhttpproc,
                new DefaultHttpResponseFactory(),
                new DefaultConnectionReuseStrategy(),
                params);

        final IOEventDispatch connectingEventDispatch = new DefaultClientIOEventDispatch(
                connectingHandler, params);

        final IOEventDispatch listeningEventDispatch = new DefaultServerIOEventDispatch(
                listeningHandler, params);

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    connectingIOReactor.execute(connectingEventDispatch);
                } catch (InterruptedIOException ex) {
                    System.err.println("Interrupted");
                } catch (IOException e) {
                    System.err.println("I/O error: " + e.getMessage());
                }
            }
        });
        t.start();

        try {
            int port = (args.length >= 1) ? Integer.parseInt(args[0]) : 80;

            listeningIOReactor.listen(new InetSocketAddress(port));
            listeningIOReactor.execute(listeningEventDispatch);

            System.out.println("Listening on port " + Integer.toString(port) + "...");
        } catch (InterruptedIOException ex) {
            System.err.println("Interrupted");
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
