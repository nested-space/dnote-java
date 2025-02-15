package uk.co.nestedspace.controllers;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Completable;
import jakarta.inject.Inject;
import uk.co.nestedspace.dao.NoteDAO;
import uk.co.nestedspace.models.Note;
import uk.co.nestedspace.services.DnoteService;
import java.net.URI;
import java.time.LocalDate;

@Controller("/tasks/edit")
public class EditController {

    @Inject
    private DnoteService dnoteService;

    @Post(consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> editTask(@Body TaskEditForm form) {
        Note note = new Note(
                form.uuid,
                form.getMessage(),
                form.isWaiting(),
                LocalDate.parse(form.getNeededBy()));

        Completable result = dnoteService.updateNoteContentByUUID(NoteDAO.fromNote(note));
        result.blockingAwait();  // Don't redirect until operation is complete

        return HttpResponse.redirect(URI.create("/tasks"));
    }

    @Introspected
    @Serdeable.Deserializable
    public static class TaskEditForm {
        private String uuid;
        private String message;
        private String neededBy;
        private boolean isWaiting;

        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getNeededBy() { return neededBy; }
        public void setNeededBy(String neededBy) { this.neededBy = neededBy; }

        public boolean isWaiting() { return isWaiting; }
        public void setWaiting(boolean waiting) { isWaiting = waiting; }

        @Override
        public String toString() {
            return String.format("UUID: %s\nMessage: %s\nisWaiting: %s\nneededBy: %s", uuid, message, isWaiting, neededBy);
        }
    }
}
