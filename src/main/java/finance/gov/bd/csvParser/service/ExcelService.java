package finance.gov.bd.csvParser.service;

import finance.gov.bd.csvParser.dto.BrnVerificationCsv;
import finance.gov.bd.csvParser.dto.MfsVerificationCsv;
import finance.gov.bd.csvParser.dto.NidVerificationCsv;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelService {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String SHEET = "Sheet1";
    @Autowired
    UploadService uploadService;

    public boolean hasExcelFormat(MultipartFile file) {
        if (!TYPE.equals(file.getContentType())) {
            return false;
        }
        return true;
    }

    private void processNidList(InputStream is, String importType, Model model) {
        try {
            Date started = new Date();
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();

            List<NidVerificationCsv> nidList = new ArrayList<>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                NidVerificationCsv nidObj = new NidVerificationCsv();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            double numericValue = Double.parseDouble(currentCell.toString());
                            if (String.valueOf(numericValue).contains("E")) {
                                DecimalFormat df = new DecimalFormat("0");
                                String plainNumber = df.format(numericValue);
                                nidObj.setNid(new BigInteger(plainNumber));
                            }
                            break;
                        case 1:
                            CellType c = currentCell.getCellType();
                            Date dateValue = currentCell.getDateCellValue();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedDate = dateFormat.format(dateValue);
                            nidObj.setDateOfBirth(formattedDate);
                            break;
                        case 2:
                            nidObj.setNameEn(currentCell.toString());
                            break;
                        case 3:
                            nidObj.setNameBn(currentCell.toString());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                nidList.add(nidObj);
            }

            workbook.close();

            if (nidList != null && nidList.size() > 0) {
                int count = uploadService.saveNidListToDb(nidList);

                Date ended = new Date();

                model.addAttribute("totalCount", nidList.size());
                model.addAttribute("importCount", count);
                model.addAttribute("importStarted", started);
                model.addAttribute("importEnded", ended);
                model.addAttribute("status", true);
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private void processBrnList(InputStream is, String importType, Model model) {
        try {
            Date started = new Date();
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();

            List<BrnVerificationCsv> brnList = new ArrayList<>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                BrnVerificationCsv brnObj = new BrnVerificationCsv();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            double numericValue = Double.parseDouble(currentCell.toString());
                            if (String.valueOf(numericValue).contains("E")) {
                                DecimalFormat df = new DecimalFormat("0");
                                String plainNumber = df.format(numericValue);
                                brnObj.setBirthRegNo(new BigInteger(plainNumber));
                            }
                            break;
                        case 1:
                            CellType c = currentCell.getCellType();
                            Date dateValue = currentCell.getDateCellValue();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedDate = dateFormat.format(dateValue);
                            brnObj.setDateOfBirth(formattedDate);
                            break;
                        case 2:
                            brnObj.setNameEn(currentCell.toString());
                            break;
                        case 3:
                            brnObj.setNameBn(currentCell.toString());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                brnList.add(brnObj);
            }

            workbook.close();

            if (brnList != null && brnList.size() > 0) {
                int count = uploadService.saveBrnListToDb(brnList);

                Date ended = new Date();

                model.addAttribute("totalCount", brnList.size());
                model.addAttribute("importCount", count);
                model.addAttribute("importStarted", started);
                model.addAttribute("importEnded", ended);
                model.addAttribute("status", true);
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private void processMfsInfoList(InputStream is, String importType, Model model) {
        try {
            Date started = new Date();
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();

            List<MfsVerificationCsv> mfsInfoList = new ArrayList<>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                MfsVerificationCsv mfsVerifyObj = new MfsVerificationCsv();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            double numericValue = Double.parseDouble(currentCell.toString());
                            if (String.valueOf(numericValue).contains("E")) {
                                DecimalFormat df = new DecimalFormat("0");
                                String plainNumber = df.format(numericValue);
                                mfsVerifyObj.setNid(new BigInteger(plainNumber));
                            }
                            break;
                        case 1:
                            mfsVerifyObj.setMfsName(currentCell.toString());
                            break;
                        case 2:
                            mfsVerifyObj.setMobileNumber(currentCell.toString());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                mfsInfoList.add(mfsVerifyObj);
            }

            workbook.close();

            if (mfsInfoList != null && mfsInfoList.size() > 0) {
                int count = uploadService.saveMfsListToDb(mfsInfoList);

                Date ended = new Date();

                model.addAttribute("totalCount", mfsInfoList.size());
                model.addAttribute("importCount", count);
                model.addAttribute("importStarted", started);
                model.addAttribute("importEnded", ended);
                model.addAttribute("status", true);
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public void processExcelFile(InputStream is, String importType, Model model) {
        if (importType.equals("N")) {
            processNidList(is, importType, model);
        } else if (importType.equals("B")) {
            processBrnList(is, importType, model);
        } else if (importType.equals("M")) {
            processMfsInfoList(is, importType, model);
        }
    }
}
