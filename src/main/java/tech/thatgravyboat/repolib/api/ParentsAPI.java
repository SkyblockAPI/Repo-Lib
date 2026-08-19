package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import tech.thatgravyboat.repolib.internal.Utils;

public final class ParentsAPI {
    private final Map<String, List<String>> parentToChildren = new HashMap<>();
    private final Map<String, String> childToParent = new HashMap<>();

    void load(JsonElement json) {
        if (json instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                String parent = entry.getKey().toUpperCase(Locale.ROOT);
                List<String> children = new ArrayList<>();
                for (JsonElement childElement : entry.getValue().getAsJsonArray()) {
                    String child = childElement.getAsString().toUpperCase(Locale.ROOT);
                    children.add(child);
                    this.childToParent.put(child, parent);
                }
                this.parentToChildren.put(parent, children);
            }
        } else {
            RepoLibLogger.warn("/Parents/ Failed to load, expected JsonObject but got " + Utils.typeName(json));
        }
    }

    public record ParentId(@NotNull String id, @Nullable String suffix) {
        public static ParentId parse(@NotNull String raw) {
            int separatorIndex = raw.indexOf(';');
            if (separatorIndex != -1) {
                return new ParentId(raw.substring(0, separatorIndex), raw.substring(separatorIndex + 1));
            }
            return new ParentId(raw, null);
        }

        public String asString() {
            return suffix == null ? id : id + ";" + suffix;
        }

        public int getSuffixAsInt(int fallback) {
            if (suffix == null) return fallback;
            try {
                return Integer.parseInt(suffix);
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
    }

    public record Family(@NotNull String mainParent, @NotNull List<String> allChildren) {}

    public String formatId(String id, int level) {
        return id.toUpperCase(Locale.ROOT) + ";" + level;
    }

    public String formatId(String id, String suffix) {
        return id.toUpperCase(Locale.ROOT) + ";" + suffix.toUpperCase(Locale.ROOT);
    }

    public List<String> getChildren(String id) {
        return this.parentToChildren.getOrDefault(id.toUpperCase(Locale.ROOT), List.of());
    }

    public List<String> getChildren(String id, int level) {
        return getChildren(formatId(id, level));
    }

    public List<String> getChildren(String id, String suffix) {
        return getChildren(formatId(id, suffix));
    }

    public String getMainParent(String id) {
        String current = id.toUpperCase(Locale.ROOT);
        while (this.childToParent.containsKey(current)) {
            current = this.childToParent.get(current);
        }
        return current;
    }

    public String getMainParent(String id, int level) {
        return getMainParent(formatId(id, level));
    }

    public String getMainParent(String id, String suffix) {
        return getMainParent(formatId(id, suffix));
    }

    public List<String> getAllDescendants(String id) {
        String startId = id.toUpperCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(startId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> children = this.parentToChildren.getOrDefault(current, List.of());
            result.addAll(children);
            queue.addAll(children);
        }
        return result;
    }

    public Family getFamily(String id) {
        String mainParent = getMainParent(id);
        return new Family(mainParent, getAllDescendants(mainParent));
    }

    public Family getFamily(String id, int level) {
        return getFamily(formatId(id, level));
    }

    public Family getFamily(String id, String suffix) {
        return getFamily(formatId(id, suffix));
    }

    public Map<String, List<String>> parentToChildren() {
        return this.parentToChildren;
    }

    public Map<String, String> childToParent() {
        return this.childToParent;
    }
}