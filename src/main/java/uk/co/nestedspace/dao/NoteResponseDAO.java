package uk.co.nestedspace.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.core.annotation.Nullable;
import uk.co.nestedspace.models.Note;

@Serdeable.Deserializable
public class NoteResponseDAO {
    @Nullable
    private String uuid;

    @Nullable
    private String createdAt;

    @Nullable
    private String updatedAt;

    private String content;

    @Nullable
    private int addedOn;

    @JsonProperty("public")
    private boolean isPublic;

    @Nullable
    private int usn;

    @Nullable
    private SimpleBookDAO book;

    @Nullable
    private UserDAO user;

    // Getters and Setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getAddedOn() { return addedOn; }
    public void setAddedOn(int addedOn) { this.addedOn = addedOn; }

    @JsonProperty("public")
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public int getUsn() { return usn; }
    public void setUsn(int usn) { this.usn = usn; }

    public SimpleBookDAO getBook() { return book; }
    public void setBook(SimpleBookDAO book) { this.book = book; }

    public UserDAO getUser() { return user; }
    public void setUser(UserDAO user) { this.user = user; }

    public Note noteFactory(){
        return new Note(getUuid(),
                getContent(),
                getUsn(),
                getBook().factory());
    }

    public static NoteResponseDAO fromNote(Note note){
        NoteResponseDAO noteResponseDAO = new NoteResponseDAO();
        noteResponseDAO.setUuid(note.getUuid());
        noteResponseDAO.setContent(note.getContent());
        return noteResponseDAO;
    }

//    public String update(){
//
//    }
}