package com.example.erpserver.services;

import com.example.erpserver.DTOs.CadastroDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.repositories.CeoRepositorio;
import com.example.erpserver.repositories.FuncionariosRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Service
public class ServicoCadastro {

    private final CeoRepositorio ceos;
    private final FuncionariosRepositorio funcionarios;
    private final PasswordEncoder passwordEncoder;

    public ServicoCadastro(
            CeoRepositorio ceos,
            PasswordEncoder passwordEncoder,
            FuncionariosRepositorio funcionarios
    ) {
        this.ceos = ceos;
        this.passwordEncoder = passwordEncoder;
        this.funcionarios = funcionarios;
    }

    // ---------- Adicionar Ceo ----------
    @Transactional
    public Optional<Ceo> adicionarCeo(CadastroDTO dto) {

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
        novofun.setCeo(novo);
        novofun.setSetor("CEO DA EMPRESA");

        novo.getFuncionarios().add(novofun);

        return Optional.of(ceos.save(novo));
    }
}
