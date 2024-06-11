package finance.gov.bd.csvParser.repository;

import finance.gov.bd.csvParser.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileInfoRepository extends JpaRepository<FileInfo, Integer> {
}
