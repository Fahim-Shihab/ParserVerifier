package finance.gov.bd.csvParser.controller;

import finance.gov.bd.csvParser.dto.request.VerificationRequest;
import finance.gov.bd.csvParser.dto.response.ServiceResponse;
import finance.gov.bd.csvParser.service.AllVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {

    @Autowired
    AllVerificationService allVerificationService;

    @PostMapping(path = "/all-verify")
    public ServiceResponse validateBrn(@RequestBody VerificationRequest req) {
        return allVerificationService.verify(req);
    }
}
