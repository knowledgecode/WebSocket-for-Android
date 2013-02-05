/*
 * WebSocket.java v0.1.0 (c) 2013 knowledgecode | MIT licensed
 * This source file is using Jetty in terms of Apache License 2.0.
 */
package org.apache.cordova.plugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.SparseArray;

/**
 * WebSocket for CordovaPlugin
 * @author knowledgecode
 *
 */
public class WebSocket extends CordovaPlugin {

    private WebSocketClientFactory _factory = null;
    private SparseArray<Connection> _conn = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        _factory = new WebSocketClientFactory();
        _factory.setBufferSize(4096);
        _conn = new SparseArray<Connection>();
        try {
            _factory.start();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean execute(String action, JSONArray args,
        CallbackContext callbackContext) throws JSONException {

        if ("create".equals(action)) {
            create(args.getInt(0), args.getString(1), args.getString(2));
            callbackContext.success();
            return true;
        } else if ("send".equals(action)) {
            send(args.getInt(0), args.getString(1));
            callbackContext.success();
            return true;
        } else if ("close".equals(action)) {
            close(args.getInt(0));
            callbackContext.success();
            return true;
        }
        return false;
    }

    /**
     * Connect a server.
     * @param id
     * @param uri
     * @param protocol
     */
    private void create(
            final int id,
            String uri,
            String protocol) {
        WebSocketClient client = _factory.newWebSocketClient();

        client.setMaxTextMessageSize(1024);
        client.setProtocol(protocol);
        try {
            client.open(
                    new URI(uri),
                    new org.eclipse.jetty.websocket.WebSocket.OnTextMessage() {
                @Override
                public void onOpen(Connection conn) {
                    _conn.put(id, conn);
                    callback(id, "onopen");
                }
                @Override
                public void onMessage(String data) {
                    callback(id, "onmessage", data);
                }
                @Override
                public void onClose(int code, String message) {
                    callback(id, "onclose", code);
                    _conn.remove(id);
                }
            }).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            callback(id, "onerror", e.toString());
        } catch (ExecutionException e) {
            callback(id, "onerror", e.toString());
        } catch (TimeoutException e) {
            callback(id, "onerror", e.toString());
        } catch (IOException e) {
            callback(id, "onerror", e.toString());
        } catch (URISyntaxException e) {
            callback(id, "onerror", e.toString());
        }
    }

    /**
     * Send a data.
     * @param id
     * @param data
     */
    private void send(int id, String data) {
        try {
            _conn.get(id).sendMessage(data);
        } catch (NullPointerException e) {
            callback(id, "onerror", e.toString());
        } catch (IndexOutOfBoundsException e) {
            callback(id, "onerror", e.toString());
        } catch (IOException e) {
            callback(id, "onerror", e.toString());
        }
    }

    /**
     * Close the connection.
     * @param id
     */
    private void close(int id) {
        try {
            _conn.get(id).close();
        } catch (NullPointerException e) {
            callback(id, "onerror", e.toString());
        } catch (IndexOutOfBoundsException e) {
            callback(id, "onerror", e.toString());
        }
    }

    /**
     * Callback
     * @param id
     * @param type
     */
    private void callback(int id, String type) {
        callback(id, type, null);
    }

    /**
     * Callback
     * @param id
     * @param type
     * @param data
     */
    private void callback(int id, String type, Object data) {
        String arg = "";

        if (data != null) {
            arg = String.format(", '%s'", String.valueOf(data));
        }
        webView.loadUrl(String.format(
                "javascript:plugins.WebSocket.callback(%d, '%s'%s)",
                id, type, arg));
    }

    @Override
    public void onDestroy() {
        if (_conn != null) {
            for (int i = 0; i < _conn.size(); i++) {
                if (_conn.get(i).isOpen()) {
                    _conn.get(i).close();
                }
            }
            _conn.clear();
            _conn = null;
        }
        if (_factory != null) {
            try {
                _factory.stop();
            } catch (Exception e) {
            }
            _factory.destroy();
            _factory = null;
        }

        super.onDestroy();
    }
}
