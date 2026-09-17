package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

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
    public void precode(NameTable table) {
        fields.forEach((key, expression) -> {
            table.insert(key);
            table.insert(expression);
        });
        table.insert(this.spread);
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeCollection(this.fields.entrySet(), (entry, _) -> {
            buffer.writeLiteral(entry.getKey());
            ExpressionCodec.write(entry.getValue(), buffer);
        });
        ExpressionCodec.writeUntypedNullable(this.spread, buffer);
    }

    @SuppressWarnings("unchecked")
    public static StructExpression decode(DecoderContext buffer) throws IOException {
        return new StructExpression(
                Map.ofEntries(buffer.readCollection(_ -> Map.entry(
                        buffer.readLiteral(),
                        ExpressionCodec.read(buffer))).toArray(Map.Entry[]::new)),
                ExpressionCodec.readUntypedNullable(ExpressionTypes.ACCESS, buffer));
    }
}
