package uk.co.nestedspace.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.views.View;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import uk.co.nestedspace.services.DnoteService;

import java.util.Map;

@Controller("/notes")
public class NoteController {

    @Inject
    private DnoteService dnoteService;

    @View("note")
    @Get("/{uuid}")
    public Single<Map<String, Object>> getNote(@PathVariable String uuid) {
        return dnoteService.authenticate()
                .flatMap(authKey -> {
                    if (!authKey.isEmpty()) {
                        return dnoteService.fetchNoteByUUID(authKey, uuid)
                                .map(note -> {
                                    if (note == null) {
                                        System.out.println("Note not found.");
                                        return Map.of("error", "Note not found");
                                    }
                                    return Map.of("note", note);
                                });
                    }
                    return Single.just(Map.of("error", "Authentication failed"));
                });
    }

    @Get("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> testRoute() {
        return HttpResponse.ok("Route /notes/test is working");
    }
}
