package com.example.erpserver.utils;

import com.example.erpserver.services.ServicoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoSave {

    @Autowired
    private ServicoRepositorio servico;

    @Scheduled(fixedDelay = 5000)
    public void salvarPeriodicamente() {
        servico.salvarJson();
    }
}
