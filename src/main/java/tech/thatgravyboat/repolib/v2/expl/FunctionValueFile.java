package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.binary.TypedFile;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;

public interface FunctionValueFile<Self extends TypedFile<Self>> extends FunctionValue, TypedFile<Self> {
}
