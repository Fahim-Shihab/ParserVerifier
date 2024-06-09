package finance.gov.bd.csvParser.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MFSResponse {
    private Boolean AccountExist;
    private Boolean NIDMatched;
    private Boolean NIDMobileMatched;
    private String message;
    private int errorCode;
}
