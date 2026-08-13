package tech.thatgravyboat.repolib.v2.expl.expression;

import java.nio.file.Path;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record FileAccessExpression(List<Expression> path) implements Expression {

    @Override
    public @NotNull String toString() {
        return path.toString();
    }
}
