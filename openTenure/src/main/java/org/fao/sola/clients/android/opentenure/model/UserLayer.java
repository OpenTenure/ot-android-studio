package org.fao.sola.clients.android.opentenure.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserLayer extends Model {
    String id;
    String displayName;
    String filePath;
    Integer order;
    Boolean enabled;
    private static final String fields = "ID, DISPLAY_NAME, FILE_PATH, LAYER_ORDER, ENABLED";

    public UserLayer(){ }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int insert() {
        try {
            PreparedStatement statement = prepareStatement("INSERT INTO USER_LAYER (" + fields + ") VALUES (?,?,?,?,?)");
            statement.setString(1, getId());
            statement.setString(2, getDisplayName());
            statement.setString(3, getFilePath());
            statement.setInt(4, getOrder());
            statement.setBoolean(5, getEnabled());

            return executeStatement(statement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public static UserLayer getUserLayer(String id) {
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            statement = prepareStatement("SELECT " + fields + " FROM USER_LAYER WHERE ID=?");
            statement.setString(1, id);
            rs = executeSelect(statement);

            if(rs != null) {
                if (rs.next()) {
                    return UserLayer.RsToProject(rs);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    if(statement.getConnection() != null && !statement.getConnection().isClosed()) {
                        statement.getConnection().close();
                    }
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
        return null;
    }

    public static int getLastOrder() {
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            statement = prepareStatement("SELECT MAX(LAYER_ORDER) FROM USER_LAYER");
            rs = executeSelect(statement);

            if(rs != null) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    if(statement.getConnection() != null && !statement.getConnection().isClosed()) {
                        statement.getConnection().close();
                    }
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
        return 0;
    }

    private static UserLayer RsToProject(ResultSet rs) throws SQLException {
        UserLayer userLayer = new UserLayer();
        userLayer.setId(rs.getString(1));
        userLayer.setDisplayName(rs.getString(2));
        userLayer.setFilePath(rs.getString(3));
        userLayer.setOrder(rs.getInt(4));
        userLayer.setEnabled(rs.getBoolean(5));
        return userLayer;
    }

    public static List<UserLayer> getUserLayers(boolean orderAsc) {
        List<UserLayer> userLayers = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            String orderDirection = "";
            if(!orderAsc){
                orderDirection = " DESC";
            }

            statement = prepareStatement("SELECT " + fields + " FROM USER_LAYER ORDER BY LAYER_ORDER" + orderDirection);
            rs = executeSelect(statement);

            if(rs != null) {
                while (rs.next()) {
                    userLayers.add(UserLayer.RsToProject(rs));
                }
            }
            return userLayers;
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    if(statement.getConnection() != null && !statement.getConnection().isClosed()) {
                        statement.getConnection().close();
                    }
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
        return userLayers;
    }

    public int update() {
        try {
            PreparedStatement statement = prepareStatement("UPDATE USER_LAYER SET DISPLAY_NAME=?, FILE_PATH=?, LAYER_ORDER=?, ENABLED=? WHERE ID = ?");
            statement.setString(1, getDisplayName());
            statement.setString(2,  getFilePath());
            statement.setInt(3,  getOrder());
            statement.setBoolean(4,  getEnabled());
            statement.setString(5,  getId());

            return executeStatement(statement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public int delete() {
        return UserLayer.delete(getId());
    }

    public static int delete(String id) {
        try {
            PreparedStatement statement = prepareStatement("DELETE FROM USER_LAYER WHERE ID = ?");
            statement.setString(1,  id);

            return executeStatement(statement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }
}
