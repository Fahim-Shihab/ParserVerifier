package finance.gov.bd.csvParser.controller;

import finance.gov.bd.csvParser.service.ExcelService;
import finance.gov.bd.csvParser.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class UploadController {

    @Autowired
    UploadService uploadService;

    @Autowired
    ExcelService excelService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload-csv-file")
    public String uploadCSVFile(@RequestParam("file") MultipartFile file, @RequestParam("importType") String importType, Model model) {

        // validate file
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            model.addAttribute("status", false);
        } else if (importType == null || importType.isEmpty()) {
            model.addAttribute("message", "Please select Import Type for the file.");
            model.addAttribute("status", false);
        } else {

            System.out.println("File Details: " + file.getName() + ", " + file.getContentType() + ", " + file.getOriginalFilename()
                    + ", " + file.getResource().toString());
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            if (ext != null && ext.equals("csv")) {
                uploadService.processCsvFile(file, importType, model);
            } else if (ext != null && (ext.equals("xls") || ext.equals("xlsx"))) {
                try {
                    excelService.processExcelFile(file, importType, model);
                    model.addAttribute("status", true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return "file-upload-status";
    }
}
