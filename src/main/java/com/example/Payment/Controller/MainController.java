package com.example.Payment.Controller;

import com.example.Payment.Dto.OperationResponseDTO;
import com.example.Payment.Service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class MainController {

    private final OperationService operationService;

    // Используем конструктор вместо @Autowired на поле
    @Autowired
    public MainController(OperationService operationService) {
        this.operationService = operationService;
    }

    // Главная страница
    @GetMapping
    public String home(Model model) {
        model.addAttribute("message", "Платежный сервис работает!");
        return "home";
    }

//    // Страница со списком операций
    @GetMapping("/operations")
    public String getAllOperations(Model model) {
        try {
            List<OperationResponseDTO> operations = operationService.getAllOperations();
            model.addAttribute("operations", operations);

            // Статистика
            long total = operations.size();
            long success = operations.stream().filter(op -> "SUCCESS".equals(op.getStatus())).count();
            long failed = operations.stream().filter(op -> "FAILED".equals(op.getStatus())).count();

            model.addAttribute("totalOperations", total);
            model.addAttribute("successOperations", success);
            model.addAttribute("failedOperations", failed);

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке операций: " + e.getMessage());
        }

        return "operations";
    }

    // API endpoint для получения операций (если нужен JSON)
    @GetMapping("/api/operations")
    @ResponseBody
    public List<OperationResponseDTO> getOperationsApi() {
        return operationService.getAllOperations();
    }
}