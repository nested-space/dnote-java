package uk.co.nestedspace.models;

public class Book {

    //Not implemented: API endpoint to change book label
    private final String uuid;
    private final String label;

    public Book(String uuid, String label) {
        this.uuid = uuid;
        this.label = label;
    }

    public String getUuid() {
        return uuid;
    }

    public String getLabel() {
        return label;
    }
}
