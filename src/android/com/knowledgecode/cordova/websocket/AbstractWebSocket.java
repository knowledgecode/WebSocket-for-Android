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

import org.apache.http.util.ByteArrayBuffer;

abstract class AbstractWebSocket implements
    org.eclipse.jetty.websocket.WebSocket.OnTextMessage,
    org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage,
    org.eclipse.jetty.websocket.WebSocket.OnFrame {

    private static final int BUFFER_SIZE = 8192;

    private FrameConnection _frame;
    private boolean _binary;
    private ByteArrayBuffer _buffer;

    /**
     * Constructor
     */
    public AbstractWebSocket() {
        _buffer = new ByteArrayBuffer(BUFFER_SIZE);
    }

    @Override
    public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {
        if (_frame.isBinary(opcode) || (_frame.isContinuation(opcode) && _binary)) {
            _binary = true;
            _buffer.append(data, offset, length);
            if (_frame.isMessageComplete(flags)) {
                _binary = false;
                byte[] msg = _buffer.toByteArray();
                this.onMessage(msg, 0, msg.length);
                _buffer.clear();
            }
            return true;
        } else if (_frame.isClose(opcode)) {
            release();
        }
        return false;
    }

    @Override
    public void onHandshake(FrameConnection connection) {
        _frame = connection;
    }

    /**
     * Release resources.
     */
    private void release() {
        _buffer.clear();
        _buffer = null;
        if (_frame.isOpen()) {
            _frame.close();
        }
        _frame = null;
    }
}