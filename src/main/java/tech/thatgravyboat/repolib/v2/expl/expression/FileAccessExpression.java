package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record FileAccessExpression(Collection<Expression<?>> path) implements Expression<FileAccessExpression> {

    public static final BinaryCodec<FileAccessExpression> CODEC =
        BinaryCodec.EXPRESSION.collection().mapped(FileAccessExpression::new, FileAccessExpression::path);

    @Override
    public @NotNull String toString() {
        return path.toString();
    }

    @Override
    public ExpressionTypeRegistry.Type<FileAccessExpression> expressionId() {
        return ExpressionTypes.FILE_ACCESS;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var list = new ArrayList<String>();

        for (var expr : path()) {
            list.add(evaluator.eval0(expr).asString());
        }

        var result = evaluator.getFileAccess(String.join("/", list));

        if (result == null) {
            throw new Evaluator.Panic("failed to find " + String.join("/", list));
        }

        return result;
    }


}
