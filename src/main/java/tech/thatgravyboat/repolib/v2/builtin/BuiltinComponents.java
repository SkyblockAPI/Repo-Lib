package tech.thatgravyboat.repolib.v2.builtin;

public class BuiltinComponents {

    public static final Constants COMPONENTS = new Constants(builder -> {
        builder.function("join", function -> {
            function.vararg(true);

        });
    });

}
