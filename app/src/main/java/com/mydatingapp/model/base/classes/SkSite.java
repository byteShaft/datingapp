package com.mydatingapp.model.base.classes;

/**
 * Created by sardar on 1/16/15.
 */
public class SkSite {
    private String siteName;
    private String facebookAppId;
    private Boolean userApprove;
    private Boolean confirmEmail;
    private Boolean maintenance;
    private int serverTimestamp;
    private String serverTimezone;

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
    }

    public Boolean getUserApprove() {
        return userApprove;
    }

    public void setUserApprove(Boolean userApprove) {
        this.userApprove = userApprove;
    }

    public Boolean getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(Boolean confirmEmail) {
        this.confirmEmail = confirmEmail;
    }

    public Boolean getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Boolean maintenance) {
        this.maintenance = maintenance;
    }

    public int getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(int serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public String getServerTimezone() {
        return serverTimezone;
    }

    public void setServerTimezone(String serverTimezone) {
        this.serverTimezone = serverTimezone;
    }
}
