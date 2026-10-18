package xyz.erupt.test.openapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Finishes the OpenAPI document smart-doc produced: smart-doc only knows plain request headers,
 * so the login token is turned into a proper security scheme here, and the info/servers blocks
 * get what smart-doc cannot read from the build. Runs from the erupt-test {@code openapi} profile:
 * {@code OpenApiSpec <smart-doc openapi.json> <target erupt.json> <erupt version>}.
 */
public class OpenApiSpec {

    private static final String TOKEN_HEADER = "token";

    private static final String TOKEN_PARAM = "_token";

    private static final String SCHEME_HEADER = "tokenHeader";

    private static final String SCHEME_PARAM = "tokenParam";

    /** Sidebar sections (x-tagGroups, honoured by Redoc and Scalar); every tag must belong to one. */
    private static final Map<String, List<String>> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put("Account", List.of("Session and user", "Multi-factor auth", "Open API token", "Permission and app settings"));
        GROUPS.put("Data", List.of("Page build", "Data query", "Data modify", "Tab rows", "Form view", "Page scope"));
        GROUPS.put("Widgets and files", List.of("Form components", "Files", "Import and export"));
        GROUPS.put("Views", List.of("Board view", "Calendar view", "Gantt view"));
        GROUPS.put("Platform", List.of("Template pages", "Developer tools", "Framework"));
    }

    private static final String DESCRIPTION = """
            REST API of the erupt framework: every endpoint the admin UI calls, usable by any client.

            **Authentication.** `POST /erupt-api/login` returns a token (`token` field when `pass` is true). \
            Server-to-server callers use `GET /erupt-api/open-api/create-token` with an appid and secret instead. \
            Send the token in the `token` request header. Endpoints opened by the browser or by an editor \
            (template pages, editor uploads) cannot set headers and take the same value as the `_token` query parameter. \
            An expired or missing token answers 401, a missing menu or erupt permission answers 403.

            **The `erupt` header.** Data, build, component, file and export endpoints are scoped to one `@Erupt` class. \
            The `erupt` header carries its simple class name and must match the `{erupt}` path segment, \
            or name the parent erupt when the path addresses a nested erupt (tab, sub-form, reference).

            **Errors.** Business errors come back as HTTP 500 with an `R` body: `success` is false, `message` holds \
            the text to show and `promptWay` tells the UI how to show it.
            """;

    public static void main(String[] args) throws IOException {
        Path source = Path.of(args[0]);
        Path target = Path.of(args[1]);
        String version = args[2];
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonObject doc = gson.fromJson(Files.readString(source, StandardCharsets.UTF_8), JsonObject.class);
        info(doc, version);
        servers(doc);
        securitySchemes(doc);
        for (Map.Entry<String, JsonElement> path : doc.getAsJsonObject("paths").entrySet()) {
            for (Map.Entry<String, JsonElement> operation : path.getValue().getAsJsonObject().entrySet()) {
                secure(operation.getValue().getAsJsonObject());
            }
        }
        orderTags(doc);
        groupTags(doc);
        Files.createDirectories(target.getParent());
        Files.writeString(target, gson.toJson(doc) + "\n", StandardCharsets.UTF_8);
    }

    private static void info(JsonObject doc, String version) {
        JsonObject info = doc.getAsJsonObject("info");
        info.addProperty("version", version);
        info.addProperty("description", DESCRIPTION);
        JsonObject license = new JsonObject();
        license.addProperty("name", "Apache-2.0");
        license.addProperty("url", "https://www.apache.org/licenses/LICENSE-2.0");
        info.add("license", license);
        JsonObject docs = new JsonObject();
        docs.addProperty("description", "Erupt documentation");
        docs.addProperty("url", "https://docs.erupt.xyz");
        doc.add("externalDocs", docs);
    }

    private static void servers(JsonObject doc) {
        JsonArray servers = new JsonArray();
        servers.add(server("https://demo.erupt.xyz", "Public demo"));
        servers.add(server("http://localhost:8080", "Local development"));
        doc.add("servers", servers);
    }

    private static JsonObject server(String url, String description) {
        JsonObject server = new JsonObject();
        server.addProperty("url", url);
        server.addProperty("description", description);
        return server;
    }

    private static void securitySchemes(JsonObject doc) {
        JsonObject schemes = new JsonObject();
        schemes.add(SCHEME_HEADER, apiKey("header", TOKEN_HEADER,
                "Login token from POST /erupt-api/login, or from /erupt-api/open-api/create-token for server-to-server calls."));
        schemes.add(SCHEME_PARAM, apiKey("query", TOKEN_PARAM,
                "The same login token as a query parameter, for endpoints opened by the browser or an editor that cannot set headers."));
        doc.getAsJsonObject("components").add("securitySchemes", schemes);
    }

    private static JsonObject apiKey(String in, String name, String description) {
        JsonObject scheme = new JsonObject();
        scheme.addProperty("type", "apiKey");
        scheme.addProperty("in", in);
        scheme.addProperty("name", name);
        scheme.addProperty("description", description);
        return scheme;
    }

    /**
     * The token smart-doc listed as a plain parameter becomes a security requirement, and the
     * two auth failures every secured endpoint shares are added next to the 200.
     */
    private static void secure(JsonObject operation) {
        List<String> schemes = new ArrayList<>();
        JsonArray parameters = operation.getAsJsonArray("parameters");
        if (null != parameters) {
            for (Iterator<JsonElement> it = parameters.iterator(); it.hasNext(); ) {
                JsonObject parameter = it.next().getAsJsonObject();
                String in = parameter.get("in").getAsString();
                String name = parameter.get("name").getAsString();
                if ("header".equals(in) && TOKEN_HEADER.equals(name)) {
                    schemes.add(SCHEME_HEADER);
                    it.remove();
                } else if ("query".equals(in) && TOKEN_PARAM.equals(name)) {
                    schemes.add(SCHEME_PARAM);
                    it.remove();
                }
            }
        }
        JsonArray security = new JsonArray();
        for (String scheme : schemes) {
            JsonObject requirement = new JsonObject();
            requirement.add(scheme, new JsonArray());
            security.add(requirement);
        }
        operation.add("security", security);
        if (schemes.isEmpty()) return;
        JsonObject responses = operation.getAsJsonObject("responses");
        responses.add("401", response("Token missing or expired"));
        responses.add("403", response("The session has no permission on this menu or erupt"));
    }

    /**
     * smart-doc sorts the paths by the controllers' {@code @order} but emits the tag list in hash
     * order, so the tags are re-sorted by their first appearance among the paths.
     */
    private static void orderTags(JsonObject doc) {
        List<String> seen = new ArrayList<>();
        for (Map.Entry<String, JsonElement> path : doc.getAsJsonObject("paths").entrySet()) {
            for (Map.Entry<String, JsonElement> operation : path.getValue().getAsJsonObject().entrySet()) {
                for (JsonElement tag : operation.getValue().getAsJsonObject().getAsJsonArray("tags")) {
                    if (!seen.contains(tag.getAsString())) seen.add(tag.getAsString());
                }
            }
        }
        List<JsonObject> tags = new ArrayList<>();
        for (JsonElement tag : doc.getAsJsonArray("tags")) tags.add(tag.getAsJsonObject());
        tags.sort((a, b) -> Integer.compare(seen.indexOf(a.get("name").getAsString()), seen.indexOf(b.get("name").getAsString())));
        JsonArray sorted = new JsonArray();
        tags.forEach(sorted::add);
        doc.add("tags", sorted);
    }

    private static void groupTags(JsonObject doc) {
        List<String> grouped = GROUPS.values().stream().flatMap(List::stream).toList();
        for (JsonElement tag : doc.getAsJsonArray("tags")) {
            String name = tag.getAsJsonObject().get("name").getAsString();
            if (!grouped.contains(name)) throw new IllegalStateException("tag '" + name + "' is not assigned to a group in " + OpenApiSpec.class.getSimpleName());
        }
        JsonArray groups = new JsonArray();
        GROUPS.forEach((name, tags) -> {
            JsonObject group = new JsonObject();
            group.addProperty("name", name);
            JsonArray members = new JsonArray();
            tags.forEach(members::add);
            group.add("tags", members);
            groups.add(group);
        });
        doc.add("x-tagGroups", groups);
    }

    private static JsonObject response(String description) {
        JsonObject response = new JsonObject();
        response.addProperty("description", description);
        return response;
    }
}
