package org.fao.sola.clients.android.opentenure.network.response;

public class RefDataResponse {
    String code;
    String displayValue;
    String description;
    String status;

    public RefDataResponse(){ }

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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
