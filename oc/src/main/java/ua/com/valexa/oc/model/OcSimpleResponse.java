package ua.com.valexa.oc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "oc_simple_response")
public class OcSimpleResponse {

    @Id
    private UUID id;
    private String irsEin;
    private String companyName;
    private String state;

    private String searchUrl;

    private int searchResultCount;

    private boolean isActiveInSearchResult;
    @Column(columnDefinition = "text")
    private String error;


}
