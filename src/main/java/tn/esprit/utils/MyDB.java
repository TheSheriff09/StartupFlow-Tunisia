package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {
    private final String URL="jdbc:mysql://127.0.0.1:3306/startupflow";
    private final String USERNAME="root";
    private final String PWD="";

    public static MyDB instance;
    private Connection conx;
    private MyDB(){
        try {
            conx =DriverManager.getConnection(URL,USERNAME,PWD);
            System.out.println("connected to DB");
        } catch (SQLException e) {
            System.out.println(e.getMessage());;
        }
    }
    public static MyDB getInstance(){
        if(instance==null){
            instance =new MyDB();
        }
        return instance;
    }
    public Connection getconx(){
        return conx;
    }

}
