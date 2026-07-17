package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public interface StructValue extends KeyValue, Iterable<Map.Entry<String, Value>> {

    static String prettyPrint(StructValue result) {
        return prettyPrint(result, "");
    }
    static String prettyPrint(StructValue result, String prefix) {
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

    interface MutableStruct extends StructValue, KeyValue.Mutable {}

    interface Forwarding extends KeyValue.Forwarding, StructValue {
        @Override
        StructValue delegate();

        @Override
        default @NotNull Iterator<Map.Entry<String, Value>> iterator() {
            return delegate().iterator();
        }
    }

    @Override
    default String type() {
        return "struct";
    }
}
