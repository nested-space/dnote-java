package uk.co.nestedspace.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.MediaType;
import io.micronaut.views.View;

import java.io.File;
import java.util.Collections;
import java.util.Map;

@Controller("/")
public class HomeController {

    @Get(produces = MediaType.TEXT_HTML)
    @View("home")
    public Map<String, Object> index() {
        return Collections.emptyMap(); // No dynamic data needed
    }
}

