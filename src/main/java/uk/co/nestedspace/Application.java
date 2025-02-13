package uk.co.nestedspace;

import io.micronaut.runtime.Micronaut;
import uk.co.nestedspace.services.DnoteService;

public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}