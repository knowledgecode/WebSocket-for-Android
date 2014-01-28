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
(function () {
    'use strict';
    var exec = require('cordova/exec'),
        uuid = require('cordova/utils').createUUID(),
        identifier = 0,
        socks = [],
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
            var evt;

            switch (type) {
            case 'open':
                evt = document.createEvent('Event');
                evt.initEvent(type, false, false);
                break;
            case 'message':
                evt = document.createEvent('MessageEvent');
                evt.initMessageEvent(type, false, false, data.data, origin);
                break;
            case 'error':
                evt = document.createEvent('Event');
                evt.initEvent(type, false, false);
                break;
            case 'close':
                evt = document.createEvent('Event');
                evt.initEvent(type, false, false);
                evt.wasClean = data.wasClean;
                evt.code = data.code;
                evt.reason = data.reason;
                break;
            }
            return evt;
        },
        stringToBinary = function (data, binaryType) {
            var i, len, array, blob;

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
        binaryToString = function (data, onComplete) {
            var blob, r;

            if (data instanceof ArrayBuffer || data.buffer instanceof ArrayBuffer) {
                blob = new WebKitBlobBuilder();
                blob.append(data);
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
        WebSocket = function (url, protocols, origin) {
            var that = this;

            if (this === window) {
                throw new TypeError('Failed to construct \'WebSocket\': ' +
                    'Please use the \'new\' operator, ' +
                    'this DOM object constructor cannot be called as a function.');
            }
            this.url = url;
            this.binaryType = 'blob';
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
            WebSocket.prototype.send = function (data) {
                if (typeof data === 'string') {
                    exec(null, null, 'WebSocket', 'send', [this._getId(), data, false]);
                } else {
                    binaryToString(data, function (blob) {
                        exec(null, null, 'WebSocket', 'send', [that._getId(), blob, true]);
                    });
                }
            };
            WebSocket.prototype.close = function (code, reason) {
                exec(null, null, 'WebSocket', 'close', [this._getId(), code || 0, reason || '']);
            };
            socks[identifier] = this;
            this._getId = (function (id) {
                return function () {
                    return id;
                };
            }(identifier));

            exec(function (data) {
                post(function () {
                    data = JSON.parse(data);
                    switch (data.event) {
                    case 'onopen':
                        if (typeof that.onopen === 'function') {
                            that.onopen(createMessage('open'));
                        }
                        break;
                    case 'onmessage':
                        if (typeof that.onmessage === 'function') {
                            if (data.binary) {
                                data.data = stringToBinary(data.data, that.binaryType);
                            }
                            that.onmessage(createMessage('message', data, url));
                        }
                        break;
                    case 'onclose':
                        if (typeof that.onclose === 'function') {
                            that.onclose(createMessage('close', data));
                        }
                        socks[that._getId()] = undefined;
                        break;
                    }
                });
            }, function () {
                post(function () {
                    if (typeof that.onerror === 'function') {
                        that.onerror(createMessage('error'));
                    }
                });
            }, 'WebSocket', 'create', [identifier++, url, this.protocol, origin || '']);
        };

    if (!window.WebSocket) {
        window.addEventListener('message', listener, true);
    }
    module.exports = WebSocket;
}());
