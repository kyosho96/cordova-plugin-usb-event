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

  /**
   * TAG
   */
  private final String TAG = UsbEvent.class.getSimpleName();

  /**
   * Action key for list device.
   */
  private static final String ACTION_LIST_DEVICES = "listDevices";

  /**
   * Action key for checking register status.
   */
  private static final String ACTION_EVENT_EXISTS_CALLBACK = "existsRegisteredCallback";

  /**
   * Action key for registering event callback.
   */
  private static final String ACTION_EVENT_REGISTER_CALLBACK = "registerEventCallback";

  /**
   * Action key for unregistering event callback.
   */
  private static final String ACTION_EVENT_UNREGISTER_CALLBACK = "unregisterEventCallback";

  /**
   * Output event value property keys.
   */
  private static final String PROPERTY_EVENT_KEY_DEVICE_LIST = "devices";
  private static final String PROPERTY_EVENT_KEY_ID = "id";
  private static final String PROPERTY_EVENT_KEY_VID = "vendorId";
  private static final String PROPERTY_EVENT_KEY_PID = "productId";

  /**
   * Output event ids.
   */
  private static final String PROPERTY_EVENT_VALUE_LIST = "list";
  private static final String PROPERTY_EVENT_VALUE_REGISTER_CALLBACK = "callbackRegistered";
  private static final String PROPERTY_EVENT_VALUE_UNREGISTER_CALLBACK = "callbackUnregistered";
  private static final String PROPERTY_EVENT_VALUE_ATTACHED = "attached";
  private static final String PROPERTY_EVENT_VALUE_DETACHED = "detached";

  /**
   * Registered event callback.
   */
  private CallbackContext eventCallback;

  /**
   * USB Manager.
   */
  private UsbManager usbManager;

  /**
   * Javascript entry point.
   *
   * @param action          The action to execute.
   * @param args            The exec() arguments.
   * @param callbackContext The callback context used when calling back into JavaScript.
   * @return result.
   */
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
    switch (action) {
      case ACTION_LIST_DEVICES:
        this.listDevices(callbackContext);
        return true;
      case ACTION_EVENT_EXISTS_CALLBACK:
        this.existsRegisteredCallback(callbackContext);
        return true;
      case ACTION_EVENT_REGISTER_CALLBACK:
        this.registerEventCallback(callbackContext);
        return true;
      case ACTION_EVENT_UNREGISTER_CALLBACK:
        this.unregisterEventCallback(callbackContext);
        return true;
      default:
        callbackContext.error(String.format("Unsupported action. (action=%s)", action));
    }
    return false;
  }

  /**
   * List USB devices.
   *
   * @param callbackContext The callback context used when calling back into JavaScript.
   */
  private void listDevices(final CallbackContext callbackContext) {
    try {
      if (null == this.usbManager) {
        // Caching USBManager
        this.usbManager = (UsbManager) this.cordova.getActivity().getSystemService(Context.USB_SERVICE);
      }

      // Get USB devices
      HashMap<String, UsbDevice> deviceMap = this.usbManager.getDeviceList();

      // create output JSON object
      JSONObject jsonObject = new JSONObject();
      jsonObject.put(PROPERTY_EVENT_KEY_ID, PROPERTY_EVENT_VALUE_LIST);

      JSONArray jsonArrayObject = new JSONArray();
      for (UsbDevice device : deviceMap.values()) {
        JSONObject jsonDevice = new JSONObject();
        jsonDevice.put(PROPERTY_EVENT_KEY_VID, device.getVendorId());
        jsonDevice.put(PROPERTY_EVENT_KEY_PID, device.getProductId());
        jsonArrayObject.put(jsonDevice);
      }
      jsonObject.put(PROPERTY_EVENT_KEY_DEVICE_LIST, jsonArrayObject);

      // Callback with result.
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonObject);
      pluginResult.setKeepCallback(false);
      callbackContext.sendPluginResult(pluginResult);
    } catch (JSONException e) {
      if (null == callbackContext) {
        Log.e(TAG, "callbackContext is null.");
      } else {
        callbackContext.error(e.getMessage());
      }
    }
  }

  /**
   * Check callback is already exists.
   *
   * @param callbackContext The callback context used when calling back into JavaScript.
   */
  private void existsRegisteredCallback(final CallbackContext callbackContext) {
    cordova.getThreadPool().execute(() -> {
      boolean exists = (null != eventCallback);

      // Callback with result.
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, exists);
      pluginResult.setKeepCallback(false);
      callbackContext.sendPluginResult(pluginResult);
    });
  }

  /**
   * Register event callback.
   * Callback emit device information at attaching and detaching USB after this method call.
   *
   * @param callbackContext The callback context used when calling back into JavaScript.
   */
  private void registerEventCallback(final CallbackContext callbackContext) {
    // Start monitoring
    this.registerUsbAttached();
    this.registerUsbDetached();

    cordova.getThreadPool().execute(() -> {
      try {
        // Update callback
        eventCallback = callbackContext;

        // create output JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PROPERTY_EVENT_KEY_ID, PROPERTY_EVENT_VALUE_REGISTER_CALLBACK);

        // Callback with result.
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonObject);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
      } catch (JSONException e) {
        if (null == callbackContext) {
          Log.e(TAG, "eventCallback is null.");
        } else {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  /**
   * Unregister event callback.
   *
   * @param callbackContext The callback context used when calling back into JavaScript.
   */
  private void unregisterEventCallback(final CallbackContext callbackContext) {
    // Stop monitoring
    this.unregisterUsbDetached();
    this.unregisterUsbAttached();

    cordova.getThreadPool().execute(() -> {
      try {
        // Update callback
        eventCallback = null;

        // create output JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PROPERTY_EVENT_KEY_ID, PROPERTY_EVENT_VALUE_UNREGISTER_CALLBACK);

        // Callback with result.
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonObject);
        pluginResult.setKeepCallback(false);
        callbackContext.sendPluginResult(pluginResult);
      } catch (JSONException e) {
        if (null == callbackContext) {
          Log.e(TAG, "eventCallback is null.");
        } else {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  /**
   * Start monitoring USB attached.
   */
  private void registerUsbAttached() {
    IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
    this.cordova.getActivity().registerReceiver(this.usbAttachReceiver, filter);
  }

  /**
   * Start monitoring USB detached.
   */
  private void registerUsbDetached() {
    IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
    this.cordova.getActivity().registerReceiver(this.usbDetachReceiver, filter);
  }

  /**
   * Stop monitoring USB attached and detached.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    this.unregisterUsbDetached();
    this.unregisterUsbAttached();
  }

  /**
   * Stop monitoring USB attached.
   */
  private void unregisterUsbAttached() {
    this.cordova.getActivity().unregisterReceiver(this.usbAttachReceiver);
  }

  /**
   * Stop monitoring USB detached.
   */
  private void unregisterUsbDetached() {
    this.cordova.getActivity().unregisterReceiver(this.usbDetachReceiver);
  }

  /**
   * USB attaching monitor.
   */
  private BroadcastReceiver usbAttachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      try {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) &&
          UsbEvent.this.eventCallback != null && device != null) {

          // create output JSON object
          JSONObject jsonObject = new JSONObject();
          jsonObject.put(PROPERTY_EVENT_KEY_ID, PROPERTY_EVENT_VALUE_ATTACHED);

          JSONArray jsonArrayObject = new JSONArray();
          JSONObject jsonDevice = new JSONObject();
          jsonDevice.put(PROPERTY_EVENT_KEY_VID, device.getVendorId());
          jsonDevice.put(PROPERTY_EVENT_KEY_PID, device.getProductId());
          jsonArrayObject.put(jsonDevice);
          jsonObject.put(PROPERTY_EVENT_KEY_DEVICE_LIST, jsonArrayObject);

          // Callback with result.
          PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
          result.setKeepCallback(true);
          eventCallback.sendPluginResult(result);
        }
      } catch (JSONException e) {
        if (null == eventCallback) {
          Log.e(TAG, "eventCallback is null.");
        } else {
          eventCallback.error(e.getMessage());
        }
      }
    }
  };

  /**
   * USB detaching monitor.
   */
  private BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      try {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) &&
          UsbEvent.this.eventCallback != null && device != null) {

          // create output JSON object
          JSONObject jsonObject = new JSONObject();
          jsonObject.put(PROPERTY_EVENT_KEY_ID, PROPERTY_EVENT_VALUE_DETACHED);

          JSONArray jsonArrayObject = new JSONArray();
          JSONObject jsonDevice = new JSONObject();
          jsonDevice.put(PROPERTY_EVENT_KEY_VID, device.getVendorId());
          jsonDevice.put(PROPERTY_EVENT_KEY_PID, device.getProductId());
          jsonArrayObject.put(jsonDevice);
          jsonObject.put(PROPERTY_EVENT_KEY_DEVICE_LIST, jsonArrayObject);

          // Callback with result.
          PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
          result.setKeepCallback(true);
          eventCallback.sendPluginResult(result);
        }
      } catch (JSONException e) {
        if (null == eventCallback) {
          Log.e(TAG, "eventCallback is null.");
        } else {
          eventCallback.error(e.getMessage());
        }
      }
    }
  };
}
