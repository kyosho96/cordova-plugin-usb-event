# cordova-plugin-usb-event
This plugin can handle USB attached and detached events on cordova-android.
No special permission settings are required to handle the events.

This plugin can search at [Corova Plugin Search](https://cordova.apache.org/plugins/?q=cordova-plugin-usb-event).
And it is registered at [npmjs](https://www.npmjs.com/package/cordova-plugin-usb-event).

## Install

```
$ cordova pluguin add cordova-plugin-usb-event
```

## Usage

* listDevices
* registerEventCallback

### List USB devices

`listDevices` method get current connected USB device list.

```js
cordova.plugins.usbevent.listDevices(
      function(list) {
        console.log(list);
      },
      function(error) {
        console.log(error);
      });
```

The following result is output.
USB device information has vendor ID and product ID.

```json
{
  "id": "list"
  "devices": [
    {
      "vendorId": 1234,
      "productId": 4321
    }
  ]
}
```

### Handle USB attached and detached event

`registerEventCallback` method can get USB device information on attached and detached.

```js
cordova.plugins.usbevent.registerEventCallback(
      function(result) {
        console.log(result);
      },
      function(error) {
        console.log(error);
      });
```

The following result is output.
First, the following result is returned after method call success.

```json
{
  "id": "callbackRegistered"
}
```

Next, each result is returned on attaching an detaching USB.

```json
{
  "id": "attached"
  "devices": [
    {
      "vendorId": 1234,
      "productId": 4321
    }
  ]
}
```

```json
{
  "id": "detached"
  "devices": [
    {
      "vendorId": 1234,
      "productId": 4321
    }
  ]
}
```

### Common output object definition

Returned data definition is that.

```ts
// Typescript data definitions.

/**
 * Event ID
 */
enum UsbEventId {
    Registered = 'callbackRegistered',
    Attached = 'attached',
    Detached = 'detached'
}

/**
 * USB device information
 * 
 * Information may be added more.
 */
interface UsbDevice {
    vendorId: number;
    productId: number;
}

/**
 * Output by method call.
 */
interface UsbResult {
    id: UsbEventId;
    devices?: UsbDevice[];
}
```

## Example

### listDevices

```ts
// Typescript
cordova.plugins.usbevent.listDevices(
  (result: UsbResult) => {
    console.log(result.id);
    for(const device in result.devices) {
      console.log(device.vendorId);
      console.log(device.productId);
    }
  },
  (error: string) => {
    console.log(error);
  });
```

### registerEventCallback

```ts
// Typescript
cordova.plugins.usbevent.registerEventCallback(
  (result: UsbResult) => {
    switch (result.id) {
      case 'callbackRegistered':
        console.log(`register is success.`);
        break;
      case 'attached':
        console.log(`USB device is attached.`);
        for(const device in result.devices) {
          console.log(device.vendorId);
          console.log(device.productId);
        }
        break;
      case 'detached':
        console.log(`USB device is detached.`);
        for(const device in result.devices) {
          console.log(device.vendorId);
          console.log(device.productId);
        }
        break;
      default:
        reject(`Unsupported event. (event=${JSON.stringify(result)})`);
    }
  },
  (error: string) => {
    console.log(error);
  });
```

# License - MIT

```
MIT License

Copyright (c) 2019 Akira Kurosawa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```