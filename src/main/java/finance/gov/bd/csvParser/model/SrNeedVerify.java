package finance.gov.bd.csvParser.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sr_need_verify")
@Getter
@Setter
@NoArgsConstructor
public class SrNeedVerify {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "sr_beneficiary_id")
    private Integer srBeneficiaryId;
    @Column(name = "guardian_id")
    private Integer guardianId;
    @Column(name = "nid", length = 17)
    private String nid;
    @Column(name = "smart_id", length = 10)
    private String smartId;
    @Column(name = "birth_reg_no", length = 17)
    private String birthRegNo;
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Column(name = "verification_status")
    private int verificationStatus;
    @Column(name = "verification_run_at")
    private Date verificationRunAt;
}
