package finance.gov.bd.csvParser.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    private String checkBy;
    private Integer verifyStatus;
    private Integer size;
    private Integer fileId;
}
