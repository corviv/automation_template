package com.github.corviv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;
import org.testng.annotations.Listeners;

@Listeners(LoggerListener.class)
public class JdbcUtils {

    private static final Logger logger = LoggerFactory.getLogger("JDBCUtils");

    public static final String DEF_URL = "jdbc:postgresql://127.0.0.1:5555/";
    public static final String DEF_DB = "jdbc:sqlite:C:\\defdb.db";
    private Connection connection = null;

    public JdbcUtils() {
        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean connectDB(String url) {
        return connectDB(url, "", "");
    }

    public boolean connectDB(String url, String user, String pass) {

        try {
            connection = DriverManager.getConnection(url, user, pass);

            if (connection == null) {
                logger.debug("Failed to connect to the '{}'!", url);
                return false;
            }
            logger.debug("Connected to the '{}'!", url);
            return true;

        } catch (SQLException e) {
            logger.error("SQL State: {}\n{}", e.getSQLState(), e.getMessage());
        }

        return false;
    }

    public boolean checkDBExists(String dbName) {

        try {
            ResultSet resultSet = connection.getMetaData().getCatalogs();

            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                if (databaseName.equals(dbName)) {
                    return true;
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return false;
    }

    public static String getValueInSqliteDb(String dbName, String dbQuery, String column) {
        try {
            DriverManager.registerDriver(new JDBC());
            Connection connection = DriverManager.getConnection(dbName);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(dbQuery);
            while (resultSet.next()) {
                String value = resultSet.getString(column);
                resultSet.close();
                statement.close();
                connection.close();
                return value;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}