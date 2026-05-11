package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.builtin.Constants;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        var repoLoader = new RepoLoader(Path.of("repo"));
        var errors = repoLoader.load();
        for (var error : errors) {
            System.out.println("Failed to load file " + error.file() + " due to " + error.reason());
        }
        var file = repoLoader.getStackFile("items/basic_fishing_net");

        var constants = new RepoConstants(repoLoader);
        var evaluator = file.createEvaluator(
                constants,
                new Constants(builder -> {
                    builder.constant("rarity_upgrades", 1);
                    builder.constant("baseStatBoostPercentage", 50);
                })
        );
        var result = file.evaluateScript(evaluator);
        for (var error : evaluator.errors) {
            System.out.println("[e]: " + error);
        }
        for (var debug : evaluator.debugs) {
            System.out.println("[d]: " + debug);
        }

        System.out.println(result);


    }

}
