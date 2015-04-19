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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.knowledgecode.cordova.websocket.TaskRunner.Task;
import com.knowledgecode.cordova.websocket.WebSocketGenerator.OnCloseListener;
import com.knowledgecode.cordova.websocket.WebSocketGenerator.OnOpenListener;

import android.app.Activity;
import android.util.SparseArray;
import android.webkit.CookieManager;
import android.webkit.WebView;

/**
 * Connect to server.
 */
class ConnectionTask implements Task {

    private static final long MAX_CONNECT_TIME = 75000;

    private final WebSocketClientFactory _factory;
    private final Activity _view;
    private final WebView _webView;
    private final SparseArray<Connection> _map;

    /**
     * Constructor
     *
     * @param factory
     * @param map
     */
    public ConnectionTask(WebSocketClientFactory factory, Activity view, WebView webView, SparseArray<Connection> map) {
        _factory = factory;
        _view = view;
        _webView = webView;
        _map = map;
    }

    /**
     * Get default origin.
     *
     * @return
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private String getOrigin() throws InterruptedException, ExecutionException {
        Callable<String> origin = new Callable<String>() {
            @Override
            public String call() throws Exception {
                URI uri = new URI(_webView.getUrl());
                return String.format("%s://%s", uri.getScheme(), StringUtil.nonNull(uri.getHost()));
            }
        };
        FutureTask<String> task = new FutureTask<String>(origin);

        _view.runOnUiThread(task);
        return task.get();
    }

    /**
     * Get default user-agent
     *
     * @return
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private String getAgent() throws InterruptedException, ExecutionException {
        Callable<String> agent = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return _webView.getSettings().getUserAgentString();
            }
        };
        FutureTask<String> task = new FutureTask<String>(agent);

        _view.runOnUiThread(task);
        return task.get();
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
            URI uri = new URI(args.getString(1));
            String protocol = args.getString(2);
            JSONObject options = args.getJSONObject(3);
            String origin = options.optString("origin", getOrigin());
            String agent = options.optString("agent", getAgent());
            long maxConnectTime =  options.optLong("maxConnectTime", MAX_CONNECT_TIME);

            if (protocol.length() > 0) {
                client.setProtocol(protocol);
            }
            if (origin.length() > 0) {
                client.setOrigin(origin);
            }
            if (agent.length() > 0) {
                client.setAgent(agent);
            }
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
