package lunigorn.ignite3test;

import java.sql.*;

public class Test {
    private static final String DB_URL = "jdbc:ignite:thin://127.0.1.1:10800";
    private static final int COLUMNS_COUNT = 5;
    private static final int ROWS_COUNT = 1_000_000;
    private static final int BATCH_SIZE = 1_000;
    private static final int SLEEP = 30;
    private static final int RETRIES_COUNT = 3;
    private static final int RETRIES_SLEEP = 100;

    public static void main(String[] args) throws Exception {
        System.out.println("Test started");
        String tableName = "rows_capacity_table";
        try (
                Connection connection = DriverManager.getConnection(DB_URL);
                Statement statement = connection.createStatement();
                PreparedStatement insertPrepStmt = connection.prepareStatement(insertRowPreparedStatement(tableName, COLUMNS_COUNT))) {

            System.out.println("Connection created");
            statement.executeUpdate(createTableQuery(tableName, COLUMNS_COUNT));

            int batchesAmount = ROWS_COUNT / BATCH_SIZE;
            for (int batchIndex = 0; batchIndex < batchesAmount; batchIndex++) {
                int currentBatchStart = batchIndex * BATCH_SIZE;
                int currentBatchEnd = (batchIndex + 1) * BATCH_SIZE;

                for (int currentRow = currentBatchStart; currentRow < currentBatchEnd; currentRow++) {
                    insertPrepStmt.clearParameters();
                    setColumnValues(insertPrepStmt, currentRow);
                    insertPrepStmt.addBatch();
                }

                long timestampBefore = System.currentTimeMillis();
                insertPrepStmt.executeBatch();
                long timestampAfter = System.currentTimeMillis();
                System.out.println("Insert " + BATCH_SIZE + " rows took " + (timestampAfter - timestampBefore) + " ms");

                retryable(() -> {
                    assertCountOfRowsIsCorrect(tableName, statement, currentBatchEnd);
                });

                sleep();
            }
        } catch (Exception ex){
            ex.printStackTrace(System.out);
            throw ex;
        }
    }

    private static void assertCountOfRowsIsCorrect(String tableName, Statement statement, int currentBatchEnd) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)){
            if (resultSet.next()){
                int actualRowsCount = resultSet.getInt(1);
                int expectedRowsCount = currentBatchEnd;
                System.out.println("Table " + tableName + " contains " + actualRowsCount + " rows");
                if (actualRowsCount != expectedRowsCount){
                    throw new IllegalStateException("Rows count in table " + tableName + " expected to be " + expectedRowsCount + ", but was " + actualRowsCount);
                }
            } else {
                throw new IllegalStateException("SELECT COUNT(*) FROM " + tableName + " didn't return any result");
            }
        }
    }

    private static void setColumnValues(PreparedStatement preparedStatement, int currentRow) throws SQLException {
        for (int columnIndex = 0; columnIndex < COLUMNS_COUNT; columnIndex++) {
            if (columnIndex == 0) {
                preparedStatement.setInt(1, currentRow);
            } else {
                preparedStatement.setString(columnIndex + 1, "row_" + currentRow + "_column_" + columnIndex);
            }
        }
    }

    public static void retryable(RunnableWithException action) throws Exception {
        int retried = 0;
        while (retried < RETRIES_COUNT){
            try {
                action.run();
                break;
            } catch (Exception ex){
                if (ex instanceof IllegalStateException){
                    throw ex;
                }
                ex.printStackTrace(System.out);
                retried++;
                sleep(RETRIES_SLEEP);
            }
        }
    }

    public static String createTableQuery(String tableName, int columnsAmount) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(");
        for (int i = 0; i < columnsAmount; i++) {
            if (i == 0) {
                sb.append("id INT PRIMARY KEY");
            } else {
                sb.append("column_").append(i).append(" VARCHAR");
            }

            if (i != columnsAmount - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String insertRowPreparedStatement(String tableName, int columnsAmount) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append("(");
        for (int i = 0; i < columnsAmount; i++) {
            if (i == 0) {
                sb.append("id");
            } else {
                sb.append("column_").append(i);
            }

            if (i != columnsAmount - 1) {
                sb.append(", ");
            }
        }
        sb.append(") VALUES (");
        for (int i = 0; i < columnsAmount; i++) {
            sb.append("?");

            if (i != columnsAmount - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static void sleep() {
        sleep(SLEEP);
    }

    public static void sleep(int sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ignored) {
        }
    }

    @FunctionalInterface
    public interface RunnableWithException
    {
        void run ( ) throws Exception;
    }

}
