package uk.co.nestedspace.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.views.View;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import uk.co.nestedspace.services.DnoteService;

import java.util.Map;

@Controller("/tasks")
public class ItemController {

    @Inject
    private DnoteService dnoteService;

    @View("item")
    @Get("/{uuid}")
    public Single<Map<String, Object>> getNote(@PathVariable String uuid) {
        return dnoteService.fetchNoteByUUID(uuid)
                .map(noteDAO -> {
                    if (noteDAO == null) {
                        return Map.of("error", "Note not found");
                    }
                    return Map.of("note", noteDAO.noteFactory());
                });
    }
}
