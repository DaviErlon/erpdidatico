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

    private final ServicoLogAuditoria servicoLogAuditoria;

    public ServicoFuncionarios(
            FuncionariosRepositorio funcionarios,
            CeoRepositorio ceos,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            ServicoLogAuditoria servicoLogAuditoria
    ) {
        this.servicoLogAuditoria = servicoLogAuditoria;
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
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

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

        Ceo ceoReferencia = new Ceo();
        ceoReferencia.setId(ceoId);

        Funcionario emissor = new Funcionario();
        emissor.setId(emissorId);

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dto.getNome());
        funcionario.setCeo(ceoReferencia);
        funcionario.setCpf(dto.getCpf());
        funcionario.setSalario(dto.getSalario());
        funcionario.setBonus(dto.getBonus());
        funcionario.setSetor(dto.getSetor());
        funcionario.setTelefone(dto.getTelefone());

        funcionario = funcionarios.save(funcionario);

        servicoLogAuditoria.registrar(ceoReferencia, emissor, "CONTRATAÇÃO", "FUNCIONÁRIO", funcionario.getId(), "ADIÇÃO DE FUNCIONÁRIO");

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
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        if (funcionarios.findByCeoIdAndCpf(ceoId, dto.getCpf())
                .filter(f -> !f.getId().equals(funcionarioId))
                .isPresent()) {
            return Optional.empty(); // CPF já usado
        }

        return funcionarios.findByCeoIdAndId(ceoId, funcionarioId)
                .map(f -> {
                    f.setNome(dto.getNome());
                    f.setBonus(dto.getBonus());
                    f.setSalario(dto.getSalario());
                    f.setCpf(dto.getCpf());
                    f.setSetor(dto.getSetor());
                    f.setTelefone(dto.getTelefone());

                    Ceo ceoRef = new Ceo();
                    ceoRef.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(emissorId);

                    servicoLogAuditoria.registrar(ceoRef, emissor, "EDIÇÃO", "FUNCIONÁRIO", funcionarioId, "EDIÇÃO DOS DADOS DO FUNCIONÁRIO");

                    return funcionarios.save(f);
                });
    }

    // ---------- Promover Funcionario ----------
    @Transactional
    public Optional<Funcionario> promoverFuncionario(
            String token,
            UUID funcionarioId,
            CadastroFuncionarioDTO dto
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        if (funcionarios.findByEmail(dto.getEmail()).isPresent()) {
            return Optional.empty();
        }

        return funcionarios.findById(funcionarioId)
                .filter(f -> f.getCeo().getId().equals(ceoId))
                .map(f -> {
                    if (dto.getTipo() != null && !dto.getTipo().equals(TipoEspecializacao.CEO)) {
                        f.setEmail(dto.getEmail());
                        f.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
                        f.setTipo(dto.getTipo());
                    } else {
                        f.setEmail(null);
                        f.setTipo(null);
                        f.setSenhaHash(null);
                    }

                    Ceo ceoRef = new Ceo();
                    ceoRef.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(emissorId);

                    servicoLogAuditoria.registrar(ceoRef, emissor, "PROMOÇÃO", "FUNCIONÁRIO", funcionarioId, dto.getTipo() == null ? "FUNCIONÁRIO REBAIXADO" : "FUNCIOANRIO PROMOVIDO PARA" + dto.getTipo().toString());

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
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        Funcionario emissor = funcionarios.findByCeoIdAndId(ceoId, emissorId).orElse(null);
        if(emissor == null) return Optional.empty();

        return funcionarios.findByCeoIdAndId(ceoId, funcionarioId)
                .flatMap(f -> {

                    if(
                        f.getTipo().equals(TipoEspecializacao.CEO) ||
                        f.getTipo().equals(TipoEspecializacao.GESTOR) &&
                        !emissor.getTipo().equals(TipoEspecializacao.CEO)
                    ){
                        return Optional.empty();
                    } 

                    funcionarios.delete(f);

                    Ceo ceoRef = new Ceo();
                    ceoRef.setId(ceoId);

                    servicoLogAuditoria.registrar(ceoRef, emissor, "DEMIÇÃO", "FUNCIONÁRIO", f.getId(), "DEMISSÃO DO FUNCIONARIO " + f.getNome());

                    return Optional.empty();
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
