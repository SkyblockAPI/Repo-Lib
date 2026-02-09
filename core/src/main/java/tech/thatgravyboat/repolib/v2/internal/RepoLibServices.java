package tech.thatgravyboat.repolib.v2.internal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RepoLibServices {
    private final static Set<RepoLibService> services = ConcurrentHashMap.newKeySet();

    private RepoLibServices() {
    }

    public static void register(RepoLibService service) {
        services.add(service);
    }

    public static void save() {
        for (var service : services) {
            service.save();
        }
    }


}
