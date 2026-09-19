package tech.thatgravyboat.repolib.v2.expl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import tech.thatgravyboat.repolib.v2.binary.BinaryFileTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.FileTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public interface ModuleFile extends FunctionValueFile<ModuleFile>, Encodable {


    @Override
    default boolean needsInitialization() {
        return !isInitialized();
    }

    String name();
    void staticData(KeyValue value);
    boolean isInitialized();
    KeyValue staticData();
    Expression staticDataExpression();
    Value evaluate(Evaluator evaluator);
    Expression script();

    @Override
    default void initialize(Evaluator evaluator) {
        if (isInitialized()) return;
        var data =  ((StructValue) Objects.requireNonNullElse(evaluator, Evaluator.CONSTANT).eval0(staticDataExpression()));
        if (data instanceof KeyValue.Mutable mutable) {
            this.staticData(mutable.toFullyImmutable());
        } else {
            this.staticData(data);
        }
    }

    @Override
    default boolean canReturnValueBeReturned() {
        return true;
    }

    @Override
    default Value apply(Evaluator evaluator, List<Value> args) {
        if (args.size() == 1) {
            var scope = evaluator.getMutableStructOrThrow(args.getFirst());
            return evaluator.pushPop(this.name(), scope, () -> this.evaluate(evaluator));
        } else {
            return evaluator.pushPop(this.name(), () -> this.evaluate(evaluator));
        }
    }

    static ModuleFile decode(DecoderContext buffer) throws IOException {
        return new ModuleFile.Impl(
                buffer.readLiteral(),
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    default void encode(EncoderContext buffer) {
        buffer.writeLiteral(this.name());
        ExpressionCodec.writeNullable(this.staticDataExpression(), buffer);
        ExpressionCodec.write(this.script(), buffer);
    }

    @Override
    default void precode(NameTable table) {
        table.insert(this.name());
        if (this.staticDataExpression() != null) {
            this.staticDataExpression().precode(table);
        }
        this.script().precode(table);
    }

    @Override
    default BinaryFileTypeRegistry.Type<ModuleFile> fileId() {
        return FileTypes.MODULE;
    }

    class Impl implements ModuleFile {
        private final String name;
        private final Expression script;
        private KeyValue staticData;
        private final Expression staticDataExpression;
        private boolean hasInitialized = false;

        public Impl(String name, Expression staticData, Expression script) {
            this.name = name;
            this.script = script;
            staticDataExpression = staticData;
            if (staticData == null) {
                this.staticData = ImmutableStructValue.EMPTY;
                hasInitialized = true;
            }
        }


        @Override
        public boolean isInitialized() {
            return this.hasInitialized;
        }

        @Override
        public void staticData(KeyValue value) {
            this.hasInitialized = true;
            this.staticData = value;
        }

        public KeyValue staticData() {
            return staticData;
        }

        public Expression script() {
            return this.script;
        }

        @Override
        public Expression staticDataExpression() {
            return this.staticDataExpression;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Value evaluate(Evaluator evaluator) {
            evaluator.setInOverlay("static_data", this.staticData());
            return evaluator.evaluate(this.script());
        }
    }
}

