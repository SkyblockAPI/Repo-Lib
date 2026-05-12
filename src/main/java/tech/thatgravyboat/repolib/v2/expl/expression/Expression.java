package tech.thatgravyboat.repolib.v2.expl.expression;


import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.expl.StackFile;

public sealed interface Expression
        permits Access, Assign, Block, Bool, Call, For, If, In, Num, SelfEvaluatingExpression, Str, Struct,
        TokenExpression, Unary {

    static StackFile parseFileOrThrow(String source) {
        return new Parser(source).parseFile();
    }

    static Expression parse(String source) {
        return new Parser(source).parseExpression();
    }


}
