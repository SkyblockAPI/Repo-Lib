package tech.thatgravyboat.repolib.v2.expl.expression;

public record StrExpression(String value) implements Expression {

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
