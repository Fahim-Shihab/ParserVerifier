package finance.gov.bd.csvParser.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import finance.gov.bd.csvParser.common.Utility;
import finance.gov.bd.csvParser.dto.BrnVerificationCsv;
import finance.gov.bd.csvParser.dto.MfsVerificationCsv;
import finance.gov.bd.csvParser.dto.NidVerificationCsv;
import finance.gov.bd.csvParser.model.BrnVerificationLog;
import finance.gov.bd.csvParser.model.FileInfo;
import finance.gov.bd.csvParser.model.MfsVerificationLog;
import finance.gov.bd.csvParser.model.NidVerificationLog;
import finance.gov.bd.csvParser.repository.BrnVerificationLogRepo;
import finance.gov.bd.csvParser.repository.FileInfoRepository;
import finance.gov.bd.csvParser.repository.MfsVerificationLogRepo;
import finance.gov.bd.csvParser.repository.NidVerificationLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploadService {
    @Autowired
    NidVerificationLogRepo nidVerificationLogRepo;

    @Autowired
    BrnVerificationLogRepo brnVerificationLogRepo;

    @Autowired
    MfsVerificationLogRepo mfsVerificationLogRepo;

    @Autowired
    FileInfoRepository fileInfoRepo;

    public Integer saveUploadedFileInfo(MultipartFile file, String importType, Integer totalCount, Integer importCount, Date startedAt, Date endedAt, Integer fileId) {
        FileInfo fileInfo = new FileInfo();
        if (fileId != null) {
            fileInfo = fileInfoRepo.findById(fileId).orElse(new FileInfo());
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileExtension(ext);
        fileInfo.setImportType(importType);
        fileInfo.setTotalRowCount(totalCount);
        fileInfo.setImportCount(importCount);
        fileInfo.setImportStartedAt(startedAt);
        fileInfo.setImportEndedAt(endedAt);
        try {
            FileInfo fileInfoEntity = fileInfoRepo.save(fileInfo);
            return fileInfoEntity.getId();
        } catch (Exception e) {
            System.out.println("Error during saving uploaded file info");
            e.printStackTrace();
            throw e;
        }
    }

    public void processCsvFile(MultipartFile file, String importType, Model model) {
        Date started = new Date();
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            if (importType != null && importType.equals("N")) {
                CsvToBean<NidVerificationCsv> csvToBean = new CsvToBeanBuilder(reader)
                        .withType(NidVerificationCsv.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<NidVerificationCsv> list = csvToBean.parse();

                Integer fileId = saveUploadedFileInfo(file, importType, list.size(), null, started, null, null);
                if (fileId != null) {
                    int count = saveNidListToDb(list, fileId);

                    Date ended = new Date();

                    saveUploadedFileInfo(file, importType, list.size(), count, started, ended, fileId);

                    model.addAttribute("totalCount", list.size());
                    model.addAttribute("importCount", count);
                    model.addAttribute("importStarted", started);
                    model.addAttribute("importEnded", ended);

                    model.addAttribute("status", true);
                    return;
                }
            } else if (importType != null && importType.equals("B")) {
                CsvToBean<BrnVerificationCsv> csvToBean = new CsvToBeanBuilder(reader)
                        .withType(BrnVerificationCsv.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<BrnVerificationCsv> list = csvToBean.parse();

                Integer fileId = saveUploadedFileInfo(file, importType, list.size(), null, started, null, null);
                if (fileId != null) {
                    int count = saveBrnListToDb(list, fileId);

                    Date ended = new Date();

                    saveUploadedFileInfo(file, importType, list.size(), count, started, ended, fileId);

                    model.addAttribute("totalCount", list.size());
                    model.addAttribute("importCount", count);
                    model.addAttribute("importStarted", started);
                    model.addAttribute("importEnded", ended);

                    model.addAttribute("status", true);
                    return;
                }
            } else if (importType != null && importType.equals("M")) {
                CsvToBean<MfsVerificationCsv> csvToBean = new CsvToBeanBuilder(reader)
                        .withType(MfsVerificationCsv.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<MfsVerificationCsv> list = csvToBean.parse();

                Integer fileId = saveUploadedFileInfo(file, importType, list.size(), null, started, null, null);

                if (fileId != null) {
                    int count = saveMfsListToDb(list, fileId);

                    Date ended = new Date();

                    saveUploadedFileInfo(file, importType, list.size(), count, started, ended, fileId);

                    model.addAttribute("totalCount", list.size());
                    model.addAttribute("importCount", count);
                    model.addAttribute("importStarted", started);
                    model.addAttribute("importEnded", ended);

                    model.addAttribute("status", true);
                    return;
                }
            }
            model.addAttribute("message", "An error occurred while processing the CSV file.");
            model.addAttribute("status", false);
        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("message", "An error occurred while processing the CSV file.");
            model.addAttribute("status", false);
        }
    }

    public int saveNidListToDb(List<NidVerificationCsv> list, Integer fileId) {

        List<NidVerificationLog> logs = new ArrayList<>();

        int count = 0;

        for (NidVerificationCsv dto : list) {
            NidVerificationLog log = new NidVerificationLog();
            if (dto.getNid() != null && (dto.getNid().toString().length() == 10 ||
                    dto.getNid().toString().length() == 13 || dto.getNid().toString().length() == 17)) {
                NidVerificationLog logEntity1 = nidVerificationLogRepo.findByNid(dto.getNid());
                if (logEntity1 != null && logEntity1.getId() != null) {
                    log = logEntity1;
                } else {
                    NidVerificationLog logEntity2 = nidVerificationLogRepo.findByAlternateNid(dto.getNid());
                    if (logEntity2 != null && logEntity2.getId() != null) {
                        log = logEntity2;
                    }
                }
            } else {
                System.out.println("Invalid Nid:" + dto.getNid());
                continue;
            }

            log.setNid(dto.getNid());
            if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
                Date d = Utility.convertStringToDate(dto.getDateOfBirth());
                if (d == null) {
                    continue;
                }
                log.setDateOfBirth(d);
            } else {
                System.out.println("Valid Nid:" + dto.getNid() + "Invalid dob: "+dto.getDateOfBirth());
                continue;
            }
            log.setExcelNameBn(dto.getNameBn());
            log.setExcelNameEn(dto.getNameEn());
            log.setNidVerifyStatus(0);
            log.setFileId(fileId);

            logs.add(log);
            count++;
        }

        try {
            nidVerificationLogRepo.saveAll(logs);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int saveBrnListToDb(List<BrnVerificationCsv> list, Integer fileId) {

        List<BrnVerificationLog> logs = new ArrayList<>();

        int count = 0;

        for (BrnVerificationCsv dto : list) {
            BrnVerificationLog log = new BrnVerificationLog();
            if (dto.getBirthRegNo() != null && dto.getBirthRegNo().toString().length() == 17) {
                BrnVerificationLog logEntity1 = brnVerificationLogRepo.findByBirthRegNo(dto.getBirthRegNo());
                if (logEntity1 != null && logEntity1.getId() != null) {
                    log = logEntity1;
                }
            } else {
                continue;
            }

            log.setBirthRegNo(dto.getBirthRegNo());
            if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
                Date d = Utility.convertStringToDate(dto.getDateOfBirth());
                if (d == null) {
                    continue;
                }
                log.setDateOfBirth(d);
            } else {
                continue;
            }
            log.setExcelNameBn(dto.getNameBn());
            log.setExcelNameEn(dto.getNameEn());
            log.setBrnVerifyStatus(0);
            log.setFileId(fileId);

            logs.add(log);
            count++;
        }

        try {
            brnVerificationLogRepo.saveAll(logs);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int saveMfsListToDb(List<MfsVerificationCsv> list, Integer fileId) {

        List<MfsVerificationLog> logs = new ArrayList<>();

        int count = 0;

        for (MfsVerificationCsv dto : list) {
            MfsVerificationLog log = new MfsVerificationLog();
            if (dto.getNid() != null && (dto.getNid().toString().length() == 10 ||
                    dto.getNid().toString().length() == 13 || dto.getNid().toString().length() == 17)) {
                log.setNid(dto.getNid());
                NidVerificationLog nidVerificationLogObj = nidVerificationLogRepo.findByNid(dto.getNid());
                if (nidVerificationLogObj != null) {
                    if (nidVerificationLogObj.getNid() != null && nidVerificationLogObj.getNid().equals(dto.getNid())) {
                        log.setAlternateNid(nidVerificationLogObj.getAlternateNid());
                    } else if (nidVerificationLogObj.getAlternateNid() != null && nidVerificationLogObj.getAlternateNid().equals(dto.getNid())) {
                        log.setAlternateNid(nidVerificationLogObj.getNid());
                    }
                } else {
                    NidVerificationLog nidVerificationLogObj1 = nidVerificationLogRepo.findByAlternateNid(dto.getNid());
                    if (nidVerificationLogObj1 != null) {
                        if (nidVerificationLogObj1.getNid() != null && nidVerificationLogObj1.getNid().equals(dto.getNid())) {
                            log.setAlternateNid(nidVerificationLogObj1.getAlternateNid());
                        } else if (nidVerificationLogObj1.getAlternateNid() != null && nidVerificationLogObj1.getAlternateNid().equals(dto.getNid())) {
                            log.setAlternateNid(nidVerificationLogObj1.getNid());
                        }
                    }
                }
            } else {
                continue;
            }

            if (dto.getMobileNumber() != null && dto.getMobileNumber().length() > 9 && dto.getMobileNumber().length() < 15) {
                log.setMobileNumber(dto.getMobileNumber());
            } else {
                continue;
            }
            if (dto.getMfsName() != null && dto.getMfsName().length() <= 10) {
                log.setMfsName(dto.getMfsName());
            } else {
                continue;
            }
            log.setMfsVerifyStatus(0);
            log.setFileId(fileId);

            logs.add(log);
            count++;
        }

        try {
            mfsVerificationLogRepo.saveAll(logs);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
