package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.Map;
import java.util.Set;

public class RepoConfig implements KeyValue {

    private boolean romanNumerals = true;

    public static final RepoConfig DEFAULT = new RepoConfig();

    public RepoConfig() {
    }

    @Override
    public Value get(String field) {
        return switch (field) {
            case "roman_numerals" -> BoolValue.wrap(romanNumerals);
            default -> NIL;
        };
    }

    @Override
    public Set<String> keySet() {
        return Set.of("roman_numerals");
    }

    @Override
    public Map<String, KeyValue> sourceMap() {
        return Map.of("roman_numerals", this);
    }

    public RepoConfig withRomanNumerals(boolean romanNumerals) {
        this.romanNumerals = romanNumerals;
        return this;
    }

    @Override
    public Mutable toMutable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(String field) {
        return !(get(field) instanceof NilValue);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
