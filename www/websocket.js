/**
 * @preserve WebSocket.js v0.3.0 (c) 2013 knowledgecode | MIT licensed
 */
var exec = require('cordova/exec'),
    identifier = 0,
    socks = [],
    WebSocket = function (uri, protocol, origin) {
        var that = this;

        if (this === window) {
            return;
        }
        this.getId = (function (id) {
            return function () {
                return id;
            };
        }(identifier));

        this.send = function (data) {
            exec(null, null, 'WebSocket', 'send', [this.getId(), data]);
        };
        this.close = function () {
            exec(null, null, 'WebSocket', 'close', [this.getId()]);
        };
        socks[identifier] = this;

        /*jslint plusplus: true */
        exec(
            function (data) {
                setTimeout(function () {
                    switch (data.event) {
                    case 'onopen':
                        that.onopen && that.onopen();
                        break;
                    case 'onmessage':
                        that.onmessage && that.onmessage(data.value);
                        break;
                    case 'onclose':
                        that.onclose && that.onclose(data.value);
                        socks[that.getId()] = undefined;
                        break;
                    }
                }, 0);
            },
            function (err) {
                that.onerror && that.onerror(err);
            },
            'WebSocket',
            'create',
            [identifier++, uri, protocol || '', origin || '']
        );
    };

module.exports = WebSocket;

