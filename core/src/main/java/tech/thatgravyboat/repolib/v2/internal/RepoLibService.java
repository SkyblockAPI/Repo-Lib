package tech.thatgravyboat.repolib.v2.internal;

import java.util.concurrent.CompletableFuture;

public interface RepoLibService {

    CompletableFuture<Void> load(RepoLibLoadingContext settings);

}
