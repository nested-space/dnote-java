package uk.co.nestedspace.dao;

import io.micronaut.serde.annotation.Serdeable;
import uk.co.nestedspace.models.Note;

import java.util.ArrayList;
import java.util.List;

@Serdeable.Deserializable
public class NotesResponseDAO {
    private List<NoteResponseDAO> noteResponseDAOS;
    private int total;

    // Getters and Setters
    public List<NoteResponseDAO> getNotes() { return noteResponseDAOS; }
    public void setNotes(List<NoteResponseDAO> notes) { this.noteResponseDAOS = notes; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<Note> factory(){
        List<Note> notes = new ArrayList<>();
        for (NoteResponseDAO noteResponseDAO : this.noteResponseDAOS){
            notes.add(new Note(
                    noteResponseDAO.getUuid(),
                    noteResponseDAO.getContent(),
                    noteResponseDAO.getUsn(),
                    noteResponseDAO.getBook().factory()
            ));
        }
        return notes;
    }
}