package finance.gov.bd.csvParser.threads;

import finance.gov.bd.csvParser.dto.request.VerificationRequest;
import finance.gov.bd.csvParser.dto.response.MFSResponse;
import finance.gov.bd.csvParser.model.MfsVerificationLog;
import finance.gov.bd.csvParser.repository.MfsVerificationLogRepo;
import finance.gov.bd.csvParser.service.AllVerificationService;

import java.util.ArrayList;
import java.util.List;

public class MfsVerifyThread extends Thread {
    private String threadName;
    private Integer offset;
    public MfsVerifyThread(String threadName, Integer offset, MfsVerificationLogRepo mfsVerificationLogRepo, AllVerificationService allVerificationService) {
        this.threadName = threadName;
        this.offset = offset;
        this.mfsVerificationLogRepo = mfsVerificationLogRepo;
        this.allVerificationService = allVerificationService;
    }


    private MfsVerificationLogRepo mfsVerificationLogRepo;

    private AllVerificationService allVerificationService;

    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            VerificationRequest req = new VerificationRequest();
            req.setVerifyStatus(0);
            verify(req);
        } catch (Exception e) {
            System.out.println("Error in thread: "+threadName+"\n"+e.toString());
        }
    }

    public synchronized void verify(VerificationRequest req) {
        Integer size = 20;

        long count = 1000;

        int index = 0;

        while (index < count) {

            List<MfsVerificationLog> list = mfsVerificationLogRepo.findByMfsVerifyStatusAndOffset(0, size, offset);

            List<MfsVerificationLog> verifyList = new ArrayList<>();

            if (list != null && list.size() > 0) {
                for (MfsVerificationLog dto : list) {
                    if (dto.getNid() != null && dto.getMfsName() != null && dto.getMobileNumber() != null) {
                        try {
                            MFSResponse response = allVerificationService.getMFSData(dto.getMfsName().toLowerCase(), dto.getNid().toString(), dto.getMobileNumber());
                            if (response != null) {
                                if (response.getAccountExist() == false || response.getNIDMatched() == false || response.getNIDMobileMatched() == false) {
                                    if (dto.getAlternateNid() != null) {
                                        MFSResponse response2 = allVerificationService.getMFSData(dto.getMfsName().toLowerCase(), dto.getAlternateNid().toString(), dto.getMobileNumber());
                                        if (response2 != null) {
                                            if (response2.getAccountExist() != null && response2.getAccountExist() == true && response.getAccountExist() == false) {
                                                response.setAccountExist(true);
                                            }
                                            if (response2.getNIDMatched() != null && response2.getNIDMatched() == true && response.getNIDMatched() == false) {
                                                response.setNIDMatched(true);
                                            }
                                            if (response2.getNIDMobileMatched() != null && response2.getNIDMobileMatched() == true && response.getNIDMobileMatched() == false) {
                                                response.setNIDMobileMatched(true);
                                            }
                                        }
                                    }
                                }
                                dto = allVerificationService.updateMfsAccountInfo(dto, response);
                            } else {
                                dto.setAccountExist(3);
                                dto.setNidMatched(3);
                                dto.setNidMobileMatched(3);
                                dto.setMfsVerifyStatus(3);
                            }

                            dto.setLastVerifyAt(new java.util.Date());
                            verifyList.add(dto);

                        } catch (Exception ex) {
                            dto.setMfsVerifyStatus(3);
                            dto.setLastVerifyAt(new java.util.Date());
                            verifyList.add(dto);
                            ex.printStackTrace();
                        }
                    }
                }
            }

            if (verifyList.size() > 0) {
                mfsVerificationLogRepo.saveAll(verifyList);
            }

            index += size;
        }
    }
}

