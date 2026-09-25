package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.ArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record AccessExpression(@Nullable Expression<?> lhs, Expression<?> field)
    implements Expression<AccessExpression> {

    public static BinaryCodec<AccessExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.NULLABLE_EXPRESSION.forGetter(AccessExpression::lhs),
        BinaryCodec.EXPRESSION.forGetter(AccessExpression::field),
        AccessExpression::new);

    @Override
    public @NotNull String toString() {
        return lhs + "." + field;
    }

    @Override
    public ExpressionTypeRegistry.Type<AccessExpression> expressionId() {
        return ExpressionTypes.ACCESS;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var lhs = lhs();
        var field = evaluator.eval0(field());

        if (lhs == null) {
            return evaluator.scope().get(field.asString());
        }
        var left = evaluator.eval0(lhs);
        if (left instanceof ArrayValue arrayValue && field instanceof NumValue(double value)) {
            return arrayValue.get((int) value);
        }
        if (left instanceof KeyValue keyValue) {
            return Objects.requireNonNullElse(keyValue.get(field.asString()), Value.NIL);
        }

        throw new Evaluator.Panic("Unable to access property " + field() + " of non key/value " + lhs);
    }
}
