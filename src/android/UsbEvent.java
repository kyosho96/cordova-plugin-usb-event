package net.kyosho.usb.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class UsbEvent extends CordovaPlugin {

  private final String TAG = UsbEvent.class.getSimpleName();

  // private static final String ACTION_ATTACH_CALLBACK =
  // "registerAttachCallback";
  // private static final String ACTION_DETACH_CALLBACK =
  // "registerDetachCallback";
  private static final String ACTION_EVENT_CALLBACK = "registerEventCallback";

  private UsbManager usbManager;

  private CallbackContext eventCallback;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    switch (action) {
    // case ACTION_ATTACH_CALLBACK:
    // this.coolMethod(args, callbackContext);
    // return true;
    // case ACTION_DETACH_CALLBACK:
    // this.coolMethod(args, callbackContext);
    // return true;
    case ACTION_EVENT_CALLBACK:
      this.registerEventCallback(args, callbackContext);
      return true;
    default:
      return false;
    }
  }

  // private void coolMethod(JSONArray args, CallbackContext callbackContext)
  // throws JSONException {
  // String message = args.getString(0);
  //
  // if (message != null && message.length() > 0) {
  // callbackContext.success(message);
  // } else {
  // callbackContext.error("Expected one non-empty string argument.");
  // }
  // }

  private void registerEventCallback(final JSONArray args, final CallbackContext callbackContext) {
    Log.d(TAG, "Registering callback");
    this.registerUsbAttached();
    this.registerUsbDetached();

    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        Log.d(TAG, "Registering Event Callback");
        eventCallback = callbackContext;
        JSONObject returnObj = new JSONObject();
        addProperty(returnObj, ACTION_EVENT_CALLBACK, true);
        // Keep the callback
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
      }
    });
  }

  private void addProperty(JSONObject obj, String key, Object value) {
    try {
      obj.put(key, value);
    } catch (JSONException e) {
    }
  }

  private void registerUsbAttached() {
    IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
    this.cordova.getActivity().registerReceiver(this.usbAttachReceiver, filter);
  }

  private void registerUsbDetached() {
    IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
    this.cordova.getActivity().registerReceiver(this.usbDetachReceiver, filter);
  }

  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    this.cordova.getActivity().unregisterReceiver(this.usbDetachReceiver);
    this.cordova.getActivity().unregisterReceiver(this.usbAttachReceiver);
  }

  private static final String PROPERTY_EVENT_KEY = "event";
  private static final String PROPERTY_EVENT_VALUE_ATTACHED = "attached";
  private static final String PROPERTY_EVENT_VALUE_DETACHED = "detached";

  BroadcastReceiver usbAttachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
        // showDevices();
        if (UsbEvent.this.eventCallback != null) {
          JSONObject returnObj = new JSONObject();
          addProperty(returnObj, PROPERTY_EVENT_KEY, PROPERTY_EVENT_VALUE_ATTACHED);
          PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
          result.setKeepCallback(true);
          eventCallback.sendPluginResult(result);
        }
      }
    }
  };

  BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device != null) {
          // call your method that cleans up and closes communication with the device
          // UsbDataBinder binder = mHashMap.get(device);
          // if (binder != null) {
          // binder.onDestroy();
          // mHashMap.remove(device);
          // }
          if (UsbEvent.this.eventCallback != null) {
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, PROPERTY_EVENT_KEY, PROPERTY_EVENT_VALUE_DETACHED);
            PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
            result.setKeepCallback(true);
            eventCallback.sendPluginResult(result);
          }
        }
      }
    }
  };
}
