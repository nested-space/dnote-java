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
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import uk.co.nestedspace.dao.NoteDAO;
import uk.co.nestedspace.dao.NotesResponseDAO;

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
        AuthRequest authRequest = new AuthRequest(email, password);

        HttpRequest<AuthRequest> request = HttpRequest.POST("signin", authRequest)
                .header(HttpHeaders.CONTENT_TYPE, "application/json");

        return Single.fromPublisher(httpClient.exchange(request, AuthResponse.class))
                .map(response -> {
                    System.out.println("Auth Response Status: " + response.getStatus().getCode());

                    if (response.getBody().isPresent()) {
                        return response.getBody().get().getKey();
                    } else {
                        System.out.println("Auth Response Body is empty!");
                        return "";
                    }
                })
                .onErrorReturn(throwable -> {
                    System.out.println("Authentication Error: " + throwable.getMessage());
                    return "";
                });
    }

    /**
     * Fetch notes from the Dnote API.
     */
    public Single<NotesResponseDAO> fetchNotes(String authKey) {
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
                .onErrorReturn(throwable -> {
                    return new NotesResponseDAO();
                });
    }

    public Single<NoteDAO> fetchNoteByUUID(String authKey, String uuid) {
        String url = "notes/" + uuid;
        System.out.println("Connecting to: " + url);

        HttpRequest<?> request = HttpRequest.GET(url)
                .header("Authorization", "Bearer " + authKey)
                .header("Content-Type", "application/json");

        return Single.fromPublisher(httpClient.retrieve(request))
                .map(responseBody -> {
                    System.out.println("Response Body: " + responseBody);
                    return jsonMapper.readValue(responseBody, NoteDAO.class);
                });
    }

    /**
     * Update a note's content in Dnote.
     */
    public Single<Boolean> updateNote(String authKey, String noteUuid, String updatedContent) {
        HttpRequest<?> request = HttpRequest.PATCH("notes/" + noteUuid, new UpdateRequest(updatedContent))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authKey);

        return Single.fromPublisher(httpClient.exchange(request, String.class))
                .map(response -> response.getStatus().getCode() == 200)
                .onErrorReturn(throwable -> false);
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
    public static class AuthResponse {
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
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