package tech.thatgravyboat.repolib.v2.expl.expression;


import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.FunctionFile;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.Parser;
import tech.thatgravyboat.repolib.v2.expl.StackFile;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public sealed interface Expression<ExpressionType extends Expression<ExpressionType>>
    permits AccessExpression, ArrayExpression, AssignExpression, BinaryExpression, BlockExpression,
    BlockExpression.LastElement, BoolExpression, CallExpression, DebugExpression, FileAccessExpression,
    FileCallExpression, ForEachExpression, ForExpression, IfExpression, InExpression, LambdaExpression,
    LambdaIdentityFunction, MatchExpression, NonSerializableExpression, NumExpression, RangeExpression,
    ReturnExpression, StatementExpression, StrExpression, StructExpression, UnaryExpression {

    static StackFile parseFileOrThrow(RepoLoader loader, String source, String name) {
        return new Parser(source).parseFile(loader, name);
    }

    static ModuleFile parseModuleOrThrow(RepoLoader loader, String name, String source) {
        return new Parser(source).parseModuleFile(name, loader);
    }

    static FunctionFile parseFunctionOrThrow(RepoLoader loader, String name, String source) {
        return new Parser(source).parseFunctionFile(name, loader);
    }

    static Expression<?> parse(String source) {
        return new Parser(source).parseExpression();
    }

    default boolean requiresSemicolon() {
        return true;
    }

    default boolean canReturnValueBeReturned() {
        return false;
    }

    ExpressionTypeRegistry.Type<ExpressionType> expressionId();

    Value evaluate(Evaluator evaluator);
    default Value evaluateStructValue(Evaluator evaluator, MutableStructValue self) {
        return this.evaluate(evaluator);
    }
}
