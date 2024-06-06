package finance.gov.bd.csvParser.dto.response;

import finance.gov.bd.csvParser.common.Success;
import finance.gov.bd.csvParser.common.Error;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NidResponse {
    private String status;
    private String statusCode;
    private Success success ;
    private Error error;
    private Object mobileAll;
    private Object tINAccount;
    private Object nagadAccount;
    private Object nagadAccountTransaction;
    private Object bkashAccount;
    private Object bkashAccountTransaction;
    private Object rocketAccount;
    private Object rocketAccountTransaction;
    private Object upayAccount;
    private Object upayAccountTransaction;
}
