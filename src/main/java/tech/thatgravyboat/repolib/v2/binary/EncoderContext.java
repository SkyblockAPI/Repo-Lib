package tech.thatgravyboat.repolib.v2.binary;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class EncoderContext implements ByteBuffer.Forwarding {

    List<String> nameTable;
    private final ByteBuffer buffer;

    public EncoderContext(List<String> nameTable, ByteBuffer buffer) {
        this.nameTable = nameTable;
        this.buffer = buffer;
    }

    public void writeLiteral(String string) {
        var index = nameTable.indexOf(string);
        if (index == -1) {
            throw new UnsupportedOperationException("Unknown literal " + string);
        }
        buffer.writeInt(index);
    }

    @Override
    public ByteBuffer delegate() {
        return this.buffer;
    }

    public <Data> void writeCollection(Collection<Data> data, BiConsumer<Data, EncoderContext> serializer) {
        writeInt(data.size());
        for (var datum : data) {
            serializer.accept(datum, this);
        }
    }
}
