package uk.co.nestedspace.services;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import uk.co.nestedspace.dao.*;
import uk.co.nestedspace.models.Note;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class DnoteService {

    @Value("${dnote.email}")
    private String email;

    @Value("${dnote.password}")
    private String password;

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;

    @Inject
    public DnoteService(@Client("${server.address}") HttpClient httpClient, JsonMapper jsonMapper) {
        this.httpClient = httpClient;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Authenticate with Dnote API and get the authentication key.
     */
    public Single<String> authenticate() {
        AuthResponse cachedToken = loadAuthToken();
        if (cachedToken != null && (System.currentTimeMillis() / 1000) < cachedToken.getExpires_at()) {
            return Single.just(cachedToken.getKey());
        }

        System.out.println("Password: " + password);
        System.out.println("Email: " + email);

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
                .onErrorReturn(throwable -> {
                    System.out.println(throwable.getMessage());
                    return "Authentication Error:" + throwable.getMessage();
                });
    }

    public Single<List<SimpleBookDAO>> fetchBooks() {
        String authKey = authenticate().blockingGet();

        String url = "books";

        HttpRequest<?> request = HttpRequest.GET(url)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody ->
                        jsonMapper.readValue(responseBody, Argument.listOf(SimpleBookDAO.class)
                        )
                )
                .onErrorResumeNext(throwable -> {
                    if (isAuthError(throwable)) {
                        invalidateCachedToken();
                        return fetchBooks();
                    }
                    return Single.error(throwable);
                })
                .onErrorReturn(throwable -> Collections.emptyList());
    }

    public Single<NotesResponseDAO> fetchNotes() {
        String authKey = authenticate().blockingGet();
        return Single.fromCallable(() -> fetchNotesWithToken(authKey));
    }

    private NotesResponseDAO fetchNotesWithToken(String authKey) {
        List<NoteResponseDAO> allNotes = new ArrayList<>();
        int currentPage = 1;
        int totalNotes = 0;

        while (true) {
            NotesResponseDAO response = fetchNotesPage(authKey, currentPage);
            if (response.getNotes() == null || response.getNotes().isEmpty()) {
                break; // No more notes to fetch
            }
            allNotes.addAll(response.getNotes());
            totalNotes = response.getTotal();
            if (allNotes.size() >= totalNotes) {
                break; // All notes have been fetched
            }
            currentPage++;
        }

        NotesResponseDAO aggregatedResponse = new NotesResponseDAO();
        aggregatedResponse.setNotes(allNotes);
        aggregatedResponse.setTotal(totalNotes);
        return aggregatedResponse;
    }

    private NotesResponseDAO fetchNotesPage(String authKey, int page) {
        HttpRequest<?> request = HttpRequest.GET("notes?page=" + page)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authKey);
        try {
            return Single.fromPublisher(httpClient.retrieve(request))
                    .map(responseBody -> jsonMapper.readValue(responseBody, NotesResponseDAO.class))
                    .blockingGet();
        } catch (Exception e) {
            // Handle exceptions such as IO errors or JSON parsing errors
            return new NotesResponseDAO();
        }
    }

    public Single<NoteResponseDAO> fetchNoteByUUID(String uuid) {
        return authenticate().flatMap(token -> fetchNoteByUUIDWithToken(token, uuid));
    }

    private Single<NoteResponseDAO> fetchNoteByUUIDWithToken(String authKey, String uuid) {
        String url = "notes/" + uuid;
        HttpRequest<?> request = HttpRequest.GET(url)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody -> jsonMapper.readValue(responseBody, NoteResponseDAO.class))
                .onErrorResumeNext(throwable -> {
                    if (isAuthError(throwable)) {
                        invalidateCachedToken();
                        return authenticate().flatMap(token -> fetchNoteByUUIDWithToken(token, uuid));
                    }
                    return Single.error(throwable);
                })
                .onErrorReturn(throwable -> {
                    NoteResponseDAO dao = new NoteResponseDAO();
                    dao.setUuid("Error loading Note");
                    dao.setContent(throwable.getMessage() + "\n" + throwable.getCause());
                    return dao;
                });
    }

    public Completable updateNoteContentByUUID(NoteResponseDAO note) {
        String authKey = authenticate().blockingGet();
        String url = "notes/" + note.getUuid();

        Map<String, Object> body = new HashMap<>();
        body.put("content", note.getContent());
        body.put("public", true);

        HttpRequest<?> request = HttpRequest.PATCH(url, body)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody -> jsonMapper.readValue(responseBody, NoteResponseDAO.class))
                .ignoreElement()
                .doOnError(error -> System.err.println("Error in updateNoteContentByUUID: " + error.getMessage()));
    }

    public Completable addTask(String bookUUID, String content) throws IOException {
        String authKey = authenticate().blockingGet();
        String url = "notes";

        NoteRequestDAO body = new NoteRequestDAO(bookUUID, content, true);

        HttpRequest<?> request = HttpRequest.create(HttpMethod.POST, url)
                .body(body)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody -> jsonMapper.readValue(responseBody, NoteResultWrapper.class).getResult())
                .delay(3, TimeUnit.SECONDS)
                .flatMapCompletable(this::updateNoteContentByUUID)
                .doOnError(error -> System.err.println("Error in addTask: " + error.getMessage()));
    }

    public Completable deleteTaskById(String uuid) {
        String authKey = authenticate().blockingGet();

        String url = "notes/" + uuid;

        HttpRequest<?> request = HttpRequest.DELETE(url)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody -> jsonMapper.readValue(responseBody, NoteResponseDAO.class))
                .ignoreElement();
    }

    private boolean isAuthError(Throwable throwable) {
        return throwable.getMessage().contains("401") || throwable.getMessage().toLowerCase().contains("unauthorized");
    }

    private void saveAuthToken(AuthResponse authResponse) {
        try {
            String json = jsonMapper.writeValueAsString(authResponse);
            Files.write(Paths.get("dnote_token.json"), json.getBytes());
        } catch (IOException e) {
            System.err.println("Error in addTask: " + e.getMessage());
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
            System.err.println("Error in addTask: " + e.getMessage());
        }
        return null;
    }

    private void invalidateCachedToken() {
        try {
            Files.deleteIfExists(Paths.get("dnote_token.json"));
        } catch (IOException e) {
            System.err.println("Error in addTask: " + e.getMessage());
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

