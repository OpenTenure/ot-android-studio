package org.fao.sola.clients.android.opentenure.model;

import org.fao.sola.clients.android.opentenure.network.response.ProjectResponse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Project extends Model {
    String id;
    String displayName;
    String boundary;
    String tilesServerType;
    String tilesServerUrl;
    String tilesLayerName;
    String languageCode;
    Boolean active = true;

    Boolean geomRequired;
    private static final String fields = "ID, DISPLAY_NAME, BOUNDARY, TILES_SERVER_TYPE, TILES_SERVER_URL, TILES_LAYER_NAME, LANGUAGE_CODE, ACTIVE, GEOM_REQUIRED";

    public Project(){ }

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

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    public String getTilesServerType() {
        return tilesServerType;
    }

    public void setTilesServerType(String tilesServerType) {
        this.tilesServerType = tilesServerType;
    }

    public String getTilesServerUrl() {
        return tilesServerUrl;
    }

    public void setTilesServerUrl(String tilesServerUrl) {
        this.tilesServerUrl = tilesServerUrl;
    }

    public String getTilesLayerName() {
        return tilesLayerName;
    }

    public void setTilesLayerName(String tilesLayerName) {
        this.tilesLayerName = tilesLayerName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Boolean getGeomRequired() {
        return geomRequired;
    }

    public void setGeomRequired(Boolean geomRequired) {
        this.geomRequired = geomRequired;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public int insert() {
        try {
            PreparedStatement statement = prepareStatement("INSERT INTO PROJECT (" + fields + ") VALUES (?,?,?,?,?,?,?,?,?)");
            statement.setString(1, getId());
            statement.setString(2, getDisplayName());
            statement.setString(3, getBoundary());
            statement.setString(4, getTilesServerType());
            statement.setString(5, getTilesServerUrl());
            statement.setString(6, getTilesLayerName());
            statement.setString(7, getLanguageCode());
            statement.setBoolean(8, getActive());
            statement.setBoolean(9, getGeomRequired());

            return executeStatement(statement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public static Project getProject(String id) {
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            statement = prepareStatement("SELECT " + fields + " FROM PROJECT WHERE ID=?");
            statement.setString(1, id);
            rs = executeSelect(statement);

            if(rs != null) {
                if (rs.next()) {
                    return Project.RsToProject(rs);
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

    private static Project RsToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getString(1));
        project.setDisplayName(rs.getString(2));
        project.setBoundary(rs.getString(3));
        project.setTilesServerType(rs.getString(4));
        project.setTilesServerUrl(rs.getString(5));
        project.setTilesLayerName(rs.getString(6));
        project.setLanguageCode(rs.getString(7));
        project.setActive(rs.getBoolean(8));
        project.setGeomRequired(rs.getBoolean(9));
        return project;
    }

    public static List<Project> getProjects(boolean onlyActive) {
        List<Project> projects = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            String where = "";
            if(onlyActive){
                where = " where ACTIVE = true";
            }

            statement = prepareStatement("SELECT " + fields + " FROM PROJECT" + where + " ORDER BY DISPLAY_NAME");
            rs = executeSelect(statement);

            if(rs != null) {
                while (rs.next()) {
                    projects.add(Project.RsToProject(rs));
                }
            }
            return projects;
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
        return projects;
    }

    public int update() {
        try {
            PreparedStatement statement = prepareStatement("UPDATE PROJECT SET DISPLAY_NAME=?, BOUNDARY=?, TILES_SERVER_TYPE=?, TILES_SERVER_URL=?, TILES_LAYER_NAME=?, LANGUAGE_CODE=?, ACTIVE=?, GEOM_REQUIRED=? WHERE ID = ?");
            statement.setString(1, getDisplayName());
            statement.setString(2,  getBoundary());
            statement.setString(3,  getTilesServerType());
            statement.setString(4,  getTilesServerUrl());
            statement.setString(5,  getTilesLayerName());
            statement.setString(6,  getLanguageCode());
            statement.setBoolean(7,  getActive());
            statement.setBoolean(8,  getGeomRequired());
            statement.setString(9,  getId());

            return executeStatement(statement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public static void update(List<ProjectResponse> projects) {
        if (projects != null && (projects.size() > 0)) {
            PreparedStatement statement = prepareStatement("UPDATE PROJECT SET ACTIVE=false WHERE ACTIVE= true");
            executeStatement(statement);

            for (Iterator<ProjectResponse> iterator = projects.iterator(); iterator.hasNext();) {
                ProjectResponse response = iterator.next();

                try {
                    Project project = new Project();
                    project.setId(response.getId());
                    project.setDisplayName(response.getDisplayName());
                    project.setBoundary(response.getBoundary());
                    project.setLanguageCode(response.getLanguageCode());
                    project.setTilesServerType(response.getTilesServerType());
                    project.setTilesServerUrl(response.getTilesServerUrl());
                    project.setTilesLayerName(response.getTilesLayerName());
                    project.setGeomRequired(response.isGeomRequired());
                    project.setActive(true);

                    if (Project.getProject(response.getId()) == null) {
                        project.insert();
                    }
                    else {
                        project.update();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
