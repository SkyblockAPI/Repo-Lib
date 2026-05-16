package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.expl.expression.*;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Evaluator {

    private static final int MAX_ITERATIONS = Short.MAX_VALUE;

    public final KeyValue defaults;
    public final List<ContentInfo> debugs = new ArrayList<>();
    public final LinkedList<String> fileStack = new LinkedList<>();
    public final LinkedList<String> stack = new LinkedList<>();
    public final List<ContentInfo> errors = new ArrayList<>();

    public Value pushPop(String stack, Supplier<Value> supplier) {
        try {
            this.stack.addLast(stack);
            return supplier.get();
        } finally {
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

    public Evaluator(KeyValue defaults) {
        this.defaults = defaults;
    }

    public void evaluate(Expression expression) {
        try {
            eval0(expression);
        } catch (ExecutionExceptions.Return ignored) {
            // execution escaped. Do nothing.
        } catch (ExecutionExceptions.Break e) {
            error("Break statement not within loop.");
        } catch (ExecutionExceptions.Continue e) {
            error("Continue statement not within loop.");
        }
    }

    public void panic(String message) {
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
        return defaults.get(field);
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
        if (value instanceof BoolValue(boolean literal)) {
            return literal;
        }
        throw new Panic("Failed to convert " + value + " into a boolean");
    }

    public boolean asBooleanConversion(Value value) {
        return asBool(value);
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
                case BoolExpression bool -> new BoolValue(bool.value());
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
                case SelfEvaluatingExpression self -> self.evaluate(this);
                case null -> Value.NIL;
            };
        } catch (Panic e) {
            error(e.getMessage());
        }
        return Value.NIL;
    }

    private Value evalIn(InExpression in) {
        var holder = evalAccess(in.holder());
        if (holder instanceof KeyValue keyValue) {
            return new BoolValue(keyValue.contains(in.field()));
        } else if (holder instanceof StrValue(String value)) {
            return new BoolValue(value.contains(in.field()));
        }
        throw new Panic("Can't check if '" + in.field() + "' is in non string or keyvalue type " + holder);
    }

    private Value evalUnary(UnaryExpression unary) {
        return switch (unary.op()) {
            case NOT -> new BoolValue(!asBool(eval0(unary.rhs())));
            case NEGATE -> new NumValue(-getNumberOrThrow(eval0(unary.rhs())));
        };
    }

    private Value evalStruct(StructExpression struct) {
        var fields = new HashMap<String, Value>();
        for (var entry : struct.fields().entrySet()) {
            fields.put(entry.getKey(), eval0(entry.getValue()));
        }
        return new MutableStructValue(fields);
    }

    private boolean asBool(Value value) {
        return switch (value) {
            case NilValue ignored -> false;
            case BoolValue bool -> bool.value();
            case StrValue str -> !str.value().isEmpty();
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
            last = eval0(expr);
        }
        return last;
    }

    private Value evalAssign(AssignExpression assign) {
        var access = assign.lhs();
        final Value field;
        if (access.lhs() == null) {
            field = defaults;
        } else {
            field = eval0(access.lhs());
        }

        if (field instanceof KeyValue.Mutable keyValue) {
            var value = eval0(assign.value());
            keyValue.set(access.field(), value);
            return value;
        } else if (field instanceof KeyValue) {
            throw new Panic("Unable to set property '" + access.field() + "' on immutable key/value " + access.lhs());
        }

        throw new Panic("Unable to set property '" + access.field() + "' on non key/value " + access.lhs());
    }


    private Value evalAccess(AccessExpression expression) {
        var lhs = expression.lhs();
        if (lhs == null) {
            return defaults.get(expression.field());
        }
        var left = eval0(lhs);
        if (left instanceof KeyValue keyValue) {
            keyValue.get(expression.field());
            return Objects.requireNonNullElse(keyValue.get(expression.field()), Value.NIL);
        }

        throw new Panic("Unable to access property " + expression.field() + " of non key/value " + lhs);
    }

    public static class Panic extends RuntimeException {

        public Panic(String message) {
            super(message);
        }
    }
}
