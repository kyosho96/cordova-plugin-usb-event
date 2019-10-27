package net.kyosho.usb.event;

enum UsbEventId {
  List("list"),
  Registered("callbackRegistered"),
  Unregistered("callbackUnregistered"),
  Attached("attached"),
  Detached("detached"),
  Include("include");

  private final String id;

  public static UsbEventId parse(final String id) {
    UsbEventId result = null;

    for (UsbEventId usbEventId : UsbEventId.values()) {
      if(usbEventId.toString().equals(id)) {
        result = usbEventId;
        break;
      }
    }

    return result;
  }

  UsbEventId(final String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return this.id;
  }
}
