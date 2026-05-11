package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Map;
import java.util.stream.Collectors;

public record Struct(Map<String, Expression> fields) implements Expression {

    @Override
    public String toString() {
        return fields.entrySet()
                .stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
