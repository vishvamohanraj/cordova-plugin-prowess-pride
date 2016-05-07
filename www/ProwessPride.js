/*global cordova*/
module.exports = {
	activatePrinterLibrary: function () {
        cordova.exec(null, null, "ProwessPrideBluetoothPrinter", "activatePrinterLibrary", []);
    },
    sendDataForPrint: function (success, failure,jsonObject) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "sendDataForPrint", [jsonObject]);
    },
    connect: function (success, failure,macAddress) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "connect", [macAddress]);
    },

    // Android only - see http://goo.gl/1mFjZY
    connectInsecure: function (success, failure,macAddress) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "connectInsecure", [macAddress]);
    },

    disconnect: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "disconnect", []);
    },

    // list bound devices
    list: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "list", []);
    },

    isEnabled: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "isEnabled", []);
    },

    isConnected: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "isConnected", []);
    },

    // the number of bytes of data available to read is passed to the success function
    available: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "available", []);
    },

    // read all the data in the buffer
    read: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "read", []);
    },

    // reads the data in the buffer up to and including the delimiter
    readUntil: function (success, failure,delimiter) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "readUntil", [delimiter]);
    },

    // writes data to the bluetooth serial port
    // data can be an ArrayBuffer, string, integer array, or Uint8Array
    write: function (success, failure,data) {

        // convert to ArrayBuffer
        if (typeof data === 'string') {
            data = stringToArrayBuffer(data);
        } else if (data instanceof Array) {
            // assuming array of interger
            data = new Uint8Array(data).buffer;
        } else if (data instanceof Uint8Array) {
            data = data.buffer;
        }

        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "write", [data]);
    },

    // calls the success callback when new data is available
    subscribe: function (success, failure,delimiter) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "subscribe", [delimiter]);
    },

    // removes data subscription
    unsubscribe: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "unsubscribe", []);
    },

    // calls the success callback when new data is available with an ArrayBuffer
    subscribeRawData: function (success, failure) {

        successWrapper = function(data) {
            // Windows Phone flattens an array of one into a number which
            // breaks the API. Stuff it back into an ArrayBuffer.
            if (typeof data === 'number') {
                var a = new Uint8Array(1);
                a[0] = data;
                data = a.buffer;
            }
            success(data);
        };
        cordova.exec(successWrapper, failure, "ProwessPrideBluetoothPrinter", "subscribeRaw", []);
    },

    // removes data subscription
    unsubscribeRawData: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "unsubscribeRaw", []);
    },

    // clears the data buffer
    clear: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "clear", []);
    },

    // reads the RSSI of the *connected* peripherial
    readRSSI: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "readRSSI", []);
    },

    showBluetoothSettings: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "showBluetoothSettings", []);
    },

    enable: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "enable", []);
    },

    discoverUnpaired: function (success, failure) {
        cordova.exec(success, failure, "ProwessPrideBluetoothPrinter", "discoverUnpaired", []);
    },

    setDeviceDiscoveredListener: function (notify) {
        if (typeof notify != 'function')
            throw 'BluetoothSerial.setDeviceDiscoveredListener: Callback not a function';

        cordova.exec(notify, null, "ProwessPrideBluetoothPrinter", "setDeviceDiscoveredListener", []);
    },

    clearDeviceDiscoveredListener: function () {
        cordova.exec(null, null, "ProwessPrideBluetoothPrinter", "clearDeviceDiscoveredListener", []);
    },

    setName: function (newName) {
        cordova.exec(null, null, "ProwessPrideBluetoothPrinter", "setName", [newName]);
    },

    setDiscoverable: function (discoverableDuration) {
        cordova.exec(null, null, "ProwessPrideBluetoothPrinter", "setDiscoverable", [discoverableDuration]);
    }
};

var stringToArrayBuffer = function(str) {
    var ret = new Uint8Array(str.length);
    for (var i = 0; i < str.length; i++) {
        ret[i] = str.charCodeAt(i);
    }
    return ret.buffer;
};