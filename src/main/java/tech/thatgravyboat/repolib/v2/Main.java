package tech.thatgravyboat.repolib.v2;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var struct = new Value.Struct();
        struct.set("print", (Value.Function) args1 -> {
            System.out.println(args1);
            return Value.NIL;
        });

        var evaluator = new Evaluator(struct);
        var expr = new Parser("""
                rarity = "EPIC";
                breaking_power = 8;
                name = "Pickonimbus 2000";
                category = "Pickaxe";
                reforgable = true;
                coin_value = 20;
            
                stats.damage = 30;
                stats.mining_speed = 750;
                
                if (reforgable) {
                    print("mrow");
                };
                """).parse();

        evaluator.evaluate(expr);

        System.out.println(struct);
    }
}
