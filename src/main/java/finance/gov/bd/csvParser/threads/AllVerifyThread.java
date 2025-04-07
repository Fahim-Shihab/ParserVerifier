package finance.gov.bd.csvParser.threads;

import finance.gov.bd.csvParser.common.Data;
import finance.gov.bd.csvParser.common.VerificationStatus;
import finance.gov.bd.csvParser.dto.response.BRNResponse;
import finance.gov.bd.csvParser.model.BirthRegInfo;
import finance.gov.bd.csvParser.model.NidInfo;
import finance.gov.bd.csvParser.model.SrNeedVerify;
import finance.gov.bd.csvParser.repository.BirthRegInfoRepo;
import finance.gov.bd.csvParser.repository.NidInfoRepo;
import finance.gov.bd.csvParser.repository.SrNeedVerifyRepo;
import finance.gov.bd.csvParser.service.AllVerificationService;
import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllVerifyThread extends Thread {

    private String threadName;
    private Long offset;
    private Long limit;
    private SrNeedVerifyRepo srNeedVerifyRepo;
    private NidInfoRepo nidInfoRepo;
    private BirthRegInfoRepo birthRegInfoRepo;
    private AllVerificationService allVerificationService;
    private EntityManager em;

    public AllVerifyThread(String threadName, Long offset, Long limit,
                           SrNeedVerifyRepo srNeedVerifyRepo,
                           NidInfoRepo nidInfoRepo,
                           BirthRegInfoRepo birthRegInfoRepo,
                           AllVerificationService allVerificationService,
                           EntityManager em
    ) {
        this.threadName = threadName;
        this.offset = offset;
        this.limit = limit;
        this.srNeedVerifyRepo = srNeedVerifyRepo;
        this.nidInfoRepo = nidInfoRepo;
        this.birthRegInfoRepo = birthRegInfoRepo;
        this.allVerificationService = allVerificationService;
        this.em = em;
    }

    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            System.out.println(threadName + ", offset:" + offset + ", limit:" + limit);
            verify();
        } catch (Exception e) {
            System.out.println("Error in thread: " + threadName + "\n" + e.toString());
        }
    }

    private void saveNidInfo(Data data, Date dob, Integer srBeneficiaryId, Integer guardianId) {
        try {
            NidInfo nidInfo = new NidInfo();
            nidInfo.setNid(data.getPin());
            nidInfo.setSmartId(data.getNationalId());
            nidInfo.setBloodGroup(data.getBloodGroup());
            nidInfo.setDateOfBirth(dob);
            nidInfo.setFatherName(data.getFather());
            nidInfo.setFatherNid(data.getNidFather());
            nidInfo.setMotherName(data.getMother());
            nidInfo.setMotherNid(data.getNidMother());
            nidInfo.setGender(data.getGender());
            nidInfo.setNameBn(data.getName());
            nidInfo.setNameEn(data.getNameEn());

            nidInfo.setPermanentAddress(data.getPermanentAddress());
            nidInfo.setPresentAddress(data.getPresentAddress());

            nidInfo.setPhoto(data.getPhoto());
            nidInfo.setReligion(data.getReligion());
            nidInfo.setSrBeneficiaryId(srBeneficiaryId);
            nidInfo.setGuardianId(guardianId);
            nidInfo.setVerifiedAt(new Date());

            nidInfoRepo.save(nidInfo);
        } catch (Exception e) {
            System.out.println("Error saving nid info\n"+e.toString());
        }
    }

    private void saveBrnInfo(BRNResponse brnResponse, Integer srBeneficiaryId) {
        try {
            BirthRegInfo birthRegInfo = new BirthRegInfo();
            birthRegInfo.setBirthRegNo(brnResponse.getUbrn());
            birthRegInfo.setDateOfBirth(DateUtils.parseDate(brnResponse.getDob(), "yyyy-MM-dd"));
            birthRegInfo.setNameBn(brnResponse.getPersonname());
            birthRegInfo.setNameEn(brnResponse.getPersonnameEn());
            birthRegInfo.setFatherNameBn(brnResponse.getFathername());
            birthRegInfo.setFatherNameEn(brnResponse.getFathernameEn());
            birthRegInfo.setMotherNameBn(brnResponse.getMothername());
            birthRegInfo.setMotherNameEn(brnResponse.getMothernameEn());
            birthRegInfo.setFatherNationalityBn(brnResponse.getFatherNationality());
            birthRegInfo.setFatherNationalityEn(brnResponse.getFatherNationalityEn());
            birthRegInfo.setMotherNationalityBn(brnResponse.getMotherNationality());
            birthRegInfo.setMotherNationalityEn(brnResponse.getMotherNationalityEn());
            birthRegInfo.setGender(brnResponse.getSex());
            birthRegInfo.setPlaceOfBirthBn(brnResponse.getPlaceofbirth());
            birthRegInfo.setPlaceOfBirthEn(brnResponse.getPlaceofbirthEn());
            birthRegInfo.setSrBeneficiaryId(srBeneficiaryId);
            birthRegInfo.setVerifiedAt(new Date());

            birthRegInfoRepo.save(birthRegInfo);
        } catch (Exception e) {
            System.out.println("Error saving birth reg info\n"+e.toString());
        }
    }

    public void verify() {
        List<SrNeedVerify> list = srNeedVerifyRepo.findByStatusAndOffset(limit, offset);

        if (list != null && !list.isEmpty()) {

            List<SrNeedVerify> updatedList = new ArrayList<>();

            for (SrNeedVerify dto : list) {
                if ((dto.getNid() != null || dto.getSmartId() != null) && dto.getDateOfBirth() != null) {
                    if (dto.getNid() != null) {
                        Data response = allVerificationService.getNidData(dto.getNid(), dto.getDateOfBirth().toString());
                        if (response != null) {
                            if (response.getName() != null || response.getNameEn() != null) {
                                System.out.println(threadName + " NID verified successfully");
                                if (dto.getSrBeneficiaryId() != null) {
                                    saveNidInfo(response, dto.getDateOfBirth(), dto.getSrBeneficiaryId(), null);
                                } else if (dto.getGuardianId() != null) {
                                    saveNidInfo(response, dto.getDateOfBirth(), null, dto.getGuardianId());
                                }
                                dto.setVerificationStatus(VerificationStatus.VALID.ordinal());
                            } else {
                                dto.setVerificationStatus(VerificationStatus.INVALID.ordinal());
                            }
                        } else {
                            dto.setVerificationStatus(VerificationStatus.EXCEPTION.ordinal());
                        }
                        dto.setVerificationRunAt(new Date());
                        srNeedVerifyRepo.save(dto);
                        updatedList.add(dto);
                    } else if (dto.getSmartId() != null) {
                        Data response = allVerificationService.getNidData(dto.getSmartId(), dto.getDateOfBirth().toString());
                        if (response != null) {
                            if (response.getName() != null || response.getNameEn() != null) {
                                System.out.println(threadName + " SmartId verified successfully");
                                if (dto.getSrBeneficiaryId() != null) {
                                    saveNidInfo(response, dto.getDateOfBirth(), dto.getSrBeneficiaryId(), null);
                                } else if (dto.getGuardianId() != null) {
                                    saveNidInfo(response, dto.getDateOfBirth(), null, dto.getGuardianId());
                                }
                                dto.setVerificationStatus(VerificationStatus.VALID.ordinal());
                            } else {
                                dto.setVerificationStatus(VerificationStatus.INVALID.ordinal());
                            }
                        } else {
                            dto.setVerificationStatus(VerificationStatus.EXCEPTION.ordinal());
                        }
                        dto.setVerificationRunAt(new Date());
                        srNeedVerifyRepo.save(dto);
                        updatedList.add(dto);
                    }
                } else if (dto.getBirthRegNo() != null && dto.getDateOfBirth() != null) {
                    BRNResponse response = allVerificationService.getBRNdata(dto.getBirthRegNo(), dto.getDateOfBirth().toString());
                    if (response != null) {
                        if (response.getPersonname() != null || response.getPersonnameEn() != null) {
                            System.out.println(threadName + " BRN verified successfully");
                            saveBrnInfo(response, dto.getSrBeneficiaryId());
                            dto.setVerificationStatus(VerificationStatus.VALID.ordinal());
                        } else {
                            dto.setVerificationStatus(VerificationStatus.INVALID.ordinal());
                        }
                    } else {
                        dto.setVerificationStatus(VerificationStatus.EXCEPTION.ordinal());
                    }
                    dto.setVerificationRunAt(new Date());
                    srNeedVerifyRepo.save(dto);
                    updatedList.add(dto);
                }
            }

//            srNeedVerifyRepo.saveAll(updatedList);
        }
    }
}
