package finance.gov.bd.csvParser.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ServiceResponse {
    protected boolean opResult;
    protected String opMsg;
    protected Integer opCode;

    public ServiceResponse() {
        this.opResult = true;
        this.opMsg = "Success";
        this.opCode = 200;
    }
}
