package ua.com.valexa.cdpcommon.dto;

import lombok.Data;

@Data
public class EtiProfileDto {
    private String orgName;
    private String irsEin;
    private String doingBusinessAs;
    private String typeOfBusiness;

    private String description;

    private String businessProfile;
    private String businessAddress;
    private String businessAddressLine2;
    private String businessCity;
    private String businessState;
    private String businessZip;

    private String mailingAddress;
    private String mailingAddress2;
    private String mailingCity;
    private String mailingState;
    private String mailingZIP;

    private String cik;
    private String endOfFiscalYear;
    private String incState;
    private String incSubDiv;
    private String incCountry;
    private String fillingYear;

    private String link;

}
