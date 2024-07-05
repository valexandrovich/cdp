package ua.com.valexa.cdpcommon.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OcRequest {
    private String irsEin;
    private String companyName;
    private String state;
    private Integer maxRetries;

    public OcRequest(String companyName, String state, Integer maxRetries) {
        this.companyName = companyName;
        this.state = state;
        this.maxRetries = maxRetries;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OcRequest{");
        sb.append("irsEin='").append(irsEin).append('\'');
        sb.append("companyName='").append(companyName).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", maxRetries=").append(maxRetries);
        sb.append('}');
        return sb.toString();
    }
}
