package models;

public class User {
    public String uid;
    public String username;
    public String email;
    public String role;

    public User() {
    }

    public User(String uid, String username, String email, String role) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
