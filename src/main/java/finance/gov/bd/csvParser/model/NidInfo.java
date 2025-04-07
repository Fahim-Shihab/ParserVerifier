package finance.gov.bd.csvParser.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import finance.gov.bd.csvParser.common.Address;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "nid_info")
@Getter
@Setter
@NoArgsConstructor
//@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "json", typeClass = JsonType.class)
public class NidInfo {
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
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_bn")
    private String nameBn;
    @Column(name = "gender")
    private String gender;
    @Column(name = "religion")
    private String religion;
    @Column(name = "blood_group")
    private String bloodGroup;
    @Column(name = "father_name")
    private String fatherName;
    @Column(name = "father_nid")
    private String fatherNid;
    @Column(name = "mother_name")
    private String motherName;
    @Column(name = "mother_nid")
    private String motherNid;
    @Column(name = "photo")
    private String photo;

    @Type(type = "json")
    @Column(name = "present_address", columnDefinition = "json")
    private Address presentAddress;

    @Type(type = "json")
    @Column(name = "permanent_address", columnDefinition = "json")
    private Address permanentAddress;

    @Column(name = "verified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date verifiedAt;
}
