package tech.thatgravyboat.repolib.api.idoverlays;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.internal.JsonHelper;
import tech.thatgravyboat.repolib.internal.Utils;

public interface Requirement {
    String type();

    @Nullable String rawString();

    String formattedString();

    static Requirement parse(JsonObject json) {
        String type = JsonHelper.getStringOrNull(json, "type");
        if (type == null) type = "unknown";

        return switch (type) {
            case "slayer" -> Slayer.fromJson(json);
            case "hotm" -> Hotm.fromJson(json);
            case "hotf" -> Hotf.fromJson(json);
            case "bossCollection" -> BossCollection.fromJson(json);
            case "skill" -> Skill.fromJson(json);
            case "collection" -> Collection.fromJson(json);
            default -> Unknown.fromJson(json);
        };
    }

    record Slayer(String name, int level, @Nullable String rawString) implements Requirement {
        @Override
        public String type() {
            return "slayer";
        }

        @Override
        public String formattedString() {
            return rawString != null ? rawString : Utils.toTitleCase(name) + " Slayer " + level;
        }

        static Slayer fromJson(JsonObject json) {
            return new Slayer(
                    JsonHelper.getStringOrNull(json, "name"),
                    JsonHelper.getInt(json, "level", 0),
                    JsonHelper.getStringOrNull(json, "rawString")
            );
        }
    }

    record Hotm(int level, @Nullable String rawString) implements Requirement {
        @Override
        public String type() {
            return "hotm";
        }

        @Override
        public String formattedString() {
            return rawString != null ? rawString : "Heart of the Mountain " + level;
        }

        static Hotm fromJson(JsonObject json) {
            return new Hotm(
                    JsonHelper.getInt(json, "level", 0),
                    JsonHelper.getStringOrNull(json, "rawString")
            );
        }
    }

    record Hotf(int level, @Nullable String rawString) implements Requirement {
        @Override
        public String type() {
            return "hotf";
        }

        @Override
        public String formattedString() {
            return rawString != null ? rawString : "Heart of the Forest " + level;
        }

        static Hotf fromJson(JsonObject json) {
            return new Hotf(
                    JsonHelper.getInt(json, "level", 0),
                    JsonHelper.getStringOrNull(json, "rawString")
            );
        }
    }

    record BossCollection(
            String name,
            int level,
            @Nullable String rawString
    ) implements Requirement {
        @Override
        public String type() {
            return "bossCollection";
        }

        @Override
        public String formattedString() {
            return rawString != null ? rawString : Utils.toTitleCase(name) + " Collection " + level;
        }

        static BossCollection fromJson(JsonObject json) {
            return new BossCollection(
                    JsonHelper.getStringOrNull(json, "name"),
                    JsonHelper.getInt(json, "level", 0),
                    JsonHelper.getStringOrNull(json, "rawString")
            );
        }
    }

    record Skill(
            String name,
            int level,
            @Nullable String rawString
    ) implements Requirement {
        @Override
        public String type() {
            return "skill";
        }

        @Override
        public String formattedString() {
            return rawString != null ? rawString : Utils.toTitleCase(name) + " " + level;
        }

        static Skill fromJson(JsonObject json) {
            return new Skill(
                    JsonHelper.getStringOrNull(json, "name"),
                    JsonHelper.getInt(json, "level", 0),
                    JsonHelper.getStringOrNull(json, "rawString")
            );
        }
    }

    record Collection(
            String name,
            int level,
            String id,
            @Nullable String rawString
    ) implements Requirement {
        @Override
        public String type() {
            return "collection";
        }

        @Override
        public String formattedString() {
            return rawString != null ? rawString : Utils.toTitleCase(name) + " Collection " + level;
        }

        static Collection fromJson(JsonObject json) {
            return new Collection(
                    JsonHelper.getStringOrNull(json, "name"),
                    JsonHelper.getInt(json, "level", 0),
                    JsonHelper.getStringOrNull(json, "id"),
                    JsonHelper.getStringOrNull(json, "rawString")
            );
        }
    }

    record Unknown(
            @Nullable String name,
            @Nullable Integer level,
            @Nullable String rawString
    ) implements Requirement {
        @Override
        public String type() {
            return "unknown";
        }

        @Override
        public String formattedString() {
            if (rawString != null) return rawString;

            String base = name != null ? Utils.toTitleCase(name) : "Unknown";
            return level != null ? base + " " + level : base;
        }

        static Unknown fromJson(JsonObject json) {
            return new Unknown(
                    JsonHelper.getStringOrNull(json, "name"),
                    json.has("level") && !json.get("level").isJsonNull() ? json.get("level").getAsInt() : null,
                    JsonHelper.getStringOrNull(json, "rawString")
            );
        }
    }
}