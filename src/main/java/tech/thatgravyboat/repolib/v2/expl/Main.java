package tech.thatgravyboat.repolib.v2.expl;

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
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.utils.WatchDir;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Main extends WebSocketServer {
    private List<WebSocket> connections = new ArrayList<>();
    public String lastModifiedItem = "items/aspect_of_the_void";

    boolean running = true;

    public static void main(String[] args) throws IOException {
        RepoLoader loader = new RepoLoader(Path.of("Repo-Data").toRealPath().normalize().toAbsolutePath());
        var instance = loader.create();

        var errors = loader.load();
        errors.forEach(System.out::println);

        var data =
            JsonParser.parseString(Files.readString(Path.of("data.jsonc"), StandardCharsets.UTF_8)).getAsJsonObject();

        var stackFile = Objects.requireNonNull(loader.getStackFile("items/aspect_of_the_void"));

        long sum = 0;
        for (int i = 0; i < 1000; i++) {

            var evaluator = stackFile.createEvaluator(instance.constants(), ImmutableStructValue.EMPTY, RepoConfig.DEFAULT);
            long start = System.nanoTime();
            var stack = stackFile.evaluateScript(evaluator);
            sum += System.nanoTime() - start;
            evaluator.errors.forEach(System.out::println);
            evaluator.debugs.forEach(System.out::println);
        }

        System.out.println("Took " + (sum / 1000_000000.0) + "ms");

    }

    private static StructValue toValue(JsonObject data) {
        return new MutableStructValue(data.entrySet()
            .stream()
            .map((entry) -> Map.entry(entry.getKey(), toValue(entry.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static ArrayValue toValue(JsonArray data) {
        return MutableArrayValue.create(data.asList().stream().map(Main::toValue).toList());
    }

    private static Value toValue(JsonElement element) {
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


    private static JsonElement toJson(Value value) {
        return switch (value) {
            case StructValue kv -> StreamSupport.stream(kv.spliterator(), false)
                .map((e) -> Map.entry(e.getKey(), toJson(e.getValue())))
                .collect(
                    JsonObject::new,
                    (obj, entry) -> obj.add(entry.getKey(), entry.getValue()),
                    (obj1, obj2) -> obj1.asMap().forEach(obj2::add));
            case ArrayValue array -> StreamSupport.stream(array.spliterator(), false)
                .map(Main::toJson)
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
            case StrValue(String val) -> new JsonPrimitive(val);
            case NumValue(double val) -> new JsonPrimitive(val);
            case BoolValue bool -> new JsonPrimitive(bool.value());
            case NilValue nil -> JsonNull.INSTANCE;
            default -> throw new NoSuchElementException("");
        };
    }

    private static JsonObject asComponent(Value value) {
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
                        .map(Main::asComponent)
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

    private static Optional<Boolean> getBool(StructValue val, String field) {
        return Optional.of(val.get(field)).filter(it -> it instanceof BoolValue).map(it -> ((BoolValue) it).value());
    }

    private static Optional<String> getString(StructValue val, String field) {
        return Optional.of(val.get(field)).filter(it -> it instanceof StrValue).map(it -> ((StrValue) it).value());
    }

    private static IntConsumer setInt(JsonObject obj, String field) {
        return (value) -> obj.addProperty(field, "#" + Integer.toString(value, 16));
    }

    private static Consumer<Boolean> setBoolean(JsonObject obj, String field) {
        return (value) -> obj.addProperty(field, value);
    }

    private static Consumer<String> setString(JsonObject obj, String field) {
        return (value) -> obj.addProperty(field, value);
    }

    static Optional<String> BLACK = Optional.of("black");
    static Optional<String> DARK_BLUE = Optional.of("dark_blue");
    static Optional<String> DARK_GREEN = Optional.of("dark_green");
    static Optional<String> DARK_AQUA = Optional.of("dark_aqua");
    static Optional<String> DARK_RED = Optional.of("dark_red");
    static Optional<String> DARK_PURPLE = Optional.of("dark_purple");
    static Optional<String> GOLD = Optional.of("gold");
    static Optional<String> GRAY = Optional.of("gray");
    static Optional<String> DARK_GRAY = Optional.of("dark_gray");
    static Optional<String> BLUE = Optional.of("blue");
    static Optional<String> GREEN = Optional.of("green");
    static Optional<String> AQUA = Optional.of("aqua");
    static Optional<String> RED = Optional.of("red");
    static Optional<String> LIGHT_PURPLE = Optional.of("light_purple");
    static Optional<String> YELLOW = Optional.of("yellow");
    static Optional<String> WHITE = Optional.of("white");

    private static Optional<String> color(Value value) {
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
