package finance.gov.bd.csvParser.repository;

import finance.gov.bd.csvParser.model.BrnVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface BrnVerificationLogRepo extends JpaRepository<BrnVerificationLog, Integer> {
    public BrnVerificationLog findByBirthRegNo(String birthRegNo);
    @Query(value = "select * from brn_verification_log where 1=1 and birth_reg_no is not null and brn_verify_status = :brnVerifyStatus order by id limit :size", nativeQuery = true)
    public List<BrnVerificationLog> findByBrnVerifyStatus(@Param("brnVerifyStatus") Integer brnVerifyStatus, @Param("size") Integer size);
}
