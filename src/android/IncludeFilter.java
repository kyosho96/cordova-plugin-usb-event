package net.kyosho.usb.event;

import android.hardware.usb.UsbDevice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

final class IncludeFilter {

  static class DeviceModel {
    private int vendorId;
    private int productId;

    DeviceModel(final int vendorId, final int productId) {
      this.vendorId = vendorId;
      this.productId = productId;
    }

    int getVendorId() {
      return this.vendorId;
    }

    int getProductId() {
      return this.productId;
    }
  }

  private List<DeviceModel> deviceModelList;

  public static IncludeFilter create(JSONObject jsonObject) throws JSONException {
    List<DeviceModel> deviceModelList = new ArrayList<>();

    String id = jsonObject.getString(UsbEventModel.PROPERTY_EVENT_KEY_ID);
    if (!(UsbEventId.Include.toString().equals(id))) {
      throw new JSONException("id is not 'include'.");
    }

    JSONArray jsonArray = jsonObject.getJSONArray(UsbEventModel.PROPERTY_EVENT_KEY_DEVICE_LIST);
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonDevice = jsonArray.getJSONObject(i);
      int vid = jsonDevice.getInt(UsbEventModel.PROPERTY_EVENT_KEY_VID);
      int pid = jsonDevice.getInt(UsbEventModel.PROPERTY_EVENT_KEY_PID);

      deviceModelList.add(new DeviceModel(vid, pid));
    }

    // delete double instance
    deviceModelList = new ArrayList<>(new HashSet<>(deviceModelList));

    return new IncludeFilter(deviceModelList);
  }

  private IncludeFilter(final List<DeviceModel> deviceModelList) {
    this.deviceModelList = deviceModelList;
  }

  public UsbDevice filter(final UsbDevice device) {
    UsbDevice filteredDevice = null;

    if (null != this.deviceModelList && 0 < this.deviceModelList.size()) {
      for (DeviceModel model : this.deviceModelList) {
        if (device.getVendorId() == model.getVendorId() &&
          device.getProductId() == model.getProductId()) {
          filteredDevice = device;
          break;
        }
      }
    }

    return filteredDevice;
  }
}
