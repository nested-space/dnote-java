package uk.co.nestedspace.dao;

import io.micronaut.serde.annotation.Serdeable;
import uk.co.nestedspace.models.Book;

@Serdeable.Deserializable
public class BookDAO {
    private String uuid;
    private String label;

    // Getters and Setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Book factory(){
        return new Book(getUuid(), getLabel());
    }
}