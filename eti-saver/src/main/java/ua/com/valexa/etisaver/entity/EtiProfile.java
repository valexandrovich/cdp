package ua.com.valexa.etisaver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "eti_profile")
@Data
public class EtiProfile {

    @Id
    private String irsEin;

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String orgName;

    private String name;

    private String doingBusinessAs;
    private String typeOfBusiness;

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