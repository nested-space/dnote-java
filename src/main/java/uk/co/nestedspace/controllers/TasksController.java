package uk.co.nestedspace.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import uk.co.nestedspace.models.Note;
import uk.co.nestedspace.services.DnoteService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
                        return Map.of("error", "No notes were retrieved from the server.");
                    }

                    List<Note> dueThisWeek = new ArrayList<>();
                    List<Note> dueThisMonth = new ArrayList<>();
                    List<Note> dueLongTerm = new ArrayList<>();
                    List<Note> waiting = new ArrayList<>();

                    LocalDate now = LocalDate.now();
                    for (Note note : notesResponse.factory()) {
                        long daysUntilDue = ChronoUnit.DAYS.between(now, note.getNeededBy());
                        if (daysUntilDue < 7 && !note.isWaiting()) {
                            dueThisWeek.add(note);
                        } else if (daysUntilDue < 30 && !note.isWaiting()) {
                            dueThisMonth.add(note);
                        } else if (!note.isWaiting() && !note.isWaiting()) {
                            dueLongTerm.add(note);
                        } else {
                            waiting.add(note);
                        }
                    }

                    dueThisWeek.sort(Comparator.comparing(Note::getNeededBy));
                    dueThisMonth.sort(Comparator.comparing(Note::getNeededBy));
                    dueLongTerm.sort(Comparator.comparing(Note::getNeededBy));
                    waiting.sort(Comparator.comparing(Note::getNeededBy));

                    Map<String, Object> data = new HashMap<>();
                    data.put("dueThisWeek", dueThisWeek);
                    data.put("dueThisMonth", dueThisMonth);
                    data.put("dueLongTerm", dueLongTerm);
                    data.put("waiting", waiting);

                    return data;
                });
    }
}