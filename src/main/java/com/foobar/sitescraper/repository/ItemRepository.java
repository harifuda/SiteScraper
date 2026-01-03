package com.foobar.sitescraper.repository;

import com.foobar.sitescraper.model.Item;

import java.util.List;

public interface ItemRepository {
    void save(Item item);
    void saveAll(List<Item> items);
    List<Item> findAll();
}
