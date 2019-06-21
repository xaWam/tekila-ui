package com.jaravir.tekila.ui.jsf.managed.util;

/**
 * Created by kmaharov on 19.07.2016.
 */
public class ChargeForView {
    String name;
    double count;
    double price;
    double total;
    int remainingdays;
    String remainingDaysStr;

    public ChargeForView(String name, double count, double price, int remainingdays) {
        this.name = name;
        this.count = count;
        this.price = price;
        this.total = price * count;
        this.remainingdays = remainingdays;
        if (remainingdays == -1) {
            this.remainingDaysStr = "N/A";
        } else {
            this.remainingDaysStr = String.valueOf(remainingdays);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getRemainingDaysStr() {
        return remainingDaysStr;
    }

    public void setRemainingDaysStr(String remainingDaysStr) {
        this.remainingDaysStr = remainingDaysStr;
    }
}