package tech.thatgravyboat.repolib.v2.expl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import tech.thatgravyboat.repolib.v2.binary.BinaryFileTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.FileTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.binary.TypedFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.LambdaExpression;
import tech.thatgravyboat.repolib.v2.expl.value.LambdaValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructuredFunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record FunctionFile(String name, Collection<LambdaExpression.LambdaArgument> arguments, Expression body) implements LambdaValue, StructuredFunctionValue,
        FunctionValueFile<FunctionFile> {

    @Override
    public Value apply(Evaluator evaluator, StructValue structValue) {
        for (var argument : arguments) {
            if (argument.optional()) continue;
            if (!structValue.contains(argument.name())) {
                return evaluator.panic("Missing required argument on " + name);
            }
        }

        return evaluator.pushPop(name, structValue.toMutableStruct(), () -> evaluator.evaluate(this.body));
    }

    @Override
    public Value apply(Evaluator evaluator, List<Value> values) {

        return evaluator.pushPop(name, () -> {

            for (var argument : arguments) {
                if (argument.optional() && argument.position() >= values.size()) continue;
                evaluator.set(argument.name(), values.get(argument.position()));
            }

            return evaluator.evaluate(body);
        });
    }

    @Override
    public boolean vararg() {
        int min = 0;
        int max = 0;
        boolean hasEncounteredOptional = false;

        for (var argument : arguments) {
            max++;

            if (argument.optional()) {
                hasEncounteredOptional = true;
                continue;
            }

            if (hasEncounteredOptional) {
                throw new IllegalStateException("Optional before required argument!");
            }

            min++;
        }

        return min != max;
    }

    @Override
    public int arityMin() {
        int min = 0;
        for (var argument : arguments) {
            if (argument.optional()) continue;

            min++;
        }

        return min;
    }

    @Override
    public int arityMax() {
        return this.arguments.size();
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeLiteral(this.name);
        buffer.writeCollection(this.arguments, LambdaExpression.LambdaArgument::encode);
        ExpressionCodec.write(this.body, buffer);
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.name);
        for (var argument : this.arguments) {
            argument.precode(table);
        }
        this.body.precode(table);
    }

    @Override
    public BinaryFileTypeRegistry.Type<FunctionFile> fileId() {
        return FileTypes.FUNCTION;
    }

    public static FunctionFile decode(DecoderContext buffer) throws IOException {
        return new FunctionFile(
                buffer.readLiteral(),
                buffer.readCollection(LambdaExpression.LambdaArgument::decode),
                ExpressionCodec.read(buffer)
        );
    }
}
