package com.foobar.sitescraper.repository;

import com.foobar.sitescraper.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLRepository implements ItemRepository {
    private final Connection connection;

    public MySQLRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Item item) {
        String query = "INSERT INTO items (name, price, url, status) VALUES (?, ?, ?, ?)";
        // To execute parameterized SQL statements
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, item.getName());
            preparedStatement.setBigDecimal(2, item.getPrice());
            preparedStatement.setString(3, item.getUrl());
            preparedStatement.setString(4, item.getStatus());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAll(List<Item> items) {
        for (Item item : items) {
            save(item);
        }
    }

    @Override
    public List<Item> findAll() {
        return null;
    }
}
