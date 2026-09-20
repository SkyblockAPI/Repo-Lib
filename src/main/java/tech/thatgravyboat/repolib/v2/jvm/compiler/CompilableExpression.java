package tech.thatgravyboat.repolib.v2.jvm.compiler;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;

import static java.lang.constant.ConstantDescs.CD_boolean;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public interface CompilableExpression {
    /***
     * Compile the expression to JVM bytecode
     * @param cb where the code gets built
     * @param lc a tracker that handles a lot of logic shared across different types
     * @return whether the expression unconditionally jumped
     */
    boolean compile(CodeBuilder cb, CompilationTracker lc);
    boolean isBoolean();
    void compileBoolean(CodeBuilder cb, CompilationTracker lc);
    boolean isNumber();
    void compileNumber(CodeBuilder cb, CompilationTracker lc);

    default void compileNumberElseConvert(CodeBuilder cb, CompilationTracker lc) {
        if (isNumber()) {
            compileNumber(cb, lc);
        } else {
            compile(cb, lc);
            Snippets.getNumberOrThrow(cb);
        }
    }
    default void compileBooleanElseConvert(CodeBuilder cb, CompilationTracker lc) {
        if (isBoolean()) {
            compileBoolean(cb, lc);
        } else {
            cb.aload(1);
            compile(cb, lc);
            cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
        }
    }
}
