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

import org.apache.cordova.CallbackContext;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.json.JSONArray;

import com.knowledgecode.cordova.websocket.TaskRunner.Task;

import android.util.SparseArray;

/**
 * Close a connection.
 */
class DisconnectionTask implements Task {

    private final SparseArray<Connection> _map;

    /**
     * Constructor
     *
     * @param map
     */
    public DisconnectionTask(SparseArray<Connection> map) {
        _map = map;
    }

    @Override
    public void execute(JSONArray args, CallbackContext ctx) {
        try {
            int id = args.getInt(0);
            int code = args.getInt(1);
            String reason = args.getString(2);
            Connection conn = _map.get(id);

            if (conn != null) {
                if (code > 0) {
                    conn.close(code, reason);
                } else {
                    conn.close();
                }
            }
        } catch (Exception e) {
            ctx.error("close");
        }
    }
}