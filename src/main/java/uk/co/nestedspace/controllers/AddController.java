package uk.co.nestedspace.controllers;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.views.View;
import jakarta.inject.Inject;
import uk.co.nestedspace.models.Note;
import uk.co.nestedspace.services.DnoteService;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

@Controller("/tasks/add")
public class AddController {

    @Inject
    private DnoteService dnoteService;

    @View("add")
    @Get(produces = MediaType.TEXT_HTML)
    public Map<String, Object> addNote() {
        return Collections.emptyMap();
    }

    @Post(consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> addTask(@Body TaskAddForm form) {
        Note note = new Note(
                form.getBookUUID(),
                form.getMessage(),
                Boolean.parseBoolean(form.getIsWaiting()),
                LocalDate.parse(form.getNeededBy()));
        System.out.println(note.getContent());

        //Completable result = dnoteService.addTask(form.bookUUID, note.getContent());
        //result.blockingAwait();  // Don't redirect until operation is complete

        return HttpResponse.redirect(URI.create("/tasks"));
    }

    @Introspected
    @Serdeable.Deserializable
    public static class TaskAddForm {
        private String bookUUID;
        private String message;
        private String neededBy;
        private String isWaiting;

        public String getBookUUID() { return bookUUID; }
        public void setBookUUID(String bookUUID) { this.bookUUID = bookUUID; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getNeededBy() { return neededBy; }
        public void setNeededBy(String neededBy) { this.neededBy = neededBy; }

        public String getIsWaiting() { return isWaiting; }

        public void setIsWaiting(String isWaiting) {
            this.isWaiting = String.valueOf("true".equalsIgnoreCase(isWaiting));
        }

        @Override
        public String toString() {
            return String.format("Book UUID: %s\nMessage: %s\nisWaiting: %s\nneededBy: %s", bookUUID, message, isWaiting, neededBy);
        }
    }

}

