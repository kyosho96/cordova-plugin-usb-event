# cordova-plugin-usb-event
cordova-android 向けにUSB接続・切断イベントを扱えるようになります。
尚、特別なパーミッション設定は不要です。

[Corova Plugin Search](https://cordova.apache.org/plugins/?q=cordova-plugin-usb-event)と[npmjs](https://www.npmjs.com/package/cordova-plugin-usb-event)に登録しています。

## インストール

```
$ cordova pluguin add cordova-plugin-usb-event
```

## 使い方

* listDevices
* registerEventCallback
* unregisterEventCallback
* existsRegisteredCallback

### USB機器リストを取得する

`listDevices` メソッドを利用すると、接続中のUSB機器のリストを取得できます。

```js
cordova.plugins.usbevent.listDevices(
      function(list) {
        console.log(list);
      },
      function(error) {
        console.log(error);
      });
```

実行すると以下の結果を得られます。
USB機器のベンダーIDとプロダクトIDが含まれます。

```json
{
  "id": "list",
  "devices": [
    {
      "vendorId": 1234,
      "productId": 4321
    }
  ]
}
```

### USB接続・切断イベントを取得する

`registerEventCallback` メソッドを利用すると、USB機器を接続・切断したタイミングで対象USB機器の情報を取得できます。

```js
cordova.plugins.usbevent.registerEventCallback(
      function(result) {
        console.log(result);
      },
      function(error) {
        console.log(error);
      });
```

実行成功直後に、成功したメッセージを得られます。

```json
{
  "id": "callbackRegistered"
}
```

成功メッセージ取得後、USB機器の切断・接続をする度に、イベントが発生して以下のような対象USB機器の情報を得ることができます。

```json
{
  "id": "attached",
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
  "id": "detached",
  "devices": [
    {
      "vendorId": 1234,
      "productId": 4321
    }
  ]
}
```

注釈：登録できるコールバックは１つです。2回目以降は以前のコールバック関数を上書きします。
複数のイベントハンドラを扱いたい場合は、Javascript側で対処ください。

### USB接続・切断イベントを取得を停止する

`unregisterEventCallback` メソッドを利用すると
`registerEventCallback` メソッドで登録したコールバックを解除できます。

```js
cordova.plugins.usbevent.unregisterEventCallback(
      function(result) {
        console.log(result);
      },
      function(error) {
        console.log(error);
      });
```

以下のオブジェクトが返れば成功です。

```json
{
  "id": "callbackUnregistered"
}
```

### イベント取得の状態を確認する

`existsRegisteredCallback` メソッドは、コールバックを設定済みかを確認します。
コールバック設定済みのときは `true` を返します。

```js
cordova.plugins.usbevent.existsRegisteredCallback(
      function(result) {
        console.log(result); // true if exists.
      },
      function(error) {
        console.log(error);
      });
```

### 取得データの定義

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

## 利用例

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

### unregisterEventCallback

```ts
// Typescript
cordova.plugins.usbevent.unregisterEventCallback(
  (result: UsbResult) => {
    switch (result.id) {
      case 'callbackUnregistered':
        console.log(result);
        break;
      default:
        console.log(`Unsupported event. (event=${JSON.stringify(result)})`);
    }
  },
  (error: string) => {
    console.log(error);
  });
```

### existsRegisteredCallback

```ts
// Typescript
cordova.plugins.usbevent.existsRegisteredCallback(
  (exists: boolean) => {
    console.log(exists); // true if exists.
  },
  (error: any) => {
    console.log(error);
  });
```

## UML - シーケンス図

![sequence diagram](./doc/sequence(cordova-plugin-usb-event).png)

# ライセンス - MIT

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