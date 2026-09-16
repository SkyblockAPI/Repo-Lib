package tech.thatgravyboat.repolib.v2.expl.compiler;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.expression.*;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Gatherers;

import static java.lang.constant.ConstantDescs.*;

public class ModuleCompiler {
    static ClassDesc CD_Evaluator = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.Evaluator");
    static ClassDesc CD_Panic = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.Evaluator$Panic");
    static ClassDesc CD_Value = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.Value");
    static ClassDesc CD_StrValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.StrValue");
    static ClassDesc CD_BoolValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.BoolValue");
    static ClassDesc CD_StructValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.StructValue");
    static ClassDesc CD_NumValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.NumValue");
    static ClassDesc CD_ArrayValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.ArrayValue");
    static ClassDesc CD_KeyValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.KeyValue");
    static ClassDesc CD_MutableKV = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.KeyValue$Mutable");
    static ClassDesc CD_FunctionValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.FunctionValue");
    static ClassDesc CD_MutableArrayValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue");
    static ClassDesc CD_MutableStructValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue");
    static ClassDesc CD_StructuredFunctionValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.StructuredFunctionValue");

    private static class MetaCodeInfo {
        private final Set<Integer> usedLocals = new HashSet<>();
        private final List<Object> lambdas = new ArrayList<>();

        private MetaCodeInfo(ClassDesc ownClass, ClassBuilder ownBuilder) {
            usedLocals.add(0);
            usedLocals.add(1);
            this.ownClass = ownClass;
            this.ownBuilder = ownBuilder;
        }

        private int addLambda(FunctionValue functionValue) {
            lambdas.add(functionValue);
            return lambdas.size() - 1;
        }
        private int addLambda(Class<IdentityLambdaFunction> functionValue) {
            lambdas.add(functionValue);
            return lambdas.size() - 1;
        }

        public int getLowestUnused() {
            for (int i = 0; true; i++) {
                if (!usedLocals.contains(i)) {
                    usedLocals.add(i);
                    return i;
                }
            }
        }

        int depth = 0;

        public void popStack(CodeBuilder cb) {
            depth -= 1;
            cb.aload(1);
            cb.invokevirtual(CD_Evaluator, "pop", MethodTypeDesc.of(CD_void));
        }

        public void popAll(CodeBuilder cb) {
            for (; depth > 0; depth--) {
                cb.aload(1);
                cb.invokevirtual(CD_Evaluator, "pop", MethodTypeDesc.of(CD_void));
            }
        }

        public void pushStack(CodeBuilder cb, String name) {
            depth += 1;
            cb.aload(1);
            cb.loadConstant("");
            cb.invokevirtual(CD_Evaluator, "push", MethodTypeDesc.of(CD_void, CD_String));
        }

        public void mark(int index) {
            usedLocals.add(index);
        }

        public void free(int index) {
            usedLocals.remove(index);
        }

        private String codeName = "";

        public void setCodeName(String codeName) {
            this.codeName = codeName;
        }

        public String getCodeName() {
            return codeName;
        }

        private Label breakLabel = null;
        private Label continueLabel = null;

        public void setBreakLabel(Label label) {
            breakLabel = label;
        }

        public void setContinueLabel(Label label) {
            continueLabel = label;
        }

        public Label getBreakLabel() {
            return breakLabel;
        }

        public Label getContinueLabel() {
            return continueLabel;
        }

        private final ClassDesc ownClass;

        public ClassDesc ownClass() {
            return ownClass;
        }

        private final ClassBuilder ownBuilder;

        public ClassBuilder ownBuilder() {
            return ownBuilder;
        }

        private int id = 0;

        public int uniqueId() {
            return ++id;
        }
    }

    private static void pushNil(CodeBuilder cb) {
        cb.getstatic(CD_Value, "NIL", CD_Value);
    }

    private static void debugLog(CodeBuilder cb) {
        cb.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        cb.swap();
        cb.invokevirtual(ClassDesc.of("java.io.PrintStream"), "println", MethodTypeDesc.of(CD_void, CD_String));
    }

    private static void debugLog(CodeBuilder cb, String message) {
        cb.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        cb.loadConstant(message);
        cb.invokevirtual(ClassDesc.of("java.io.PrintStream"), "println", MethodTypeDesc.of(CD_void, CD_String));
    }

    private static void loadStrValue(CodeBuilder cb, String value) {
        cb.new_(CD_StrValue);
        cb.dup();
        cb.loadConstant(value);
        cb.invokespecial(CD_StrValue, "<init>", MethodTypeDesc.of(CD_void, CD_String));
    }

    private static void loadBoolValue(CodeBuilder cb, boolean value) {
        cb.getstatic(CD_BoolValue, value ? "TRUE" : "FALSE", CD_Value);
    }

    private static void loadNumValue(CodeBuilder cb, double value) {
        cb.new_(CD_NumValue);
        cb.dup();
        cb.loadConstant(value);
        cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
    }

    private static void throwPanic(CodeBuilder cb, String message) {
        cb.new_(CD_Panic);
        cb.dup();
        cb.loadConstant(message);
        cb.invokespecial(CD_Panic, "<init>", MethodTypeDesc.of(CD_void, CD_String));
        cb.athrow();
    }

    private static void pathStringConcat(CodeBuilder cb, int length) {
        ClassDesc[] classDescs = new ClassDesc[length];
        Arrays.fill(classDescs, CD_String);
        StringBuilder separated = new StringBuilder(length * 2 - 1);
        for (int index = 0; index < length; index++) {
            separated.append("\u0001");
            if (index != length - 1) separated.append("/");
        }
        stringConcat(cb, separated.toString(), classDescs);
    }

    private static void stringConcat(CodeBuilder cb, String template, ClassDesc... args) {
        cb.invokedynamic(
                DynamicCallSiteDesc.of(MethodHandleDesc.of(
                        DirectMethodHandleDesc.Kind.STATIC,
                        ClassDesc.of("java.lang.invoke.StringConcatFactory"),
                        "makeConcatWithConstants",
                        MethodTypeDesc.of(
                                        ClassDesc.of("java.lang.invoke.CallSite"),
                                        ClassDesc.of("java.lang.invoke.MethodHandles$Lookup"),
                                        CD_String, ClassDesc.of("java.lang.invoke.MethodType"),
                                        CD_String, ClassDesc.of("java.lang.Object").arrayType()
                                )
                                .descriptorString()), "makeConcatWithConstants", MethodTypeDesc.of(CD_String, args), template));
    }

    private static void compileBinaryExpression(CodeBuilder cb, BinaryExpression expression, MetaCodeInfo lc) {
        if (expression instanceof BinaryExpression(BinaryExpression.Op op, Expression first, Expression second)) {
            switch (op) {
                case OR -> {
                    Label falseLabel = cb.newLabel();
                    Label trueLabel = cb.newLabel();
                    Label endLabel = cb.newLabel();
                    cb.aload(1);
                    compileExpression(cb, first, lc);
                    cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                    cb.ifne(trueLabel);
                    cb.aload(1);
                    compileExpression(cb, first, lc);
                    cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                    cb.ifeq(falseLabel);
                    cb.labelBinding(trueLabel);
                    cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
                    cb.goto_(endLabel);
                    cb.labelBinding(falseLabel);
                    cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
                    cb.labelBinding(endLabel);
                }
                case AND -> {
                    Label falseLabel = cb.newLabel();
                    Label endLabel = cb.newLabel();
                    cb.aload(1);
                    compileExpression(cb, first, lc);
                    cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                    cb.ifeq(falseLabel);
                    cb.aload(1);
                    compileExpression(cb, first, lc);
                    cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                    cb.ifeq(falseLabel);
                    cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
                    cb.goto_(endLabel);
                    cb.labelBinding(falseLabel);
                    cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
                    cb.labelBinding(endLabel);
                }
                default -> {
                    if (op == BinaryExpression.Op.PLUS) {
                        if (first instanceof StrExpression(String firstString) && second instanceof StrExpression(
                                String secondString
                        )) {
                            loadStrValue(cb, firstString + secondString);
                            return;
                        } else if (first instanceof NumExpression(double firstValue) && second instanceof NumExpression(
                                double secondValue
                        )) {
                            loadNumValue(cb, firstValue + secondValue);
                            return;
                        }
                    }
                    Enum.EnumDesc<BinaryExpression.Op> meow = op.describeConstable().orElseThrow();
                    cb.loadConstant(meow);
                    cb.aload(1);
                    compileExpression(cb, first, lc);
                    compileExpression(cb, second, lc);
                    cb.invokevirtual(meow.constantType(), "perform", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_Value, CD_Value));
                }
            }
        }
    }

    private static void compileUnaryExpression(CodeBuilder cb, Expression expression, MetaCodeInfo lc) {
        if (expression instanceof UnaryExpression(UnaryExpression.Op op, Expression rhs)) {
            switch (op) {
                case NOT -> {
                    cb.aload(1);
                    compileExpression(cb, rhs, lc);
                    cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                    Label meow1 = cb.newLabel();
                    Label meow2 = cb.newLabel();
                    cb.ifne(meow1);
                    cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
                    cb.goto_(meow2);
                    cb.labelBinding(meow1);
                    cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
                    cb.labelBinding(meow2);
                }
                case NEGATE -> {
                    cb.new_(CD_NumValue);
                    cb.dup();
                    if (rhs instanceof NumExpression(double value)) {
                        cb.loadConstant(-value);
                    } else {
                        cb.dconst_0();
                        cb.aload(1);
                        compileExpression(cb, rhs, lc);
                        cb.invokevirtual(CD_Evaluator, "getNumberOrThrow", MethodTypeDesc.of(CD_double, CD_Value));
                        cb.dsub();
                    }
                    cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
                }
            }

        }
    }

    private static void compileStructExpression(CodeBuilder cb, Expression expression, MetaCodeInfo lc) {
        if (expression instanceof StructExpression(Map<String, Expression> fields, AccessExpression spread)) {
            ClassDesc hashMap = ClassDesc.of("java.util.HashMap");
            cb.new_(CD_MutableStructValue);
            cb.dup();
            cb.new_(hashMap);
            cb.dup();
            cb.invokespecial(hashMap, "<init>", MTD_void);
            cb.invokespecial(CD_MutableStructValue, "<init>", MethodTypeDesc.of(CD_void, CD_Map));
            int structSlot = lc.getLowestUnused();
            cb.astore(structSlot);
            if (spread != null) {
                int iteratorSlot = lc.getLowestUnused();
                ClassDesc iterator = ClassDesc.of("java.util.Iterator");
                ClassDesc entry = ClassDesc.of("java.util.Map$Entry");
                cb.aload(1);
                compileExpression(cb, spread, lc);
                cb.invokevirtual(CD_Evaluator, "getStructOrThrow", MethodTypeDesc.of(CD_StructValue, CD_Value));
                cb.invokeinterface(ClassDesc.of("java.lang.Iterable"), "iterator", MethodTypeDesc.of(iterator));
                cb.astore(iteratorSlot);
                Label loopLabel = cb.newLabel();
                Label endLoopLabel = cb.newLabel();
                cb.labelBinding(loopLabel);
                cb.aload(iteratorSlot);
                cb.invokeinterface(iterator, "hasNext", MethodTypeDesc.of(CD_boolean));
                cb.ifeq(endLoopLabel);
                cb.aload(structSlot);
                cb.aload(iteratorSlot);
                cb.invokeinterface(iterator, "next", MethodTypeDesc.of(CD_Object));
                cb.checkcast(entry);
                cb.dup();
                cb.invokeinterface(entry, "getValue", MethodTypeDesc.of(CD_Object));
                cb.checkcast(CD_Value);
                cb.swap();
                cb.invokeinterface(entry, "getKey", MethodTypeDesc.of(CD_Object));
                cb.checkcast(CD_String);
                cb.swap();
                cb.invokevirtual(CD_MutableStructValue, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                cb.goto_(loopLabel);
                cb.labelBinding(endLoopLabel);
                lc.free(iteratorSlot);
            }
            if (fields.size() > 100) {
                var chunkedEntries = fields.entrySet().stream()
                        .gather(Gatherers.windowFixed(500))
                        .toList();
                for (var fieldChunks : chunkedEntries) {
                    String methodName = "generated$" + lc.uniqueId();
                    lc.ownBuilder()
                            .withMethod(methodName, MethodTypeDesc.of(CD_void, CD_Evaluator, CD_MutableStructValue), ClassFile.ACC_PRIVATE, methodBuilder -> methodBuilder.withCode(subCodeBuilder -> {
                                for (var entry : fieldChunks) {
                                    subCodeBuilder.aload(2);
                                    subCodeBuilder.loadConstant(entry.getKey());
                                    if (entry.getValue() instanceof LambdaIdentityFunction lif) {
                                        subCodeBuilder.aload(2);
                                        compileIdentityExpression(cb, lif, lc);
                                    } else {
                                        compileExpression(subCodeBuilder, entry.getValue(), lc);
                                    }
                                    subCodeBuilder.invokevirtual(CD_MutableStructValue, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                                }
                                subCodeBuilder.return_();
                            }));
                    cb.aload(0);
                    cb.aload(1);
                    cb.aload(structSlot);
                    cb.invokevirtual(lc.ownClass(), methodName, MethodTypeDesc.of(CD_void, CD_Evaluator, CD_MutableStructValue));
                }

            } else {
                for (var entry : fields.entrySet()) {
                    cb.aload(structSlot);
                    cb.loadConstant(entry.getKey());
                    if (entry.getValue() instanceof LambdaIdentityFunction lif) {
                        cb.aload(structSlot);
                        compileIdentityExpression(cb, lif, lc);
                    } else {
                        compileExpression(cb, entry.getValue(), lc);
                    }
                    cb.invokevirtual(CD_MutableStructValue, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                }
            }
            cb.aload(structSlot);
            lc.free(structSlot);
        }
    }

    private static void compileMatchExpression(CodeBuilder cb, Expression expression, MetaCodeInfo lc) {
        if (expression instanceof MatchExpression(Expression value, List<MatchExpression.MatchBranch> branches)) {
            compileExpression(cb, value, lc);
            int localSlot = lc.getLowestUnused();
            cb.astore(localSlot);
            Label endEndLabel = cb.newLabel();
            boolean hasCatchall = false;
            for (MatchExpression.MatchBranch branch : branches) {
                Label endLabel = cb.newLabel();
                if (branch.check() != null) {
                    Enum.EnumDesc<MatchExpression.MatchCondition> meow = branch.condition().describeConstable().orElseThrow();
                    cb.loadConstant(meow);
                    cb.aload(1);
                    cb.aload(localSlot);
                    compileExpression(cb, branch.check(), lc);
                    cb.invokevirtual(meow.constantType(), "compare", MethodTypeDesc.of(CD_boolean, CD_Evaluator, CD_Value, CD_Value));
                    cb.ifeq(endLabel);
                } else {
                    hasCatchall = true;
                }

                compileExpression(cb, branch.branch(), lc);
                cb.goto_(endEndLabel);
                cb.labelBinding(endLabel);
            }
            if (!hasCatchall) {
                pushNil(cb);
            }
            lc.free(localSlot);
            cb.labelBinding(endEndLabel);
            cb.nop();
        }
    }

    private static void compileIfExpression(CodeBuilder cb, Expression expression, MetaCodeInfo lc) {
        if (expression instanceof IfExpression(Expression cond, Expression thenExpr, Expression elseExpr)) {
            cb.aload(1);
            compileExpression(cb, cond, lc);
            Label endLabel = cb.newLabel();
            Label endEndLabel = cb.newLabel();
            cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
            cb.ifeq(endLabel);
            compileExpression(cb, thenExpr, lc);
            cb.goto_(endEndLabel);
            cb.labelBinding(endLabel);
            if (elseExpr != null) {
                compileExpression(cb, elseExpr, lc);
            } else {
                pushNil(cb);
            }
            cb.labelBinding(endEndLabel);
            cb.nop();
        }
    }

    private static void compileForEachExpression(CodeBuilder cb, Expression expression, MetaCodeInfo lc) {
        if (expression instanceof ForEachExpression(AccessExpression field, Expression array, Expression body)) {
            lc.pushStack(cb, expression.toString());
            if (array instanceof RangeExpression(
                    boolean inclusiveStart, boolean inclusiveEnd, Expression from, Expression v
            )) {
                int localSlot = lc.getLowestUnused();
                int maxSlot = lc.getLowestUnused();
                Label checkLabel = cb.newLabel();
                Label loopLabel = cb.newLabel();
                Label endLoopLabel = cb.newLabel();
                cb.aload(1);
                compileExpression(cb, from, lc);
                cb.invokevirtual(CD_Evaluator, "getNumberOrThrow", MethodTypeDesc.of(CD_double, CD_Value));
                cb.d2i();
                if (!inclusiveStart) {
                    cb.loadConstant(1);
                    cb.iadd();
                }
                cb.istore(localSlot);
                cb.aload(1);
                compileExpression(cb, v, lc);
                cb.invokevirtual(CD_Evaluator, "getNumberOrThrow", MethodTypeDesc.of(CD_double, CD_Value));
                cb.d2i();
                cb.istore(maxSlot);

                cb.iload(localSlot);
                cb.iload(maxSlot);
                cb.if_icmplt(checkLabel);
                throwPanic(cb, "Num range start smaller than end!");
                Label oldBreakLabel = lc.getBreakLabel();
                Label oldContinueLabel = lc.getContinueLabel();
                lc.setBreakLabel(endLoopLabel);
                lc.setContinueLabel(loopLabel);
                cb.labelBinding(loopLabel);

                cb.iload(localSlot);
                cb.loadConstant(1);
                cb.iadd();
                cb.istore(localSlot);

                cb.labelBinding(checkLabel);
                cb.iload(maxSlot);
                cb.iload(localSlot);
                if (inclusiveEnd) {
                    cb.if_icmplt(endLoopLabel);
                } else {
                    cb.if_icmple(endLoopLabel);
                }

                cb.new_(CD_NumValue);
                cb.dup();
                cb.iload(localSlot);
                cb.i2d();
                cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
                assign(cb, field, lc);
                cb.pop();

                compileExpression(cb, body, lc);
                cb.pop();
                // do stuff idk
                cb.goto_(loopLabel);
                cb.labelBinding(endLoopLabel);
                lc.setBreakLabel(oldBreakLabel);
                lc.setBreakLabel(oldContinueLabel);
            } else {
                int localSlot = lc.getLowestUnused();
                ClassDesc iterator = ClassDesc.of("java.util.Iterator");
                cb.aload(1);
                compileExpression(cb, array, lc);
                cb.invokevirtual(CD_Evaluator, "getArrayOrThrow", MethodTypeDesc.of(CD_ArrayValue, CD_Value));
                cb.invokeinterface(ClassDesc.of("java.lang.Iterable"), "iterator", MethodTypeDesc.of(iterator));
                cb.astore(localSlot);
                Label loopLabel = cb.newLabel();
                Label endLoopLabel = cb.newLabel();

                Label oldBreakLabel = lc.getBreakLabel();
                Label oldContinueLabel = lc.getContinueLabel();
                lc.setBreakLabel(endLoopLabel);
                lc.setContinueLabel(loopLabel);

                cb.labelBinding(loopLabel);

                cb.aload(localSlot);
                cb.invokeinterface(iterator, "hasNext", MethodTypeDesc.of(CD_boolean));
                cb.ifeq(endLoopLabel);
                cb.aload(localSlot);
                cb.invokeinterface(iterator, "next", MethodTypeDesc.of(CD_Object));
                cb.checkcast(CD_Value);
                assign(cb, field, lc);
                cb.pop();
                compileExpression(cb, body, lc);
                cb.pop();
                // meow
                cb.goto_(loopLabel);
                cb.labelBinding(endLoopLabel);
                lc.setBreakLabel(oldBreakLabel);
                lc.setBreakLabel(oldContinueLabel);
                lc.free(localSlot);
            }
            pushNil(cb);
            lc.popStack(cb);
        }
    }

    private static void compileForExpression(CodeBuilder cb, Expression expression, MetaCodeInfo lc) {
        if (expression instanceof ForExpression(Expression init, Expression cond, Expression incr, Expression body)) {
            Label endLoopLabel = cb.newLabel();
            Label loopLabel = cb.newLabel();
            Label checkLabel = cb.newLabel();

            Label oldBreakLabel = lc.getBreakLabel();
            Label oldContinueLabel = lc.getContinueLabel();
            lc.setBreakLabel(endLoopLabel);
            lc.setContinueLabel(loopLabel);

            if (init != null) {
                compileExpression(cb, init, lc);
                cb.pop();
            }
            cb.goto_(checkLabel);
            cb.labelBinding(loopLabel);
            if (incr != null) {
                compileExpression(cb, incr, lc);
                cb.pop();
            }
            cb.labelBinding(checkLabel);
            if (cond != null) {
                cb.aload(1);
                compileExpression(cb, cond, lc);
                cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                cb.ifeq(endLoopLabel);
            }

            compileExpression(cb, body, lc);
            cb.pop();

            cb.goto_(loopLabel);
            cb.labelBinding(endLoopLabel);
            pushNil(cb);
            lc.setBreakLabel(oldBreakLabel);
            lc.setBreakLabel(oldContinueLabel);

        }
    }

    private static void assign(CodeBuilder cb, AccessExpression access, MetaCodeInfo lc) {
        if (access.lhs() == null) {
            cb.dup();
            cb.aload(1);
            cb.swap();
            if (access.field() instanceof StrExpression(String fieldValue)) {
                cb.loadConstant(fieldValue);
            } else {
                cb.dup();
                compileExpression(cb, access.field(), lc);
                cb.invokevirtual(CD_Evaluator, "getStringOrThrow", MethodTypeDesc.of(CD_String, CD_Value));
            }
            cb.swap();
            cb.invokevirtual(CD_Evaluator, "setField", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
        } else {
            Label meow = cb.newLabel();
            cb.dup();
            compileExpression(cb, access.lhs(), lc);
            cb.checkcast(CD_MutableKV);
            cb.swap();
            if (access.field() instanceof StrExpression(String string)) {
                cb.loadConstant(string);
            } else {
                cb.aload(1);
                compileExpression(cb, access.field(), lc);
                cb.invokevirtual(CD_Evaluator, "getStringOrThrow", MethodTypeDesc.of(CD_String, CD_Value));
            }
            cb.swap();
            cb.invokeinterface(CD_MutableKV, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
        }
    }

    private static boolean compileExpression(CodeBuilder cb, Expression expression, MetaCodeInfo lc) {
        switch (expression) {
            case StrExpression(String value) -> loadStrValue(cb, value);
            case NumExpression(double value) -> loadNumValue(cb, value);
            case BoolExpression(boolean value) -> loadBoolValue(cb, value);
            case IfExpression ifExpression -> compileIfExpression(cb, ifExpression, lc);
            case MatchExpression matchExpression -> compileMatchExpression(cb, matchExpression, lc);
            case StructExpression structExpression -> compileStructExpression(cb, structExpression, lc);
            case BinaryExpression binaryExpression -> compileBinaryExpression(cb, binaryExpression, lc);
            case UnaryExpression unaryExpression -> compileUnaryExpression(cb, unaryExpression, lc);
            case BlockExpression.LastElement(Expression expression2) -> compileExpression(cb, expression2, lc);
            case ForEachExpression forEachExpression -> compileForEachExpression(cb, forEachExpression, lc);
            case ForExpression forExpression -> compileForExpression(cb, forExpression, lc);
            case FileAccessExpression(List<Expression> path) -> {
                cb.aload(1);
                for (Expression pathSegment : path) {
                    if (pathSegment instanceof StrExpression(String pathSegmentString)) {
                        cb.loadConstant(pathSegmentString);
                    } else {
                        cb.aload(1);
                        compileExpression(cb, pathSegment, lc);
                        cb.invokevirtual(CD_Evaluator, "getStringOrThrow", MethodTypeDesc.of(CD_String, CD_Value));
                    }
                }
                pathStringConcat(cb, path.size());
                cb.invokevirtual(CD_Evaluator, "getFileAccess", MethodTypeDesc.of(CD_FunctionValue, CD_String));
            }
            case BlockExpression(List<Expression> exprs) -> {
                if (exprs.isEmpty()) {
                    pushNil(cb);
                }
                for (int i = 0; i < exprs.size(); i++) {
                    Expression expr = exprs.get(i);
                    if (compileExpression(cb, expr, lc)) {
                        return true;
                    }
                    if (i != exprs.size() - 1) {
                        cb.pop();
                    }
                }
            }
            case AssignExpression(AccessExpression access, Expression value) -> {
                compileExpression(cb, value, lc);
                assign(cb, access, lc);
            }
            case AccessExpression(Expression lhs, Expression field) -> {
                if (lhs == null) {
                    cb.aload(1);
                    if (field instanceof StrExpression(String value)) {
                        cb.loadConstant(value);
                    } else {
                        cb.dup();
                        compileExpression(cb, field, lc);
                        cb.invokevirtual(CD_Evaluator, "getStringOrThrow", MethodTypeDesc.of(CD_String, CD_Value));
                    }
                    cb.invokevirtual(CD_Evaluator, "getField", MethodTypeDesc.of(CD_Value, CD_String));
                } else {


                    if (field instanceof StrExpression(String value)) {
                        cb.aload(1);
                        compileExpression(cb, lhs, lc);
                        cb.loadConstant(value);
                        cb.invokevirtual(CD_Evaluator, "getField", MethodTypeDesc.of(CD_Value, CD_Value, CD_String));
                    } else {
                        Label arrayValLabel = cb.newLabel();
                        Label endLabel = cb.newLabel();
                        compileExpression(cb, lhs, lc);
                        cb.checkcast(CD_KeyValue);
                        cb.aload(1);
                        compileExpression(cb, field, lc);
                        cb.dup();
                        cb.instanceOf(CD_NumValue);
                        cb.ifne(arrayValLabel);
                        cb.invokevirtual(CD_Evaluator, "getStringOrThrow", MethodTypeDesc.of(CD_String, CD_Value));
                        cb.invokeinterface(CD_KeyValue, "get", MethodTypeDesc.of(CD_Value, CD_String));
                        cb.goto_(endLabel);
                        cb.labelBinding(arrayValLabel);
                        cb.invokevirtual(CD_Evaluator, "getNumberOrThrow", MethodTypeDesc.of(CD_double, CD_Value));
                        cb.d2i();
                        cb.swap();
                        cb.checkcast(CD_ArrayValue);
                        cb.swap();
                        cb.invokeinterface(CD_ArrayValue, "get", MethodTypeDesc.of(CD_Value, CD_int));
                        cb.labelBinding(endLabel);
                    }
                }
            }
            case FileCallExpression(Expression access, StructExpression expr) -> {
                cb.aload(1);
                compileExpression(cb, access, lc);
                cb.invokevirtual(CD_Evaluator, "getStructuredFunctionOrThrow", MethodTypeDesc.of(CD_StructuredFunctionValue, CD_Value));
                cb.aload(1);
                compileStructExpression(cb, expr, lc);
                cb.invokeinterface(CD_StructuredFunctionValue, "apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_StructValue));
            }
            case CallExpression(Expression lhs, List<Expression> args) -> {
                compileExpression(cb, lhs, lc);
                cb.checkcast(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.FunctionValue"));
                cb.aload(1); // should always be EVALUATOR
                cb.new_(ClassDesc.of("java.util.ArrayList"));
                cb.dup();
                cb.loadConstant(args.size());
                cb.invokespecial(ClassDesc.of("java.util.ArrayList"), "<init>", MethodTypeDesc.of(CD_void, CD_int));
                for (Expression arg : args) {
                    cb.dup();
                    compileExpression(cb, arg, lc);
                    cb.invokeinterface(ClassDesc.of("java.util.List"), "add", MethodTypeDesc.of(CD_boolean, CD_Object));
                    cb.pop();
                }
                lc.pushStack(cb, expression.toString());
                cb.invokeinterface(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.FunctionValue"), "apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, ClassDesc.of("java.util.List")));
                lc.popStack(cb);
            }
            case ReturnExpression(Expression retExpr) -> {
                compileExpression(cb, retExpr, lc);
                lc.popAll(cb);
                cb.areturn();
                return true;
            }
            case InExpression(AccessExpression holder, Expression field) -> {
                cb.aload(1);
                compileExpression(cb, field, lc);
                cb.invokevirtual(CD_Evaluator, "getStringOrThrow", MethodTypeDesc.of(CD_String, CD_Value));

                Label isNotKv = cb.newLabel();
                Label isNotStr = cb.newLabel();
                Label endLabel = cb.newLabel();

                compileExpression(cb, holder, lc);
                cb.dup();
                cb.instanceOf(CD_KeyValue);
                cb.ifeq(isNotKv);

                cb.checkcast(CD_KeyValue);
                cb.swap();
                cb.invokeinterface(CD_KeyValue, "contains", MethodTypeDesc.of(CD_boolean, CD_String));
                cb.goto_(endLabel);

                cb.labelBinding(isNotKv);
                cb.checkcast(CD_StrValue);
                cb.invokevirtual(CD_StrValue, "value", MethodTypeDesc.of(CD_String));
                cb.swap();
                cb.invokevirtual(CD_String, "contains", MethodTypeDesc.of(CD_boolean, ClassDesc.of("java.lang.CharSequence")));

                cb.labelBinding(endLabel);
                Label trueCase = cb.newLabel();
                Label falseCase = cb.newLabel();
                cb.ifeq(trueCase);
                cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
                cb.goto_(falseCase);
                cb.labelBinding(trueCase);
                cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
                cb.labelBinding(falseCase);
                cb.nop();

            }
            case StatementExpression(StatementExpression.Op op) -> {
                switch (op) {
                    case RETURN -> {
                        pushNil(cb);
                        lc.popAll(cb);
                        cb.areturn();
                    }
                    case BREAK -> cb.goto_(lc.getBreakLabel());
                    case CONTINUE -> cb.goto_(lc.getContinueLabel());
                    default -> {
                        throwPanic(cb, "didnt know how to compile " + op);
                        throw new Evaluator.Panic(":c");
                    }
                }
            }
            case LambdaExpression lambdaExpression -> compileLambdaExpression(cb, lambdaExpression, lc);
            case ArrayExpression(List<Expression> list) -> {
                ClassDesc arrayList = ClassDesc.of("java.util.ArrayList");
                cb.new_(arrayList);
                cb.dup();
                cb.invokespecial(arrayList, "<init>", MTD_void);
                cb.invokestatic(CD_MutableArrayValue, "create", MethodTypeDesc.of(CD_MutableArrayValue, CD_List));
                for (Expression entry : list) {
                    cb.dup();
                    compileExpression(cb, entry, lc);
                    cb.invokevirtual(CD_MutableArrayValue, "add", MethodTypeDesc.of(CD_void, CD_Value));
                }
            }
            default ->
                    throw new IllegalStateException("Unexpected value: " + expression + " type " + expression.getClass());
        }
        return false;
    }

    private static int index = 0;

    interface IdentityLambdaFunction extends FunctionValue {
        Value setSelf(Value self);
    }

    private static void compileIdentityExpression(CodeBuilder cb, LambdaIdentityFunction expression, MetaCodeInfo lc) {
        try {
            String lambdaKey = "" + index++;
            Class<IdentityLambdaFunction> lambdaClass = compileLambda(expression.expression(), lc.getCodeName() + "$" + lambdaKey + "$special", true);
            cb.getstatic(lc.ownClass(), "lambdas", CD_List);
            cb.loadConstant(lc.addLambda(lambdaClass));
            ClassDesc CD_IdentityLambdaFunction = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.compiler.ModuleCompiler$IdentityLambdaFunction");
            cb.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int));
            cb.checkcast(CD_Class);
            cb.invokevirtual(CD_Class, "newInstance", MethodTypeDesc.of(CD_Object));
            cb.checkcast(CD_IdentityLambdaFunction);
            cb.swap();
            cb.invokeinterface(CD_IdentityLambdaFunction, "setSelf", MethodTypeDesc.of(CD_Value, CD_Value));
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compileLambdaExpression(CodeBuilder cb, LambdaExpression expression, MetaCodeInfo lc) {
        try {
            String lambdaKey = "" + index++;
            Class<IdentityLambdaFunction> lambdaClass = compileLambda(expression, lc.getCodeName() + "$" + lambdaKey, false);
            cb.getstatic(lc.ownClass(), "lambdas", CD_List);
            cb.loadConstant(lc.addLambda(lambdaClass.getConstructor().newInstance()));
            cb.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int));
            cb.checkcast(CD_FunctionValue);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<IdentityLambdaFunction> compileLambda(LambdaExpression expression, String name, boolean identity) throws IllegalAccessException, InstantiationException {
        Collection<LambdaExpression.LambdaArgument> arguments = expression.arguments();
        int min = 0;
        int max = 0;
        boolean hasEncounteredOptional = false;

        for (var argument : arguments) {
            max++;

            if (argument.optional()) {
                hasEncounteredOptional = true;
                continue;
            }

            if (hasEncounteredOptional) {
                throw new IllegalStateException("Optional before required argument!");
            }

            min++;
        }

        ClassDesc lambdaClass = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.compiler.generated$" + name + "$lambda");
        int finalMin = min;
        int finalMax = max;
        ClassDesc CD_LambdaFunctionValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.LambdaFunctionValue");
        AtomicReference<List<Object>> lambdas = new AtomicReference<>();
        byte[] classBytes = ClassFile.of()
                .build(lambdaClass, (builder) -> {
                    builder.withSuperclass(CD_LambdaFunctionValue);
                    builder.withInterfaces(builder.constantPool()
                            .classEntry(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.compiler.ModuleCompiler$IdentityLambdaFunction")));
                    if (identity) builder.withField("self", CD_Value, ClassFile.ACC_PRIVATE);
                    builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> {
                        methodBuilder.withCode(codeBuilder -> {

                            codeBuilder.aload(0);

                            codeBuilder.aconst_null();

                            codeBuilder.loadConstant(finalMin == finalMax ? 0 : 1);
                            codeBuilder.loadConstant(finalMin);
                            codeBuilder.loadConstant(finalMax);

                            codeBuilder.invokespecial(CD_LambdaFunctionValue, "<init>", MethodTypeDesc.of(
                                    CD_void,
                                    ClassDesc.of("java.util.function.BiFunction"),
                                    CD_boolean,
                                    CD_int,
                                    CD_int
                            ));
                            if (identity) {
                                codeBuilder.aload(0);
                                codeBuilder.aconst_null();
                                codeBuilder.putfield(lambdaClass, "self", CD_Value);
                            }
                            codeBuilder.return_();
                        });
                    });
                    if (identity) {
                        builder.withMethod("setSelf", MethodTypeDesc.of(CD_Value, CD_Value), ClassFile.ACC_PUBLIC, methodBuilder -> {
                            methodBuilder.withCode(cb -> {
                                cb.aload(0);
                                cb.aload(1);
                                cb.putfield(lambdaClass, "self", CD_Value);
                                cb.aload(0);
                                cb.areturn();
                            });
                        });
                    }
                    builder.withMethod("canReturnValueBeReturned", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> {
                        mb.withCode(cb -> {
                            cb.iconst_1();
                            cb.ireturn();
                        });
                    });
                    builder.withMethod("apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_List), ClassFile.ACC_PUBLIC, mb -> {
                        mb.withCode(cb -> {
                            MetaCodeInfo lc = new MetaCodeInfo(lambdaClass, builder);
                            lc.setCodeName(name + "$lambda");
                            lc.mark(2);
                            cb.aload(1);
                            if (identity) {
                                cb.loadConstant("self");
                                cb.aload(0);
                                cb.getfield(lambdaClass, "self", CD_Value);
                                cb.invokevirtual(CD_Evaluator, "setField", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                                cb.aload(1);
                            }

                            lc.pushStack(cb, "lambda");

                            cb.aload(2);
                            cb.invokeinterface(CD_List, "size", MethodTypeDesc.of(CD_int));
                            int lengthLocal = lc.getLowestUnused();
                            cb.istore(lengthLocal);
                            Label correctArgLabel = cb.newLabel();
                            if (finalMin == finalMax) {
                                cb.iload(lengthLocal);
                                cb.loadConstant(finalMin);
                                cb.if_icmpeq(correctArgLabel);
                                throwPanic(cb, "Invalid Arg Count!");
                            } else {
                                Label throwLabel = cb.newLabel();
                                cb.iload(lengthLocal);
                                cb.loadConstant(finalMin);
                                cb.if_icmplt(throwLabel);
                                cb.iload(lengthLocal);
                                cb.loadConstant(finalMax);
                                cb.if_icmple(correctArgLabel);
                                cb.labelBinding(throwLabel);
                                throwPanic(cb, "Invalid Arg Count!");
                            }
                            cb.labelBinding(correctArgLabel);

                            Label ranOutOfArgument = cb.newLabel();

                            for (LambdaExpression.LambdaArgument argument : arguments) {
                                if (argument.position() >= finalMin) {
                                    cb.iload(lengthLocal);
                                    cb.loadConstant(argument.position());
                                    cb.if_icmple(ranOutOfArgument);
                                }
                                cb.aload(1);
                                cb.loadConstant(argument.name());
                                cb.aload(2);
                                cb.loadConstant(argument.position());
                                cb.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int));
                                cb.invokevirtual(CD_Evaluator, "setField", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                            }
                            cb.labelBinding(ranOutOfArgument);
                            cb.nop();

                            if (!compileExpression(cb, expression.body(), lc)) {
                                lc.popAll(cb);
                                cb.areturn();
                            }

                            lambdas.set(lc.lambdas);
                        });
                    });
                    builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                });
        Class<?> lambdaClassClass =  MethodHandles.lookup()
                .defineHiddenClass(classBytes, true)
                .lookupClass();

        try {
            lambdaClassClass.getField("lambdas").set(null, lambdas.get());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        //noinspection unchecked
        return (Class<IdentityLambdaFunction>) lambdaClassClass;
    }

    public static SelfEvaluatingExpression createSelfEvaluatingExpression(Expression expression, String name) {
        try {
            AtomicReference<List<Object>> lambdas = new AtomicReference<>();
            String cleanName = name.replaceAll("[\\\\/]", "#") + "$" + index++;
            ClassDesc classDesc = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.compiler.generated$" + cleanName);
            byte[] classBytes = ClassFile.of()
                    .build(classDesc, (builder) -> {
                        builder.withInterfaces(builder.constantPool()
                                .classEntry(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression")));

                        builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> {
                            methodBuilder.withCode(cb -> {
                                cb.aload(0);
                                cb.invokespecial(CD_Object, "<init>", MTD_void);
                                cb.return_();
                            });
                        });
                        builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                        builder.withMethod("canReturnValueBeReturned", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> {
                            mb.withCode(cb -> {
                                cb.iconst_1();
                                cb.ireturn();
                            });
                        });
                        builder.withMethod("evaluate", MethodTypeDesc.of(CD_Value, CD_Evaluator), ClassFile.ACC_PUBLIC, mb -> {
                            mb.withCode(cb -> {
                                MetaCodeInfo lc = new MetaCodeInfo(classDesc, builder);
                                lc.setCodeName(cleanName);
                                if (!compileExpression(cb, expression, lc)) {
                                    lc.popAll(cb);
                                    cb.areturn();
                                }
                                lambdas.set(lc.lambdas);
                            });
                        });
                    });

            Class<?> sexClass = MethodHandles.lookup()
                    .defineHiddenClass(classBytes, true)
                    .lookupClass();

            try {
                sexClass.getField("lambdas").set(null, lambdas.get());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }

            return (SelfEvaluatingExpression) sexClass.getConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("[" + name + "] ");
            throw new RuntimeException(e);
        }
    }
}
