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
import org.json.JSONArray;
import org.json.JSONException;

class TaskRunner implements Runnable {

    interface Task {
        public void execute(JSONArray args, CallbackContext ctx);
    }

    private final Task _task;
    private final String _rawArgs;
    private final CallbackContext _ctx;

    /**
     * Constructor
     *
     * @param task
     * @param rawArgs
     * @param ctx
     */
    public TaskRunner(Task task, String rawArgs, CallbackContext ctx) {
        _task = task;
        _rawArgs = rawArgs;
        _ctx = ctx;
    }

    @Override
    public void run() {
        try {
            _task.execute(new JSONArray(_rawArgs), _ctx);
        } catch (JSONException e) {
            _ctx.error("JSON");
        }
    }
}
