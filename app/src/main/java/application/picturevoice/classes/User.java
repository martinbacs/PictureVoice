package application.picturevoice.classes;

public class User {

    public String uid;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

}
