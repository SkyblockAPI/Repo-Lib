package tech.thatgravyboat.repolib.v2.expl.expression;


import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

public sealed interface Expression
        permits AccessExpression, AssignExpression, BlockExpression, BoolExpression, CallExpression, ForExpression,
        IfExpression, InExpression, NumExpression, SelfEvaluatingExpression, StrExpression, StructExpression,
        StatementExpression, UnaryExpression {

    static StackFile parseFileOrThrow(RepoLoader loader, String source) {
        return new Parser(source).parseFile(loader);
    }

    static Expression parse(String source) {
        return new Parser(source).parseExpression();
    }


}
