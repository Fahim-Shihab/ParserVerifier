package finance.gov.bd.csvParser.dto.response;

import finance.gov.bd.csvParser.common.BaseModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BECResponse extends BaseModel {
    private boolean matchFound;
    private BECNidResponse nidData;
    private String errorCode;
    private boolean operationResult;
    private String errorMsg;
}
