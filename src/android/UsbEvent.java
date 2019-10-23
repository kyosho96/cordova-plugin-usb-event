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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class echoes a string called from JavaScript.
 */
public class UsbEvent extends CordovaPlugin {

  private final String TAG = UsbEvent.class.getSimpleName();

  private static final String ACTION_EVENT_CALLBACK = "registerEventCallback";

  private CallbackContext eventCallback;

  private UsbManager usbManager;

  private Map<String, UsbDevice> currentDeviceList = new HashMap();

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    switch (action) {
      case ACTION_EVENT_CALLBACK:
        this.registerEventCallback(args, callbackContext);
        return true;
      default:
        return false;
    }
  }

  private void registerEventCallback(final JSONArray args, final CallbackContext callbackContext) {
    Log.d(TAG, "Registering callback");
    this.registerUsbAttached();
    this.registerUsbDetached();

    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        try {
          Log.d(TAG, "Registering Event Callback");
          eventCallback = callbackContext;
          JSONObject returnObj = new JSONObject();
          returnObj.put(ACTION_EVENT_CALLBACK, true);
          // Keep the callback
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
          pluginResult.setKeepCallback(true);
          callbackContext.sendPluginResult(pluginResult);
        } catch (JSONException e) {
          // TODO:
          e.printStackTrace();
        }
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
    this.currentDeviceList.clear();
    HashMap<String, UsbDevice> deviceList = this.usbManager.getDeviceList();
    this.currentDeviceList.putAll(deviceList);
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

  private static final String PROPERTY_EVENT_KEY = "event";
  private static final String PROPERTY_EVENT_VALUE_ATTACHED = "attached";
  private static final String PROPERTY_EVENT_VALUE_DETACHED = "detached";

  BroadcastReceiver usbAttachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      try {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) &&
          UsbEvent.this.eventCallback != null &&
          device != null) {

          JSONObject returnObj = new JSONObject();
          returnObj.put(PROPERTY_EVENT_KEY, PROPERTY_EVENT_VALUE_ATTACHED);
          returnObj.put("vendorId", device.getVendorId());
          returnObj.put("productId", device.getProductId());

          PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
          result.setKeepCallback(true);
          eventCallback.sendPluginResult(result);
        }
      } catch (JSONException e) {
        // TODO:
        e.printStackTrace();
      }
    }
  };

  BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      try {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) &&
          UsbEvent.this.eventCallback != null &&
          device != null) {

          JSONObject returnObj = new JSONObject();
          returnObj.put(PROPERTY_EVENT_KEY, PROPERTY_EVENT_VALUE_DETACHED);
          returnObj.put("vendorId", device.getVendorId());
          returnObj.put("productId", device.getProductId());

          PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
          result.setKeepCallback(true);
          eventCallback.sendPluginResult(result);
        }
      } catch (JSONException e) {
        // TODO:
        e.printStackTrace();
      }
    }
  };
}
