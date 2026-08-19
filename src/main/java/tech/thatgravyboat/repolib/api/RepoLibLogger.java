package tech.thatgravyboat.repolib.api;


import java.util.function.BiConsumer;

public interface RepoLibLogger {

    static void setInstance(RepoLibLogger instance) {
        Holder.INSTANCE = instance;
    }

    void info0(String message);

    void debug0(String message);

    void trace0(String message);

    void error0(String message);

    void warn0(String message);

    void error0(String message, Throwable throwable);

    private static void log(String message, BiConsumer<RepoLibLogger, String> consumer) {
        var instance = Holder.INSTANCE;
        if (instance == null) {
            return;
        }
        consumer.accept(instance, message);
    }

    static void info(String message) {
        log(message, RepoLibLogger::info0);
    }

    static void debug(String message) {
        log(message, RepoLibLogger::debug0);
    }

    static void trace(String message) {
        log(message, RepoLibLogger::trace0);
    }

    static void warn(String message) {
        log(message, RepoLibLogger::warn0);
    }

    static void error(String message) {
        log(message, RepoLibLogger::error0);
    }

    static void error(String message, Throwable throwable) {
        var instance = Holder.INSTANCE;
        if (instance == null) {
            return;
        }
        instance.error0(message, throwable);
    }

}

class Holder {
    static RepoLibLogger INSTANCE = null;
}
