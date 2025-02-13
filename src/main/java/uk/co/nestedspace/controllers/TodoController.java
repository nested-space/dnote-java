package uk.co.nestedspace.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import uk.co.nestedspace.models.Note;
import uk.co.nestedspace.services.DnoteService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller("/todo")
public class TodoController {

    @Inject
    private DnoteService dnoteService;

    @View("todo")
    @Get("/")
    public Single<Map<String, Object>> getNotes() {
        return dnoteService.authenticate()
                .flatMap(authKey -> {
                    if (!authKey.isEmpty()) {
                        return dnoteService.fetchNotes(authKey)
                                .map(notesResponse -> {
                                    if (notesResponse == null || notesResponse.getNotes() == null) {
                                        System.out.println("NotesResponse is null or has no notes.");
                                        return Map.of("error", "No notes found");
                                    }

                                    List<Note> notes = notesResponse.factory();
                                    notes.sort(Comparator.comparing(Note::getNeededBy));
                                    return Map.of("notes", notes);
                                });
                    }
                    return Single.just(Map.of("error", "Authentication failed"));
                });
    }
}