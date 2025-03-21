package uk.co.nestedspace.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class Note {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yy", Locale.ENGLISH);
    private static final String SEPARATOR = " >>> ";
    private static final String WAIT_FLAG = "(WAITING)";

    private boolean isWaiting;
    private LocalDate neededBy;
    private String message;

    // No API endpoint to update these
    private final String uuid;
    private int usn;
    private Book book;

    public Note(String uuid, String content, int usn, Book book) {
        this.uuid = uuid;
        this.usn = usn;
        this.book = book;
        parseContent(content);
    }

    public Note(String uuid, String message, boolean isWaiting, LocalDate due){
        this.uuid = uuid;
        this.message = message;
        this.isWaiting = isWaiting;
        this.neededBy = due;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public boolean isOverdue(){
        return LocalDate.now().isAfter(neededBy.minusDays(1));
    }

    public String getUuid() {
        return uuid;
    }

    public String getContent() {
        String waitingString = isWaiting ? WAIT_FLAG + SEPARATOR : "";
        return waitingString + neededBy.format(DATE_FORMAT) + SEPARATOR + message;
    }

    public String getMessage() {
        return message;
    }

    public LocalDate getNeededBy() {
        return neededBy;
    }

    public int getUsn() {
        return usn;
    }

    public String getBook() {
        return book.getLabel();
    }

    public String getDateAsString(){
        return neededBy.format(DATE_FORMAT);
    }

    public void setContent(boolean isWaiting, LocalDate neededBy, String message){
        this.isWaiting = isWaiting;
        this.neededBy = neededBy;
        this.message = message;
    }

    private void parseContent(String content){
        boolean isWaiting = false;
        LocalDate neededBy = LocalDate.MIN;
        String message = "Error Parsing Content";

        String[] contents = content.split(SEPARATOR);
        if(contents[0].equals(WAIT_FLAG)){
            isWaiting = true;
            neededBy = parseDate(contents[1]);
            message = contents[2];
        } else if (contents.length == 1) {
            isWaiting = false;
            neededBy = LocalDate.MAX;
            message = contents[0];
        } else {
            //getIsWaiting already false//
            neededBy = parseDate(contents[0]);
            message = contents[1];
        }

        setContent(isWaiting, neededBy, message);
    }

    private LocalDate parseDate(String dateString){
        LocalDate date;

        try {
            date = LocalDate.parse(dateString, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date = LocalDate.MAX;
        }

        return date;
    }
}
