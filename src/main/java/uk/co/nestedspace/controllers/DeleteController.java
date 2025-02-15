package uk.co.nestedspace.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.reactivex.rxjava3.core.Completable;
import jakarta.inject.Inject;
import uk.co.nestedspace.services.DnoteService;

import java.net.URI;

@Controller ("/tasks/delete")
public class DeleteController {

    @Inject
    private DnoteService dnoteService;

    @Post(uris = "/{uuid}", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> deleteTask(@PathVariable String uuid) {
        Completable deleted = dnoteService.deleteTaskById(uuid);
        deleted.blockingAwait();
        return HttpResponse.redirect(URI.create("/tasks"));

    }
}
