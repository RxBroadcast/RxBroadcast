package rxbroadcast;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import org.jetbrains.annotations.NotNull;

public final class SingleSourceFifoOrderProtobufSerializer<T> implements Serializer<Timestamped<T>> {
    private static final String MESSAGE_NAME = "Timestamped";

    private static final String TIMESTAMP_FIELD_NAME = "timestamp";

    private static final String VALUE_FIELD_NAME = "value";

    private static final int TIMESTAMP_FIELD_NUMBER = 1;

    private static final int VALUE_FIELD_NUMBER = 2;

    private final FieldDescriptor timestampedMessageField;

    private final FieldDescriptor valueMessageField;

    private final DynamicMessage.Builder messageBuilder;

    private final Parser<DynamicMessage> messageParser;

    private final Serializer<T> objectSerializer;

    public SingleSourceFifoOrderProtobufSerializer(@NotNull final Serializer<T> objectSerializer) {
        this.objectSerializer = objectSerializer;
        try {
            final FileDescriptorProto timestampedMessageFile = FileDescriptorProto.newBuilder()
                .addMessageType(
                    DescriptorProto.newBuilder()
                        .setName(MESSAGE_NAME)
                        .addField(
                            FieldDescriptorProto.newBuilder()
                                .setName(TIMESTAMP_FIELD_NAME)
                                .setNumber(TIMESTAMP_FIELD_NUMBER)
                                .setType(FieldDescriptorProto.Type.TYPE_INT64))
                        .addField(
                            FieldDescriptorProto.newBuilder()
                                .setName(VALUE_FIELD_NAME)
                                .setNumber(VALUE_FIELD_NUMBER)
                                .setType(FieldDescriptorProto.Type.TYPE_BYTES)))
                .build();
            final Descriptor message = FileDescriptor
                .buildFrom(timestampedMessageFile, new FileDescriptor[0])
                .findMessageTypeByName(MESSAGE_NAME);
            this.timestampedMessageField = message.findFieldByName(TIMESTAMP_FIELD_NAME);
            this.valueMessageField = message.findFieldByName(VALUE_FIELD_NAME);
            this.messageBuilder = DynamicMessage.newBuilder(message);
            this.messageParser = messageBuilder.buildPartial().getParserForType();
        } catch (final DescriptorValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public final Timestamped<T> decode(@NotNull final byte[] data) {
        try {
            final DynamicMessage message = messageParser.parseFrom(data);
            final long timestamp = (long) message.getField(timestampedMessageField);
            final byte[] bytes = ((ByteString) message.getField(valueMessageField)).toByteArray();
            final T value = objectSerializer.decode(bytes);
            return new Timestamped<>(timestamp, value);
        } catch (final InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public final byte[] encode(@NotNull final Timestamped<T> data) {
        return messageBuilder
            .setField(timestampedMessageField, data.timestamp)
            .setField(valueMessageField, objectSerializer.encode(data.value))
            .build()
            .toByteArray();
    }
}
