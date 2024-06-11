package finance.gov.bd.csvParser.repository;

import finance.gov.bd.csvParser.model.NidVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface NidVerificationLogRepo extends JpaRepository<NidVerificationLog, Integer> {
    public NidVerificationLog findByNid(BigInteger nid);
    public NidVerificationLog findByAlternateNid(BigInteger alternateNid);
    @Query(value = "select * from nid_verification_log where 1=1 and nid is not null and nid_verify_status = :nidVerifyStatus order by id limit :size", nativeQuery = true)
    public List<NidVerificationLog> findByNidVerifyStatus(@Param("nidVerifyStatus") Integer nidVerifyStatus, @Param("size") Integer size);
    @Query(value = "select * from nid_verification_log where 1=1 and nid is not null and file_id=:fileId " +
            " and nid_verify_status = :nidVerifyStatus order by id limit :size", nativeQuery = true)
    public List<NidVerificationLog> findByNidVerifyStatusAndFileId(@Param("nidVerifyStatus") Integer nidVerifyStatus,
                                                                   @Param("fileId") Integer fileId,
                                                                   @Param("size") Integer size);

    @Query(value = "select count(*) from nid_verification_log where 1=1 and nid is not null and nid_verify_status = :nidVerifyStatus", nativeQuery = true)
    public long countByNidVerifyStatus(@Param("nidVerifyStatus") Integer nidVerifyStatus);
}
