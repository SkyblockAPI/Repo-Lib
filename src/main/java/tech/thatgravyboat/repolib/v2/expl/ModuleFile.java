package tech.thatgravyboat.repolib.v2.expl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import tech.thatgravyboat.repolib.v2.binary.BinaryFileTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.FileTypes;
import tech.thatgravyboat.repolib.v2.binary.TypedFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public final class ModuleFile implements FunctionValue, TypedFile<ModuleFile>, Encodable {
    private final String name;
    private final Expression script;
    private KeyValue staticData;
    private final Expression staticDataExpression;
    private boolean hasInitialized = false;

    public ModuleFile(String name, Expression staticData, Expression script) {
        this.name = name;
        this.script = script;
        staticDataExpression = staticData;
        if (staticData == null) {
            this.staticData = ImmutableStructValue.EMPTY;
            hasInitialized = true;
        }
    }

    @Override
    public boolean needsInitialization() {
        return !hasInitialized;
    }

    @Override
    public void initialize(Evaluator evaluator) {
        if (hasInitialized) return;
        var data =  ((StructValue) Objects.requireNonNullElse(evaluator, Evaluator.CONSTANT).eval0(staticDataExpression));
        if (data instanceof KeyValue.Mutable mutable) {
            this.staticData = mutable.toFullyImmutable();
        } else {
            this.staticData = data;
        }
    }

    public KeyValue getStaticData() {
        return staticData;
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        evaluator.setInOverlay("static_data", this.staticData);
        return evaluator.evaluate(script);
    }

    private Value evaluate0(Evaluator evaluator) {
        evaluator.setInOverlay("static_data", this.staticData);
        return evaluator.evaluate(script);
    }

    @Override
    public Value apply(Evaluator evaluator, List<Value> args) {
        if (args.size() == 1) {
            var scope = evaluator.getMutableStructOrThrow(args.getFirst());
            return evaluator.pushPop(name, scope, () -> this.evaluate0(evaluator));
        } else {
            return evaluator.pushPop(name, () -> this.evaluate0(evaluator));
        }
    }

    public static ModuleFile decode(ByteBuffer buffer) throws IOException {
        return new ModuleFile(
                buffer.readString(),
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeString(this.name);
        ExpressionCodec.writeNullable(this.staticDataExpression, buffer);
        ExpressionCodec.write(this.script, buffer);
    }

    @Override
    public BinaryFileTypeRegistry.Type<ModuleFile> fileId() {
        return FileTypes.MODULE;
    }
}

