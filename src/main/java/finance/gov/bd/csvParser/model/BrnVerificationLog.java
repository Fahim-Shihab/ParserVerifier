package finance.gov.bd.csvParser.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name = "brn_verification_log")
@Getter
@Setter
public class BrnVerificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "birth_reg_no", unique = true)
    private BigInteger birthRegNo;
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_bn")
    private String nameBn;
    @Column(name = "excel_name_en")
    private String excelNameEn;
    @Column(name = "excel_name_bn")
    private String excelNameBn;
    @Column(name = "brn_verify_status")
    private Integer brnVerifyStatus;
    @Column(name = "last_verify_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastVerifyAt;
}
