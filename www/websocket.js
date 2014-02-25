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

/*jslint browser: true, nomen: true, plusplus: true */
/*global require, module, Uint8Array, WebKitBlobBuilder, Blob, FileReader, ArrayBuffer */
/**
 * Cordova WebSocket Plugin for Android
 * @author KNOWLEDGECODE <knowledgecode@gmail.com>
 * @version 0.4.2
 */
(function () {
    'use strict';
    var exec = require('cordova/exec'),
        uuid = require('cordova/utils').createUUID(),
        identifier = 0,
        listeners = [],
        tasks = [],
        post = function (fn) {
            tasks.push(fn);
            window.postMessage(uuid, '*');
        },
        listener = function (event) {
            if (event.source === window && event.data === uuid) {
                event.stopPropagation();
                if (tasks.length > 0) {
                    tasks.shift()();
                }
            }
        },
        createMessage = function (type, data, origin) {
            var evt = document.createEvent('Event');

            evt.initEvent(type, false, false);
            switch (type) {
            case 'message':
                evt.data = data.data;
                evt.origin = origin;
                break;
            case 'close':
                evt.wasClean = data.wasClean;
                evt.code = data.code;
                evt.reason = data.reason;
                break;
            }
            return evt;
        },
        EventTarget = function () {
            this.addEventListener = function (type, listener) {
                var el = listeners[this.__getId__()][type] || [];

                if (el.indexOf(listener) < 0) {
                    el.push(listener);
                    listeners[this.__getId__()][type] = el;
                }
            };
            this.removeEventListener = function (type, listener) {
                var i, el = listeners[this.__getId__()][type] || [];

                i = el.indexOf(listener);
                if (i >= 0) {
                    el.splice(i, 1);
                }
            };
            this.dispatchEvent = function (evt) {
                var i, len, el = listeners[this.__getId__()][evt.type] || [];

                for (i = 0, len = el.length; i < len; i++) {
                    el[i](evt);
                }
            };
        },
        WebSocketPrototype = function () {
            var binaryToString;

            binaryToString = function (data, onComplete) {
                var blob, r;

                if (data instanceof ArrayBuffer || data.buffer instanceof ArrayBuffer) {
                    blob = new WebKitBlobBuilder();
                    blob.append(data);
                    blob = blob.getBlob();
                } else if (data instanceof Blob) {
                    blob = data;
                } else {
                    throw new TypeError('\'%s\' is not a valid value for binaryType.'.replace('%s', typeof data));
                }
                r = new FileReader();
                r.onload = function () {
                    onComplete(this.result.substring(this.result.indexOf(',') + 1));
                };
                r.readAsDataURL(blob);
            };
            this.CONNECTING = 0;
            this.OPEN = 1;
            this.CLOSING = 2;
            this.CLOSED = 3;
            this.send = function (data, asBinary) {
                var that = this;

                if (typeof data === 'string') {
                    exec(null, null, 'WebSocket', 'send', [this.__getId__(), data, asBinary || false]);
                } else {
                    binaryToString(data, function (blob) {
                        exec(null, null, 'WebSocket', 'send', [that.__getId__(), blob, true]);
                    });
                }
            };
            this.close = function (code, reason) {
                exec(null, null, 'WebSocket', 'close', [this.__getId__(), code || 0, reason || '']);
            };
        },
        WebSocket = function (url, protocols) {
            var that = this, id = identifier, stringToBinary;

            if (this === window) {
                throw new TypeError('Failed to construct \'WebSocket\': ' +
                    'Please use the \'new\' operator, ' +
                    'this DOM object constructor cannot be called as a function.');
            }

            this.url = url;
            this.binaryType = window.WebKitBlobBuilder ? 'blob' : window.ArrayBuffer ? 'arraybuffer' : 'text';
            this.readyState = 0;
            this.bufferedAmount = 0;
            this.onopen = null;
            this.onmessage = null;
            this.onerror = null;
            this.onclose = null;
            this.extensions = '';
            if (Array.isArray(protocols)) {
                this.protocol = protocols.length > 0 ? protocols[0] : '';
            } else {
                this.protocol = protocols || '';
            }
            stringToBinary = function (data, binaryType) {
                var i, len, array, blob;

                if (binaryType === 'text') {
                    return data;
                }
                data = window.atob(data);
                len = data.length;
                array = new Uint8Array(len);
                for (i = 0; i < len; i++) {
                    array[i] = data.charCodeAt(i);
                }
                if (binaryType === 'arraybuffer') {
                    return array.buffer;
                }
                if (binaryType === 'blob') {
                    blob = new WebKitBlobBuilder();
                    blob.append(array.buffer);
                    return blob.getBlob();
                }
                throw new TypeError('\'%s\' is not a valid value for binaryType.'.replace('%s', binaryType));
            };
            this.__getId__ = function () {
                return id;
            };
            listeners[id] = {};

            exec(function (data) {
                post(function () {
                    var evt;

                    data = JSON.parse(data);
                    switch (data.event) {
                    case 'onopen':
                        evt = createMessage('open');
                        if (that.onopen) {
                            that.onopen(evt);
                        } else {
                            that.dispatchEvent(evt);
                        }
                        break;
                    case 'onmessage':
                        if (data.binary) {
                            data.data = stringToBinary(data.data, that.binaryType);
                        }
                        evt = createMessage('message', data, url);
                        if (that.onmessage) {
                            that.onmessage(evt);
                        } else {
                            that.dispatchEvent(evt);
                        }
                        break;
                    case 'onclose':
                        evt = createMessage('close', data);
                        if (that.onclose) {
                            that.onclose(evt);
                        } else {
                            that.dispatchEvent(evt);
                        }
                        listeners[that.__getId__()] = undefined;
                        break;
                    }
                });
            }, function () {
                post(function () {
                    var evt = createMessage('error');

                    if (that.onerror) {
                        that.onerror(evt);
                    } else {
                        that.dispatchEvent(evt);
                    }
                });
            }, 'WebSocket', 'create', [identifier++, url, this.protocol, WebSocket.pluginOptions || {}]);
        };

    WebSocketPrototype.prototype = new EventTarget();
    WebSocketPrototype.prototype.constructor = WebSocketPrototype;
    WebSocket.prototype = new WebSocketPrototype();
    WebSocket.prototype.constructor = WebSocket;
    WebSocket.pluginOptions = {};

    if (!window.WebSocket) {
        window.addEventListener('message', listener, true);
    }
    module.exports = WebSocket;
}());
