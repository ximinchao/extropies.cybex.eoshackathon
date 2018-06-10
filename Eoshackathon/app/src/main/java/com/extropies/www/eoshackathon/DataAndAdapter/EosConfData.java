package com.extropies.www.eoshackathon.DataAndAdapter;

/**
 * Created by inst on 18-6-9.
 */

public class EosConfData {
    private String confName;
    private int confId;
    private String confOrganizer;
    private String confFee;

    public EosConfData(String confName, int confId, String confOrganizer, String confFee) {
        this.confName = confName;
        this.confId = confId;
        this.confOrganizer = confOrganizer;
        this.confFee = confFee;
    }

    public void setConfName(String name) {
        this.confName = name;
    }

    public void setConfId(int confId) {
        this.confId = confId;
    }

    public void setConfOrganizer(String confOrganizer) {
        this.confOrganizer = confOrganizer;
    }

    public void setConfFee(String confFee) {
        this.confFee = confFee;
    }

    public String getConfName() {
        return this.confName;
    }

    public String getConfId() {
        return Integer.toString(this.confId);
    }

    public String getConfOrganizer() {
        return this.confOrganizer;
    }
    public String getConfFee() {
        return this.confFee;
    }
}
