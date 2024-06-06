package finance.gov.bd.csvParser.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import finance.gov.bd.csvParser.common.Utility;
import finance.gov.bd.csvParser.dto.BrnVerificationCsv;
import finance.gov.bd.csvParser.dto.NidVerificationCsv;
import finance.gov.bd.csvParser.model.BrnVerificationLog;
import finance.gov.bd.csvParser.model.NidVerificationLog;
import finance.gov.bd.csvParser.repository.BrnVerificationLogRepo;
import finance.gov.bd.csvParser.repository.NidVerificationLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
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

    public void processCsvFile(MultipartFile file, String importType, Model model) {
        Date started = new Date();
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            if (importType != null && importType.equals("N")) {
                CsvToBean<NidVerificationCsv> csvToBean = new CsvToBeanBuilder(reader)
                        .withType(NidVerificationCsv.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<NidVerificationCsv> list = csvToBean.parse();

                int count = saveNidListToDb(list);

                Date ended = new Date();

                model.addAttribute("totalCount", list.size());
                model.addAttribute("importCount", count);
                model.addAttribute("importStarted", started);
                model.addAttribute("importEnded", ended);

                model.addAttribute("status", true);
            } else if (importType != null && importType.equals("B")) {
                CsvToBean<BrnVerificationCsv> csvToBean = new CsvToBeanBuilder(reader)
                        .withType(BrnVerificationCsv.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<BrnVerificationCsv> list = csvToBean.parse();

                int count = saveBrnListToDb(list);

                Date ended = new Date();

                model.addAttribute("totalCount", list.size());
                model.addAttribute("importCount", count);
                model.addAttribute("importStarted", started);
                model.addAttribute("importEnded", ended);

                model.addAttribute("status", true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("message", "An error occurred while processing the CSV file.");
            model.addAttribute("status", false);
        }
    }

    public int saveNidListToDb(List<NidVerificationCsv> list) {

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
                continue;
            }
            log.setExcelNameBn(dto.getNameBn());
            log.setExcelNameEn(dto.getNameEn());
            log.setNidVerifyStatus(0);

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

    public int saveBrnListToDb(List<BrnVerificationCsv> list) {

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
}
