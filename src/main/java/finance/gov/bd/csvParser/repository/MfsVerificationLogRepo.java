package finance.gov.bd.csvParser.repository;

import finance.gov.bd.csvParser.model.MfsVerificationLog;
import finance.gov.bd.csvParser.model.NidVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface MfsVerificationLogRepo extends JpaRepository<MfsVerificationLog, Integer> {
    public List<MfsVerificationLog> findByNid(BigInteger nid);
    public List<MfsVerificationLog> findByAlternateNid(BigInteger alternateNid);
    @Query(value = "select * from mfs_verification_log where 1=1 " +
            " and nid is not null and mobile_number is not null and mfs_name is not null " +
            " and mfs_verify_status = :mfsVerifyStatus order by id limit :size", nativeQuery = true)
    public List<MfsVerificationLog> findByMfsVerifyStatus(@Param("mfsVerifyStatus") Integer mfsVerifyStatus, @Param("size") Integer size);

    @Query(value = "select count(*) from mfs_verification_log where 1=1 and nid is not null " +
            " and mfs_name is not null and mobile_number is not null and mfs_verify_status = :mfsVerifyStatus", nativeQuery = true)
    public long countByNidVerifyStatus(@Param("mfsVerifyStatus") Integer mfsVerifyStatus);
}
