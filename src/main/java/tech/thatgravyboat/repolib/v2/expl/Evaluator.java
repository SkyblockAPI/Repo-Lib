package tech.thatgravyboat.repolib.v2.expl;

import org.jetbrains.annotations.Contract;
import tech.thatgravyboat.repolib.v2.expl.expression.*;
import tech.thatgravyboat.repolib.v2.expl.value.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Evaluator {

    private static final int MAX_ITERATIONS = Short.MAX_VALUE;

    public final KeyValue defaults;
    private final Scope scope;
    public final List<ContentInfo> debugs = new ArrayList<>();
    public final LinkedList<String> fileStack = new LinkedList<>();
    public final LinkedList<String> stack = new LinkedList<>();
    public final List<ContentInfo> errors = new ArrayList<>();

    public Value pushPop(String stack, StructValue.MutableStruct scope, Supplier<Value> supplier) {
        try {
            this.scope.pushWithScope(scope);
            this.stack.addLast(stack);
            return supplier.get();
        } finally {
            this.scope.pop();
            this.stack.removeLast();
        }
    }

    public Value pushPop(String stack, Supplier<Value> supplier) {
        try {
            this.scope.push();
            this.stack.addLast(stack);
            return supplier.get();
        } finally {
            this.scope.pop();
            this.stack.removeLast();
        }
    }

    private String stack() {
        var stringBuilder = new StringBuilder();
        for (var s : this.stack) {
            stringBuilder.append('[');
            stringBuilder.append(s);
            stringBuilder.append(']');
        }

        return stringBuilder.toString();
    }

    public Evaluator(StructValue defaults) {
        this.defaults = defaults;
        scope = new Scope(defaults);
    }

    public static final Evaluator CONSTANT = new Evaluator(ImmutableStructValue.EMPTY);

    public Value evaluate(Expression expression) {
        try {
            if (expression != null && expression.canReturnValueBeReturned()) {
                return eval0(expression);
            } else {
                eval0(expression);
            }
        } catch (ExecutionExceptions.Return ret) {
            return ret.retVal;
        } catch (ExecutionExceptions.Break e) {
            error("Break statement not within loop.");
        } catch (ExecutionExceptions.Continue e) {
            error("Continue statement not within loop.");
        }
        return Value.NIL;
    }

    @Contract("_->fail")
    public <T> T panic(String message) {
        throw new Panic(message);
    }

    public void error(String message) {
        errors.add(new ContentInfo(this.stack(), message));
    }

    public void debug(String message) {
        debugs.add(new ContentInfo(this.stack(), message));
    }

    public Value getField(Value holder, String field) {
        if (holder instanceof KeyValue kv) {
            return kv.get(field);
        }
        throw new Panic("Unable to access property " + field + " of non key/value " + holder);
    }

    public Value getField(String field) {
        return scope.get().get(field);
    }

    public String getStringOrNull(Value value) {
        if (value instanceof StrValue(String literal)) {
            return literal;
        }
        return null;
    }

    public String getStringOrThrow(Value value) {
        if (value instanceof StrValue(String literal)) {
            return literal;
        }
        throw new Panic("Failed to convert " + value + " into a string");
    }

    public LambdaFunctionValue getLambdaOrThrow(Value value) {
        if (value instanceof LambdaFunctionValue function) {
            return function;
        }
        throw new Panic("Failed to convert " + value + " into a function");
    }

    public double getNumber(Value value, double defaultValue) {
        if (value instanceof NumValue(double literal)) {
            return literal;
        }
        return defaultValue;
    }

    public double getNumberOrThrow(Value value) {
        if (value instanceof NumValue(double literal)) {
            return literal;
        }
        throw new Panic("Failed to convert " + value + " into a number");
    }

    public boolean getBooleanOrThrow(Value value) {
        if (value instanceof BoolValue bool) {
            return bool.value();
        }
        throw new Panic("Failed to convert " + value + " into a boolean");
    }

    public boolean asBooleanConversion(Value value) {
        return asBool(value);
    }

    public ArrayValue getArrayOrThrow(Value value) {
        if (value instanceof ArrayValue array) {
            return array;
        }
        throw new Panic("Failed to convert " + value + " into an array");
    }

    public KeyValue getKeyValueOrThrow(Value value) {
        if (value instanceof KeyValue kv) {
            return kv;
        }
        throw new Panic("Failed to convert " + value + " into a key value");
    }

    public StructValue.MutableStruct getMutableStructOrThrow(Value value) {
        if (value instanceof StructValue.MutableStruct msv) {
            return msv;
        }
        throw new Panic("Failed to convert " + value + " into a mutable struct");
    }

    public StructValue getStructOrThrow(Value value) {
        if (value instanceof StructValue msv) {
            return msv;
        }
        throw new Panic("Failed to convert " + value + " into a struct");
    }

    public Value eval0(Expression expression) {
        try {
            return switch (expression) {
                case AccessExpression access -> evalAccess(access);
                case AssignExpression assign -> evalAssign(assign);
                case BlockExpression block -> evalBlock(block);
                case CallExpression call -> evalCall(call);
                case IfExpression anIf -> evalIf(anIf);
                case InExpression in -> evalIn(in);
                case ForExpression aFor -> evalFor(aFor);
                case NumExpression num -> new NumValue(num.value());
                case StrExpression str -> new StrValue(str.value());
                case BoolExpression bool -> BoolValue.wrap(bool.value());
                case StructExpression struct -> evalStruct(struct);
                case UnaryExpression unary -> evalUnary(unary);
                case StatementExpression token -> {
                    switch (token.op()) {
                        case RETURN -> throw ExecutionExceptions.RETURN;
                        case BREAK -> throw ExecutionExceptions.BREAK;
                        case CONTINUE -> throw ExecutionExceptions.CONTINUE;
                    }
                    throw new Panic("Unexpected statement expression " + token);
                }
                case DebugExpression ignored -> pauseForDebug();
                case SelfEvaluatingExpression self -> self.evaluate(this);
                case null -> Value.NIL;
            };
        } catch (Panic e) {
            error(e.getMessage());
        }
        return Value.NIL;
    }

    private Value pauseForDebug() {
        return Value.NIL;
    }

    private Value evalIn(InExpression in) {
        var holder = evalAccess(in.holder());
        if (holder instanceof KeyValue keyValue) {
            return BoolValue.wrap(keyValue.contains(in.field()));
        } else if (holder instanceof StrValue(String value)) {
            return BoolValue.wrap(value.contains(in.field()));
        }
        throw new Panic("Can't check if '" + in.field() + "' is in non string or keyvalue type " + holder);
    }

    private Value evalUnary(UnaryExpression unary) {
        return switch (unary.op()) {
            case NOT -> BoolValue.wrap(!asBool(eval0(unary.rhs())));
            case NEGATE -> new NumValue(-getNumberOrThrow(eval0(unary.rhs())));
        };
    }

    private Value evalStructValue(MutableStructValue self, Expression expression) {
        if (expression instanceof IdentityExpression identity) {
            return identity.valueFunction().apply(self);
        }
        return this.eval0(expression);
    }

    private Value evalStruct(StructExpression struct) {
        var fields = new MutableStructValue(new HashMap<>());

        if (struct.spread() != null) {
            getStructOrThrow(this.eval0(struct.spread())).forEach(entry -> fields.set(entry.getKey(), entry.getValue()));
        }

        for (var entry : struct.fields().entrySet()) {
            fields.set(entry.getKey(), evalStructValue(fields, entry.getValue()));
        }
        return fields;
    }

    private boolean asBool(Value value) {
        return switch (value) {
            case NilValue ignored -> false;
            case BoolValue bool -> bool.value();
            case StrValue str -> !str.value().isEmpty();
            case NumValue num -> ((int) num.value()) != 0;
            case KeyValue kv -> !kv.isEmpty();
            default -> throw new Panic("Unable to convert " + value + " into boolean.");
        };
    }

    private Value evalIf(IfExpression anIf) {
        var condition = asBool(eval0(anIf.cond()));

        if (condition) {
            return pushPop("if (" + anIf.cond() + ")", () -> eval0(anIf.thenExpr()));
        } else if (anIf.elseExpr() != null) {
            return pushPop("if (" + anIf.cond() + ") { ... } else", () -> eval0(anIf.elseExpr()));
        }

        return Value.NIL;
    }

    private Value evalFor(ForExpression aFor) {
        var init = aFor.init();
        if (init != null) {
            eval0(init);
        }

        int iteration = 0;

        while (true) {
            if (iteration > MAX_ITERATIONS) {
                throw new Panic("For loop has iterated more than %d times, aborting to prevent infinite loop.".formatted(MAX_ITERATIONS));
            }

            var cond = aFor.cond();
            if (cond != null && !asBool(eval0(cond))) {
                break;
            }

            try {
                pushPop("for (...; " + aFor.cond() + "; ...)", () -> eval0(aFor.body()));
            } catch (ExecutionExceptions.Break e) {
                break;
            } catch (ExecutionExceptions.Continue e) {
                // do nothing, just continue to the next iteration.
            }

            var incr = aFor.incr();
            if (incr != null) {
                eval0(incr);
            }

            iteration++;
        }

        return Value.NIL;
    }

    private Value evalCall(CallExpression call) {
        var left = eval0(call.lhs());
        if (left instanceof FunctionValue function) {
            var args = new ArrayList<Value>();
            for (var arg : call.args()) {
                args.add(eval0(arg));
            }

            return pushPop(call.lhs().toString(), () -> function.apply(this, args));
        }

        throw new Panic("Unable to call invoke on non function type " + call.lhs());
    }

    private Value evalBlock(BlockExpression block) {
        Value last = Value.NIL;
        for (var expr : block.exprs()) {
            if (expr.canReturnValueBeReturned()) {
                last = eval0(expr);
            } else {
                eval0(expr);
            }
        }
        return last;
    }

    public void set(String fieldName, Expression value) {
        set(scope.get(), fieldName, value);
    }

    Value set(Value value, String fieldName, Expression valueSupplier) {
        if (value instanceof KeyValue.Mutable keyValue) {
            var val = eval0(valueSupplier);
            keyValue.set(fieldName, val);
            return val;
        } else if (value instanceof KeyValue) {
            throw new Panic("Unable to set property '" + fieldName + "' on immutable key/value " + value);
        }

        throw new Panic("Unable to set property '" + fieldName + "' on non key/value " + value);
    }

    private Value evalAssign(AssignExpression assign) {
        var access = assign.lhs();
        final Value field;
        if (access.lhs() == null) {
            field = scope.get();
        } else {
            field = eval0(access.lhs());
        }

        return set(field, getStringOrThrow(eval0(access.field())), assign.value());
    }


    private Value evalAccess(AccessExpression expression) {
        var lhs = expression.lhs();
        var field = eval0(expression.field());

        if (lhs == null) {
            return scope.get().get(getStringOrThrow(field));
        }
        var left = eval0(lhs);
        if (left instanceof ArrayValue arrayValue && field instanceof NumValue(double value)) {
            return arrayValue.get((int) value);
        }
        if (left instanceof KeyValue keyValue) {
            keyValue.get(getStringOrThrow(field));
            return Objects.requireNonNullElse(keyValue.get(getStringOrThrow(field)), Value.NIL);
        }

        throw new Panic("Unable to access property " + expression.field() + " of non key/value " + lhs);
    }

    public static class Panic extends RuntimeException {

        public Panic(String message) {
            super(message);
        }
    }

    private static class Scope {
        StructValue defaults;
        LinkedList<StructValue> scopes = new LinkedList<>();
        public Scope(StructValue defaults) {
            this.defaults = defaults;
            scopes.add(defaults);
        }
        public KeyValue get() {
            return scopes.getLast();
        }
        public void push() {
            scopes.add(new ScopeLayeredStructValue(scopes.getLast(), new MutableStructValue()));
        }
        public void pushWithScope(StructValue.MutableStruct newScope) {
            scopes.add(new ScopeLayeredStructValue(defaults, newScope));
        }
        public void pop() {
            if (scopes.size() == 1) throw new IllegalStateException("Cannot pop base scope");
            scopes.removeLast();
        }
    }
}
