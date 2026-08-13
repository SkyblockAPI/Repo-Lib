package tech.thatgravyboat.repolib.v2.expl.expression;


import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.FunctionFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

public sealed interface Expression
    permits AccessExpression, AssignExpression, BlockExpression, BoolExpression, CallExpression, DebugExpression,
    FileAccessExpression, ForExpression, IfExpression, InExpression, NumExpression, SelfEvaluatingExpression,
    StatementExpression, StrExpression, StructExpression, UnaryExpression {

    static StackFile parseFileOrThrow(RepoLoader loader, String source) {
        return new Parser(source).parseFile(loader);
    }
    static ModuleFile parseModuleOrThrow(RepoLoader loader, String name, String source, Evaluator evaluator) {
        return new Parser(source).parseModuleFile(name, loader, evaluator);
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

}
