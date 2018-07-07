// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/assistant/embedded/v1alpha2/embedded_assistant.proto

package com.google.assistant.embedded.v1alpha2;

/**
 * <pre>
 * Debug info for any developer
 * </pre>
 *
 * Protobuf type {@code google.assistant.embedded.v1alpha2.DebugInfo}
 */
public  final class DebugInfo extends
    com.google.protobuf.GeneratedMessageLite<
        DebugInfo, DebugInfo.Builder> implements
    // @@protoc_insertion_point(message_implements:google.assistant.embedded.v1alpha2.DebugInfo)
    DebugInfoOrBuilder {
  private DebugInfo() {
    aogAgentToAssistantJson_ = "";
  }
  public static final int AOG_AGENT_TO_ASSISTANT_JSON_FIELD_NUMBER = 1;
  private java.lang.String aogAgentToAssistantJson_;
  /**
   * <pre>
   * The original JSON response from an Action-on-Google agent to Google server.
   * The requester must have the action_on_google_debug_level set to 1 to
   * request this field. However, it will only be populated if the request maker
   * owns the AoG project and the AoG project is in preview mode.
   * </pre>
   *
   * <code>optional string aog_agent_to_assistant_json = 1;</code>
   */
  public java.lang.String getAogAgentToAssistantJson() {
    return aogAgentToAssistantJson_;
  }
  /**
   * <pre>
   * The original JSON response from an Action-on-Google agent to Google server.
   * The requester must have the action_on_google_debug_level set to 1 to
   * request this field. However, it will only be populated if the request maker
   * owns the AoG project and the AoG project is in preview mode.
   * </pre>
   *
   * <code>optional string aog_agent_to_assistant_json = 1;</code>
   */
  public com.google.protobuf.ByteString
      getAogAgentToAssistantJsonBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(aogAgentToAssistantJson_);
  }
  /**
   * <pre>
   * The original JSON response from an Action-on-Google agent to Google server.
   * The requester must have the action_on_google_debug_level set to 1 to
   * request this field. However, it will only be populated if the request maker
   * owns the AoG project and the AoG project is in preview mode.
   * </pre>
   *
   * <code>optional string aog_agent_to_assistant_json = 1;</code>
   */
  private void setAogAgentToAssistantJson(
      java.lang.String value) {
    if (value == null) {
    throw new NullPointerException();
  }
  
    aogAgentToAssistantJson_ = value;
  }
  /**
   * <pre>
   * The original JSON response from an Action-on-Google agent to Google server.
   * The requester must have the action_on_google_debug_level set to 1 to
   * request this field. However, it will only be populated if the request maker
   * owns the AoG project and the AoG project is in preview mode.
   * </pre>
   *
   * <code>optional string aog_agent_to_assistant_json = 1;</code>
   */
  private void clearAogAgentToAssistantJson() {
    
    aogAgentToAssistantJson_ = getDefaultInstance().getAogAgentToAssistantJson();
  }
  /**
   * <pre>
   * The original JSON response from an Action-on-Google agent to Google server.
   * The requester must have the action_on_google_debug_level set to 1 to
   * request this field. However, it will only be populated if the request maker
   * owns the AoG project and the AoG project is in preview mode.
   * </pre>
   *
   * <code>optional string aog_agent_to_assistant_json = 1;</code>
   */
  private void setAogAgentToAssistantJsonBytes(
      com.google.protobuf.ByteString value) {
    if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
    
    aogAgentToAssistantJson_ = value.toStringUtf8();
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!aogAgentToAssistantJson_.isEmpty()) {
      output.writeString(1, getAogAgentToAssistantJson());
    }
  }

  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;

    size = 0;
    if (!aogAgentToAssistantJson_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeStringSize(1, getAogAgentToAssistantJson());
    }
    memoizedSerializedSize = size;
    return size;
  }

  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.google.assistant.embedded.v1alpha2.DebugInfo parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.google.assistant.embedded.v1alpha2.DebugInfo prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  /**
   * <pre>
   * Debug info for any developer
   * </pre>
   *
   * Protobuf type {@code google.assistant.embedded.v1alpha2.DebugInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.google.assistant.embedded.v1alpha2.DebugInfo, Builder> implements
      // @@protoc_insertion_point(builder_implements:google.assistant.embedded.v1alpha2.DebugInfo)
      com.google.assistant.embedded.v1alpha2.DebugInfoOrBuilder {
    // Construct using com.google.assistant.embedded.v1alpha2.DebugInfo.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * The original JSON response from an Action-on-Google agent to Google server.
     * The requester must have the action_on_google_debug_level set to 1 to
     * request this field. However, it will only be populated if the request maker
     * owns the AoG project and the AoG project is in preview mode.
     * </pre>
     *
     * <code>optional string aog_agent_to_assistant_json = 1;</code>
     */
    public java.lang.String getAogAgentToAssistantJson() {
      return instance.getAogAgentToAssistantJson();
    }
    /**
     * <pre>
     * The original JSON response from an Action-on-Google agent to Google server.
     * The requester must have the action_on_google_debug_level set to 1 to
     * request this field. However, it will only be populated if the request maker
     * owns the AoG project and the AoG project is in preview mode.
     * </pre>
     *
     * <code>optional string aog_agent_to_assistant_json = 1;</code>
     */
    public com.google.protobuf.ByteString
        getAogAgentToAssistantJsonBytes() {
      return instance.getAogAgentToAssistantJsonBytes();
    }
    /**
     * <pre>
     * The original JSON response from an Action-on-Google agent to Google server.
     * The requester must have the action_on_google_debug_level set to 1 to
     * request this field. However, it will only be populated if the request maker
     * owns the AoG project and the AoG project is in preview mode.
     * </pre>
     *
     * <code>optional string aog_agent_to_assistant_json = 1;</code>
     */
    public Builder setAogAgentToAssistantJson(
        java.lang.String value) {
      copyOnWrite();
      instance.setAogAgentToAssistantJson(value);
      return this;
    }
    /**
     * <pre>
     * The original JSON response from an Action-on-Google agent to Google server.
     * The requester must have the action_on_google_debug_level set to 1 to
     * request this field. However, it will only be populated if the request maker
     * owns the AoG project and the AoG project is in preview mode.
     * </pre>
     *
     * <code>optional string aog_agent_to_assistant_json = 1;</code>
     */
    public Builder clearAogAgentToAssistantJson() {
      copyOnWrite();
      instance.clearAogAgentToAssistantJson();
      return this;
    }
    /**
     * <pre>
     * The original JSON response from an Action-on-Google agent to Google server.
     * The requester must have the action_on_google_debug_level set to 1 to
     * request this field. However, it will only be populated if the request maker
     * owns the AoG project and the AoG project is in preview mode.
     * </pre>
     *
     * <code>optional string aog_agent_to_assistant_json = 1;</code>
     */
    public Builder setAogAgentToAssistantJsonBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setAogAgentToAssistantJsonBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:google.assistant.embedded.v1alpha2.DebugInfo)
  }
  protected final Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      Object arg0, Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.google.assistant.embedded.v1alpha2.DebugInfo();
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
        com.google.assistant.embedded.v1alpha2.DebugInfo other = (com.google.assistant.embedded.v1alpha2.DebugInfo) arg1;
        aogAgentToAssistantJson_ = visitor.visitString(!aogAgentToAssistantJson_.isEmpty(), aogAgentToAssistantJson_,
            !other.aogAgentToAssistantJson_.isEmpty(), other.aogAgentToAssistantJson_);
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

                aogAgentToAssistantJson_ = s;
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
        if (PARSER == null) {    synchronized (com.google.assistant.embedded.v1alpha2.DebugInfo.class) {
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


  // @@protoc_insertion_point(class_scope:google.assistant.embedded.v1alpha2.DebugInfo)
  private static final com.google.assistant.embedded.v1alpha2.DebugInfo DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new DebugInfo();
    DEFAULT_INSTANCE.makeImmutable();
  }

  public static com.google.assistant.embedded.v1alpha2.DebugInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<DebugInfo> PARSER;

  public static com.google.protobuf.Parser<DebugInfo> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

