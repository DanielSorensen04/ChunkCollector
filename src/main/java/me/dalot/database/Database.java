package me.dalot.database;

import me.dalot.ChunkCollector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static Connection connection;

    private static CollectorDataAccess collectorDataAccess;

    public static CollectorDataAccess getCollectorDataAccess() {
        return collectorDataAccess;
    }

    public static Connection getConnection() {

        if (connection == null){
            try {
                Class.forName("org.h2.Driver");

                try {
                    connection = DriverManager.getConnection(ChunkCollector.getConnectionURL());

                } catch (SQLException e) {
                    System.out.println("Ikke i stand til at oprette forbindelse til databasen");
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException ex) {
                System.out.println("Kunne ikke finde h2 DB sql driver");
            }
        }
        return connection;
    }

    public static void initializeDatabase() {

        try {

            Statement statement = getConnection().createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Collectors(CollectorID int NOT NULL IDENTITY(1, 1), Type varchar(255), OwnerUUID varchar(255), Items clob, Sold long, Earned double, Capacity int, Fortune int, isEnabled boolean);");
            statement.execute("CREATE TABLE IF NOT EXISTS OfflineProfits(ID int NOT NULL IDENTITY(1, 1), UUID varchar(255), TotalEarned double, TotalSold long)");

            collectorDataAccess = new CollectorDataAccess();

            System.out.println("Database loaded");

            statement.close();

        } catch (SQLException e) {
            System.out.println("Database intialization fejl.");
            e.printStackTrace();
        }
    }
}
