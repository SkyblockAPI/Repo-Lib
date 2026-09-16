package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public record StructExpression(Map<String, Expression> fields, AccessExpression spread) implements Expression {

    @Override
    public String toString() {
        return fields.entrySet()
                .stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.STRUCT;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeCollection(this.fields.entrySet(), (entry, _) -> {
            buffer.writeString(entry.getKey());
            ExpressionCodec.write(entry.getValue(), buffer);
        });
        ExpressionCodec.writeUntypedNullable(this.spread, buffer);
    }

    @SuppressWarnings("unchecked")
    public static StructExpression decode(ByteBuffer buffer) throws IOException {
        return new StructExpression(
                Map.ofEntries(buffer.readCollection(_ -> Map.entry(
                        buffer.readString(),
                        ExpressionCodec.read(buffer))).toArray(Map.Entry[]::new)),
                ExpressionCodec.readUntypedNullable(ExpressionTypes.ACCESS, buffer));
    }
}
