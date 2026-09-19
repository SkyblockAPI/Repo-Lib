package tech.thatgravyboat.repolib.v2.jvm.compiler;

import java.lang.classfile.CodeBuilder;

public interface CompilableExpression {
    boolean compile(CodeBuilder cb, CompilationTracker lc);
}
