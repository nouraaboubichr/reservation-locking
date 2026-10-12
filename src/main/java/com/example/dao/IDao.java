package com.example.dao;

import java.util.List;

public interface IDao<T> {
    boolean create(T o);
    boolean update(T o);
    boolean delete(T o);
    T findById(long id);
    List<T> findAll();
}