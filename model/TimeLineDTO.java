package induk.soft.meeting.indukmeeting.model;

/**
 * Created by SIK on 2016-07-09.
 */
public class TimeLineDTO {
    private String idx;
    private String email;
    private String name;
    private String date;
    private String contents;

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
