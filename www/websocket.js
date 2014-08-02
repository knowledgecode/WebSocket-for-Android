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
/*global require, module, ArrayBuffer, Uint8Array, Blob, WebKitBlobBuilder, FileReader */
/**
 * Cordova WebSocket Plugin for Android
 * @author KNOWLEDGECODE <knowledgecode@gmail.com>
 * @version 0.6.3
 */
(function () {
    'use strict';
    var exec = require('cordova/exec'),
        identifier = 0,
        listeners = [],
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
                    if (taskQueue.tasks.length > 0) {
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
        },
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
                    exec(null, null, 'WebSocket', 'send', [this.__getId__(), data, false]);
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
                    if (protocols[i].length === 0) {
                        throw new SyntaxError('Failed to construct \'WebSocket\': The subprotocol \'\' is invalid.');
                    }
                }
                protocols = len > 0 ? protocols.join() : '';
                break;
            default:
                throw new TypeError('Failed to construct \'WebSocket\': No matching constructor signature.');
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
            this.protocol = '';
            this.__getId__ = function () {
                return id;
            };
            listeners[id] = {};

            exec(function (json) {
                taskQueue.push(function () {
                    var evt, data = JSON.parse(json);

                    switch (data.event) {
                    case 'onopen':
                        that.protocol = data.protocol;
                        that.readyState = that.OPEN;
                        evt = createMessage('open');
                        if (that.onopen) {
                            that.onopen(evt);
                        }
                        that.dispatchEvent(evt);
                        break;
                    case 'onmessage':
                        if (data.binary) {
                            data.data = stringToBinary(data.data, that.binaryType);
                        }
                        evt = createMessage('message', data, that.url);
                        if (that.onmessage) {
                            that.onmessage(evt);
                        }
                        that.dispatchEvent(evt);
                        break;
                    case 'onclose':
                        that.readyState = that.CLOSED;
                        evt = createMessage('close', data);
                        if (that.onclose) {
                            that.onclose(evt);
                        }
                        that.dispatchEvent(evt);
                        listeners[that.__getId__()] = undefined;
                        break;
                    }
                });
            }, function () {
                taskQueue.push(function () {
                    var evt = createMessage('error');

                    if (that.onerror) {
                        that.onerror(evt);
                    }
                    that.dispatchEvent(evt);
                });
            }, 'WebSocket', 'create', [identifier++, url, protocols, WebSocket.pluginOptions || {}]);
        },
        ver = navigator.userAgent.match(/Android (\d+\.\d+)/);

    if ((ver && parseFloat(ver[1]) < 4.4) || !window.WebSocket) {
        WebSocketPrototype.prototype = new EventTarget();
        WebSocketPrototype.prototype.constructor = WebSocketPrototype;
        WebSocket.prototype = new WebSocketPrototype();
        WebSocket.prototype.constructor = WebSocket;
        WebSocket.pluginOptions = {};
        module.exports = WebSocket;
        window.addEventListener('message', taskQueue.listener, true);
    } else {
        module.exports = window.WebSocket;
    }
}());
