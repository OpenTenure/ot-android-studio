package org.fao.sola.clients.android.opentenure.network.response;

public class ProjectResponse {
    private String id;
    private String displayName;
    private String boundary;
    private String languageCode;
    private String tilesServerType;
    private String tilesServerUrl;
    private String tilesLayerName;
    private boolean geomRequired;

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

    public boolean isGeomRequired() {
        return geomRequired;
    }

    public void setGeomRequired(boolean geomRequired) {
        this.geomRequired = geomRequired;
    }
}
