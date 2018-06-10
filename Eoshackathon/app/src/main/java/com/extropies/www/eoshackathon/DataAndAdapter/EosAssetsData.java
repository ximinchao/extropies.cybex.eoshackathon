package com.extropies.www.eoshackathon.DataAndAdapter;

/**
 * Created by inst on 18-6-9.
 */

public class EosAssetsData {
    private String name;
    private String account;
    private String accountY;

    public EosAssetsData(String name, String account, String accountY) {
        this.name = name;
        this.account = account;
        this.accountY = accountY;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAccountY(String accountY) {
        this.accountY = accountY;
    }

    public String getName() {
        return this.name; 
    }

    public String getAccount() {
        return this.account;
    }

    public String getAccountY() {
        return this.accountY;
    }
}
