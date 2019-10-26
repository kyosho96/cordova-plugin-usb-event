var exec = require('cordova/exec');

/**
 * list USB devices.
 */
exports.listDevices = function (success, error) {
    exec(success, error, 'UsbEvent', 'listDevices', []);
};

/**
 * Register USB attached and detached event callback.
 */
exports.registerEventCallback = function (success, error) {
    exec(success, error, 'UsbEvent', 'registerEventCallback', []);
};
