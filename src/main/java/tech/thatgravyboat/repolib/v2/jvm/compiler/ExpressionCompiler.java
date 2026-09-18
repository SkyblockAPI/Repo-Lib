package tech.thatgravyboat.repolib.v2.jvm.compiler;

import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.ModuleFile;
import tech.thatgravyboat.repolib.v2.expl.StackFile;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.LambdaExpression;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;

import java.lang.classfile.ClassFile;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;

@SuppressWarnings("preview")
public class ExpressionCompiler {
    private static final MethodHandles.Lookup theLookup;

    static {
        try {
            theLookup = MethodHandles.privateLookupIn(RepoLoader.class, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static BiConsumer<String, byte[]> saveFunction = null;

    private static void maybeSave(String name, byte[] clazzBytes) {
        if (saveFunction == null) return;
        saveFunction.accept(name, clazzBytes);
    }

    public static void registerSaver(BiConsumer<String, byte[]> saveFunction) {
        ExpressionCompiler.saveFunction = saveFunction;
    }

    private static int index = 0;

    public static Class<?> compileLambda(LambdaExpression expression, String name, boolean identity) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        Collection<LambdaExpression.LambdaArgument> arguments = expression.arguments();
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

        ClassDesc lambdaClass = ClassDesc.of("tech.thatgravyboat.repolib.v2.lambda" + index++);
        int finalMin = min;
        int finalMax = max;
        ClassDesc CD_LambdaFunctionValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.LambdaFunctionValue");
        List<Object> lambdas = new ArrayList<>();
        byte[] classBytes = ClassFile.of()
                .build(lambdaClass, (builder) -> {
                    builder.withSuperclass(CD_LambdaFunctionValue);
                    builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> methodBuilder.withCode(codeBuilder -> {

                        codeBuilder.aload(0);

                        codeBuilder.aconst_null();

                        codeBuilder.loadConstant(finalMin == finalMax ? 0 : 1);
                        codeBuilder.loadConstant(finalMin);
                        codeBuilder.loadConstant(finalMax);

                        codeBuilder.invokespecial(CD_LambdaFunctionValue, "<init>", MethodTypeDesc.of(
                                CD_void,
                                ClassDesc.of("java.util.function.BiFunction"),
                                CD_boolean,
                                CD_int,
                                CD_int
                        ));
                        if (identity) {
                            codeBuilder.aload(0);
                            codeBuilder.aconst_null();
                            codeBuilder.putfield(lambdaClass, "self", CD_Value);
                        }
                        codeBuilder.return_();
                    }));
                    if (identity) {
                        builder.withInterfaces(builder.constantPool()
                                .classEntry(ClassDesc.of("tech.thatgravyboat.repolib.v2.jvm.IdentityLambdaFunctionValue")));
                        builder.withField("self", CD_Value, ClassFile.ACC_PRIVATE);
                        builder.withMethod("setSelf", MethodTypeDesc.of(CD_Value, CD_Value), ClassFile.ACC_PUBLIC, methodBuilder -> methodBuilder.withCode(cb -> {
                            cb.aload(0);
                            cb.aload(1);
                            cb.putfield(lambdaClass, "self", CD_Value);
                            cb.aload(0);
                            cb.areturn();
                        }));
                    }
                    builder.withMethod("canReturnValueBeReturned", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                        cb.iconst_1();
                        cb.ireturn();
                    }));
                    builder.withMethod("apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_List), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                        CompilationTracker lc = new CompilationTracker(lambdaClass, builder, lambdas);
                        lc.setCodeName(name + "$lambda");
                        lc.mark(2);
                        cb.aload(1);

                        lc.pushStack(cb, "lambda");

                        if (identity) {
                            cb.loadConstant("self");
                            cb.aload(0);
                            cb.getfield(lambdaClass, "self", CD_Value);
                            cb.invokevirtual(CD_Evaluator, "setField", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                            cb.aload(1);
                        }

                        cb.aload(2);
                        cb.invokeinterface(CD_List, "size", MethodTypeDesc.of(CD_int));
                        int lengthLocal = lc.getLowestUnused();
                        cb.istore(lengthLocal);
                        Label correctArgLabel = cb.newLabel();
                        if (finalMin == finalMax) {
                            cb.iload(lengthLocal);
                            cb.loadConstant(finalMin);
                            cb.if_icmpeq(correctArgLabel);
                            Snippets.throwPanic(cb, "Invalid Arg Count!");
                        } else {
                            Label throwLabel = cb.newLabel();
                            cb.iload(lengthLocal);
                            cb.loadConstant(finalMin);
                            cb.if_icmplt(throwLabel);
                            cb.iload(lengthLocal);
                            cb.loadConstant(finalMax);
                            cb.if_icmple(correctArgLabel);
                            cb.labelBinding(throwLabel);
                            Snippets.throwPanic(cb, "Invalid Arg Count!");
                        }
                        cb.labelBinding(correctArgLabel);

                        Label ranOutOfArgument = cb.newLabel();

                        for (LambdaExpression.LambdaArgument argument : arguments) {
                            if (argument.position() >= finalMin) {
                                cb.iload(lengthLocal);
                                cb.loadConstant(argument.position());
                                cb.if_icmple(ranOutOfArgument);
                            }
                            cb.aload(1);
                            cb.loadConstant(argument.name());
                            cb.aload(2);
                            cb.loadConstant(argument.position());
                            cb.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int));
                            cb.invokevirtual(CD_Evaluator, "setField", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                        }
                        cb.labelBinding(ranOutOfArgument);

                        if (!expression.body().compile(cb, lc)) {
                            lc.popAll(cb);
                            cb.areturn();
                        }
                    }));
                    builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                });
        maybeSave(name, classBytes);

        Class<?> lambdaClassClass = theLookup
                .defineHiddenClass(classBytes, true)
                .lookupClass();

        lambdaClassClass.getField("lambdas").set(null, lambdas);

        return lambdaClassClass;
    }

    public static ModuleFile compileModule(ModuleFile.Impl uncompiled) {
        try {
            List<Object> lambdas = new ArrayList<>();
            ClassDesc classDesc = ClassDesc.of("tech.thatgravyboat.repolib.v2.module$" + uncompiled.name().replaceAll("[.\\\\/]", "\\$") + "$" + index++);
            byte[] classBytes = ClassFile.of()
                    .build(classDesc, (builder) -> {
                        builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                        builder.withInterfaces(builder.constantPool()
                                .classEntry(CD_ModuleFile));

                        builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> methodBuilder.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            cb.aload(0);
                            cb.invokespecial(CD_Object, "<init>", MTD_void);
                            cb.aload(0);
                            cb.loadConstant(uncompiled.isInitialized() ? 1 : 0);
                            cb.putfield(classDesc, "hasInitialized", CD_boolean);
                            cb.return_();
                            if (uncompiled.isInitialized()) {
                                cb.aload(0);
                                lc.loadTrackedObject(cb, lc.addTrackedObject(uncompiled.staticData()));
                                cb.putfield(classDesc, "staticData", CD_KeyValue);
                            }
                        }));

                        // so that it can be bundled still :3
                        builder.withMethod("staticDataExpression", MethodTypeDesc.of(CD_Expression), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            lc.loadTrackedObject(cb, lc.addTrackedObject(uncompiled.staticDataExpression()));
                            cb.areturn();
                        }));
                        builder.withMethod("script", MethodTypeDesc.of(CD_Expression), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            lc.loadTrackedObject(cb, lc.addTrackedObject(uncompiled.script()));
                            cb.areturn();
                        }));

                        builder.withField("hasInitialized", CD_boolean, ClassFile.ACC_PRIVATE);
                        builder.withField("staticData", CD_KeyValue, ClassFile.ACC_PRIVATE);

                        builder.withMethod("initialize", MethodTypeDesc.of(CD_void, CD_Evaluator), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            if (uncompiled.staticDataExpression() != null) {
                                cb.aload(0);
                                cb.getfield(classDesc, "hasInitialized", CD_boolean);
                                Label endLabel = cb.newLabel();
                                cb.ifeq(endLabel);
                                cb.return_();
                                cb.labelBinding(endLabel);
                                CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                                lc.pushStack(cb, "static");
                                cb.aload(0);
                                if (!uncompiled.staticDataExpression().compile(cb, lc)) {
                                    lc.popAll(cb);
                                    Label alreadyImmutable = cb.newLabel();
                                    cb.dup();
                                    cb.instanceOf(CD_MutableKV);
                                    cb.ifeq(alreadyImmutable);
                                    cb.invokeinterface(CD_MutableKV, "toFullyImmutable", MethodTypeDesc.of(CD_KeyValue));
                                    cb.labelBinding(alreadyImmutable);
                                    cb.putfield(classDesc, "staticData", CD_KeyValue);
                                    cb.aload(0);
                                    cb.loadConstant(1);
                                    cb.putfield(classDesc, "hasInitialized", CD_boolean);
                                }
                            }
                            cb.return_();
                        }));

                        builder.withMethod("name", MethodTypeDesc.of(CD_String), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.loadConstant(uncompiled.name());
                            cb.areturn();
                        }));


                        builder.withMethod("staticData", MethodTypeDesc.of(CD_void, CD_KeyValue), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.aload(0);
                            cb.dup();
                            cb.aload(1);
                            cb.putfield(classDesc, "staticData", CD_KeyValue);
                            cb.loadConstant(1);
                            cb.putfield(classDesc, "hasInitialized", CD_boolean);
                            cb.return_();
                        }));

                        builder.withMethod("staticData", MethodTypeDesc.of(CD_KeyValue), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.aload(0);
                            cb.getfield(classDesc, "staticData", CD_KeyValue);
                            cb.areturn();
                        }));

                        builder.withMethod("isInitialized", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.aload(0);
                            cb.getfield(classDesc, "hasInitialized", CD_boolean);
                            cb.ireturn();
                        }));

                        builder.withMethod("apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_List), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.aload(2);
                            cb.invokeinterface(CD_List, "size", MethodTypeDesc.of(CD_int));
                            cb.iconst_1();
                            Label notOne = cb.newLabel();
                            Label main = cb.newLabel();
                            cb.if_icmpne(notOne);
                            cb.aload(1);
                            cb.aload(2);
                            cb.invokeinterface(CD_List, "getFirst", MethodTypeDesc.of(CD_Object));
                            cb.checkcast(CD_StructValueMutableStruct);
                            cb.loadConstant(uncompiled.name());
                            cb.invokevirtual(CD_Evaluator, "push", MethodTypeDesc.of(CD_void, CD_StructValueMutableStruct, CD_String));
                            cb.goto_(main);
                            cb.labelBinding(notOne);
                            cb.aload(1);
                            cb.loadConstant(uncompiled.name());
                            cb.invokevirtual(CD_Evaluator, "push", MethodTypeDesc.of(CD_void, CD_String));
                            cb.labelBinding(main);
                            cb.aload(0);
                            cb.aload(1);
                            cb.invokevirtual(classDesc, "evaluate", MethodTypeDesc.of(CD_Value, CD_Evaluator));
                            cb.aload(1);
                            cb.invokevirtual(CD_Evaluator, "pop", MTD_void);
                            cb.areturn();
                        }));

                        builder.withMethod("evaluate", MethodTypeDesc.of(CD_Value, CD_Evaluator), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            lc.setCodeName(uncompiled.name());
                            cb.aload(1);
                            cb.loadConstant("static_data");
                            cb.aload(0);
                            cb.getfield(classDesc, "staticData", CD_KeyValue);
                            cb.invokevirtual(CD_Evaluator, "setInOverlay", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                            if (!uncompiled.script().compile(cb, lc)) {
                                lc.popAll(cb);
                                cb.areturn();
                            }
                        }));
                    });

            maybeSave(uncompiled.name(), classBytes);

            Class<?> sexClass = theLookup
                    .defineHiddenClass(classBytes, true)
                    .lookupClass();

            sexClass.getField("lambdas").set(null, lambdas);


            return (ModuleFile) sexClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static StackFile compileStack(StackFile.Impl uncompiled) {
        try {
            List<Object> lambdas = new ArrayList<>();
            ClassDesc classDesc = ClassDesc.of("tech.thatgravyboat.repolib.v2.stack$" + uncompiled.name().replaceAll("[.\\\\/]", "\\$") + "$" + index++);
            byte[] classBytes = ClassFile.of()
                    .build(classDesc, (builder) -> {
                        builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                        builder.withInterfaces(builder.constantPool()
                                .classEntry(CD_StackFile));

                        builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> methodBuilder.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            cb.aload(0);
                            cb.dup();
                            cb.dup();
                            cb.invokespecial(CD_Object, "<init>", MTD_void);
                            lc.loadTrackedObject(cb, lc.addTrackedObject(uncompiled.meta()));
                            cb.putfield(classDesc, "meta", CD_KeyValue);
                            cb.loadConstant(uncompiled.hasInitialized() ? 1 : 0);
                            cb.putfield(classDesc, "hasInitialized", CD_boolean);
                            cb.return_();
                        }));

                        // so that it can be bundled still :3
                        builder.withMethod("metaScript", MethodTypeDesc.of(CD_Expression), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            lc.loadTrackedObject(cb, lc.addTrackedObject(uncompiled.metaScript()));
                            cb.areturn();
                        }));
                        builder.withMethod("script", MethodTypeDesc.of(CD_Expression), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            lc.loadTrackedObject(cb, lc.addTrackedObject(uncompiled.script()));
                            cb.areturn();
                        }));

                        builder.withField("hasInitialized", CD_boolean, ClassFile.ACC_PRIVATE);
                        builder.withField("meta", CD_KeyValue, ClassFile.ACC_PRIVATE);

                        builder.withMethod("evaluateMetaScript", MethodTypeDesc.of(CD_void, CD_Evaluator), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            cb.aload(0);
                            if (!uncompiled.metaScript().compile(cb, lc)) {
                                lc.popAll(cb);
                                cb.pop();
                            }
                            cb.return_();
                        }));

                        builder.withMethod("name", MethodTypeDesc.of(CD_String), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.loadConstant(uncompiled.name());
                            cb.areturn();
                        }));


                        builder.withMethod("meta", MethodTypeDesc.of(CD_void, CD_KeyValue), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.aload(0);
                            cb.dup();
                            cb.aload(1);
                            cb.putfield(classDesc, "meta", CD_KeyValue);
                            cb.loadConstant(1);
                            cb.putfield(classDesc, "hasInitialized", CD_boolean);
                            cb.return_();
                        }));

                        builder.withMethod("meta", MethodTypeDesc.of(CD_KeyValue), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.aload(0);
                            cb.getfield(classDesc, "meta", CD_KeyValue);
                            cb.areturn();
                        }));

                        builder.withMethod("hasInitialized", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.aload(0);
                            cb.getfield(classDesc, "hasInitialized", CD_boolean);
                            cb.ireturn();
                        }));


                        builder.withMethod("evaluateScript", MethodTypeDesc.of(CD_StructValue, CD_Evaluator), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            lc.setCodeName(uncompiled.name());
                            if (!uncompiled.script().compile(cb, lc)) {
                                lc.popAll(cb);
                                cb.pop();

                                cb.aload(1);
                                cb.getfield(CD_Evaluator, "defaults", CD_KeyValue);
                                cb.loadConstant("stack");
                                cb.invokeinterface(CD_KeyValue, "get", MethodTypeDesc.of(CD_Value, CD_String));
                                Label notStruct = cb.newLabel();
                                cb.dup();
                                cb.instanceOf(CD_StructValue);
                                cb.ifeq(notStruct);
                                cb.areturn();
                                cb.labelBinding(notStruct);
                                cb.pop();
                                ClassDesc CD_ImmutableStructValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue");
                                cb.getstatic(CD_ImmutableStructValue, "EMPTY", CD_ImmutableStructValue);
                                cb.areturn();
                            }
                        }));
                    });

            maybeSave(uncompiled.name(), classBytes);

            Class<?> sexClass = theLookup
                    .defineHiddenClass(classBytes, true)
                    .lookupClass();

            sexClass.getField("lambdas").set(null, lambdas);


            return (StackFile) sexClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SelfEvaluatingExpression compileExpression(Expression expression, String name) {
        try {
            List<Object> lambdas = new ArrayList<>();
            ClassDesc classDesc = ClassDesc.of("tech.thatgravyboat.repolib.v2.generated" + index++);
            byte[] classBytes = ClassFile.of()
                    .build(classDesc, (builder) -> {
                        builder.withInterfaces(builder.constantPool()
                                .classEntry(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression")));

                        builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> methodBuilder.withCode(cb -> {
                            cb.aload(0);
                            cb.invokespecial(CD_Object, "<init>", MTD_void);
                            cb.return_();
                        }));
                        builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                        builder.withMethod("canReturnValueBeReturned", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            cb.iconst_1();
                            cb.ireturn();
                        }));
                        builder.withMethod("evaluate", MethodTypeDesc.of(CD_Value, CD_Evaluator), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(classDesc, builder, lambdas);
                            lc.setCodeName(name);
                            if (!expression.compile(cb, lc)) {
                                lc.popAll(cb);
                                cb.areturn();
                            }
                        }));
                    });

            maybeSave(name, classBytes);

            Class<?> sexClass = theLookup
                    .defineHiddenClass(classBytes, true)
                    .lookupClass();

            sexClass.getField("lambdas").set(null, lambdas);


            return (SelfEvaluatingExpression) sexClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
