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
package org.eclipse.jetty.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;
import org.eclipse.jetty.websocket.WebSocketParser.FrameHandler;

import android.text.TextUtils;

/**
 * PerMessageDeflateExtension
 *
 * Copyright (c) 2015 KNOWLEDGECODE
 */
public class PerMessageDeflateExtension implements Extension {

    private static final Logger __log = Log.getLogger(PerMessageDeflateExtension.class.getName());
    private static final byte[] TAIL_BYTES = new byte[] { 0x00, 0x00, (byte)0xff, (byte)0xff };
    private static final int INITIAL_CAPACITY = 65536;
    private static final String EXTENSION = "permessage-deflate";
    private static final String OPTIONS = "client_no_context_takeover";

    private FrameHandler _inbound;
    private WebSocketGenerator _outbound;
    private Deflater _deflater;
    private Inflater _inflater;
    private WebSocketBuffer _buffer;
    private byte[] _buf;
    private String _parameters;
    private boolean _compressible;
    private boolean _compressed;

    public PerMessageDeflateExtension() {
        _deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        _inflater = new Inflater(true);
        _buffer = new WebSocketBuffer(INITIAL_CAPACITY);
        _buf = new byte[INITIAL_CAPACITY];
        _parameters = TextUtils.join("; ", new String[]{ EXTENSION, OPTIONS });
        _compressible = false;
        _compressed = false;
    }

    private byte[] compress(byte[] b, int offset, int length) {
        int len;
        _deflater.reset();
        _deflater.setInput(b, offset, length);
        _deflater.finish();
        _buffer.clear();
        while ((len = _deflater.deflate(_buf)) > 0) {
            _buffer.append(_buf, 0, len);
        }
        return _buffer.array();
    }

    private Buffer decompress(byte[] b, int offset, int length) throws DataFormatException {
        int len;
        _inflater.reset();
        _inflater.setInput(b, offset, length);
        _buffer.clear();
        while ((len = _inflater.inflate(_buf)) > 0) {
            _buffer.append(_buf, 0, len);
        }
        return _buffer;
    }

    private Buffer decompress(byte[] b) throws DataFormatException {
        return decompress(b, 0, b.length);
    }

    private static byte[] concat(Buffer buffer) {
        int len1 = buffer.length();
        int len2 = TAIL_BYTES.length;
        byte[] array = new byte[len1 + len2];

        System.arraycopy(buffer.array(), buffer.getIndex(), array, 0, len1);
        System.arraycopy(TAIL_BYTES, 0, array, len1, len2);
        return array;
    }

    @Override
    public void onFrame(byte flags, byte opcode, Buffer buffer) {
        switch (opcode) {
        case WebSocketConnectionRFC6455.OP_TEXT:
        case WebSocketConnectionRFC6455.OP_BINARY:
            _compressed = ((flags & 0x07) == 0x04);
        case WebSocketConnectionRFC6455.OP_CONTINUATION:
            if (_compressed) {
                try {
                    if ((flags &= 0x08) > 0) {
                        _inbound.onFrame(flags, opcode, decompress(concat(buffer)));
                    } else {
                        _inbound.onFrame(flags, opcode, decompress(buffer.array(), buffer.getIndex(), buffer.length()));
                    }
                } catch (DataFormatException e) {
                    __log.warn(e);
                }
                return;
            }
        }
        _inbound.onFrame(flags, opcode, buffer);
    }

    @Override
    public void close(int code, String message) {
    }

    @Override
    public int flush() throws IOException {
        return 0;
    }

    @Override
    public boolean isBufferEmpty() {
        return _outbound.isBufferEmpty();
    }

    @Override
    public void addFrame(byte flags, byte opcode, byte[] content, int offset, int length) throws IOException {
        if (_compressible) {
            switch (opcode) {
            case WebSocketConnectionRFC6455.OP_TEXT:
            case WebSocketConnectionRFC6455.OP_BINARY:
                flags |= 0x04;
            case WebSocketConnectionRFC6455.OP_CONTINUATION:
                byte[] compressed = compress(content, offset, length);
                _outbound.addFrame(flags, opcode, compressed, 0, compressed.length);
                return;
            }
        }
        _outbound.addFrame(flags, opcode, content, offset, length);
    }

    @Override
    public String getName() {
        return EXTENSION;
    }

    @Override
    public String getParameterizedName() {
        return _parameters;
    }

    @Override
    public boolean init(Map<String, String> parameters) {
        boolean extension = false;

        for (String key : parameters.keySet()) {
            String value = parameters.get(key);

            if (EXTENSION.equals(key) && TextUtils.isEmpty(value)) {
                extension = true;
            } else if (OPTIONS.equals(key) && TextUtils.isEmpty(value)) {
                // It cannot compress data if the server does not permit this option.
                _compressible = true;
            } else if (TextUtils.isEmpty(value)) {
                __log.warn("Unexpected parameter: {}={}", key, value);
                _parameters = "";
                return false;
            } else {
                __log.warn("Unexpected parameter: {}", key);
                _parameters = "";
                return false;
            }
        }
        if (extension && !_compressible) {
            _parameters = EXTENSION;
        }
        return extension;
    }

    @Override
    public void bind(FrameConnection connection, FrameHandler inbound, WebSocketGenerator outbound) {
        _inbound = inbound;
        _outbound = outbound;
    }
}
