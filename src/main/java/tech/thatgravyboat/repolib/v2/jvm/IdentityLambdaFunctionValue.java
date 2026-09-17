package tech.thatgravyboat.repolib.v2.jvm;

import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.UsedByCompiler;

@UsedByCompiler
public interface IdentityLambdaFunctionValue {
    @UsedByCompiler
    Value setSelf(Value self);
}
