package finance.gov.bd.csvParser.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Data {
    private String requestId;
    private String name;
    private String nameEn;
    private String gender;
    private String bloodGroup;
    private String father;
    private String mother;
    private String nationalId;
    private String pin;
    private String photo;
    private String religion;
    private String nidFather;
    private String nidMother;
    private Address permanentAddress;
    private Address presentAddress;
    private Object MobileAll;
}
