package tn.esprit;

import tn.esprit.utils.MyDB;

public class Main {
    public static void main(String[] args) {
        // Initialize DB connection check
        MyDB db = MyDB.getInstance();
        System.out.println("Connection established. Use MainFx to run the application.");
    }
}
