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

    @Get("/debug-static")
    public String debugStatic() {
        File staticDir = new File("src/main/resources/static/images/");
        StringBuilder sb = new StringBuilder("Files in static/images: <br>");

        if (staticDir.exists() && staticDir.isDirectory()) {
            for (File file : staticDir.listFiles()) {
                sb.append(file.getName()).append("<br>");
            }
        } else {
            sb.append("Directory not found!");
        }

        return sb.toString();
    }
}

