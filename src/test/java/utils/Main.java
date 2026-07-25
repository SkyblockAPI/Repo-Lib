package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import tech.thatgravyboat.repolib.v2.RepoConfig;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.value.ArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Main extends WebSocketServer {
    private List<WebSocket> connections = new ArrayList<>();
    public String lastModifiedItem = "items/aspect_of_the_void";

    boolean running = true;

    public static void main(String[] args) throws IOException {
        new Main();
    }

    private final RepoLoader loader = new RepoLoader(Path.of("Repo-Data").toRealPath().normalize().toAbsolutePath());

    public Main() throws IOException {
        super(new InetSocketAddress("0.0.0.0", 8008));
        start();
        reloadAndSend();
        new Thread(() -> {
            try {
                new WatchDir(loader.path, this).processEvents();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private int reloadCount = 0;

    public void reloadAndSend() throws IOException {
        var instance = loader.create();

        var errors = loader.load();
        System.out.printf("Reloading (%d %s)%n", reloadCount++, lastModifiedItem);
        errors.forEach(System.out::println);

        var data =
            JsonParser.parseString(Files.readString(Path.of("data.jsonc"), StandardCharsets.UTF_8)).getAsJsonObject();

        var stackFile = Objects.requireNonNull(loader.getStackFile(lastModifiedItem));
        var evaluator = stackFile.createEvaluator(instance.constants(), toValue(data), RepoConfig.DEFAULT, this.loader::getModule);
        var stack = stackFile.evaluateScript(evaluator);

        evaluator.errors.forEach(System.out::println);
        evaluator.debugs.forEach(System.out::println);

        var item = new JsonObject();
        item.add("minecraft:custom_name", asComponent(stack.get("name")));
        //noinspection RedundantCast
        item.add("minecraft:lore", asComponent((ArrayValue) stack.get("lore")).get("extra"));
        item.add("minecraft:custom_data", data);

        var itemStack = new JsonObject();
        itemStack.add("components", item);

        this.connections.forEach(webSocket -> {
            webSocket.send("importFromJson(`%s`)".formatted(itemStack.toString().replaceAll("`", "\\`")));
            webSocket.send("console.log(`%s`)".formatted(itemStack.toString().replaceAll("`", "\\`")));
        });
    }

    private StructValue toValue(JsonObject data) {
        return new MutableStructValue(data.entrySet()
            .stream()
            .map((entry) -> Map.entry(entry.getKey(), toValue(entry.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private ArrayValue toValue(JsonArray data) {
        return MutableArrayValue.create(data.asList().stream().map(this::toValue).toList());
    }

    private Value toValue(JsonElement element) {
        return switch (element) {
            case JsonObject object -> toValue(object);
            case JsonArray array -> toValue(array);
            case JsonPrimitive primitive when primitive.isBoolean() -> BoolValue.wrap(primitive.getAsBoolean());
            case JsonPrimitive primitive when primitive.isNumber() ->
                new NumValue(primitive.getAsNumber().doubleValue());
            case JsonPrimitive primitive when primitive.isString() -> new StrValue(primitive.getAsString());
            case JsonNull nil -> NilValue.NIL;
            default -> throw new NoSuchElementException("");
        };
    }


    private JsonElement toJson(Value value) {
        return switch (value) {
            case StructValue kv -> StreamSupport.stream(kv.spliterator(), false)
                .map((e) -> Map.entry(e.getKey(), toJson(e.getValue())))
                .collect(
                    JsonObject::new,
                    (obj, entry) -> obj.add(entry.getKey(), entry.getValue()),
                    (obj1, obj2) -> obj1.asMap().forEach(obj2::add));
            case ArrayValue array -> StreamSupport.stream(array.spliterator(), false)
                .map(this::toJson)
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
            case StrValue(String val) -> new JsonPrimitive(val);
            case NumValue(double val) -> new JsonPrimitive(val);
            case BoolValue bool -> new JsonPrimitive(bool.value());
            case NilValue nil -> JsonNull.INSTANCE;
            default -> throw new NoSuchElementException("");
        };
    }

    private JsonObject asComponent(Value value) {
        return switch (value) {
            case StrValue(String literal) -> {
                var obj = new JsonObject();
                obj.addProperty("text", literal);
                yield obj;
            }
            case ArrayValue array -> {
                var obj = new JsonObject();
                obj.add(
                    "extra",
                    StreamSupport.stream(array.spliterator(), false)
                        .map(this::asComponent)
                        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
                yield obj;
            }
            case StructValue val -> {
                if (val.isEmpty()) {
                    yield new JsonObject();
                }
                var obj = new JsonObject();

                color(val.get("color")).ifPresent(setString(obj, "color"));
                color(val.get("shadow_color")).ifPresent(setString(obj, "shadow_color"));
                getBool(val, "bold").ifPresent(setBoolean(obj, "bold"));
                getBool(val, "italic").or(() -> Optional.of(false)).ifPresent(setBoolean(obj, "italic"));
                getBool(val, "obfuscated").ifPresent(setBoolean(obj, "obfuscated"));
                getBool(val, "strikethrough").ifPresent(setBoolean(obj, "strikethrough"));
                getBool(val, "underlined").ifPresent(setBoolean(obj, "underlined"));
                getString(val, "font").ifPresent(setString(obj, "font"));
                getString(val, "text").ifPresent(setString(obj, "text"));

                if (val.get("extra") instanceof ArrayValue arr) {
                    var extra = new JsonArray();
                    arr.forEach(v -> extra.add(asComponent(v)));
                    obj.add("extra", extra);
                }

                yield obj;
            }
            default -> throw new NoSuchElementException("");
        };
    }

    private Optional<Boolean> getBool(StructValue val, String field) {
        return Optional.of(val.get(field)).filter(it -> it instanceof BoolValue).map(it -> ((BoolValue) it).value());
    }

    private Optional<String> getString(StructValue val, String field) {
        return Optional.of(val.get(field)).filter(it -> it instanceof StrValue).map(it -> ((StrValue) it).value());
    }

    private IntConsumer setInt(JsonObject obj, String field) {
        return (value) -> obj.addProperty(field, "#" + Integer.toString(value, 16));
    }

    private Consumer<Boolean> setBoolean(JsonObject obj, String field) {
        return (value) -> obj.addProperty(field, value);
    }

    private Consumer<String> setString(JsonObject obj, String field) {
        return (value) -> obj.addProperty(field, value);
    }

    Optional<String> BLACK = Optional.of("black");
    Optional<String> DARK_BLUE = Optional.of("dark_blue");
    Optional<String> DARK_GREEN = Optional.of("dark_green");
    Optional<String> DARK_AQUA = Optional.of("dark_aqua");
    Optional<String> DARK_RED = Optional.of("dark_red");
    Optional<String> DARK_PURPLE = Optional.of("dark_purple");
    Optional<String> GOLD = Optional.of("gold");
    Optional<String> GRAY = Optional.of("gray");
    Optional<String> DARK_GRAY = Optional.of("dark_gray");
    Optional<String> BLUE = Optional.of("blue");
    Optional<String> GREEN = Optional.of("green");
    Optional<String> AQUA = Optional.of("aqua");
    Optional<String> RED = Optional.of("red");
    Optional<String> LIGHT_PURPLE = Optional.of("light_purple");
    Optional<String> YELLOW = Optional.of("yellow");
    Optional<String> WHITE = Optional.of("white");

    private Optional<String> color(Value value) {
        if (!(value instanceof StrValue(String color))) {
            return Optional.empty();
        }

        return switch (color.toLowerCase(Locale.ROOT)) {
            case "black" -> BLACK;
            case "dark_blue" -> DARK_BLUE;
            case "dark_green" -> DARK_GREEN;
            case "cyan", "dark_aqua" -> DARK_AQUA;
            case "lime", "green" -> GREEN;
            case "dark_red" -> DARK_RED;
            case "dark_purple", "magenta" -> DARK_PURPLE;
            case "gold", "orange" -> GOLD;
            case "gray" -> GRAY;
            case "dark_gray" -> DARK_GRAY;
            case "blue" -> BLUE;
            case "aqua" -> AQUA;
            case "red" -> RED;
            case "light_purple", "pink" -> LIGHT_PURPLE;
            case "yellow" -> YELLOW;
            case "white" -> WHITE;
            default -> {
                if (color.startsWith("#")) {
                    yield Optional.of(color);
                }

                yield Optional.empty();
            }
        };
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("a client connected");

        conn.send("""
            if (!window.hasAlreadyBeenPipped) {window.hasAlreadyBeenPipped = true;const theCanvas = document.querySelector("canvas#tooltipCanvas");theCanvas.onclick = async function () {theCanvas.onclick = undefined;const pipWindow = await window.documentPictureInPicture.requestWindow({width: theCanvas.clientWidth,height: theCanvas.clientHeight,});[...document.styleSheets].forEach((styleSheet) => {try {const cssRules = [...styleSheet.cssRules].map((rule) => rule.cssText).join("");const style = document.createElement("style");style.textContent = cssRules;pipWindow.document.head.appendChild(style);} catch (e) {const link = document.createElement("link");link.rel = "stylesheet";link.type = styleSheet.type;link.media = styleSheet.media;link.href = styleSheet.href;pipWindow.document.head.appendChild(link);}});pipWindow.document.body.append(theCanvas);theCanvas.onclick=()=>{pipWindow.resizeTo(theCanvas.clientWidth + (pipWindow.window.outerWidth - pipWindow.window.innerWidth), theCanvas.clientHeight + (pipWindow.window.outerHeight - pipWindow.window.innerHeight))}}}
            """);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        conn.sendPing();
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        System.out.println(
            "  > (()=>{function c() {const ws = new WebSocket(\"ws://localhost:8008\");ws.onopen=()=>ws.send" +
            "(\"ping\");ws.onmessage=(m)=>eval(m.data);ws.onclose=(meow)=>setTimeout(c, 1000)};c()})() <");
    }

    @Override
    public void stop(int timeout, String closeMessage) throws InterruptedException {
        super.stop(timeout, closeMessage);
        running = false;
    }
}
