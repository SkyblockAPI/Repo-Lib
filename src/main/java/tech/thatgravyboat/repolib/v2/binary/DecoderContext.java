package tech.thatgravyboat.repolib.v2.binary;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2IntArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class DecoderContext implements ByteBuffer.Forwarding {

    Int2ObjectMap<String> nameTable;
    private final ByteBuffer buffer;

    public DecoderContext(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public String readLiteral() throws IOException {
        return this.nameTable.get(buffer.readInt());
    }

    public void updateTable(Int2ObjectMap<String> newTable) {
        this.nameTable = newTable;
    }

    public <Data> Collection<Data> readCollection(Decoder<Data> decoder) throws IOException {
        var size = readInt();
        if (size == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Data>(size);
        for (var i = 0; i < size; i++) {
            list.add(decoder.decode(this));
        }
        return list;
    }

    @Override
    public ByteBuffer delegate() {
        return this.buffer;
    }
}
