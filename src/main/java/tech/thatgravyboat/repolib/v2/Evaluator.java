package tech.thatgravyboat.repolib.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Evaluator {

    private final Value.KeyValue defaults;
    private final List<ContentError> errors = new ArrayList<>();

    public Evaluator(Value.KeyValue defaults) {
        this.defaults = defaults;
    }

    public void evaluate(Expression expression) {
        eval(expression);
    }

    private Value eval(Expression expression) {
        return switch (expression) {
            case Expression.Access access -> evalAccess(access);
            case Expression.Assign assign -> evalAssign(assign);
            case Expression.Block block -> evalBlock(block);
            case Expression.Call call -> evalCall(call);
            case Expression.If anIf -> evalIf(anIf);
            case Expression.Num num -> new Value.Num(num.value());
            case Expression.Str str -> new Value.Str(str.value());
            case Expression.Bool bool -> new Value.Bool(bool.value());
            case Expression.Struct struct -> evalStruct(struct);
            case Expression.Unary unary -> evalUnary(unary);
        };
    }

    private Value evalUnary(Expression.Unary unary) { // TODO
        return Value.NIL;
    }

    private Value evalStruct(Expression.Struct struct) {
        var fields = new HashMap<String, Value>();
        for (var entry : struct.fields().entrySet()) {
            fields.put(entry.getKey(), eval(entry.getValue()));
        }
        return new Value.Struct(fields);
    }

    private Boolean asBool(Value value) {
        return switch (value) {
            case Value.Nil ignored -> false;
            case Value.Bool bool -> bool.value();
            case Value.Num num -> num.value() == 1.0d;
            case Value.Str str -> !str.value().isEmpty();
            default -> {
                errors.add(new ContentError("Unable to convert " + value + " into boolean."));
                yield null;
            }
        };
    }

    private Value evalIf(Expression.If anIf) {
        var condition = asBool(eval(anIf.cond()));

        if (condition == null) {
            return Value.NIL;
        } else if (condition) {
            return eval(anIf.thenExpr());
        } else if (anIf.elseExpr() != null) {
            return eval(anIf.elseExpr());
        }

        return Value.NIL;
    }

    private Value evalCall(Expression.Call call) {
        var left = eval(call.lhs());
        if (left instanceof Value.Function function) {
            var args = new ArrayList<Value>();
            for (var arg : call.args()) {
                args.add(eval(arg));
            }
            return function.apply(args);
        }

        errors.add(new ContentError("Unable to call invoke on non function type " + left));
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

        if (field instanceof Value.KeyValue.Mutable keyValue) {
            var value = eval(assign.value());
            keyValue.set(access.field(), value);
            return value;
        } else if (field instanceof Value.KeyValue) {
            errors.add(new ContentError("Unable to set property '" + access.field() + "' on immutable key/value " + access.lhs()));
            return Value.NIL;
        }

        errors.add(new ContentError("Unable to set property '" + access.field() + "' on non key/value " + access.lhs()));
        return Value.NIL;
    }


    private Value evalAccess(Expression.Access expression) {
        var lhs = expression.lhs();
        if (lhs == null) {
            return defaults.get(expression.field());
        }
        var left = eval(lhs);
        if (left instanceof Value.KeyValue keyValue) {
            keyValue.get(expression.field());
            return Objects.requireNonNullElse(keyValue.get(expression.field()), Value.NIL);
        }

        errors.add(new ContentError("Unable to access property " + expression.field() + " of non key/value " + lhs));
        return Value.NIL;
    }

}
