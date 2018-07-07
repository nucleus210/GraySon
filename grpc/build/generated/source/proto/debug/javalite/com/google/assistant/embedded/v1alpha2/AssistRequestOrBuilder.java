// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/assistant/embedded/v1alpha2/embedded_assistant.proto

package com.google.assistant.embedded.v1alpha2;

public interface AssistRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.assistant.embedded.v1alpha2.AssistRequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   * The `config` message provides information to the recognizer that
   * specifies how to process the request.
   * The first `AssistRequest` message must contain a `config` message.
   * </pre>
   *
   * <code>optional .google.assistant.embedded.v1alpha2.AssistConfig config = 1;</code>
   */
  com.google.assistant.embedded.v1alpha2.AssistConfig getConfig();

  /**
   * <pre>
   * The audio data to be recognized. Sequential chunks of audio data are sent
   * in sequential `AssistRequest` messages. The first `AssistRequest`
   * message must not contain `audio_in` data and all subsequent
   * `AssistRequest` messages must contain `audio_in` data. The audio bytes
   * must be encoded as specified in `AudioInConfig`.
   * Audio must be sent at approximately real-time (16000 samples per second).
   * An error will be returned if audio is sent significantly faster or
   * slower.
   * If [AssistConfig.audio_in_config][] was not set in the first message,
   * then this field is not used. If it was set, this field is populated with
   * the audio data to be recognized. Sequential chunks of audio data are sent
   * in sequential `AssistRequest` messages. If 'AudioInConfig' is provided
   * in the first `AssistRequest` message, all subsequent `AssistRequest`
   * messages must contain `audio_in` data. The audio bytes must be encoded as
   * specified in `AudioInConfig`. Audio must be sent at approximately
   * real-time (16000 samples per second). An error will be returned if audio
   * is sent significantly faster or slower.
   * --)
   * </pre>
   *
   * <code>optional bytes audio_in = 2;</code>
   */
  com.google.protobuf.ByteString getAudioIn();

  public com.google.assistant.embedded.v1alpha2.AssistRequest.TypeCase getTypeCase();
}
