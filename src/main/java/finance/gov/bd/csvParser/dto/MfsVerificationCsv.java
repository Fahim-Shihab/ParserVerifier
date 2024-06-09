package finance.gov.bd.csvParser.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class MfsVerificationCsv {
    @CsvBindByName
    private BigInteger nid;
    @CsvBindByName
    private String mfsName;
    @CsvBindByName
    private String mobileNumber;
}
