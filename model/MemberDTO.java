package induk.soft.meeting.indukmeeting.model;

import java.sql.Date;

/**
 * Created by SIK on 2016-07-02.
 */
public class MemberDTO {
    private String email;
    private String password;
    private String name;
    private String profile;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
