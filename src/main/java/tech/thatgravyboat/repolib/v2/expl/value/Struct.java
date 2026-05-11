package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public interface Struct extends KeyValue, Iterable<Map.Entry<String, Value>> {

    static String prettyPrint(Struct result) {
        return prettyPrint(result, "");
    }
    static String prettyPrint(Struct result, String prefix) {
        var builder = new StringBuilder();
        builder.append("{");

        boolean isEmpty = true;
        for (var entry : result) {
            isEmpty = false;
            builder.append("\n").append(prefix)
                    .append(" ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(Value.prettyPrint(entry.getValue(), prefix))
                    .append(",");
        }

        if (isEmpty) {
            builder.append("}");
        } else {
            builder.append("\n").append(prefix).append("}");
        }

        return builder.toString();
    }

    public interface Forwarding extends KeyValue.Forwarding, Struct {
        @Override
        Struct delegate();

        @Override
        default @NotNull Iterator<Map.Entry<String, Value>> iterator() {
            return delegate().iterator();
        }
    }

}
