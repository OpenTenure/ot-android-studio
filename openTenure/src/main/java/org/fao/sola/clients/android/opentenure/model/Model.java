package org.fao.sola.clients.android.opentenure.model;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Model {

    protected static PreparedStatement prepareStatement(String sql) {
        Connection localConnection = null;
        PreparedStatement statement = null;

        try {
            localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
            statement = localConnection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return statement;
    }

    protected static int executeStatement(PreparedStatement statement) {
        try {
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if(statement.getConnection() != null && !statement.getConnection().isClosed()) {
                    statement.getConnection().close();
                }
                statement.close();
            } catch (SQLException e) {
            }
        }
        return 0;
    }

    protected static ResultSet executeSelect(PreparedStatement statement) {
        try {
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
