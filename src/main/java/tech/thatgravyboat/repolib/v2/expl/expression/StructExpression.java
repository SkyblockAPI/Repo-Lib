package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_MutableStructValue;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_StructValue;

public record StructExpression(Map<String, Expression> fields, AccessExpression spread) implements Expression {

    @Override
    public String toString() {
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
    public void encode(ByteBuffer buffer) {
        buffer.writeCollection(this.fields.entrySet(), (entry, _) -> {
            buffer.writeString(entry.getKey());
            ExpressionCodec.write(entry.getValue(), buffer);
        });
        ExpressionCodec.writeUntypedNullable(this.spread, buffer);
    }

    @SuppressWarnings("unchecked")
    public static StructExpression decode(ByteBuffer buffer) throws IOException {
        return new StructExpression(
                Map.ofEntries(buffer.readCollection(_ -> Map.entry(
                        buffer.readString(),
                        ExpressionCodec.read(buffer))).toArray(Map.Entry[]::new)),
                ExpressionCodec.readUntypedNullable(ExpressionTypes.ACCESS, buffer));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        ClassDesc hashMap = ClassDesc.of("java.util.LinkedHashMap");
        cb.new_(CD_MutableStructValue);
        cb.dup();
        cb.new_(hashMap);
        cb.dup();
        if (spread == null) {
            cb.loadConstant((int)(fields.size() * 1.34));
            cb.invokespecial(hashMap, "<init>", MethodTypeDesc.of(CD_void, CD_int));
        } else {
            cb.invokespecial(hashMap, "<init>", MTD_void);
        }
        cb.invokespecial(CD_MutableStructValue, "<init>", MethodTypeDesc.of(CD_void, CD_Map));
        int structSlot = lc.getLowestUnused();
        cb.astore(structSlot);
        if (spread != null) {
            int iteratorSlot = lc.getLowestUnused();
            ClassDesc iterator = ClassDesc.of("java.util.Iterator");
            ClassDesc entry = ClassDesc.of("java.util.Map$Entry");
            cb.aload(1);
            spread.compile(cb, lc);
            cb.invokevirtual(CD_Evaluator, "getStructOrThrow", MethodTypeDesc.of(CD_StructValue, CD_Value));
            cb.invokeinterface(ClassDesc.of("java.lang.Iterable"), "iterator", MethodTypeDesc.of(iterator));
            cb.astore(iteratorSlot);
            Label loopLabel = cb.newLabel();
            Label endLoopLabel = cb.newLabel();
            cb.labelBinding(loopLabel);
            cb.aload(iteratorSlot);
            cb.invokeinterface(iterator, "hasNext", MethodTypeDesc.of(CD_boolean));
            cb.ifeq(endLoopLabel);
            cb.aload(structSlot);
            cb.aload(iteratorSlot);
            cb.invokeinterface(iterator, "next", MethodTypeDesc.of(CD_Object));
            cb.checkcast(entry);
            cb.dup();
            cb.invokeinterface(entry, "getValue", MethodTypeDesc.of(CD_Object));
            cb.checkcast(CD_Value);
            cb.swap();
            cb.invokeinterface(entry, "getKey", MethodTypeDesc.of(CD_Object));
            cb.checkcast(CD_String);
            cb.swap();
            cb.invokevirtual(CD_MutableStructValue, "set", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
            cb.goto_(loopLabel);
            cb.labelBinding(endLoopLabel);
            lc.free(iteratorSlot);
        }
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
        cb.aload(structSlot);
        lc.free(structSlot);
        return false;
    }
}
