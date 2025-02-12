package uk.co.nestedspace.dao;

import io.micronaut.serde.annotation.Serdeable;
import uk.co.nestedspace.models.Note;

import java.util.ArrayList;
import java.util.List;

@Serdeable.Deserializable
public class NotesResponseDAO {
    private List<NoteDAO> noteDAOS;
    private int total;

    // Getters and Setters
    public List<NoteDAO> getNotes() { return noteDAOS; }
    public void setNotes(List<NoteDAO> notes) { this.noteDAOS = notes; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<Note> factory(){
        List<Note> notes = new ArrayList<>();
        for (NoteDAO noteDAO : this.noteDAOS){
            notes.add(new Note(
                    noteDAO.getUuid(),
                    noteDAO.getContent(),
                    noteDAO.getUsn(),
                    noteDAO.getBook().factory()
            ));
        }
        return notes;
    }
}