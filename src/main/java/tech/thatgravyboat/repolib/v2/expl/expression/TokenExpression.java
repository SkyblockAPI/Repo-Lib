package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Lexer;

import java.util.Locale;

public record TokenExpression(Lexer.Token token) implements Expression {
    @Override
    public @NotNull String toString() {
        return token.name().toLowerCase(Locale.ROOT);
    }
}
