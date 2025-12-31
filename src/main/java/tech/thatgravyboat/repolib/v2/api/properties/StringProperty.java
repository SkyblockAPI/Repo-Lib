package tech.thatgravyboat.repolib.v2.api.properties;

public record StringProperty(
        String name,
        String... values
) implements IdProperty<String> {
    @Override
    public String serialize(String value) {
        return "";
    }
}
