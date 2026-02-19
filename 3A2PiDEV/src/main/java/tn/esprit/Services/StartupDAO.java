package tn.esprit.Services;

import tn.esprit.entities.Startup;
import tn.esprit.utils.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StartupDAO {

    private final Connection conx;

    public StartupDAO() {
        conx = MyDB.getInstance().getCnx();
    }

    public List<Startup> getAllStartups() {
        List<Startup> list = new ArrayList<>();

        String sql = "SELECT startupID, name, description, sector FROM startup ORDER BY startupID DESC";

        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Startup s = new Startup(
                        rs.getInt("startupID"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("sector")
                );
                list.add(s);
            }

        } catch (Exception e) {
            System.out.println("Error fetching startups: " + e.getMessage());
        }

        return list;
    }
}
