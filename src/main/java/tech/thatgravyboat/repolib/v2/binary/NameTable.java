package tech.thatgravyboat.repolib.v2.binary;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class NameTable {
    private final Collection<String> names;

    private NameTable(Collection<String> data) {
        this.names = data;
    }

    public static NameTable builder() {
        return new NameTable(new HashSet<>());
    }

    public static NameTable decode(ByteBuffer buffer) throws IOException {
        var builder = new ArrayList<String>();
        var amount = buffer.readInt();
        for (int i = 0; i < amount; i++) {
            builder.add(buffer.readString());
        }
        return new NameTable(builder);
    }

    public void insert(String name) {
        this.names.add(name);
    }

    public List<String> freezeForEncode() {
        return List.copyOf(this.names);
    }

    public Int2ObjectMap<String> freezeForDecode() {
        var names = List.copyOf(this.names);
        var map = new Int2ObjectArrayMap<String>();
        for (var i = 0; i < names.size(); i++) {
            map.put(i, names.get(i));
        }

        return map;
    }

    public void encode(ByteBuffer buffer) {
        buffer.writeInt(this.names.size());
        for (var name : this.names) {
            buffer.writeString(name);
        }
    }

    public void insert(@Nullable Encodable expression) {
        if (expression == null) {
            return;
        }
        expression.precode(this);
    }
}
