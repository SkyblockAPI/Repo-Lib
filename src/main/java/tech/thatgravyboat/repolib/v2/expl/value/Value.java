package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.expression.NonSerializableExpression;

public sealed interface Value extends Comparable<Value>, NonSerializableExpression
    permits ArrayValue, BoolValue, FunctionValue, KeyValue, NilValue, NumValue, StrValue {

    static String prettyPrint(Value value) {
        return prettyPrint(value, "", 0);
    }

    static String prettyPrint(Value value, String prefix, int depth) {
        return switch (value) {
            case ArrayValue arr when depth < 10 -> ArrayValue.prettyPrint(arr, prefix + " ", depth);
            case ArrayValue ignored -> "[Array]";
            case LambdaFunctionValue lambda -> "[function arityMin=%s arityMax=%s vararg=%s]".formatted(
                lambda.arityMin(),
                lambda.arityMax(),
                lambda.vararg());
            case FunctionValue ignored -> "[function]";
            case StructValue s when depth < 10 -> StructValue.prettyPrint(s, prefix + " ", depth);
            case KeyValue kv -> "[Object " + kv.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "]";
            default -> value.toString();
        };
    }

    String type();

    Value NIL = new NilValue();

    @Override
    default int compareTo(@NotNull Value o) {
        return 0;
    }

    @Override
    default Value evaluate(Evaluator evaluator) {
        return this;
    }

    default String asString() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a string");
    }

    default double asNumber() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a string");
    }

    default double asNumber(double defaultValue) {
        return defaultValue;
    }

    default boolean asBoolean() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a boolean");
    }

    default boolean asBooleanConversion() {
        throw new Evaluator.Panic("Unable to convert " + this + " into boolean.");
    }

    default StructValue asStruct() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a struct");
    }

    default StructValue.MutableStruct asMutableStruct() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a mutable struct");
    }

    default String asStringOrNull() {
        return null;
    }

    default StructuredFunctionValue asStructuredFunction() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a Structured Function");
    }

    default LambdaFunctionValue asLambda() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a function");
    }

    default FunctionValue asFunctionValue() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a function");
    }

    default ArrayValue asArray() {
        throw new Evaluator.Panic("Failed to convert " + this + " into an array");
    }

    default KeyValue asKeyValue() {
        throw new Evaluator.Panic("Failed to convert " + this + " into a key value");
    }

    default Value containsValue(String value) {
        throw new Evaluator.Panic(
            "Can't check if '" + value + "' is in non string or keyvalue type " + this);
    }

}
