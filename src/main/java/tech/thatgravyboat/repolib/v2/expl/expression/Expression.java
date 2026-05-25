package tech.thatgravyboat.repolib.v2.expl.expression;


import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

public sealed interface Expression
        permits AccessExpression, AssignExpression, BlockExpression, BoolExpression, CallExpression, DebugExpression,
        ForExpression, IfExpression, InExpression, NumExpression, SelfEvaluatingExpression, StatementExpression,
        StrExpression, StructExpression, UnaryExpression {

    static StackFile parseFileOrThrow(RepoLoader loader, String source) {
        return new Parser(source).parseFile(loader);
    }
    static ModuleFile parseModuleOrThrow(RepoLoader loader, String source) {
        return new Parser(source).parseModuleFile(loader);
    }

    static Expression parse(String source) {
        return new Parser(source).parseExpression();
    }


}
