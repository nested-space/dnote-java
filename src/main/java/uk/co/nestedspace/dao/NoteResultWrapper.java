package uk.co.nestedspace.dao;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class NoteResultWrapper {
    private NoteResponseDAO result;

    public NoteResponseDAO getResult() {
        return result;
    }

    public void setResult(NoteResponseDAO result) {
        this.result = result;
    }
}