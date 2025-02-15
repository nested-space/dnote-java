package uk.co.nestedspace.dao;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable.Deserializable
public class LibraryDAO {

    private List<SimpleBookDAO> books;

    public List<SimpleBookDAO> getBooks() {
        return books;
    }

    public void setBooks(List<SimpleBookDAO> books) {
        this.books = books;
    }
}
