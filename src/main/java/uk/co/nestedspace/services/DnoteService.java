package uk.co.nestedspace.services;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import uk.co.nestedspace.dao.NoteDAO;
import uk.co.nestedspace.dao.NotesResponseDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DnoteService {

    @Value("${dnote.email}")
    private String email;

    @Value("${dnote.password}")
    private String password;

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper; // 👈 Use Micronaut's JSON mapper

    @Inject
    public DnoteService(@Client("${server.address}") HttpClient httpClient, JsonMapper jsonMapper) {
        this.httpClient = httpClient;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Authenticate with Dnote API and get the authentication key.
     */
    public Single<String> authenticate() {
        System.out.println("Authenticating");
        AuthResponse cachedToken = loadAuthToken();
        if (cachedToken != null && (System.currentTimeMillis() / 1000) < cachedToken.getExpires_at()) {
            return Single.just(cachedToken.getKey());
        }

        AuthRequest authRequest = new AuthRequest(email, password);
        HttpRequest<AuthRequest> request = HttpRequest.POST("signin", authRequest)
                .header(HttpHeaders.CONTENT_TYPE, "application/json");

        return Single.fromPublisher(httpClient.exchange(request, AuthResponse.class))
                .map(response -> {
                    if (response.getBody().isPresent()) {
                        AuthResponse authResponse = response.getBody().get();
                        saveAuthToken(authResponse);
                        return authResponse.getKey();
                    } else {
                        return "Auth Response Body is empty!";
                    }
                })
                .onErrorReturn(throwable -> "Authentication Error:" + throwable.getMessage());
    }

    /**
     * Fetch notes from the Dnote API.
     */
    public Single<NotesResponseDAO> fetchNotes() {
        return authenticate().flatMap(this::fetchNotesWithToken);
    }

    private Single<NotesResponseDAO> fetchNotesWithToken(String authKey) {
        HttpRequest<?> request = HttpRequest.GET("notes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authKey);

        return Single.fromPublisher(httpClient.exchange(request, String.class))
                .map(response -> {
                    String rawJson = response.body();

                    try {
                        return jsonMapper.readValue(rawJson, NotesResponseDAO.class);
                    } catch (Exception e) {
                        return new NotesResponseDAO();
                    }
                })
                .onErrorResumeNext(throwable -> {
                    if (isAuthError(throwable)) {
                        invalidateCachedToken();
                        return authenticate().flatMap(this::fetchNotesWithToken);
                    }
                    return Single.error(throwable);
                })
                .onErrorReturn(throwable -> new NotesResponseDAO());
    }

    public Single<NoteDAO> fetchNoteByUUID(String uuid){
        return authenticate().flatMap(token -> fetchNoteByUUIDWithToken(token, uuid));
    }

    private Single<NoteDAO> fetchNoteByUUIDWithToken(String authKey, String uuid) {
        String url = "notes/" + uuid;
        HttpRequest<?> request = HttpRequest.GET(url)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody -> {
                    return jsonMapper.readValue(responseBody, NoteDAO.class);
                })
                .onErrorResumeNext(throwable -> {
                    if (isAuthError(throwable)) {
                        invalidateCachedToken();
                        return authenticate().flatMap(token -> fetchNoteByUUIDWithToken(token, uuid));
                    }
                    return Single.error(throwable);
                })
                .onErrorReturn(throwable -> new NoteDAO("", "Error fetching Note from API"));
    }

    public Disposable updateNoteByUUID(NoteDAO note) {
        String authKey = authenticate().blockingGet();

        System.out.println("Doing it");
        String url = "notes/" + note.getUuid();

        Map<String, Object> body = new HashMap<>();
        body.put("content", note.getContent());
        body.put("public", true);
        HttpRequest<?> request = HttpRequest.PATCH(url, body)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody -> jsonMapper.readValue(responseBody, NoteDAO.class))
                .subscribe();
    }

    private boolean isAuthError(Throwable throwable) {
        return throwable.getMessage().contains("401") || throwable.getMessage().toLowerCase().contains("unauthorized");
    }

    private void saveAuthToken(AuthResponse authResponse) {
        try {
            String json = jsonMapper.writeValueAsString(authResponse);
            Files.write(Paths.get("dnote_token.json"), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AuthResponse loadAuthToken() {
        try {
            Path path = Paths.get("dnote_token.json");
            if (Files.exists(path)) {
                String json = new String(Files.readAllBytes(path));
                return jsonMapper.readValue(json, AuthResponse.class);
            }
        } catch (IOException e) {
            e.getMessage();
        }
        return null;
    }

    private void invalidateCachedToken() {
        try {
            Files.deleteIfExists(Paths.get("dnote_token.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Models
     */
    @Serdeable
    public static class AuthRequest {
        private String email;
        private String password;

        public AuthRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Serdeable.Deserializable
    @Serdeable.Serializable
    public static class AuthResponse {
        private String key;
        private long expires_at;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getExpires_at() {
            return expires_at;
        }

        public void setExpires_at(long expires_at) {
            this.expires_at = expires_at;
        }
    }

    @Serdeable
    public static class UpdateRequest {
        private String content;

        public UpdateRequest(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}

