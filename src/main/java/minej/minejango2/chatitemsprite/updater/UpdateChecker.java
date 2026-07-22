package minej.minejango2.chatitemsprite.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

public final class UpdateChecker {

    private static final String API_URL = "https://api.modrinth.com/v3/project/chatitemsprite/version";
    private static final Set<String> ALWAYS_SKIPPED_TYPES = Set.of("alpha");

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    private final ChatItemSpritePlugin plugin;

    public UpdateChecker(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
    }

    public @Nullable VersionInfo getLatestVersion() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("User-Agent", "ChatItemSprite/" + plugin.getPluginMeta().getVersion())
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode());
        }

        JsonArray versions;
        try {
            versions = JsonParser.parseString(response.body()).getAsJsonArray();
        } catch (RuntimeException e) {
            throw new IOException("Malformed response from Modrinth API", e);
        }

        if (versions.isEmpty()) {
            return null;
        }

        boolean notifyBeta = plugin.getConfig().getBoolean("update-check.notify-beta", false);

        // check all versions and find latest version
        VersionInfo best = null;

        for (JsonElement element : versions) {
            JsonObject version = element.getAsJsonObject();

            String type = getStringOrNull(version, "version_type");
            if (type == null || ALWAYS_SKIPPED_TYPES.contains(type)) {
                continue;
            }
            if ("beta".equals(type) && !notifyBeta) {
                continue;
            }

            String versionNumber = getStringOrNull(version, "version_number");
            String id = getStringOrNull(version, "id");
            if (versionNumber == null || id == null) {
                continue;
            }
            String changelog = getStringOrDefault(version, "changelog", "");

            VersionInfo candidate = new VersionInfo(
                    versionNumber,
                    type,
                    changelog,
                    "https://modrinth.com/plugin/chatitemsprite/version/" + id
            );

            if (best == null) {
                best = candidate;
                continue;
            }

            var cmp = VersionComparator.tryIsNewer(best.version(), candidate.version());
            if (cmp.isPresent() && cmp.get()) {
                best = candidate;
            }
        }

        return best;
    }

    @Nullable
    private static String getStringOrNull(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private static String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
        String value = getStringOrNull(obj, key);
        return value != null ? value : defaultValue;
    }
}
