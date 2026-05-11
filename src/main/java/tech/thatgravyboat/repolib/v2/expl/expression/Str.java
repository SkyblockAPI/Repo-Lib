package tech.thatgravyboat.repolib.v2.expl.expression;

public record Str(String value) implements Expression {

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
