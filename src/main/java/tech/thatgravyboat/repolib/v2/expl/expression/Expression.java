package tech.thatgravyboat.repolib.v2.expl.expression;


import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.expl.FunctionFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.expl.StackFile;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilableExpression;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;

import java.lang.classfile.CodeBuilder;

public sealed interface Expression extends Encodable, CompilableExpression
        permits AccessExpression, AssignExpression, BlockExpression, BoolExpression, CallExpression, DebugExpression,
    FileAccessExpression, ForExpression, IfExpression, InExpression, NumExpression, SelfEvaluatingExpression,
    StatementExpression, StrExpression, StructExpression, UnaryExpression {

    static StackFile parseFileOrThrow(RepoLoader loader, String source, String name) {
        return new Parser(source).parseFile(loader, name);
    }
    static ModuleFile parseModuleOrThrow(RepoLoader loader, String name, String source) {
        return new Parser(source).parseModuleFile(name, loader);
    }

    static FunctionFile parseFunctionOrThrow(RepoLoader loader, String name,  String source) {
        return new Parser(source).parseFunctionFile(name, loader);
    }

    static Expression parse(String source) {
        return new Parser(source).parseExpression();
    }

    default boolean requiresSemicolon() {
        return true;
    }
    default boolean canReturnValueBeReturned() {
        return false;
    }

    @Override
    default boolean compile(CodeBuilder cb, CompilationTracker lc) {
        throw new RuntimeException("Invalid type " + getClass().getSimpleName());
    }

    ExpressionTypeRegistry.Type<?> expressionId();
}
