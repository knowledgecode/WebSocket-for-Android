/**
 * @preserve WebSocket.js v0.1.0 (c) 2013 knowledgecode | MIT licensed
 */
/*jslint browser: true */
(function () {
    'use strict';

    var identifier = 0,
        socks = [],
        webSocket = {};

    /**
     * create
     * @param {String} uri
     * @param {String} protocol
     * @param {Function} onopen function
     * @param {Function} onmessage function
     * @param {Function} onerror function
     * @param {Function} onclose function
     * @return {Object} socket object
     */
    webSocket.create = function (
        uri,
        protocol,
        onopen,
        onmessage,
        onerror,
        onclose
    ) {
        var sock = {};

        sock.getId = (function (id) {
            return function () {
                return id;
            };
        }(identifier));
        sock.onopen = onopen;
        sock.onmessage = onmessage;
        sock.onerror = onerror;
        sock.onclose = onclose;
        sock.send = function (data) {
            window.cordova.exec(
                null,
                null,
                'WebSocket',
                'send',
                [this.getId(), data]
            );
        };
        sock.close = function () {
            window.cordova.exec(
                null,
                null,
                'WebSocket',
                'close',
                [this.getId()]
            );
        };
        socks[identifier] = sock;

        /*jslint plusplus: true */
        window.cordova.exec(
            null,
            null,
            'WebSocket',
            'create',
            [identifier++, uri, protocol || '']
        );

        return sock;
    };

    webSocket.callback = function (id, type, arg) {
        setTimeout(function () {
            var sock = socks[id];

            if (sock) {
                switch (type) {
                case 'onopen':
                    sock.onopen();
                    break;
                case 'onmessage':
                    sock.onmessage(arg);
                    break;
                case 'onerror':
                    sock.onerror(arg);
                    break;
                case 'onclose':
                    sock.onclose(parseInt(arg, 10));
                    delete socks[id];
                    break;
                }
            }
        }, 0);
    };

    if (!window.plugins) {
        window.plugins = {};
    }
    window.plugins.WebSocket = window.plugins.WebSocket || webSocket;

}());
