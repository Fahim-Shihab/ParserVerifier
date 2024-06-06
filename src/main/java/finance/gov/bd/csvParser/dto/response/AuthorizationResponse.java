package finance.gov.bd.csvParser.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorizationResponse {
    private String access_token;
    private String token_type;
    private Integer expires_in;
    private String refresh_token;
    private String client_id;
    private String userName;
    private String issued;
    private String expires;
}
