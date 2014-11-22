/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.knowledgecode.cordova.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.knowledgecode.cordova.websocket.TaskRunner.Task;
import com.knowledgecode.cordova.websocket.WebSocketGenerator.OnCloseListener;
import com.knowledgecode.cordova.websocket.WebSocketGenerator.OnOpenListener;

import android.util.SparseArray;
import android.webkit.CookieManager;

/**
 * Connect to server.
 */
class ConnectionTask implements Task {

    private static final long MAX_CONNECT_TIME = 75000;

    private final WebSocketClientFactory _factory;
    private final SparseArray<Connection> _map;

    /**
     * Constructor
     *
     * @param factory
     * @param map
     */
    public ConnectionTask(WebSocketClientFactory factory, SparseArray<Connection> map) {
        _factory = factory;
        _map = map;
    }

    /**
     * Complement default port number.
     *
     * @param url
     * @return URI
     * @throws URISyntaxException
     */
    private static URI complementPort(String url) throws URISyntaxException {
        URI uri = new URI(url);
        int port = uri.getPort();

        if (port < 0) {
            if ("ws".equals(uri.getScheme())) {
                port = 80;
            } else if ("wss".equals(uri.getScheme())) {
                port = 443;
            }
            uri = new URI(uri.getScheme(), "", uri.getHost(), port, uri.getPath(), uri.getQuery(), "");
        }
        return uri;
    }

    /**
     * Set cookies, if any.
     *
     * @param cookies
     * @param url
     */
    private static void setCookie(Map<String, String> cookies, String url) {
        String cookie = CookieManager.getInstance().getCookie(url);

        if (cookie != null) {
            for (String c : cookie.split(";")) {
                String[] pair = c.split("=");

                if (pair.length == 2) {
                    cookies.put(pair[0], pair[1]);
                }
            }
        }
    }

    @Override
    public void execute(JSONArray args, CallbackContext ctx) {
        try {
            WebSocketClient client = _factory.newWebSocketClient();

            int id = args.getInt(0);
            String url = args.getString(1);
            String protocol = args.getString(2);
            JSONObject options = args.getJSONObject(3);
            String origin = options.optString("origin", "");
            long maxConnectTime =  options.optLong("maxConnectTime", MAX_CONNECT_TIME);

            URI uri = complementPort(url);

            if (protocol.length() > 0) {
                client.setProtocol(protocol);
            }
            if (origin.length() > 0) {
                client.setOrigin(origin);
            }
            client.setMaxTextMessageSize(-1);
            client.setMaxBinaryMessageSize(-1);

            setCookie(client.getCookies(), uri.getHost());

            WebSocketGenerator gen = new WebSocketGenerator(id, ctx);

            gen.setOnOpenListener(new OnOpenListener() {
                @Override
                public void onOpen(int id, Connection conn) {
                    _map.put(id, conn);
                }
            });
            gen.setOnCloseListener(new OnCloseListener() {
                @Override
                public void onClose(int id) {
                    if (_map.indexOfKey(id) >= 0) {
                        _map.remove(id);
                    }
                }
            });
            client.open(uri, gen, maxConnectTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (!ctx.isFinished()) {
                PluginResult result = new PluginResult(Status.ERROR);
                result.setKeepCallback(true);
                ctx.sendPluginResult(result);
            }
        }
    }
}
