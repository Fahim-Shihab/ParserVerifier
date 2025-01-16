package finance.gov.bd.csvParser.repository;

import finance.gov.bd.csvParser.model.NidInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NidInfoRepo extends JpaRepository<NidInfo, Integer> {
}
