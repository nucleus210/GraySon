// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/assistant/embedded/v1alpha2/embedded_assistant.proto

package com.google.assistant.embedded.v1alpha2;

/**
 * <pre>
 * *Required* Fields that identify the device to the Assistant.
 * See also:
 * *   [Register a Device - REST
 * API](https://developers.google.com/assistant/sdk/reference/device-registration/register-device-manual)
 * *   [Device Model and Instance
 * Schemas](https://developers.google.com/assistant/sdk/reference/device-registration/model-and-instance-schemas)
 * *   [Device
 * Proto](https://developers.google.com/assistant/sdk/reference/rpc/google.assistant.devices.v1alpha2#device)
 * </pre>
 *
 * Protobuf type {@code google.assistant.embedded.v1alpha2.DeviceConfig}
 */
public  final class DeviceConfig extends
    com.google.protobuf.GeneratedMessageLite<
        DeviceConfig, DeviceConfig.Builder> implements
    // @@protoc_insertion_point(message_implements:google.assistant.embedded.v1alpha2.DeviceConfig)
    DeviceConfigOrBuilder {
  private DeviceConfig() {
    deviceId_ = "";
    deviceModelId_ = "";
  }
  public static final int DEVICE_ID_FIELD_NUMBER = 1;
  private java.lang.String deviceId_;
  /**
   * <pre>
   * *Required* Unique identifier for the device. The id length must be 128
   * characters or less. Example: DBCDW098234. This MUST match the device_id
   * returned from device registration. This device_id is used to match against
   * the user's registered devices to lookup the supported traits and
   * capabilities of this device. This information should not change across
   * device reboots. However, it should not be saved across
   * factory-default resets.
   * </pre>
   *
   * <code>optional string device_id = 1;</code>
   */
  public java.lang.String getDeviceId() {
    return deviceId_;
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device. The id length must be 128
   * characters or less. Example: DBCDW098234. This MUST match the device_id
   * returned from device registration. This device_id is used to match against
   * the user's registered devices to lookup the supported traits and
   * capabilities of this device. This information should not change across
   * device reboots. However, it should not be saved across
   * factory-default resets.
   * </pre>
   *
   * <code>optional string device_id = 1;</code>
   */
  public com.google.protobuf.ByteString
      getDeviceIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(deviceId_);
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device. The id length must be 128
   * characters or less. Example: DBCDW098234. This MUST match the device_id
   * returned from device registration. This device_id is used to match against
   * the user's registered devices to lookup the supported traits and
   * capabilities of this device. This information should not change across
   * device reboots. However, it should not be saved across
   * factory-default resets.
   * </pre>
   *
   * <code>optional string device_id = 1;</code>
   */
  private void setDeviceId(
      java.lang.String value) {
    if (value == null) {
    throw new NullPointerException();
  }
  
    deviceId_ = value;
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device. The id length must be 128
   * characters or less. Example: DBCDW098234. This MUST match the device_id
   * returned from device registration. This device_id is used to match against
   * the user's registered devices to lookup the supported traits and
   * capabilities of this device. This information should not change across
   * device reboots. However, it should not be saved across
   * factory-default resets.
   * </pre>
   *
   * <code>optional string device_id = 1;</code>
   */
  private void clearDeviceId() {
    
    deviceId_ = getDefaultInstance().getDeviceId();
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device. The id length must be 128
   * characters or less. Example: DBCDW098234. This MUST match the device_id
   * returned from device registration. This device_id is used to match against
   * the user's registered devices to lookup the supported traits and
   * capabilities of this device. This information should not change across
   * device reboots. However, it should not be saved across
   * factory-default resets.
   * </pre>
   *
   * <code>optional string device_id = 1;</code>
   */
  private void setDeviceIdBytes(
      com.google.protobuf.ByteString value) {
    if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
    
    deviceId_ = value.toStringUtf8();
  }

  public static final int DEVICE_MODEL_ID_FIELD_NUMBER = 3;
  private java.lang.String deviceModelId_;
  /**
   * <pre>
   * *Required* Unique identifier for the device model. The combination of
   * device_model_id and device_id must have been previously associated through
   * device registration.
   * </pre>
   *
   * <code>optional string device_model_id = 3;</code>
   */
  public java.lang.String getDeviceModelId() {
    return deviceModelId_;
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device model. The combination of
   * device_model_id and device_id must have been previously associated through
   * device registration.
   * </pre>
   *
   * <code>optional string device_model_id = 3;</code>
   */
  public com.google.protobuf.ByteString
      getDeviceModelIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(deviceModelId_);
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device model. The combination of
   * device_model_id and device_id must have been previously associated through
   * device registration.
   * </pre>
   *
   * <code>optional string device_model_id = 3;</code>
   */
  private void setDeviceModelId(
      java.lang.String value) {
    if (value == null) {
    throw new NullPointerException();
  }
  
    deviceModelId_ = value;
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device model. The combination of
   * device_model_id and device_id must have been previously associated through
   * device registration.
   * </pre>
   *
   * <code>optional string device_model_id = 3;</code>
   */
  private void clearDeviceModelId() {
    
    deviceModelId_ = getDefaultInstance().getDeviceModelId();
  }
  /**
   * <pre>
   * *Required* Unique identifier for the device model. The combination of
   * device_model_id and device_id must have been previously associated through
   * device registration.
   * </pre>
   *
   * <code>optional string device_model_id = 3;</code>
   */
  private void setDeviceModelIdBytes(
      com.google.protobuf.ByteString value) {
    if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
    
    deviceModelId_ = value.toStringUtf8();
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!deviceId_.isEmpty()) {
      output.writeString(1, getDeviceId());
    }
    if (!deviceModelId_.isEmpty()) {
      output.writeString(3, getDeviceModelId());
    }
  }

  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;

    size = 0;
    if (!deviceId_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeStringSize(1, getDeviceId());
    }
    if (!deviceModelId_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeStringSize(3, getDeviceModelId());
    }
    memoizedSerializedSize = size;
    return size;
  }

  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.google.assistant.embedded.v1alpha2.DeviceConfig parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.google.assistant.embedded.v1alpha2.DeviceConfig prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  /**
   * <pre>
   * *Required* Fields that identify the device to the Assistant.
   * See also:
   * *   [Register a Device - REST
   * API](https://developers.google.com/assistant/sdk/reference/device-registration/register-device-manual)
   * *   [Device Model and Instance
   * Schemas](https://developers.google.com/assistant/sdk/reference/device-registration/model-and-instance-schemas)
   * *   [Device
   * Proto](https://developers.google.com/assistant/sdk/reference/rpc/google.assistant.devices.v1alpha2#device)
   * </pre>
   *
   * Protobuf type {@code google.assistant.embedded.v1alpha2.DeviceConfig}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.google.assistant.embedded.v1alpha2.DeviceConfig, Builder> implements
      // @@protoc_insertion_point(builder_implements:google.assistant.embedded.v1alpha2.DeviceConfig)
      com.google.assistant.embedded.v1alpha2.DeviceConfigOrBuilder {
    // Construct using com.google.assistant.embedded.v1alpha2.DeviceConfig.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * *Required* Unique identifier for the device. The id length must be 128
     * characters or less. Example: DBCDW098234. This MUST match the device_id
     * returned from device registration. This device_id is used to match against
     * the user's registered devices to lookup the supported traits and
     * capabilities of this device. This information should not change across
     * device reboots. However, it should not be saved across
     * factory-default resets.
     * </pre>
     *
     * <code>optional string device_id = 1;</code>
     */
    public java.lang.String getDeviceId() {
      return instance.getDeviceId();
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device. The id length must be 128
     * characters or less. Example: DBCDW098234. This MUST match the device_id
     * returned from device registration. This device_id is used to match against
     * the user's registered devices to lookup the supported traits and
     * capabilities of this device. This information should not change across
     * device reboots. However, it should not be saved across
     * factory-default resets.
     * </pre>
     *
     * <code>optional string device_id = 1;</code>
     */
    public com.google.protobuf.ByteString
        getDeviceIdBytes() {
      return instance.getDeviceIdBytes();
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device. The id length must be 128
     * characters or less. Example: DBCDW098234. This MUST match the device_id
     * returned from device registration. This device_id is used to match against
     * the user's registered devices to lookup the supported traits and
     * capabilities of this device. This information should not change across
     * device reboots. However, it should not be saved across
     * factory-default resets.
     * </pre>
     *
     * <code>optional string device_id = 1;</code>
     */
    public Builder setDeviceId(
        java.lang.String value) {
      copyOnWrite();
      instance.setDeviceId(value);
      return this;
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device. The id length must be 128
     * characters or less. Example: DBCDW098234. This MUST match the device_id
     * returned from device registration. This device_id is used to match against
     * the user's registered devices to lookup the supported traits and
     * capabilities of this device. This information should not change across
     * device reboots. However, it should not be saved across
     * factory-default resets.
     * </pre>
     *
     * <code>optional string device_id = 1;</code>
     */
    public Builder clearDeviceId() {
      copyOnWrite();
      instance.clearDeviceId();
      return this;
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device. The id length must be 128
     * characters or less. Example: DBCDW098234. This MUST match the device_id
     * returned from device registration. This device_id is used to match against
     * the user's registered devices to lookup the supported traits and
     * capabilities of this device. This information should not change across
     * device reboots. However, it should not be saved across
     * factory-default resets.
     * </pre>
     *
     * <code>optional string device_id = 1;</code>
     */
    public Builder setDeviceIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDeviceIdBytes(value);
      return this;
    }

    /**
     * <pre>
     * *Required* Unique identifier for the device model. The combination of
     * device_model_id and device_id must have been previously associated through
     * device registration.
     * </pre>
     *
     * <code>optional string device_model_id = 3;</code>
     */
    public java.lang.String getDeviceModelId() {
      return instance.getDeviceModelId();
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device model. The combination of
     * device_model_id and device_id must have been previously associated through
     * device registration.
     * </pre>
     *
     * <code>optional string device_model_id = 3;</code>
     */
    public com.google.protobuf.ByteString
        getDeviceModelIdBytes() {
      return instance.getDeviceModelIdBytes();
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device model. The combination of
     * device_model_id and device_id must have been previously associated through
     * device registration.
     * </pre>
     *
     * <code>optional string device_model_id = 3;</code>
     */
    public Builder setDeviceModelId(
        java.lang.String value) {
      copyOnWrite();
      instance.setDeviceModelId(value);
      return this;
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device model. The combination of
     * device_model_id and device_id must have been previously associated through
     * device registration.
     * </pre>
     *
     * <code>optional string device_model_id = 3;</code>
     */
    public Builder clearDeviceModelId() {
      copyOnWrite();
      instance.clearDeviceModelId();
      return this;
    }
    /**
     * <pre>
     * *Required* Unique identifier for the device model. The combination of
     * device_model_id and device_id must have been previously associated through
     * device registration.
     * </pre>
     *
     * <code>optional string device_model_id = 3;</code>
     */
    public Builder setDeviceModelIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDeviceModelIdBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:google.assistant.embedded.v1alpha2.DeviceConfig)
  }
  protected final Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      Object arg0, Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.google.assistant.embedded.v1alpha2.DeviceConfig();
      }
      case IS_INITIALIZED: {
        return DEFAULT_INSTANCE;
      }
      case MAKE_IMMUTABLE: {
        return null;
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case VISIT: {
        Visitor visitor = (Visitor) arg0;
        com.google.assistant.embedded.v1alpha2.DeviceConfig other = (com.google.assistant.embedded.v1alpha2.DeviceConfig) arg1;
        deviceId_ = visitor.visitString(!deviceId_.isEmpty(), deviceId_,
            !other.deviceId_.isEmpty(), other.deviceId_);
        deviceModelId_ = visitor.visitString(!deviceModelId_.isEmpty(), deviceModelId_,
            !other.deviceModelId_.isEmpty(), other.deviceModelId_);
        if (visitor == com.google.protobuf.GeneratedMessageLite.MergeFromVisitor
            .INSTANCE) {
        }
        return this;
      }
      case MERGE_FROM_STREAM: {
        com.google.protobuf.CodedInputStream input =
            (com.google.protobuf.CodedInputStream) arg0;
        com.google.protobuf.ExtensionRegistryLite extensionRegistry =
            (com.google.protobuf.ExtensionRegistryLite) arg1;
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              default: {
                if (!input.skipField(tag)) {
                  done = true;
                }
                break;
              }
              case 10: {
                String s = input.readStringRequireUtf8();

                deviceId_ = s;
                break;
              }
              case 26: {
                String s = input.readStringRequireUtf8();

                deviceModelId_ = s;
                break;
              }
            }
          }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw new RuntimeException(e.setUnfinishedMessage(this));
        } catch (java.io.IOException e) {
          throw new RuntimeException(
              new com.google.protobuf.InvalidProtocolBufferException(
                  e.getMessage()).setUnfinishedMessage(this));
        } finally {
        }
      }
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        if (PARSER == null) {    synchronized (com.google.assistant.embedded.v1alpha2.DeviceConfig.class) {
            if (PARSER == null) {
              PARSER = new DefaultInstanceBasedParser(DEFAULT_INSTANCE);
            }
          }
        }
        return PARSER;
      }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:google.assistant.embedded.v1alpha2.DeviceConfig)
  private static final com.google.assistant.embedded.v1alpha2.DeviceConfig DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new DeviceConfig();
    DEFAULT_INSTANCE.makeImmutable();
  }

  public static com.google.assistant.embedded.v1alpha2.DeviceConfig getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<DeviceConfig> PARSER;

  public static com.google.protobuf.Parser<DeviceConfig> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
