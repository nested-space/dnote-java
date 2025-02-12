package uk.co.nestedspace.dao;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Deserializable
public class UserDAO {
    private String name;
    private String uuid;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}