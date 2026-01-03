package com.foobar.sitescraper.model;

import java.math.BigDecimal;

public class Item {
    private int id;
    private String name;
    private BigDecimal price;
    private String status;
    private String url;

    public Item(int id, String name, BigDecimal price, String status, String url) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.status = status;
        this.url = url;
    }

    public Item() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return String.format("Name: %s\nPrice: %s\nURL: %s\nAvailability: %s\n"
                , name, price, url, status);
    }
}
