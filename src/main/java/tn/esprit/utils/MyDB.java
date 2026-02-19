package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {

    private static final String URL = "jdbc:mysql://localhost:3306/startupflow";
    private static final String USERNAME = "root";
    private static final String PWD = "";

    private Connection conx;
    private static MyDB instance;

    private MyDB() {
        try {
            conx = DriverManager.getConnection(URL, USERNAME, PWD);
            System.out.println("Successfully connected to the database!");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    public static synchronized MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    public Connection getConx() {
        return conx;
    }
}
