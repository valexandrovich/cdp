package ua.com.valexa.oc.model;


import com.opencsv.bean.CsvBindByName;

import java.util.Objects;

public class OcUserProfile {

    @CsvBindByName(column = "userName")
    private String userName;
    @CsvBindByName(column = "userPassword")
    private String userPassword;
    @CsvBindByName(column = "proxyHost")
    private String proxyHost;
    @CsvBindByName(column = "proxyPort")
    private Integer proxyPort;
    @CsvBindByName(column = "proxyUser")
    private String proxyUser;
    @CsvBindByName(column = "proxyPassword")
    private String proxyPassword;

    public OcUserProfile(String userName, String userPassword, String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
    }

    public OcUserProfile() {
    }

    @Override
    public String toString() {
        return "OcUserProfile{" +
                "userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", proxyHost='" + proxyHost + '\'' +
                ", proxyPort=" + proxyPort +
                ", proxyUser='" + proxyUser + '\'' +
                ", proxyPassword='" + proxyPassword + '\'' +
                '}';
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OcUserProfile that = (OcUserProfile) o;
        return Objects.equals(userName, that.userName) && Objects.equals(userPassword, that.userPassword) && Objects.equals(proxyHost, that.proxyHost) && Objects.equals(proxyPort, that.proxyPort) && Objects.equals(proxyUser, that.proxyUser) && Objects.equals(proxyPassword, that.proxyPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, userPassword, proxyHost, proxyPort, proxyUser, proxyPassword);
    }
}
