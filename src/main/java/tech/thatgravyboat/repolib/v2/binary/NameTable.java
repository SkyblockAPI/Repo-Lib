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

public final record NameTable(Collection<String> names) {
    public static final BinaryCodec<NameTable> CODEC = BinaryCodec.LITERAL_STRING.collection().mapped(NameTable::new, NameTable::names);
    public static NameTable builder() {
        return new NameTable(new HashSet<>());
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
}
