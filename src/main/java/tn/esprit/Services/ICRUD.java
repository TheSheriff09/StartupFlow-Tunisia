package tn.esprit.Services;
import java.util.List;

public interface ICRUD<T> {
        T add(T t);

        List<T> list();

        void update(T t);

        void delete(T t);

    }

