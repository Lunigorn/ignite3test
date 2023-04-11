package lunigorn.ignite3test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test {
    private static final String DB_URL = "jdbc:ignite:thin://172.24.1.2:10800";

    public static void main(String[] args) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL)){
        }
    }
}
