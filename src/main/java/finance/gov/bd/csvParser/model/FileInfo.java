package finance.gov.bd.csvParser.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "file_info")
@Getter
@Setter
public class FileInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_extension")
    private String fileExtension;
    @Column(name = "import_type")
    private String importType;
    @Column(name="total_row_count")
    private Integer totalRowCount;
    @Column(name="import_count")
    private Integer importCount;
    @Column(name = "import_started_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date importStartedAt;
    @Column(name = "import_ended_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date importEndedAt;
}
