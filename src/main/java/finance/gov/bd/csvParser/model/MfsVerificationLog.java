package finance.gov.bd.csvParser.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name = "mfs_verification_log")
@Getter
@Setter
public class MfsVerificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "nid")
    private BigInteger nid;
    @Column(name = "alternate_nid")
    private BigInteger alternateNid;
    @Column(name = "mobile_number")
    private String mobileNumber;
    @Column(name = "mfs_name")
    private String mfsName;
    @Column(name = "account_exist")
    private Integer accountExist;
    @Column(name = "nid_matched")
    private Integer nidMatched;
    @Column(name = "nid_mobile_matched")
    private Integer nidMobileMatched;
    @Column(name = "mfs_verify_status")
    private Integer mfsVerifyStatus;
    @Column(name = "last_verify_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastVerifyAt;
}
