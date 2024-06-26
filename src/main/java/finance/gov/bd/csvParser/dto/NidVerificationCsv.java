package finance.gov.bd.csvParser.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NidVerificationCsv {
    @CsvBindByName
    private String nid;
    @CsvBindByName
    private String dateOfBirth;
    @CsvBindByName
    private String nameEn;
    @CsvBindByName
    private String nameBn;
}
