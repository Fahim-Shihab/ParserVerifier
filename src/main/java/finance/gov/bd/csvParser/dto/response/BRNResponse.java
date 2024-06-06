package finance.gov.bd.csvParser.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BRNResponse {
    public String ubrn;
    public String dob;
    public String personname;
    public String personnameEn;
    public String mothername;
    public String mothernameEn;
    public String motherNationality;
    public String motherNationalityEn;
    public String fathername;
    public String fathernameEn;
    public String fatherNationality;
    public String fatherNationalityEn;
    public String sex;
    public String placeofbirth;
    public String placeofbirthEn;
    private String errorMsg;
    private boolean matchFound;
}
