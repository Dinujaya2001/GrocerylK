package lk.tharaka.grocerylk.models;

import java.io.Serializable;

public class MyCartModel implements Serializable {

    String productName;
    String productPrice;
    String TotalQuantity;
    int TotalPrice;
    String currentDate;
    String currentTime;
    String documentId;



    public MyCartModel() {
    }

    public MyCartModel(String productName, String productPrice, String TotalQuantity, int TotalPrice, String currentDate, String currentTime) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.TotalQuantity = TotalQuantity;
        this.TotalPrice = TotalPrice;
        this.currentDate = currentDate;
        this.currentTime = currentTime;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getTotalQuantity() {
        return TotalQuantity;
    }

    public void setTotalQuantity(String totalQuantity) {
        this.TotalQuantity = totalQuantity;
    }

    public int getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.TotalPrice = totalPrice;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}