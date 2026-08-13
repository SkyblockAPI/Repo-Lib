package utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WatchDir {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final Path dir;
    private final Main main;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    WatchDir(Path dir, Main main) throws IOException {
        this.dir = dir;
        this.main = main;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();

        registerAll(dir);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        while (main.running) {
            WatchKey key;
            try {
                key = watcher.poll(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException x) {
                return;
            }
            if (key == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }


            Path dir = keys.get(key);
            if (dir == null) {
                System.out.println(key);
                System.err.println("WatchKey not recognized!! :C");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                var kind = event.kind();
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                var fileName = this.dir.relativize(child).toString();
                if (fileName.endsWith("srls")) {
                    main.lastModifiedItem = fileName.substring(0, fileName.lastIndexOf('.'));
                }

                if ((kind == StandardWatchEventKinds.ENTRY_CREATE)) {

                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readable
                    }
                }
            }

            try {
                main.reloadAndSend();
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}