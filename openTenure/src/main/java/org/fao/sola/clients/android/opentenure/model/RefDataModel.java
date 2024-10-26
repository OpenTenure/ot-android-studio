package org.fao.sola.clients.android.opentenure.model;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.network.response.RefDataResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class RefDataModel extends Model {
    String code;
    String displayValue;
    String description;
    Boolean active = true;

    public RefDataModel(){ }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getDisplayValue() {
        return displayValue;
    }
    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public abstract int insert();

    public abstract int update();

    protected static <T extends RefDataModel> int insert(String tableName, T refData) {
        if(tableName == null || refData == null){
            return 0;
        }
        try {
            PreparedStatement statement = prepareStatement("INSERT INTO " + tableName + " (CODE, DISPLAY_VALUE, DESCRIPTION, ACTIVE) VALUES (?,?,?,?)");
            statement.setString(1, refData.getCode());
            statement.setString(2, refData.getDisplayValue());
            statement.setString(3, refData.getDescription());
            statement.setBoolean(4, refData.getActive());

            return executeStatement(statement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    protected static <T extends RefDataModel> RefDataModel getItem(String tableName, Class<T> klass, String code) {
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            statement = prepareStatement("SELECT CODE, DISPLAY_VALUE, DESCRIPTION, ACTIVE FROM " + tableName + " WHERE CODE=?");
            statement.setString(1, code);
            rs = executeSelect(statement);

            if(rs != null) {
                if (rs.next()) {
                    T item = klass.newInstance();
                    item.setCode(rs.getString(1));
                    item.setDisplayValue(rs.getString(2));
                    item.setDescription(rs.getString(3));
                    item.setActive(rs.getBoolean(4));
                    return item;
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

    protected static String getDisplayValueByCode(String tableName, String code) {
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            statement = prepareStatement("SELECT DISPLAY_VALUE FROM " + tableName + " WHERE CODE=?");
            statement.setString(1, code);
            rs = executeSelect(statement);

            if(rs != null) {
                if (rs.next()) {
                    return rs.getString(1);
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

    protected String getCodeByDisplayValue(String tableName, String value) {
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            statement = prepareStatement("SELECT CODE FROM " + tableName + " WHERE DISPLAY_VALUE LIKE  '%' || ? || '%'");
            statement.setString(1, value);
            rs = statement.executeQuery();

            while (rs.next()) {
                return rs.getString(1);
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
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

    protected static <T extends RefDataModel> Map<String,String> getKeyValueMap(String tableName, Class<T> klass, boolean onlyActive) {
        List<T> list = getItems(tableName, klass, onlyActive, true, false);

        Map<String,String> keyValueMap = new LinkedHashMap<>();

        for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
            T item = iterator.next();
            keyValueMap.put(item.getCode(),item.getDisplayValue());
        }
        return keyValueMap;
    }

    protected static <T extends RefDataModel> Map<String,String> getValueKeyMap(String tableName, Class<T> klass, boolean onlyActive) {
        List<T> list = getItems(tableName, klass, onlyActive, true, false);

        Map<String,String> keyValueMap = new LinkedHashMap<>();

        for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
            T item = iterator.next();
            keyValueMap.put(item.getDisplayValue(),item.getCode());
        }
        return keyValueMap;
    }

    protected static <T extends RefDataModel>  int getIndexByCode(String tableName, Class<T> klass, String code, boolean onlyActive) {
        List<T> list = getItems(tableName, klass, onlyActive, false, false);
        int i = 0;

        for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
            T item = iterator.next();
            if (item.getCode().equals(code)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    protected static <T extends RefDataModel> List<T> getItems(String tableName, Class<T> klass, boolean onlyActive, boolean localized, boolean addDummy) {
        List<T> types = new ArrayList<T>();
        ResultSet rs = null;
        PreparedStatement statement = null;
        DisplayNameLocalizer localizer = null;

        try {
            if(localized) {
                localizer = new DisplayNameLocalizer();
            }

            String where = "";
            if(onlyActive){
                where = " where ACTIVE = true";
            }

            statement = prepareStatement("SELECT CODE, DISPLAY_VALUE, DESCRIPTION, ACTIVE FROM " + tableName + where + " ORDER BY DISPLAY_VALUE");
            rs = executeSelect(statement);

            if(rs != null) {
                while (rs.next()) {
                    T item = klass.newInstance();
                    item.setCode(rs.getString(1));
                    if(localized) {
                        item.setDisplayValue(localizer.getLocalizedDisplayName(rs.getString(2)));
                    } else {
                        item.setDisplayValue(rs.getString(2));
                    }
                    item.setDescription(rs.getString(3));
                    item.setActive(rs.getBoolean(4));
                    types.add(item);
                }
            }

            if(addDummy) {
                T item = klass.newInstance();
                item.setCode(null);
                item.setDisplayValue("");
                types.add(0, item);
            }
            return types;
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
        return types;
    }

    protected static <T extends RefDataModel> int update(String tableName, T refData) {
        if(tableName == null || refData == null){
            return 0;
        }

        try {
            PreparedStatement statement = prepareStatement("UPDATE " + tableName + " SET CODE=?, DISPLAY_VALUE=?, DESCRIPTION=?, ACTIVE=? WHERE CODE = ?");
            statement.setString(1, refData.getCode());
            statement.setString(2,  refData.getDisplayValue());
            statement.setString(3,  refData.getDescription());
            statement.setBoolean(4,  refData.getActive());
            statement.setString(5,  refData.getCode());

            return executeStatement(statement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    protected static <T extends RefDataModel, S extends RefDataResponse> void update(List<S> types, String tableName, Class<T> klass) {
        if (types != null && (types.size() > 0) && tableName != null) {
            PreparedStatement statement = prepareStatement("UPDATE " + tableName + " SET ACTIVE=false WHERE ACTIVE= true");
            executeStatement(statement);

            for (Iterator<S> iterator = types.iterator(); iterator.hasNext();) {
                RefDataResponse response = iterator.next();

                try {
                    T item = klass.newInstance();

                    item.setDescription(response.getDescription());
                    item.setCode(response.getCode());
                    item.setDisplayValue(response.getDisplayValue());
                    item.setActive(response.getStatus().equalsIgnoreCase("c"));
                    if (item.getItem(tableName, klass, response.getCode()) == null) {
                        item.insert();
                    }
                    else {
                        item.update();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
