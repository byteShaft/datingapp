package com.mydatingapp.model.base.classes;

/**
 * Created by sardar on 1/16/15.
 */
public class SkUser {
    private int userId;
    private String displayName;
    private String avatarUrl;
    private String bigAvatarUrl;
    private String origAvatarUrl;
    private Boolean isSuspended;
    private Boolean isApproved;
    private Boolean isEmailVerified;
    private Boolean isAvatarApproved;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public String getBigAvatarUrl() {
        return bigAvatarUrl;
    }

    public void setBigAvatarUrl(String bigAvatarUrl) {
        this.bigAvatarUrl = bigAvatarUrl;
    }

    public String getOrigAvatarUrl() {
        return origAvatarUrl;
    }

    public void setOrigAvatarUrl(String origAvatarUrl) {
        this.origAvatarUrl = origAvatarUrl;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public Boolean getIsAvatarApproved() {
        return isAvatarApproved;
    }

    public void setIsAvatarApproved(Boolean isAvatarApproved) {
        this.isAvatarApproved = isAvatarApproved;
    }
}
