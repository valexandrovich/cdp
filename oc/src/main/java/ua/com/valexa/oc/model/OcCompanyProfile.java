package ua.com.valexa.oc.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "oc_company_profile")
public class OcCompanyProfile {

    @Id
    private UUID id;
    private String irsEin;

    private String companyNumber;
    private String incorparatedDate;
    private String agentName;
    private String agentAddress;
    private String companyType;
    private String jurisdiction;
    private String directors;
    private String registryPage;

    @Column(columnDefinition = "TEXT")
    private String searchUrl;

    @Column(columnDefinition = "TEXT")
    private String error;

    private Integer countResults;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public String toString() {
        return "OcCompanyProfile{" +
                "id=" + id +
                ", irsEin='" + irsEin + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", incorparatedDate='" + incorparatedDate + '\'' +
                ", agentName='" + agentName + '\'' +
                ", agentAddress='" + agentAddress + '\'' +
                ", companyType='" + companyType + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                ", directors='" + directors + '\'' +
                ", registryPage='" + registryPage + '\'' +
                ", searchUrl='" + searchUrl + '\'' +
                '}';
    }
}
