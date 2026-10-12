package lk.tharaka.grocerylk.models;

public class UserModel {
    String name;
    String email;
    String password;
    String profileImg;

    // NEW FIELDS
    String number;
    String address;

    public UserModel() {
        // Required empty public constructor for Firebase
    }

    public UserModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Constructor including new fields (Optional, useful for Registration)
    public UserModel(String name, String email, String password, String number, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.number = number;
        this.address = address;
    }

    // Getters and Setters for New Fields
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Original Getters and Setters
    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
}