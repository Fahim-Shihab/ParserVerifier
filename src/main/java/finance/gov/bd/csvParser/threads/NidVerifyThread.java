package finance.gov.bd.csvParser.threads;

import finance.gov.bd.csvParser.dto.request.VerificationRequest;
import finance.gov.bd.csvParser.dto.response.BECResponse;
import finance.gov.bd.csvParser.dto.response.MFSResponse;
import finance.gov.bd.csvParser.model.NidVerificationLog;
import finance.gov.bd.csvParser.repository.NidVerificationLogRepo;
import finance.gov.bd.csvParser.service.AllVerificationService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NidVerifyThread extends Thread {
    private String threadName;
    private Integer offset;
    public NidVerifyThread(String threadName, Integer offset, NidVerificationLogRepo nidVerificationLogRepo, AllVerificationService allVerificationService) {
        this.threadName = threadName;
        this.offset = offset;
        this.nidVerificationLogRepo = nidVerificationLogRepo;
        this.allVerificationService = allVerificationService;
    }


    private NidVerificationLogRepo nidVerificationLogRepo;

    private AllVerificationService allVerificationService;

    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            VerificationRequest req = new VerificationRequest();
            req.setVerifyStatus(0);
//            verify(req);
        } catch (Exception e) {
            System.out.println("Error in thread: "+threadName+"\n"+e.toString());
        }
    }
//
//    public void verify(VerificationRequest req) {
//        Integer size = 20;
//
//        long count = 1000;
//
//        int index = 0;
//
//        while (index < count) {
//
//            List<NidVerificationLog> list = nidVerificationLogRepo.findByNidVerifyStatusAndOffset(0, size, offset);
//
//            List<NidVerificationLog> verifyList = new ArrayList<>();
//
//            if (list != null && list.size() > 0) {
//                for (NidVerificationLog dto : list) {
//                    if (dto.getNid() != null && dto.getDateOfBirth() != null) {
//                        try {
//                            BECResponse response = allVerificationService.getNidData(dto.getNid().toString(), dto.getDateOfBirth().toString());
//                            if (response != null) {
//                                if (response.isOperationResult() && response.getNidData() != null) {
//                                    dto.setNidVerifyStatus(1);
//                                    dto.setNameEn(response.getNidData().getNameEn());
//                                    dto.setNameBn(response.getNidData().getName());
//                                    String nid10 = response.getNidData().getNid10();
//                                    String nid17 = response.getNidData().getNid17();
//                                    if (nid10 != null && dto.getNid().toString().equals(nid10)) {
//                                        dto.setAlternateNid(nid17);
//                                    } else if (nid17 != null && dto.getNid().toString().equals(nid17)) {
//                                        dto.setAlternateNid(nid10);
//                                    }
//                                } else {
//                                    dto.setNidVerifyStatus(2);
//                                }
//                            } else {
//                                dto.setNidVerifyStatus(3);
//                            }
//
//                            dto.setLastVerifyAt(new java.util.Date());
//                            verifyList.add(dto);
//
//                        } catch (Exception ex) {
//                            dto.setNidVerifyStatus(3);
//                            dto.setLastVerifyAt(new java.util.Date());
//                            verifyList.add(dto);
//                            ex.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//            if (verifyList.size() > 0) {
//                nidVerificationLogRepo.saveAll(verifyList);
//            }
//
//            index += size;
//        }
//    }
}

