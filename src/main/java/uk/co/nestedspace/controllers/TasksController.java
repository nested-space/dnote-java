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

@Controller("/tasks")
public class TasksController {

    @Inject
    private DnoteService dnoteService;

    @View("tasks")
    @Get("/")
    public Single<Map<String, Object>> getNotes() {
        return dnoteService.fetchNotes()
                .map(notesResponse -> {
                    if (notesResponse == null || notesResponse.getNotes() == null) {
                        return Map.of("error", "NotesResponse is null or has no notes.");
                    }

                    List<Note> notes = notesResponse.factory();
                    notes.sort(Comparator.comparing(Note::getNeededBy));
                    return Map.of("notes", notes);
                });
    }
}