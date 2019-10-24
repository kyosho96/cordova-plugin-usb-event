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

import java.util.HashMap;

/**
 * This class echoes a string called from JavaScript.
 */
public class UsbEvent extends CordovaPlugin {

  private final String TAG = UsbEvent.class.getSimpleName();

  private static final String ACTION_EVENT_CALLBACK = "registerEventCallback";

  private static final String PROPERTY_EVENT_KEY = "event";
  private static final String PROPERTY_VID_KEY = "vendorId";
  private static final String PROPERTY_PID_KEY = "productId";

  private static final String PROPERTY_EVENT_VALUE_CALLBACK = "callbackRegistered";
  private static final String PROPERTY_EVENT_VALUE_ATTACHED = "attached";
  private static final String PROPERTY_EVENT_VALUE_DETACHED = "detached";

  private CallbackContext eventCallback;

  private UsbManager usbManager;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
    switch (action) {
      case ACTION_EVENT_CALLBACK:
        this.registerEventCallback(args, callbackContext);
        return true;
      default:
        callbackContext.error(String.format("Unsupported action. (action=%s)", action));
    }
    return false;
  }

  private void registerEventCallback(final JSONArray args, final CallbackContext callbackContext) {
    Log.d(TAG, "Registering callback");
    this.registerUsbAttached();
    this.registerUsbDetached();

    cordova.getThreadPool().execute(() -> {
      try {
        Log.d(TAG, "Registering Event Callback");
        eventCallback = callbackContext;
        JSONObject returnObj = new JSONObject();
        returnObj.put(PROPERTY_EVENT_KEY, PROPERTY_EVENT_VALUE_CALLBACK);
        // Keep the callback
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    });
  }

  private void registerUsbAttached() {
    IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
    this.cordova.getActivity().registerReceiver(this.usbAttachReceiver, filter);
  }

  private void registerUsbDetached() {
    IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
    this.cordova.getActivity().registerReceiver(this.usbDetachReceiver, filter);
  }

  private void updateCurrentUsbDeviceList() {
    HashMap<String, UsbDevice> deviceList = this.usbManager.getDeviceList();
  }

  @Override
  public void onStart() {
    super.onStart();
    this.usbManager = (UsbManager) this.cordova.getActivity().getSystemService(Context.USB_SERVICE);
    this.updateCurrentUsbDeviceList();
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

  private BroadcastReceiver usbAttachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      try {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) &&
          UsbEvent.this.eventCallback != null && device != null) {

          JSONObject returnObj = new JSONObject();
          returnObj.put(PROPERTY_EVENT_KEY, PROPERTY_EVENT_VALUE_ATTACHED);
          returnObj.put(PROPERTY_VID_KEY, device.getVendorId());
          returnObj.put(PROPERTY_PID_KEY, device.getProductId());

          PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
          result.setKeepCallback(true);
          eventCallback.sendPluginResult(result);
        }
      } catch (JSONException e) {
        eventCallback.error(e.getMessage());
      }
    }
  };

  private BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      try {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) &&
          UsbEvent.this.eventCallback != null && device != null) {

          JSONObject returnObj = new JSONObject();
          returnObj.put(PROPERTY_EVENT_KEY, PROPERTY_EVENT_VALUE_DETACHED);
          returnObj.put(PROPERTY_VID_KEY, device.getVendorId());
          returnObj.put(PROPERTY_PID_KEY, device.getProductId());

          PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
          result.setKeepCallback(true);
          eventCallback.sendPluginResult(result);
        }
      } catch (JSONException e) {
        eventCallback.error(e.getMessage());
      }
    }
  };
}
