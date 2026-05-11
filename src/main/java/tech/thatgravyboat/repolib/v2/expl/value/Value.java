package tech.thatgravyboat.repolib.v2.expl.value;

public sealed interface Value permits Array, Bool, Function, KeyValue, Nil, Num, Str {

    Value NIL = new Nil();

}
