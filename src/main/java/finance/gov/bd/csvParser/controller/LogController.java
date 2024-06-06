package finance.gov.bd.csvParser.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogController {
    @GetMapping(path = "/log/viewNidList")
    public String viewNidList(Model model) {
        model.addAttribute("message", null);
        model.addAttribute("status", true);
        return "nidList";
    }

    @GetMapping(path = "/log/viewBrnList")
    public String viewBrnList(Model model) {
        model.addAttribute("message", null);
        model.addAttribute("status", true);
        return "brnList";
    }
}
