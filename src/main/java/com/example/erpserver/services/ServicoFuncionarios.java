package com.example.erpserver.services;

import com.example.erpserver.DTOs.FuncionarioDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.TipoEspecializacao;
import com.example.erpserver.entities.TipoPlano;
import com.example.erpserver.repository.CeoRepositorio;
import com.example.erpserver.repository.FuncionariosRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.FuncionarioSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ServicoFuncionarios {

    private final FuncionariosRepositorio funcionarios;
    private final CeoRepositorio ceos;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public ServicoFuncionarios(
            FuncionariosRepositorio funcionarios,
            CeoRepositorio ceos,
            JwtUtil jwtUtil
    ) {
        this.funcionarios = funcionarios;
        this.ceos = ceos;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ---------- Adicionar Funcionario ----------
    @Transactional
    public Optional<Funcionario> addFuncionario(
            String token,
            FuncionarioDTO dto
    ) {

        UUID ceoId = jwtUtil.extrairCeoId(token);

        return ceos.findById(ceoId)
                .flatMap(c -> {

                    int limite = switch (c.getPlano()) {
                        case TipoPlano.COMPLETO -> 20;
                        case TipoPlano.INTERMEDIARIO -> 10;
                        default -> 5;
                    };

                    long membrosAtuais = funcionarios.countByCeoId(ceoId);
                    if (membrosAtuais >= limite) {
                        return Optional.empty();
                    }

                    Funcionario funcionario = Funcionario
                            .builder()
                            .nome(dto.getNome())
                            .ceo(c)
                            .cpf(dto.getCpf())
                            .salario(dto.getSalario())
                            .bonus(dto.getBonus())
                            .setor(dto.getSetor())
                            .telefone(dto.getTelefone())
                            .build();

                    return Optional.of(funcionarios.save(funcionario));
                });
    }

    // ---------- Editar Funcionario ----------
    @Transactional
    public Optional<Funcionario> editarFuncionario(
            String token,
            UUID funcionarioId,
            FuncionarioDTO dto
    ) {

        UUID ceoId = jwtUtil.extrairCeoId(token);

        return funcionarios.findById(funcionarioId)
                .filter(f -> f.getCeo().getId().equals(ceoId))
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

    // ---------- Remover Funcionario ----------
    @Transactional
    public Optional<Funcionario> removerFuncionario(
            String token,
            UUID funcionarioId
    ) {

        UUID ceoId = jwtUtil.extrairCeoId(token);

        return funcionarios.findById(funcionarioId)
                .filter(f -> f.getCeo().getId().equals(ceoId))
                .map(f -> {
                    funcionarios.delete(f);
                    return f;
                });
    }

    // ---------- Buscar Funcionario (Paginação) ----------
    public PaginaDTO<Funcionario> buscarPorNome(
            String token,
            String cpf,
            String nome,
            TipoEspecializacao tipo,
            int pagina,
            int tamanho
    ) {

        UUID ceoID = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Funcionario> spec = FuncionarioSpecifications.comFiltros(ceoID, cpf, nome, tipo);

        return PaginaDTO.from(funcionarios.findAll(spec, pageable));
    }
}
