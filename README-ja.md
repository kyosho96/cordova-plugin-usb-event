## cordova-plugin-usb-event
USB attached event plugin.

```ts
cordova.plugins.usbevent.listDevices(
      (list: any) => {
        console.log(list);
      },
      (error: any) => {
        console.log(error);
      });

cordova.plugins.usbevent.registerEventCallback(
      (result: string) => {
        console.log(result);
      },
      (error: any) => {
        console.log(error);
      });
```