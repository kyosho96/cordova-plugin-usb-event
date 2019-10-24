var exec = require('cordova/exec');

exports.registerEventCallback = function (success, error) {
    exec(success, error, 'UsbEvent', 'registerEventCallback', []);
};

exports.listDevices = function (success, error) {
    exec(success, error, 'UsbEvent', 'listDevices', []);
};
