package tech.thatgravyboat.repolib.v2.api.components;

import com.google.gson.JsonElement;

public interface LoadingRepoDataComponent<Type> extends RepoDataComponent<Type> {

    Type load(JsonElement element);
    String name();

}
