package finance.gov.bd.csvParser.repository;

import finance.gov.bd.csvParser.model.SrNeedVerify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SrNeedVerifyRepo extends JpaRepository<SrNeedVerify, Integer> {

    @Query(value = "select * from sr_need_verify where 1=1 and verification_status in (0,3) " +
            " limit :size offset :offset", nativeQuery = true)
    List<SrNeedVerify> findByStatusAndOffset(
                                                                   @Param("size") Long size, @Param("offset") Long offset);

    @Query(value = "select count(*) from sr_need_verify where verification_status in (0,3)", nativeQuery = true)
    Long countVerificationPending();

}
