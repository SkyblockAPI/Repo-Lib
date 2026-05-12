package tech.thatgravyboat.repolib.v2.expl;

public class ExecutionExceptions {

    public static final Return RETURN = new Return();
    public static final Break BREAK = new Break();
    public static final Continue CONTINUE = new Continue();

    public static class Return extends RuntimeException {}
    public static class Break extends RuntimeException {}
    public static class Continue extends RuntimeException {}
}
