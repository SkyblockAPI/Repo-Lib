package tech.thatgravyboat.repolib.v2.jvm.compiler;

import tech.thatgravyboat.repolib.v2.expl.expression.*;
import tech.thatgravyboat.repolib.v2.jvm.IdentityLambdaFunctionValue;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;

public class ModuleCompiler {
    private static int index = 0;

    public static void compileIdentityExpression(CodeBuilder cb, LambdaIdentityFunction expression, CompilationTracker lc) {
        try {
            String lambdaKey = "" + index++;
            Class<IdentityLambdaFunctionValue> lambdaClass = compileLambda(expression.expression(), lc.getCodeName() + "$" + lambdaKey + "$special", true);
            cb.getstatic(lc.ownClass(), "lambdas", CD_List);
            cb.loadConstant(lc.addTrackedObject(lambdaClass));
            ClassDesc CD_IdentityLambdaFunction = ClassDesc.of("tech.thatgravyboat.repolib.v2.jvm.IdentityLambdaFunctionValue");
            cb.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int));
            cb.checkcast(CD_Class);
            cb.invokevirtual(CD_Class, "newInstance", MethodTypeDesc.of(CD_Object));
            cb.checkcast(CD_IdentityLambdaFunction);
            cb.swap();
            cb.invokeinterface(CD_IdentityLambdaFunction, "setSelf", MethodTypeDesc.of(CD_Value, CD_Value));
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void compileLambdaExpression(CodeBuilder cb, LambdaExpression expression, CompilationTracker lc) {
        try {
            String lambdaKey = "" + index++;
            Class<IdentityLambdaFunctionValue> lambdaClass = compileLambda(expression, lc.getCodeName() + "$" + lambdaKey, false);
            lc.loadTrackedObject(cb, lc.addTrackedObject(lambdaClass.getConstructor().newInstance()));
            cb.checkcast(CD_FunctionValue);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<IdentityLambdaFunctionValue> compileLambda(LambdaExpression expression, String name, boolean identity) throws IllegalAccessException, InstantiationException {
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

        ClassDesc lambdaClass = ClassDesc.of("tech.thatgravyboat.repolib.v2.jvm.compiler.generated$" + name + "$lambda");
        int finalMin = min;
        int finalMax = max;
        ClassDesc CD_LambdaFunctionValue = ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.value.LambdaFunctionValue");
        AtomicReference<List<Object>> lambdas = new AtomicReference<>();
        byte[] classBytes = ClassFile.of()
                .build(lambdaClass, (builder) -> {
                    builder.withSuperclass(CD_LambdaFunctionValue);
                    builder.withInterfaces(builder.constantPool()
                            .classEntry(ClassDesc.of("tech.thatgravyboat.repolib.v2.jvm.IdentityLambdaFunctionValue")));
                    if (identity) builder.withField("self", CD_Value, ClassFile.ACC_PRIVATE);
                    builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> {
                        methodBuilder.withCode(codeBuilder -> {

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
                        });
                    });
                    if (identity) {
                        builder.withMethod("setSelf", MethodTypeDesc.of(CD_Value, CD_Value), ClassFile.ACC_PUBLIC, methodBuilder -> {
                            methodBuilder.withCode(cb -> {
                                cb.aload(0);
                                cb.aload(1);
                                cb.putfield(lambdaClass, "self", CD_Value);
                                cb.aload(0);
                                cb.areturn();
                            });
                        });
                    }
                    builder.withMethod("canReturnValueBeReturned", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> {
                        mb.withCode(cb -> {
                            cb.iconst_1();
                            cb.ireturn();
                        });
                    });
                    builder.withMethod("apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_List), ClassFile.ACC_PUBLIC, mb -> {
                        mb.withCode(cb -> {
                            CompilationTracker lc = new CompilationTracker(lambdaClass, builder);
                            lc.setCodeName(name + "$lambda");
                            lc.mark(2);
                            cb.aload(1);
                            if (identity) {
                                cb.loadConstant("self");
                                cb.aload(0);
                                cb.getfield(lambdaClass, "self", CD_Value);
                                cb.invokevirtual(CD_Evaluator, "setField", MethodTypeDesc.of(CD_void, CD_String, CD_Value));
                                cb.aload(1);
                            }

                            lc.pushStack(cb, "lambda");

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

                            lambdas.set(lc.getTrackedObjects());
                        });
                    });
                    builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                });
        Class<?> lambdaClassClass =  MethodHandles.lookup()
                .defineHiddenClass(classBytes, true)
                .lookupClass();

        try {
            lambdaClassClass.getField("lambdas").set(null, lambdas.get());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        //noinspection unchecked
        return (Class<IdentityLambdaFunctionValue>) lambdaClassClass;
    }

    public static SelfEvaluatingExpression createSelfEvaluatingExpression(Expression expression, String name) {
        try {
            AtomicReference<List<Object>> lambdas = new AtomicReference<>();
            String cleanName = name.replaceAll("[\\\\/]", "#") + "$" + index++;
            ClassDesc classDesc = ClassDesc.of("tech.thatgravyboat.repolib.v2.jvm.compiler.generated$" + cleanName);
            byte[] classBytes = ClassFile.of()
                    .build(classDesc, (builder) -> {
                        builder.withInterfaces(builder.constantPool()
                                .classEntry(ClassDesc.of("tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression")));

                        builder.withMethod("<init>", MethodTypeDesc.of(CD_void), ClassFile.ACC_PUBLIC, methodBuilder -> {
                            methodBuilder.withCode(cb -> {
                                cb.aload(0);
                                cb.invokespecial(CD_Object, "<init>", MTD_void);
                                cb.return_();
                            });
                        });
                        builder.withField("lambdas", CD_List, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
                        builder.withMethod("canReturnValueBeReturned", MethodTypeDesc.of(CD_boolean), ClassFile.ACC_PUBLIC, mb -> {
                            mb.withCode(cb -> {
                                cb.iconst_1();
                                cb.ireturn();
                            });
                        });
                        builder.withMethod("evaluate", MethodTypeDesc.of(CD_Value, CD_Evaluator), ClassFile.ACC_PUBLIC, mb -> {
                            mb.withCode(cb -> {
                                CompilationTracker lc = new CompilationTracker(classDesc, builder);
                                lc.setCodeName(cleanName);
                                if (!expression.compile(cb, lc)) {
                                    lc.popAll(cb);
                                    cb.areturn();
                                }
                                lambdas.set(lc.getTrackedObjects());
                            });
                        });
                    });

            Class<?> sexClass = MethodHandles.lookup()
//                    .defineHiddenClass(classBytes, true)
//                    .lookupClass();
                    .defineClass(classBytes);

            try {
                sexClass.getField("lambdas").set(null, lambdas.get());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }

            return (SelfEvaluatingExpression) sexClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[" + name + "] ");
            System.exit(1);
            throw new RuntimeException(e);
        }
    }
}
