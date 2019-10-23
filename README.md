# cordova-plugin-usb-event
USB attached event plugin.

```ts
cordova.plugins.usbevent.registerEventCallback(
      'test...',
      (result: string) => {
        console.log(result);
      },
      (error: any) => {
        console.log(error);
      }
```