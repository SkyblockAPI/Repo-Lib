package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record StructExpression(Map<String, Expression<?>> fields, @Nullable AccessExpression spread)
    implements Expression<StructExpression> {

    public static final BinaryCodec<StructExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.map(BinaryCodec.STRING, BinaryCodec.EXPRESSION).forGetter(StructExpression::fields),
        AccessExpression.CODEC.nullable().forGetter(StructExpression::spread),
        StructExpression::new);

    @Override
    public @NotNull String toString() {
        return fields.entrySet()
            .stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public ExpressionTypeRegistry.Type<StructExpression> expressionId() {
        return ExpressionTypes.STRUCT;
    }

}
