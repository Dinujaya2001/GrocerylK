package lk.tharaka.grocerylk.models;

import java.io.Serializable;

public class NavCategoryModel implements Serializable {
    String img_url;
    String name;
    String description;
    String discount;
    String type;


    // Required empty constructor for Firebase
    public NavCategoryModel() {
    }

    public NavCategoryModel(String img_url, String name, String description, String discount, String type) {
        this.img_url = img_url;
        this.name = name;
        this.description = description;
        this.discount = discount;
        this.type = type; // Now it will correctly assign the value
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
