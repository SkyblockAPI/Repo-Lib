package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;

public record StructExpression(Map<String, Expression> fields, AccessExpression spread) implements Expression {

    @Override
    public @NotNull String toString() {
        return fields.entrySet()
                .stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.STRUCT;
    }

    @Override
    public void precode(NameTable table) {
        fields.forEach((key, expression) -> {
            table.insert(key);
            table.insert(expression);
        });
        table.insert(this.spread);
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeCollection(this.fields.entrySet(), (entry, _) -> {
            buffer.writeLiteral(entry.getKey());
            ExpressionCodec.write(entry.getValue(), buffer);
        });
        ExpressionCodec.writeUntypedNullable(this.spread, buffer);
    }

    @SuppressWarnings("unchecked")
    public static StructExpression decode(DecoderContext buffer) throws IOException {
        return new StructExpression(
                Map.ofEntries(buffer.readCollection(_ -> Map.entry(
                        buffer.readLiteral(),
                        ExpressionCodec.read(buffer))).toArray(Map.Entry[]::new)),
                ExpressionCodec.readUntypedNullable(ExpressionTypes.ACCESS, buffer));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        ClassDesc hashMap = ClassDesc.of("java.util.HashMap");
        cb.new_(CD_MutableStructValue);
        cb.dup();
        cb.new_(hashMap);
        cb.dup();
        cb.loadConstant((int) Math.clamp(fields.size() * 1.34, 16, 128));
        cb.invokespecial(hashMap, "<init>", MethodTypeDesc.of(CD_void, CD_int));
        cb.invokespecial(CD_MutableStructValue, "<init>", MethodTypeDesc.of(CD_void, CD_Map));
        int structSlot = lc.getLowestUnused();
        cb.astore(structSlot);
        if (fields.size() > 100) {
            var chunkedEntries = fields.entrySet().stream()
                    .gather(Gatherers.windowFixed(500))
                    .toList();
            for (var fieldChunks : chunkedEntries) {
                String methodName = "generated$" + lc.uniqueId();
                lc.ownBuilder()
                        .withMethod(methodName, MethodTypeDesc.of(CD_void, CD_Evaluator, CD_MutableStructValue), ClassFile.ACC_PRIVATE, methodBuilder -> methodBuilder.withCode(subCodeBuilder -> {
                            for (var entry : fieldChunks) {
                                subCodeBuilder.aload(2);
                                subCodeBuilder.loadConstant(entry.getKey());
                                if (entry.getValue() instanceof LambdaIdentityFunction lif) {
                                    subCodeBuilder.aload(2);
                                    lif.compile(subCodeBuilder, lc);
                                } else {
                                    entry.getValue().compile(subCodeBuilder, lc);
                                }
                                subCodeBuilder.invokevirtual(CD_MutableStructValue, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                            }
                            subCodeBuilder.return_();
                        }));
                cb.aload(0);
                cb.aload(1);
                cb.aload(structSlot);
                cb.invokevirtual(lc.ownClass(), methodName, MethodTypeDesc.of(CD_void, CD_Evaluator, CD_MutableStructValue));
            }

        } else {
            for (var entry : fields.entrySet()) {
                cb.aload(structSlot);
                cb.loadConstant(entry.getKey());
                if (entry.getValue() instanceof LambdaIdentityFunction lif) {
                    cb.aload(structSlot);
                    lif.compile(cb, lc);
                } else {
                    entry.getValue().compile(cb, lc);
                }
                cb.invokevirtual(CD_MutableStructValue, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
            }
        }
        if (spread != null) {
            cb.new_(CD_LayeredStructValue);
            cb.dup();
            cb.aload(structSlot);
            cb.checkcast(CD_StructValueMutableStruct);
            spread.compile(cb, lc);
            cb.checkcast(CD_KeyValue);
            cb.invokespecial(CD_LayeredStructValue, "<init>", MethodTypeDesc.of(CD_void, CD_StructValueMutableStruct, CD_KeyValue));
            cb.astore(structSlot);
        }
        cb.aload(structSlot);
        lc.free(structSlot);
        return false;
    }
}
