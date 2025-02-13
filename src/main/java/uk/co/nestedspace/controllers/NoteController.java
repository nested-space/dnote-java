package uk.co.nestedspace.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.views.View;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import uk.co.nestedspace.models.Note;
import uk.co.nestedspace.services.DnoteService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller("/edit")
public class NoteController {

    @Inject
    private DnoteService dnoteService;

    @View("item")
    @Get("/{uuid}")
    public Single<Map<String, Object>> getNote(@PathVariable String uuid) {
        return dnoteService.authenticate()
                .flatMap(authKey -> {
                    if (!authKey.isEmpty()) {
                        return dnoteService.fetchNoteByUUID(authKey, uuid)
                                .map(noteDAO -> {
                                    if (noteDAO == null) {
                                        System.out.println("Note not found.");
                                        return Map.of("error", "Note not found");
                                    }
                                    return Map.of("note", noteDAO.noteFactory());
                                });
                    }
                    return Single.just(Map.of("error", "Authentication failed"));
                });
    }
}
