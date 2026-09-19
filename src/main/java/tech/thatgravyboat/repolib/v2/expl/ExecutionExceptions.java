package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class ExecutionExceptions {

    public static final Return RETURN = new Return(Value.NIL);
    public static final Break BREAK = new Break();
    public static final Continue CONTINUE = new Continue();

    public static class Return extends RuntimeException {
        public Value retVal;
        public Return(Value retVal) {
            this.retVal = retVal;
        }
    }
    public static class Break extends RuntimeException {}
    public static class Continue extends RuntimeException {}
}
