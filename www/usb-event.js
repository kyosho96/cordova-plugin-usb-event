var exec = require('cordova/exec');

/**
 * list USB devices.
 */
exports.listDevices = function (success, error) {
    exec(success, error, 'UsbEvent', 'listDevices', []);
};

/**
 * Check callback is already exists.
 */
exports.existsRegisteredCallback = function (success, error) {
    exec(success, error, 'UsbEvent', 'existsRegisteredCallback', []);
};

/**
 * Register USB attached and detached event callback.
 */
exports.registerEventCallback = function (success, error) {
    exec(success, error, 'UsbEvent', 'registerEventCallback', []);
};

/**
 * Clear registered callback.
 */
exports.unregisterEventCallback = function (success, error) {
    exec(success, error, 'UsbEvent', 'unregisterEventCallback', []);
};
