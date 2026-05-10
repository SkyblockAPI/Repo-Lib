package tech.thatgravyboat.repolib.v2;

public class Main {

    public static void main(String[] args) {
        var expression = Expression.parseOrThrow("""
        meta {
//            rarity = "EPIC";
            breaking_power = 8;
            name = "Pickonimbus 2000";
            category = "Pickaxe";
            reforgable = true;
            coin_value = 20;
        
            stats.damage = 30;
            stats.mining_speed = 750;
        }
        
        script {
            stack.item = "minecraft:diamond_pickaxe";
            if (meta.rarity) {
                stack.rarity = meta.rarity;
            };
        }
        """);

        System.out.println(expression.evaluate(Value.KeyValue.ImmutableStruct.EMPTY));
    }
}
