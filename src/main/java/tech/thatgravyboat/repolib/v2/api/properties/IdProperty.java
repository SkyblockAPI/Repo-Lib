package tech.thatgravyboat.repolib.v2.api.properties;

public interface IdProperty<Type> {

    Type[] values();

    String name();
    String serialize(Type value);
}
