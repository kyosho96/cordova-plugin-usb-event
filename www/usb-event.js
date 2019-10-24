var exec = require('cordova/exec');

exports.registerEventCallback = function (success, error) {
    exec(success, error, 'UsbEvent', 'registerEventCallback', []);
};
