package com.example.erpserver.services;

import com.example.erpserver.DTOs.CadastroDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.TipoEspecializacao;
import com.example.erpserver.repositories.CeoRepositorio;
import com.example.erpserver.repositories.FuncionariosRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Optional;

@Service
public class ServicoCadastro {

    private final CeoRepositorio ceos;
    private final FuncionariosRepositorio funcionarios;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    public ServicoCadastro(
            CeoRepositorio ceos,
            PasswordEncoder passwordEncoder,
            FuncionariosRepositorio funcionarios,
            RestTemplate restTemplate
    ) {
        this.ceos = ceos;
        this.passwordEncoder = passwordEncoder;
        this.funcionarios = funcionarios;
        this.restTemplate = restTemplate;
    }

    // ---------- Adicionar Ceo ----------
    @Transactional
    public Optional<Ceo> adicionarCeo(CadastroDTO dto) {

        /*
        try {
            String url = "http://localhost:8081/api/aprovacao";

            var response = restTemplate.postForEntity(url, null, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return Optional.empty();
            }

        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 406) {
                return Optional.empty();
            }
            throw ex;
        }
        */

        if (funcionarios.existsByEmailOrCpf(dto.getEmail(), dto.getCpf())) {
            return Optional.empty();
        }

        String senhaHash = passwordEncoder.encode(dto.getSenha());

        // Criar CEO
        Ceo novo = new Ceo();
        novo.setNome(dto.getNome());
        novo.setEmail(dto.getEmail());
        novo.setCpf(dto.getCpf());
        novo.setPlano(dto.getPlano());
        novo.setSenhaHash(senhaHash);
        novo.setFuncionarios(new HashSet<>());

        // Criar funcionário do CEO
        Funcionario novofun = new Funcionario();
        novofun.setNome(dto.getNome());
        novofun.setEmail(dto.getEmail());
        novofun.setSenhaHash(senhaHash);
        novofun.setCpf(dto.getCpf());
        novofun.setTipo(TipoEspecializacao.CEO);
        novofun.setCeo(novo);
        novofun.setSetor("CEO DA EMPRESA");

        novo.getFuncionarios().add(novofun);

        return Optional.of(ceos.save(novo));
    }
}
