package tech.thatgravyboat.repolib.v2.expl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import tech.thatgravyboat.repolib.v2.expl.expression.AccessExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.AssignExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.BlockExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.BoolExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.CallExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.DebugExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.FileAccessExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.ForExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.IfExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.InExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.LambdaExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.LambdaIdentityFunction;
import tech.thatgravyboat.repolib.v2.expl.expression.NumExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StatementExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StrExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StructExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.UnaryExpression;
import tech.thatgravyboat.repolib.v2.expl.value.ArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.LambdaFunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.LayeredStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.ScopeLayeredStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructuredFunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class Evaluator {

    public static final int MAX_ITERATIONS = Short.MAX_VALUE;

    public final KeyValue defaults;
    private final Scope scope;
    private final Function<String, FunctionValue> fileFunction;
    public final List<ContentInfo> debugs = new ArrayList<>();
    public final LinkedList<String> stack = new LinkedList<>();
    public final List<ContentInfo> errors = new ArrayList<>();

    public void push(String stack) {
        this.scope.push();
        this.stack.addLast(stack);
    }

    public void push(StructValue.MutableStruct scope, String stack) {
        this.scope.pushWithScope(scope);
        this.stack.addLast(stack);
    }

    public void pop() {
        this.scope.pop();
        this.stack.removeLast();
    }

    public KeyValue scope() {
        return this.scope.get();
    }

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

    public void setInOverlay(String field, Value value) {
        var scope = this.scope.get();
        if (scope instanceof ScopeLayeredStructValue(StructValue ignored, StructValue.MutableStruct overlay)) {
            overlay.set(field, value);
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

    public Evaluator(StructValue defaults, Function<String, FunctionValue> fileFunction) {
        this.fileFunction = fileFunction;
        this.defaults = defaults;
        scope = new Scope(defaults instanceof LayeredStructValue ? defaults :
            new LayeredStructValue(new MutableStructValue(), defaults));
    }

    public static final Evaluator CONSTANT = new Evaluator(ImmutableStructValue.EMPTY, _ -> null);

    public Value evaluate(Expression<?> expression) {
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

    public void setField(String field, Value value) {
        KeyValue currentScope = scope.get();
        if (currentScope instanceof KeyValue.Mutable mutable) {
            mutable.set(field, value);
        }
    }

    public Value eval0(Expression<?> expression) {
        try {
            if (expression != null) {
                return expression.evaluate(this);
            }
        } catch (Panic e) {
            error(e.getMessage());
        }
        return Value.NIL;
    }

    public void set(String fieldName, Value value) {
        set(scope.get(), fieldName, value);
    }

    public Value set(Value value, String fieldName, Value val) {
        if (value instanceof KeyValue.Mutable keyValue) {
            keyValue.set(fieldName, val);
            return val;
        } else if (value instanceof KeyValue) {
            throw new Panic("Unable to set property '" + fieldName + "' on immutable key/value " + value);
        }

        throw new Panic("Unable to set property '" + fieldName + "' on non key/value " + value);
    }

    public FunctionValue getFileAccess(String name) {
        var file = fileFunction.apply(name);
        if (file == null) {
            throw new Panic("requested include " + name + " not found!");
        }
        return file;
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
            if (scopes.size() == 1) {
                throw new IllegalStateException("Cannot pop base scope");
            }
            scopes.removeLast();
        }
    }
}
