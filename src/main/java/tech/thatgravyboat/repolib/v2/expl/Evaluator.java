package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.expl.value.Bool;
import tech.thatgravyboat.repolib.v2.expl.value.Function;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.Nil;
import tech.thatgravyboat.repolib.v2.expl.value.Num;
import tech.thatgravyboat.repolib.v2.expl.value.Str;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Evaluator {

    public final KeyValue defaults;
    public final List<ContentInfo> debugs = new ArrayList<>();
    public final LinkedList<String> fileStack = new LinkedList<>();
    public final LinkedList<String> stack = new LinkedList<>();
    public final List<ContentInfo> errors = new ArrayList<>();

    public Value pushPop(String stack, Supplier<Value> supplier) {
        this.stack.addLast(stack);
        var result = supplier.get();
        this.stack.removeLast();
        return result;
    }

    private String stack() {
        var stringBuilder = new StringBuilder();
        for (var s : this.stack) {
            stringBuilder.append(s);
            stringBuilder.append(".");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public Evaluator(KeyValue defaults) {
        this.defaults = defaults;
    }

    public void evaluate(Expression expression) {
        eval(expression);
    }

    public void error(String message) {
        errors.add(new ContentInfo(this.stack(), message));
    }
    public void debug(String message) {
        debugs.add(new ContentInfo(this.stack(), message));
    }

    private Value eval(Expression expression) {
        return switch (expression) {
            case Expression.Access access -> evalAccess(access);
            case Expression.Assign assign -> evalAssign(assign);
            case Expression.Block block -> evalBlock(block);
            case Expression.Call call -> evalCall(call);
            case Expression.If anIf -> evalIf(anIf);
            case Expression.In in -> evalIn(in);
            case Expression.Num num -> new Num(num.value());
            case Expression.Str str -> new Str(str.value());
            case Expression.Bool bool -> new Bool(bool.value());
            case Expression.Struct struct -> evalStruct(struct);
            case Expression.Unary unary -> evalUnary(unary);
            case Expression.SelfEvaluatingExpression self -> self.evaluate(this);
        };
    }

    private Value evalIn(Expression.In in) {
        var holder = evalAccess(in.holder());
        if (holder instanceof KeyValue keyValue) {
            return new Bool(keyValue.contains(in.field()));
        } else if (holder instanceof Str(String value)) {
            return new Bool(value.contains(in.field()));
        }
        error("Can't check if '" + in.field() + "' is in non string or keyvalue type " + holder);
        return Bool.NIL;
    }

    private Value evalUnary(Expression.Unary unary) { // TODO
        return Value.NIL;
    }

    private Value evalStruct(Expression.Struct struct) {
        var fields = new HashMap<String, Value>();
        for (var entry : struct.fields().entrySet()) {
            fields.put(entry.getKey(), eval(entry.getValue()));
        }
        return new MutableStruct(fields);
    }

    private Boolean asBool(Value value) {
        return switch (value) {
            case Nil ignored -> false;
            case Bool bool -> bool.value();
            case Num num -> num.value() == 1.0d;
            case Str str -> !str.value().isEmpty();
            default -> {
                error("Unable to convert " + value + " into boolean.");
                yield null;
            }
        };
    }

    private Value evalIf(Expression.If anIf) {
        var condition = asBool(eval(anIf.cond()));

        if (condition == null) {
            return Value.NIL;
        } else if (condition) {
            return pushPop("if (" + anIf.cond() + ")", () -> eval(anIf.thenExpr()));
        } else if (anIf.elseExpr() != null) {
            return pushPop("if (" + anIf.cond() + ") { ... } else", () -> eval(anIf.elseExpr()));
        }

        return Value.NIL;
    }

    private Value evalCall(Expression.Call call) {
        var left = eval(call.lhs());
        if (left instanceof Function function) {
            var args = new ArrayList<Value>();
            for (var arg : call.args()) {
                args.add(eval(arg));
            }

            return pushPop(call.lhs().toString(), () -> function.apply(this, args));
        }

        error("Unable to call invoke on non function type " + left);
        return Value.NIL;
    }

    private Value evalBlock(Expression.Block block) {
        Value last = Value.NIL;
        for (var expr : block.exprs()) {
            last = eval(expr);
        }
        return last;
    }

    private Value evalAssign(Expression.Assign assign) {
        var access = assign.lhs();
        final Value field;
        if (access.lhs() == null) {
            field = defaults;
        } else {
            field = eval(access.lhs());
        }

        if (field instanceof KeyValue.Mutable keyValue) {
            var value = eval(assign.value());
            keyValue.set(access.field(), value);
            return value;
        } else if (field instanceof KeyValue) {
            error("Unable to set property '" + access.field() + "' on immutable key/value " + access.lhs());
            return Value.NIL;
        }

        error("Unable to set property '" + access.field() + "' on non key/value " + access.lhs());
        return Value.NIL;
    }


    private Value evalAccess(Expression.Access expression) {
        var lhs = expression.lhs();
        if (lhs == null) {
            return defaults.get(expression.field());
        }
        var left = eval(lhs);
        if (left instanceof KeyValue keyValue) {
            keyValue.get(expression.field());
            return Objects.requireNonNullElse(keyValue.get(expression.field()), Value.NIL);
        }

        error("Unable to access property " + expression.field() + " of non key/value " + lhs);
        return Value.NIL;
    }

}
