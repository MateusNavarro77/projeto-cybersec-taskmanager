package com.mateusnavarro77.projeto_cybersec_taskmanager.controller.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vulnerable")
public class VulnerableController {

    @GetMapping("/sql-injection")
    public String sqlInjection(@RequestParam String userInput) {
        // Simulação de uma consulta SQL vulnerável a injeção
        String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
        // Aqui, o código executaria a consulta no banco de dados, o que é perigoso se o userInput não for devidamente sanitizado
        return "Consulta SQL executada: " + query;
    }
}
