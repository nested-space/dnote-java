package uk.co.nestedspace.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;

@Serdeable
public class NoteRequestDAO {

    @JsonProperty("book_uuid")
    @Nullable
    private String bookUuid;

    @Nullable
    private String content;

    @JsonProperty("public")  // ✅ Correctly placed on a field
    private boolean isPublic;

    public NoteRequestDAO(String bookUuid, String content, boolean isPublic) {
        this.bookUuid = bookUuid;
        this.content = content;
        this.isPublic = isPublic;
    }

    public String getBookUuid() { return bookUuid; }
    public void setBookUuid(String bookUuid) { this.bookUuid = bookUuid; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @JsonProperty("public")  // ✅ Allowed on getter
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}