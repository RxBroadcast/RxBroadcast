package rx.broadcast;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import org.jetbrains.annotations.NotNull;

public class CausalOrderProtobufSerializer<T> implements Serializer<VectorTimestamped<T>> {
    private static final String MESSAGE_NAME = "VectorTimestamped";

    private static final String IDS_FIELD_NAME = "ids";

    private static final String TIMESTAMPS_FIELD_NAME = "timestamps";

    private static final String VALUE_FIELD_NAME = "value";

    private static final int IDS_FIELD_NUMBER = 1;

    private static final int TIMESTAMPS_FIELD_NUMBER = 2;

    private static final int VALUE_FIELD_NUMBER = 3;

    private final Descriptor messageDescriptor;

    private final FieldDescriptor ids;

    private final FieldDescriptor timestamps;

    private final FieldDescriptor value;

    private final Parser<DynamicMessage> messageParser;

    private final Serializer<T> objectSerializer;

    public CausalOrderProtobufSerializer(@NotNull final Serializer<T> objectSerializer) {
        this.objectSerializer = objectSerializer;
        try {
            final FileDescriptorProto timestampedMessageFile = FileDescriptorProto.newBuilder()
                .addMessageType(
                    DescriptorProto.newBuilder()
                        .setName(MESSAGE_NAME)
                        .addField(
                            FieldDescriptorProto.newBuilder()
                                .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                                .setName(IDS_FIELD_NAME)
                                .setNumber(IDS_FIELD_NUMBER)
                                .setType(FieldDescriptorProto.Type.TYPE_BYTES))
                        .addField(
                            FieldDescriptorProto.newBuilder()
                                .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                                .setName(TIMESTAMPS_FIELD_NAME)
                                .setNumber(TIMESTAMPS_FIELD_NUMBER)
                                .setOptions(FieldOptions.newBuilder()
                                    .setPacked(true))
                                .setType(FieldDescriptorProto.Type.TYPE_UINT64))
                        .addField(
                            FieldDescriptorProto.newBuilder()
                                .setName(VALUE_FIELD_NAME)
                                .setNumber(VALUE_FIELD_NUMBER)
                                .setType(FieldDescriptorProto.Type.TYPE_BYTES)))
                .build();
            final Descriptor message = FileDescriptor
                .buildFrom(timestampedMessageFile, new FileDescriptor[0])
                .findMessageTypeByName(MESSAGE_NAME);
            this.messageDescriptor = message;
            this.ids = messageDescriptor.findFieldByName(IDS_FIELD_NAME);
            this.timestamps = messageDescriptor.findFieldByName(TIMESTAMPS_FIELD_NAME);
            this.value = messageDescriptor.findFieldByName(VALUE_FIELD_NAME);
            this.messageParser = DynamicMessage.newBuilder(messageDescriptor)
                .buildPartial()
                .getParserForType();
        } catch (final DescriptorValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public final VectorTimestamped<T> decode(@NotNull final byte[] data) {
        try {
            final DynamicMessage message = messageParser.parseFrom(data);
            final byte[] bytes = ((ByteString) message.getField(this.value)).toByteArray();
            final T value = objectSerializer.decode(bytes);
            final int idsCount = message.getRepeatedFieldCount(this.ids);
            final int timestampsCount = message.getRepeatedFieldCount(this.timestamps);
            final Sender[] ids = new Sender[idsCount];
            final long[] timestamps = new long[timestampsCount];

            for (int i = 0; i < idsCount; i++) {
                ids[i] = new Sender(((ByteString) message.getRepeatedField(this.ids, i)).toByteArray());
            }
            for (int i = 0; i < timestampsCount; i++) {
                timestamps[i] = (long) message.getRepeatedField(this.timestamps, i);
            }

            return new VectorTimestamped<>(value, new VectorTimestamp(ids, timestamps));
        } catch (final InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public final byte[] encode(@NotNull final VectorTimestamped<T> data) {
        final DynamicMessage.Builder builder = DynamicMessage.newBuilder(messageDescriptor)
            .setField(this.value, objectSerializer.encode(data.value));

        data.timestamp.stream().forEachOrdered(entry -> {
            builder.addRepeatedField(this.ids, entry.id.byteBuffer);
            builder.addRepeatedField(this.timestamps, entry.timestamp);
        });

        return builder.build().toByteArray();
    }
}
