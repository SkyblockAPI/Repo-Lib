package tech.thatgravyboat.repolib.v2.binary;

import tech.thatgravyboat.repolib.v2.expl.expression.AccessExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.ArrayExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.AssignExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.BinaryExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.BlockExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.BoolExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.CallExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.DebugExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.FileAccessExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.FileCallExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.ForEachExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.ForExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.IfExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.InExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.LambdaExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.LambdaIdentityFunction;
import tech.thatgravyboat.repolib.v2.expl.expression.MatchExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.NumExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.RangeExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.ReturnExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StatementExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StrExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.StructExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.UnaryExpression;

public class ExpressionTypes {
    public static final ExpressionTypeRegistry.Type<StrExpression> STRING = register(StrExpression::decode);
    public static final ExpressionTypeRegistry.Type<BoolExpression> BOOLEAN = register(BoolExpression::decode);
    public static final ExpressionTypeRegistry.Type<NumExpression> NUMBER = register(NumExpression::decode);
    public static final ExpressionTypeRegistry.Type<AccessExpression> ACCESS = register(AccessExpression::decode);
    public static final ExpressionTypeRegistry.Type<ArrayExpression> ARRAY = register(ArrayExpression::decode);
    public static final ExpressionTypeRegistry.Type<AssignExpression> ASSIGN = register(AssignExpression::decode);
    public static final ExpressionTypeRegistry.Type<BinaryExpression> BINARY = register(BinaryExpression::decode);
    public static final ExpressionTypeRegistry.Type<BlockExpression> BLOCK = register(BlockExpression::decode);
    public static final ExpressionTypeRegistry.Type<BlockExpression.LastElement> BLOCK_LAST_ELEMENT = register(BlockExpression.LastElement::decode);
    public static final ExpressionTypeRegistry.Type<CallExpression> CALL = register(CallExpression::decode);
    public static final ExpressionTypeRegistry.Type<DebugExpression> DEBUG = register(DebugExpression::decode);
    public static final ExpressionTypeRegistry.Type<FileAccessExpression> FILE_ACCESS = register(FileAccessExpression::decode);
    public static final ExpressionTypeRegistry.Type<FileCallExpression> FILE_CALL = register(FileCallExpression::decode);
    public static final ExpressionTypeRegistry.Type<ForEachExpression> FOR_EACH = register(ForEachExpression::decode);
    public static final ExpressionTypeRegistry.Type<ForExpression> FOR = register(ForExpression::decode);
    public static final ExpressionTypeRegistry.Type<IfExpression> IF = register(IfExpression::decode);
    public static final ExpressionTypeRegistry.Type<InExpression> IN = register(InExpression::decode);
    public static final ExpressionTypeRegistry.Type<LambdaExpression> LAMBDA = register(LambdaExpression::decode);
    public static final ExpressionTypeRegistry.Type<LambdaIdentityFunction> LAMBDA_IDENTITY = register(LambdaIdentityFunction::decode);
    public static final ExpressionTypeRegistry.Type<MatchExpression> MATCH = register(MatchExpression::decode);
    public static final ExpressionTypeRegistry.Type<RangeExpression> RANGE = register(RangeExpression::decode);
    public static final ExpressionTypeRegistry.Type<ReturnExpression> RETURN = register(ReturnExpression::decode);
    public static final ExpressionTypeRegistry.Type<StatementExpression> STATEMENT = register(StatementExpression::decode);
    public static final ExpressionTypeRegistry.Type<StructExpression> STRUCT = register(StructExpression::decode);
    public static final ExpressionTypeRegistry.Type<UnaryExpression> UNARY = register(UnaryExpression::decode);

    private static <ExpressionType extends Expression & Encodable> ExpressionTypeRegistry.Type<ExpressionType> register(
            Decoder<ExpressionType> decoder
    ) {
        var type = new ExpressionTypeRegistry.Type<>(decoder, (byte) ExpressionTypeRegistry.counter.getAndIncrement());
        ExpressionTypeRegistry.registry.put(type.id(), type);
        return type;
    }


}
