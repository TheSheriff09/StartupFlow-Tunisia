package tn.esprit.service;

import java.util.List;

public interface ICrud<T> {
    void add(T t);
    List<T> getAll();
    void update(T t);
    void delete(int id);
}
