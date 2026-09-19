package tech.thatgravyboat.repolib.v2.jvm.compiler;

import tech.thatgravyboat.repolib.v2.expl.expression.AccessExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StrExpression;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.*;
import java.util.Arrays;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;

public class Snippets {

    public static void pushNil(CodeBuilder cb) {
        cb.getstatic(CD_Value, "NIL", CD_Value);
    }

    public static void debugLog(CodeBuilder cb) {
        cb.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        cb.swap();
        cb.invokevirtual(ClassDesc.of("java.io.PrintStream"), "println", MethodTypeDesc.of(CD_void, CD_String));
    }

    public static void debugLog(CodeBuilder cb, String message) {
        cb.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        cb.loadConstant(message);
        cb.invokevirtual(ClassDesc.of("java.io.PrintStream"), "println", MethodTypeDesc.of(CD_void, CD_String));
    }

    public static void loadStrValue(CodeBuilder cb, String value) {
        cb.new_(CD_StrValue);
        cb.dup();
        cb.loadConstant(value);
        cb.invokespecial(CD_StrValue, "<init>", MethodTypeDesc.of(CD_void, CD_String));
    }

    public static void loadBoolValue(CodeBuilder cb, boolean value) {
        cb.getstatic(CD_BoolValue, value ? "TRUE" : "FALSE", CD_Value);
    }

    public static void loadNumValue(CodeBuilder cb, double value) {
        cb.new_(CD_NumValue);
        cb.dup();
        cb.loadConstant(value);
        cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
    }

    public static void throwPanic(CodeBuilder cb, String message) {
        cb.new_(CD_Panic);
        cb.dup();
        cb.loadConstant(message);
        cb.invokespecial(CD_Panic, "<init>", MethodTypeDesc.of(CD_void, CD_String));
        cb.athrow();
    }

    public static void getStringOrThrow(CodeBuilder cb) {
        cb.checkcast(CD_StrValue);
        cb.invokevirtual(CD_StrValue, "value", MethodTypeDesc.of(CD_String));
    }
    public static void getNumberOrThrow(CodeBuilder cb) {
        cb.checkcast(CD_NumValue);
        cb.invokevirtual(CD_NumValue, "value", MethodTypeDesc.of(CD_double));
    }

    public static void pathStringConcat(CodeBuilder cb, int length) {
        ClassDesc[] classDescs = new ClassDesc[length];
        Arrays.fill(classDescs, CD_String);
        StringBuilder separated = new StringBuilder(length * 2 - 1);
        for (int index = 0; index < length; index++) {
            separated.append("\u0001");
            if (index != length - 1) separated.append("/");
        }
        stringConcat(cb, separated.toString(), classDescs);
    }

    public static void stringStringConcat(CodeBuilder cb, String template, int stringCount) {
        ClassDesc[] classDescs = new ClassDesc[stringCount];
        Arrays.fill(classDescs, CD_String);
        stringConcat(cb, template, classDescs);
    }

    public static void stringConcat(CodeBuilder cb, String template, ClassDesc... args) {
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

    public static void assign(CodeBuilder cb, AccessExpression access, CompilationTracker lc) {
        if (access.lhs() == null) {
            cb.dup();
            cb.aload(1);
            cb.swap();
            if (access.field() instanceof StrExpression(String fieldValue)) {
                cb.loadConstant(fieldValue);
            } else {
                access.field().compile(cb, lc);
                getStringOrThrow(cb);
            }
            cb.swap();
            cb.invokevirtual(CD_Evaluator, "setField", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
        } else {
            cb.dup();
            access.lhs().compile(cb, lc);
            cb.checkcast(CD_MutableKV);
            cb.swap();
            if (access.field() instanceof StrExpression(String string)) {
                cb.loadConstant(string);
            } else {
                access.field().compile(cb, lc);
                getStringOrThrow(cb);
            }
            cb.swap();
            cb.invokeinterface(CD_MutableKV, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
        }
    }
}
