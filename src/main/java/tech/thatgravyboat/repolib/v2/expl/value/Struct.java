package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public interface Struct extends KeyValue, Iterable<Map.Entry<String, Value>> {

    public interface Forwarding extends KeyValue.Forwarding, Struct {
        @Override
        Struct delegate();

        @Override
        default @NotNull Iterator<Map.Entry<String, Value>> iterator() {
            return delegate().iterator();
        }
    }

}
