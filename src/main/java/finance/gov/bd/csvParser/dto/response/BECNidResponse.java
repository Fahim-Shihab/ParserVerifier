package finance.gov.bd.csvParser.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BECNidResponse {
    private String name;
    private String nameEn;
    private String nid;
    private String pin;
    private String dob;
    private String gender;
    private String photo;
    private String mother;
    private String father;
    private String spouse;
    private String presentAddress;
    private String permanentAddress;
    private int age;
    private String currentDateTime;
    private String nid10;
    private String nid17;
}
