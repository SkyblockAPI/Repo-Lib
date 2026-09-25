package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record ForEachExpression(AccessExpression field, Expression array, Expression body)
    implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var values = evaluator.getArrayOrThrow(evaluator.eval0(array));

        try {
            values.forEach(value -> {
                try {
                    evaluator.pushPop(
                        this.toString(), () -> {
                            evaluator.eval0(new AssignExpression(field, value));
                            return evaluator.eval0(body);
                        });
                } catch (ExecutionExceptions.Continue ignored) {
                }
            });
        } catch (ExecutionExceptions.Break ignored) {
        }
        return NilValue.NIL;
    }

    @Override
    public @NotNull String toString() {
        return "for (%s : %s) %s".formatted(field, array, body);
    }

    @Override
    public boolean requiresSemicolon() {
        return false;
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FOR_EACH;
    }

    @Override
    public void precode(NameTable table) {
        this.field.precode(table);
        this.array.precode(table);
        this.body.precode(table);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeUntyped(this.field, buffer);
        ExpressionCodec.write(this.array, buffer);
        ExpressionCodec.write(this.body, buffer);
    }

    public static ForEachExpression decode(DecoderContext buffer) throws IOException {
        return new ForEachExpression(
            ExpressionCodec.readUntyped(ExpressionTypes.ACCESS, buffer),
            ExpressionCodec.read(buffer),
            ExpressionCodec.read(buffer));
    }
}
