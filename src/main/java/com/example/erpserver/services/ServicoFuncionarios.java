package com.example.erpserver.services;

import com.example.erpserver.DTOs.CadastroFuncionarioDTO;
import com.example.erpserver.DTOs.FuncionarioDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.TipoEspecializacao;
import com.example.erpserver.entities.TipoPlano;
import com.example.erpserver.repositories.CeoRepositorio;
import com.example.erpserver.repositories.FuncionariosRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.FuncionarioSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ServicoFuncionarios {

    private final FuncionariosRepositorio funcionarios;
    private final CeoRepositorio ceos;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public ServicoFuncionarios(
            FuncionariosRepositorio funcionarios,
            CeoRepositorio ceos,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.funcionarios = funcionarios;
        this.ceos = ceos;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- Adicionar Funcionario ----------
    @Transactional
    public Optional<Funcionario> adicionarFuncionario(
            String token,
            FuncionarioDTO dto
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        TipoPlano plano = ceos.findById(ceoId).map(Ceo::getPlano).orElse(TipoPlano.BASICO);

        long limite = switch (plano) {
            case COMPLETO -> 20;
            case INTERMEDIARIO -> 10;
            case BASICO -> 5;
        };

        if (funcionarios.countByCeoId(ceoId) >= limite ||
                funcionarios.existsByCeoIdAndCpf(ceoId, dto.getCpf())) {
            return Optional.empty();
        }

        Funcionario funcionario = Funcionario.builder()
                .nome(dto.getNome())
                .ceo(Ceo.builder().id(ceoId).build())
                .cpf(dto.getCpf())
                .salario(dto.getSalario())
                .bonus(dto.getBonus())
                .setor(dto.getSetor())
                .telefone(dto.getTelefone())
                .build();

        return Optional.of(funcionarios.save(funcionario));

    }

    // ---------- Editar Funcionario ----------
    @Transactional
    public Optional<Funcionario> editarFuncionario(
            String token,
            UUID funcionarioId,
            FuncionarioDTO dto
    ) {

        UUID ceoId = jwtUtil.extrairCeoId(token);

        if (funcionarios.findByCeoIdAndCpf(ceoId, dto.getCpf())
                .filter(f -> !f.getId().equals(funcionarioId))
                .isPresent()) {
            return Optional.empty(); // CPF já usado
        }

        return funcionarios.findByCeoIdAndId(ceoId,funcionarioId)
                .map(f -> {

                    f.setNome(dto.getNome());
                    f.setBonus(dto.getBonus());
                    f.setSalario(dto.getSalario());
                    f.setCpf(dto.getCpf());
                    f.setSetor(dto.getSetor());
                    f.setTelefone(dto.getTelefone());

                    return funcionarios.save(f);
                });
    }

    // ---------- Promover Funcionario ----------
    @Transactional
    public Optional<Funcionario> promoverFuncionario(
            String token,
            UUID funcionarioId,
            CadastroFuncionarioDTO dto
    ){
        UUID ceoId = jwtUtil.extrairCeoId(token);

        if (funcionarios.findByEmail(dto.getEmail()).isPresent()) {
            return Optional.empty();
        }

        return funcionarios.findById(funcionarioId)
                .filter(f -> f.getCeo().getId().equals(ceoId))
                .map(f -> {

                    if(dto.getTipo() != null){
                        f.setEmail(dto.getEmail());
                        f.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
                        f.setTipo(dto.getTipo());
                    } else {
                        f.setEmail(null);
                        f.setTipo(null);
                        f.setSenhaHash(null);
                    }

                    return funcionarios.save(f);
                });
    }

    // ---------- Remover Funcionario ----------
    @Transactional
    public Optional<Funcionario> removerFuncionario(
            String token,
            UUID funcionarioId
    ) {

        UUID ceoId = jwtUtil.extrairCeoId(token);

        return funcionarios.findByCeoIdAndId(ceoId, funcionarioId)
                .map(f -> {
                    funcionarios.delete(f);
                    return f;
                });
    }

    // ---------- Buscar Funcionario (Paginação) ----------
    public PaginaDTO<Funcionario> buscarFuncionarios(
            String token,
            String cpf,
            String nome,
            String telefone,
            TipoEspecializacao tipo,
            int pagina,
            int tamanho
    ) {

        UUID ceoID = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Funcionario> spec = FuncionarioSpecifications.comFiltros(ceoID, cpf, nome, telefone, tipo);

        return PaginaDTO.from(funcionarios.findAll(spec, pageable));
    }
}
