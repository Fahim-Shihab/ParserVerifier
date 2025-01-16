package finance.gov.bd.csvParser.repository;

import finance.gov.bd.csvParser.model.BirthRegInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BirthRegInfoRepo extends JpaRepository<BirthRegInfo, Integer> {
}
