package tech.thatgravyboat.repolib.v2.expl.value;

import tech.thatgravyboat.repolib.v2.Constants;

import java.util.List;

public non-sealed interface Array extends Value, KeyValue {

    static KeyValue.Mutable createPrototype(List<Value> entries, boolean mutable, Struct prototype) {
        var prototypes = prototype.iterator();


        return new KeyValue.Mutable.Forwarding() {
            final KeyValue.Mutable base = Constants.mutable(builder -> {
                if (mutable) {
                    builder.function(
                            "add", function -> {
                                function.arity(1);
                                function.executeSimpleVoid(args -> entries.add(args.getFirst()));
                            });

                    builder.function(
                            "set", function -> {
                                function.arity(2);
                                function.executeVoid((evaluator, args) -> {
                                    var index = evaluator.getNumberOrThrow(args.getFirst());
                                    entries.set((int) index, args.get(1));
                                });
                            });

                    builder.function(
                            "clear", function -> {
                                function.arity(0);
                                function.runs(entries::clear);
                            });
                }

                prototypes.forEachRemaining(entry -> builder.field(entry.getKey(), entry.getValue()));
            });

            @Override
            public Mutable delegate() {
                return base;
            }

            @Override
            public Value get(String field) {
                return switch (field) {
                    case "length", "size" -> new Num(entries.size());
                    case null, default -> base.get(field);
                };
            }
        };
    }

    Value get(int index);

    KeyValue.Mutable toMutableArray();

    interface Mutable extends Array, KeyValue.Mutable {

        void set(int index, Value value);

        void add(Value value);

        Array toImmutableArray();

    }

}
