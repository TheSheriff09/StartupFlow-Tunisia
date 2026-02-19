package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.Services.UserService;
import java.util.List;
import tn.esprit.Services.ReclamationService;
import tn.esprit.entities.Reclamation;
import tn.esprit.utils.MyDB;

import static javafx.application.Application.launch;



public class MainApp extends Application {
    public static void main(String[] args) {
        MyDB db = new MyDB();
        UserService us = new UserService();
        ReclamationService rs = new ReclamationService();

        // 1) ADD
      //  User mentor = new User("ADMIN A", "ADMIN@mail.com", "hash", "ADMIN", "ACTIVE",null, null);
                //mentor = us.add(mentor);


        System.out.println("---- LIST USERS ----");
        List<User> users = us.list();
        for (User u : users) {
            System.out.println(u);
        }
        Reclamation r = new Reclamation(
                "Unfair Evaluation",
                "I think the evaluation was unfair.",
                "OPEN",
                1,
                3
        );
       // rs.add(r);
       // r.setStatus("IN_PROGRESS");
        rs.list().forEach(System.out::println);
        launch(args);


        //  System.out.println("---- UPDATE USER ----");
      //  added.setFullName("Mohamed Updated");
      //  added.setStatus("BLOCKED");
       //us.update(added);

       // System.out.println("---- DELETE USER ----");
    //    us.deleteById(added.getId());
        }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Landing.fxml"));
        Parent root = loader.load();
        stage.setTitle("StartupFlow Tunisia - Sign Up");

        stage.setScene(new Scene(root));
        stage.show();

    }
}


