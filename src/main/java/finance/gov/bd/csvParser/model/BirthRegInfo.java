package finance.gov.bd.csvParser.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "birth_reg_info")
@Getter
@Setter
@NoArgsConstructor
public class BirthRegInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "sr_beneficiary_id")
    private Integer srBeneficiaryId;
    @Column(name = "birth_reg_no", length = 17)
    private String birthRegNo;
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_bn")
    private String nameBn;
    @Column(name = "father_name_en")
    private String fatherNameEn;
    @Column(name = "father_name_bn")
    private String fatherNameBn;
    @Column(name = "father_nationality_en")
    private String fatherNationalityEn;
    @Column(name = "father_nationality_bn")
    private String fatherNationalityBn;
    @Column(name = "mother_name_en")
    private String motherNameEn;
    @Column(name = "mother_name_bn")
    private String motherNameBn;
    @Column(name = "mother_nationality_en")
    private String motherNationalityEn;
    @Column(name = "mother_nationality_bn")
    private String motherNationalityBn;
    @Column(name = "gender")
    private String gender;
    @Column(name = "place_of_birth_en")
    private String placeOfBirthEn;
    @Column(name = "place_of_birth_bn")
    private String placeOfBirthBn;
    @Column(name = "verified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date verifiedAt;
}
