/*
 * WebSocket.java v0.3.2 (c) 2013 knowledgecode | MIT licensed
 * This source file is using Jetty 8 under the terms of the Apache License v2.0.
 */
package org.apache.cordova.plugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.apache.http.client.utils.URIUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseArray;

/**
 * WebSocket for CordovaPlugin
 * @author knowledgecode
 *
 */
public class WebSocket extends CordovaPlugin {

    private static final int CONNECTION_TIMEOUT = 5000;

    private WebSocketClientFactory _factory;
    private SparseArray<Connection> _conn;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        _factory = new WebSocketClientFactory();
        _factory.setBufferSize(4096);
        _conn = new SparseArray<Connection>();
        try {
            start();
        } catch (Exception e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) {

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if ("create".equals(action)) {
                        create(callbackContext, args.getInt(0), args.getString(1), args.getString(2), args.getString(3));
                    } else if ("send".equals(action)) {
                        send(args.getInt(0), args.getString(1));
                        callbackContext.success();
                    } else if ("close".equals(action)) {
                        close(args.getInt(0));
                        callbackContext.success();
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(Status.INVALID_ACTION, action));
                    }
                } catch (JSONException e) {
                    callbackContext.error(action);
                } catch (IOException e) {
                    callbackContext.error(action);
                }
            }
        });
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReset() {
        super.onReset();

        try {
            stop().start();
        } catch (Exception e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        try {
            stop();
        } catch (Exception e) {
        }
        _conn = null;
        _factory.destroy();
        _factory = null;

        super.onDestroy();
    }

    /**
     * Connect a server.
     * @param callbackContext
     * @param callbackId
     * @param uri
     * @param protocol
     * @param origin
     */
    private void create(final CallbackContext callbackContext, final int callbackId, String uri, String protocol, String origin) {

        WebSocketClient client = _factory.newWebSocketClient();

        client.setMaxTextMessageSize(1024);
        if (protocol.length() > 0) {
            client.setProtocol(protocol);
        }
        if (origin.length() > 0) {
            client.setOrigin(origin);
        }

        try {
            client.open(
                    createURI(uri),
                    new org.eclipse.jetty.websocket.WebSocket.OnTextMessage() {
                @Override
                public void onOpen(Connection conn) {
                    _conn.put(callbackId, conn);

                    JSONObject json = createCallbackJSON("onopen", null);
                    PluginResult result = new PluginResult(Status.OK, json);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
                @Override
                public void onMessage(String data) {
                    JSONObject json = createCallbackJSON("onmessage", data);
                    PluginResult result = new PluginResult(Status.OK, json);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
                @Override
                public void onClose(int code, String message) {
                    _conn.remove(callbackId);

                    JSONObject json = createCallbackJSON("onclose", code);
                    PluginResult result = new PluginResult(Status.OK, json);
                    if (code != 1000) {
                        result.setKeepCallback(true);
                    }
                    callbackContext.sendPluginResult(result);
                }
            }, CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /**
     * Create the uri.
     * @param uriString
     * @return
     * @throws URISyntaxException
     */
    private URI createURI(String uriString) throws URISyntaxException {
        URI uri = new URI(uriString);
        int port = uri.getPort();

        if (port == -1) {
            if ("ws".equals(uri.getScheme())) {
                port = 80;
            } else if ("wss".equals(uri.getScheme())) {
                port = 443;
            }
            uri = URIUtils.createURI(uri.getScheme(), uri.getHost(), port, uri.getPath(), uri.getQuery(), uri.getFragment());
        }
        return uri;
    }

    /**
     * Send data.
     * @param callbackId
     * @param data
     * @throws IOException 
     */
    private void send(int callbackId, String data) throws IOException {
        Connection conn = _conn.get(callbackId);

        if (conn != null) {
            conn.sendMessage(data);
        }
    }

    /**
     * Close the connection.
     * @param callbackId
     */
    private void close(int callbackId) {
        Connection conn = _conn.get(callbackId);

        if (conn != null) {
            conn.close();
        }
    }

    /**
     * Create a JSON for callback.
     * @param event
     * @param value
     * @return
     */
    private JSONObject createCallbackJSON(String event, Object value) {
        JSONObject json = new JSONObject();

        try {
            json.put("event", event);
            if (value != null) {
                json.put("value", value);
            }
        } catch (JSONException e) {
            json = null;
        }
        return json;
    }

    /**
     * Start WebSocketClientFactory.
     * @return
     * @throws Exception
     */
    private WebSocket start() throws Exception {
        _factory.start();
        return this;
    }

    /**
     * Stop WebSocketClientFactory.
     * @return
     * @throws Exception
     */
    private WebSocket stop() throws Exception {
        if (_conn != null) {
            for (int i = 0; i < _conn.size(); i++) {
                if (_conn.get(i).isOpen()) {
                    _conn.get(i).close();
                }
            }
            _conn.clear();
        }
        _factory.stop();
        return this;
    }
}
