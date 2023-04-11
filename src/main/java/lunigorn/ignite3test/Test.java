package lunigorn.ignite3test;

import java.sql.*;

public class Test {
    private static final String DB_URL = "jdbc:ignite:thin://127.0.1.1:10800";
    private static final int COLUMNS_COUNT = 5;
    private static final int TABLES_COUNT = 1000;
    private static final int SLEEP = 30;

    public static void main(String[] args) throws SQLException {
        System.out.println("Test started");
        try (Connection connection = DriverManager.getConnection(DB_URL); Statement statement = connection.createStatement()){
            System.out.println("Connection created");
            for (int i = 0; i < TABLES_COUNT; i++) {
                String createTableQuery = createTableQuery("table_" + i, COLUMNS_COUNT);
                long timestampBefore = System.currentTimeMillis();
                statement.executeUpdate(createTableQuery);
                long timestampAfter = System.currentTimeMillis();
                System.out.println("Create table " + i + " took " + (timestampAfter - timestampBefore) + " ms");
                if ((i+1) % 50 == 0){
                    int tablesCount = findTablesCount(connection);
                    if (tablesCount != i+1){
                        throw new IllegalStateException("Expected " + (i+1) + " tables in cluster, but was " + tablesCount);
                    }
                    System.out.println("Tables count in cluster: " + tablesCount);
                }
                sleep();
            }
        }
    }


    public static String createTableQuery(String tableName, int columnsAmount){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tableName).append("(");
        for (int i = 0; i < columnsAmount; i++) {
            if (i == 0){
                sb.append("id INT PRIMARY KEY");
            } else {
                sb.append("column_").append(i).append(" VARCHAR");
            }

            if (i != columnsAmount - 1){
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static int findTablesCount(Connection connection) throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        String catalog = connection.getCatalog();
        ResultSet table_rs = md.getTables(catalog, null, null, new String[]{"TABLE"});
        int count = 0;
        while (table_rs.next()){
            count++;
        }
        return count;
    }

    public static void sleep(){
        try {
            Thread.sleep(SLEEP);
        } catch (InterruptedException ignored) {
        }
    }

}
