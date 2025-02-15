package uk.co.nestedspace.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import uk.co.nestedspace.models.Note;

@Serdeable.Deserializable
public class NoteDAO {
    private String uuid;
    private String createdAt;
    private String updatedAt;
    private String content;
    private int addedOn;
    private boolean isPublic;
    private int usn;
    private BookDAO book;
    private UserDAO user;

    public NoteDAO(String uuid, String content){
        this.uuid = uuid;
        this.content = content;
    }

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

    public BookDAO getBook() { return book; }
    public void setBook(BookDAO book) { this.book = book; }

    public UserDAO getUser() { return user; }
    public void setUser(UserDAO user) { this.user = user; }

    public Note noteFactory(){
        return new Note(getUuid(),
                getContent(),
                getUsn(),
                getBook().factory());
    }

    public static NoteDAO fromNote(Note note){
        return new NoteDAO(note.getUuid(), note.getContent());
    }

//    public String update(){
//
//    }
}