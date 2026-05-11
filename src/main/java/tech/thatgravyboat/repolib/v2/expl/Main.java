package tech.thatgravyboat.repolib.v2.expl;

public class Main {

    public static void main(String[] args) {
        System.out.println(new Parser("""
                aa = [test, test2];
                """).parseExpression());
    }

}
