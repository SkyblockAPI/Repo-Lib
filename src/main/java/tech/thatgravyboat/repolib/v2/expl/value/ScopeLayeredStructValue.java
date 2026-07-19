package tech.thatgravyboat.repolib.v2.expl.value;

public record ScopeLayeredStructValue(KeyValue base, MutableStructValue overlay) implements KeyValue.Mutable.Forwarding {

    @Override
    public void set(String field, Value value) {
        if (base.contains(field)) {
            if (base instanceof KeyValue.Mutable mutableBase) {
                mutableBase.set(field, value);
            }
        } else {
            overlay.set(field, value);
        }
    }

    @Override
    public boolean contains(String field) {
        return overlay.contains(field) || base.contains(field);
    }

    @Override
    public Value get(String field) {
        if (overlay.contains(field)) {
            return overlay.get(field);
        }
        return base.get(field);
    }

    @Override
    public MutableStructValue delegate() {
        return overlay;
    }
}
