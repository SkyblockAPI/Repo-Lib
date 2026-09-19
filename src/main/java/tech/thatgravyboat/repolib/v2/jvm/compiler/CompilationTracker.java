package tech.thatgravyboat.repolib.v2.jvm.compiler;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.constant.ConstantDescs.*;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_int;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;

public  class CompilationTracker {
    private final Set<Integer> usedLocals = new HashSet<>();
    private final List<Object> trackedObjects;

    CompilationTracker(ClassDesc ownClass, ClassBuilder ownBuilder, List<Object> trackedObjects) {
        usedLocals.add(0);
        usedLocals.add(1);
        this.ownClass = ownClass;
        this.ownBuilder = ownBuilder;
        this.trackedObjects = trackedObjects;
    }

    public int addTrackedObject(Object functionValue) {
        trackedObjects.add(functionValue);
        return trackedObjects.size() - 1;
    }

    public void loadTrackedObject(CodeBuilder cb, int index) {
        cb.getstatic(this.ownClass, "lambdas", CD_List);
        cb.loadConstant(index);
        cb.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int));
    }

    public int getLowestUnused() {
        for (int i = 0; true; i++) {
            if (!usedLocals.contains(i)) {
                usedLocals.add(i);
                return i;
            }
        }
    }

    int depth = 0;

    public void popStack(CodeBuilder cb) {
        depth -= 1;
        cb.aload(1);
        cb.invokevirtual(CD_Evaluator, "pop", MethodTypeDesc.of(CD_void));
    }

    public void popAll(CodeBuilder cb) {
        for (; depth > 0; depth--) {
            cb.aload(1);
            cb.invokevirtual(CD_Evaluator, "pop", MethodTypeDesc.of(CD_void));
        }
    }

    public void pushStack(CodeBuilder cb, String name) {
        depth += 1;
        cb.aload(1);
        cb.loadConstant(name);
        cb.invokevirtual(CD_Evaluator, "push", MethodTypeDesc.of(CD_void, CD_String));
    }

    public void mark(int index) {
        usedLocals.add(index);
    }

    public void free(int index) {
        usedLocals.remove(index);
    }

    private String codeName = "";

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getCodeName() {
        return codeName;
    }

    private Label breakLabel = null;
    private Label continueLabel = null;

    public void setBreakLabel(Label label) {
        breakLabel = label;
    }

    public void setContinueLabel(Label label) {
        continueLabel = label;
    }

    public Label getBreakLabel() {
        return breakLabel;
    }

    public Label getContinueLabel() {
        return continueLabel;
    }

    private final ClassDesc ownClass;

    public ClassDesc ownClass() {
        return ownClass;
    }

    private final ClassBuilder ownBuilder;

    public ClassBuilder ownBuilder() {
        return ownBuilder;
    }

    private int id = 0;

    public int uniqueId() {
        return ++id;
    }
}