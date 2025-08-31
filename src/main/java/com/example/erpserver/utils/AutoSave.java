package com.example.erpserver.utils;

import com.example.erpserver.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoSave {

    private static final Logger logger = LoggerFactory.getLogger(AutoSave.class);

    @Autowired
    private ServicoPessoas servico1;
    @Autowired
    private ServicoProdutos servico2;
    @Autowired
    private ServicoTitulos servico3;
    @Autowired
    private ServicoUsuarios servico4;

    @Scheduled(fixedDelay = 5000)
    public void salvarPeriodicamente() {
        servico1.salvarJson();
        servico2.salvarJson();
        servico3.salvarJson();
        servico4.salvarJson();
        logger.info("Salvamento de arquivos!");
    }
}
