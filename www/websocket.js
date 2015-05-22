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
/*eslint complexity: 0 no-mixed-requires: 0 */
/*global require, module */
/**
 * Cordova WebSocket Plugin for Android
 * @author KNOWLEDGECODE <knowledgecode@gmail.com>
 * @version 0.9.1
 */
(function (window) {
    'use strict';
    var BuiltinWebSocket = window.WebSocket,
        exec = require('cordova/exec'),
        identifier = 0,
        listeners = {},
        taskQueue = {
            uuid: require('cordova/utils').createUUID(),
            tasks: [],
            push: function (fn) {
                this.tasks.push(fn);
                window.postMessage(this.uuid, '*');
            },
            listener: function (event) {
                if (event.source === window && event.data === taskQueue.uuid) {
                    event.stopPropagation();
                    if (taskQueue.tasks.length) {
                        taskQueue.tasks.shift()();
                    }
                }
            }
        },
        createMessage = function (type, data, origin) {
            var evt = document.createEvent('Event');

            evt.initEvent(type, false, false);
            switch (type) {
            case 'message':
                evt.data = data;
                evt.origin = origin;
                break;
            case 'close':
                evt.wasClean = data.substring(0, 1) === '1';
                evt.code = parseInt(data.substring(1, 5), 10) || 0;
                evt.reason = data.substring(5);
                break;
            }
            return evt;
        },
        Blob = (function () {
            if (typeof window.WebKitBlobBuilder === 'function') {
                return function (data) {
                    var blob = new window.WebKitBlobBuilder();
                    blob.append(data[0]);
                    return blob.getBlob();
                };
            }
            return window.Blob;
        }()),
        binaryToString = function (data, onComplete) {
            var blob, r;

            if (data instanceof window.ArrayBuffer || data.buffer instanceof window.ArrayBuffer) {
                blob = new Blob([data.buffer || data]);
            } else if (data instanceof window.Blob) {
                blob = data;
            } else {
                throw new TypeError('\'%s\' is not a valid value for binaryType.'.replace('%s', typeof data));
            }
            r = new window.FileReader();
            r.onload = function () {
                onComplete(this.result);
            };
            r.readAsDataURL(blob);
        },
        stringToBinary = function (data, binaryType) {
            var i, len = data.length, array = new window.Uint8Array(len);

            for (i = 0; i < len; i++) {
                array[i] = data.charCodeAt(i);
            }
            if (binaryType === 'arraybuffer') {
                return array.buffer;
            }
            if (binaryType === 'blob') {
                return new Blob([array.buffer]);
            }
            throw new TypeError('\'%s\' is not a valid value for binaryType.'.replace('%s', binaryType));
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
                    el[i].call(this, evt);
                }
            };
        },
        WebSocketPrototype = function () {
            this.CONNECTING = 0;
            this.OPEN = 1;
            this.CLOSING = 2;
            this.CLOSED = 3;
            this.send = function (data) {
                var that = this;

                if (typeof data === 'string') {
                    exec(null, null, 'WebSocket', 'send', [that.__getId__(), data, false]);
                } else {
                    binaryToString(data, function (blob) {
                        exec(null, null, 'WebSocket', 'send', [that.__getId__(), blob, true]);
                    });
                }
            };
            this.close = function (code, reason) {
                if (this.readyState === this.CONNECTING || this.readyState === this.OPEN) {
                    this.readyState = this.CLOSING;
                    exec(null, null, 'WebSocket', 'close', [this.__getId__(), code || 0, reason || '']);
                }
            };
        },
        WebSocket = function (url, protocols) {
            var i, len, that = this, id = identifier;

            if (this === window) {
                throw new TypeError('Failed to construct \'WebSocket\': ' +
                    'Please use the \'new\' operator, ' +
                    'this DOM object constructor cannot be called as a function.');
            }
            if (!WebSocket.pluginOptions.override && BuiltinWebSocket) {
                return new (BuiltinWebSocket.bind.apply(BuiltinWebSocket, [].concat(this, Array.prototype.slice.call(arguments))))();
            }
            switch (arguments.length) {
            case 0:
                throw new TypeError('Failed to construct \'WebSocket\': 1 argument required, but only 0 present.');
            case 1:
                protocols = '';
                break;
            case 2:
                if (!Array.isArray(protocols)) {
                    protocols = [String(protocols)];
                }
                for (i = 0, len = protocols.length; i < len; i++) {
                    if (!protocols[i].length) {
                        throw new SyntaxError('Failed to construct \'WebSocket\': The subprotocol \'\' is invalid.');
                    }
                }
                protocols = len > 0 ? protocols.join() : '';
                break;
            default:
                throw new TypeError('Failed to construct \'WebSocket\': No matching constructor signature.');
            }

            this.url = url;
            this.binaryType = Blob ? 'blob' : 'arraybuffer';
            this.readyState = 0;
            this.bufferedAmount = 0;
            this.onopen = null;
            this.onmessage = null;
            this.onerror = null;
            this.onclose = null;
            this.extensions = '';
            this.protocol = '';
            this.__getId__ = function () {
                return id;
            };
            listeners[id] = {};

            exec(function (data) {
                if (data instanceof ArrayBuffer) {
                    taskQueue.push(function () {
                        var evt = createMessage('message', data, that.url);

                        if (that.onmessage) {
                            that.onmessage(evt);
                        }
                        that.dispatchEvent(evt);
                    });
                } else {
                    switch (data[0]) {
                    case 'O':
                        taskQueue.push(function () {
                            var evt = createMessage('open');

                            that.readyState = that.OPEN;
                            that.protocol = data.substring(1);
                            if (that.onopen) {
                                that.onopen(evt);
                            }
                            that.dispatchEvent(evt);
                        });
                        break;
                    case 'T':
                        taskQueue.push(function () {
                            var evt = createMessage('message', data.substring(1), that.url);

                            if (that.onmessage) {
                                that.onmessage(evt);
                            }
                            that.dispatchEvent(evt);
                        });
                        break;
                    case 'C':
                        taskQueue.push(function () {
                            var evt = createMessage('close', data.substring(1));

                            that.readyState = that.CLOSED;
                            if (that.onclose) {
                                that.onclose(evt);
                            }
                            that.dispatchEvent(evt);
                            delete listeners[that.__getId__()];
                        });
                        break;
                    }
                }
            }, function () {
                taskQueue.push(function () {
                    var evt = createMessage('error');

                    if (that.onerror) {
                        that.onerror(evt);
                    }
                    that.dispatchEvent(evt);
                });
            }, 'WebSocket', 'create', [identifier++, url, protocols, location.origin, navigator.userAgent, WebSocket.pluginOptions || {}]);
        },
        ver = /Chrome\/(\d+)/.exec(navigator.userAgent);

    WebSocketPrototype.prototype = new EventTarget();
    WebSocketPrototype.prototype.constructor = WebSocketPrototype;
    WebSocket.prototype = new WebSocketPrototype();
    WebSocket.prototype.constructor = WebSocket;
    WebSocket.pluginOptions = {};
    module.exports = WebSocket;
    window.addEventListener('message', taskQueue.listener, true);

    if (!ver || parseInt(ver[1], 10) < 30) {
        BuiltinWebSocket = undefined;
    }
}(this));
