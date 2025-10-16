package com.example.erpserver.services;

import com.example.erpserver.DTOs.ItemProdutoDTO;
import com.example.erpserver.DTOs.TituloDTO;
import com.example.erpserver.entities.*;
import com.example.erpserver.repository.AssinantesRepositorio;
import com.example.erpserver.repository.ProdutosRepositorio;
import com.example.erpserver.repository.ProdutosDosTitulosRepositorio;
import com.example.erpserver.repository.TitulosRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.TituloSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoTitulos {

    private final TitulosRepositorio repositorio;
    private final ProdutosDosTitulosRepositorio produtosDosTitulos;
    private final ProdutosRepositorio produtosRepositorio;
    private final PessoasRepositorio pessoasRepositorio;
    private final AssinantesRepositorio assinantesRepositorio;
    private final JwtUtil jwtUtil;
    private final ServicoProdutos servicoProdutos;

    public ServicoTitulos(
            TitulosRepositorio repositorio,
            ProdutosDosTitulosRepositorio produtosDosTitulos,
            ProdutosRepositorio produtosRepositorio,
            PessoasRepositorio pessoasRepositorio,
            AssinantesRepositorio assinantesRepositorio,
            ServicoProdutos servicoProdutos,
            JwtUtil jwtUtil
    ) {
        this.repositorio = repositorio;
        this.produtosDosTitulos = produtosDosTitulos;
        this.produtosRepositorio = produtosRepositorio;
        this.pessoasRepositorio = pessoasRepositorio;
        this.assinantesRepositorio = assinantesRepositorio;
        this.servicoProdutos = servicoProdutos;
        this.jwtUtil = jwtUtil;
    }

    // ---------- Criar Título ----------
    @Transactional
    public Optional<Titulo> criarTitulo(String token, TituloDTO dto) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        Ceo ceo = assinantesRepositorio.findById(assinanteId).orElse(null);
        Clientes clientes = pessoasRepositorio.findByAssinanteIdAndId(assinanteId, dto.getId()).orElse(null);

        if (ceo == null || clientes == null) return Optional.empty();

        double valorTotal = 0.0;
        List<ProdutosDosTitulos> listaProdutosTitulo = new ArrayList<>();

        for (ItemProdutoDTO item : dto.getProdutos()) {
            Produto produto = produtosRepositorio.findByAssinanteIdAndId(assinanteId, item.getProdutoId()).orElse(null);
            if (produto == null) return Optional.empty();

            if (!clientes.isFornecedor() && produto.getEstoqueDisponivel() < item.getQuantidade()) {
                return Optional.empty();
            }

            ProdutosDosTitulos pdt = new ProdutosDosTitulos();
            pdt.setProduto(produto);
            pdt.setQuantidadeProduto(item.getQuantidade());
            listaProdutosTitulo.add(pdt);

            double subtotal = produto.getPreco() * item.getQuantidade();
            valorTotal += clientes.isFornecedor() ? -subtotal : subtotal;
        }

        Titulo titulo = new Titulo();
        titulo.setCeo(ceo);
        titulo.setClientes(clientes);
        titulo.setValor(valorTotal);
        titulo = repositorio.save(titulo);

        // Associar produtos ao título
        for (ProdutosDosTitulos pdt : listaProdutosTitulo) {
            pdt.setTitulo(titulo);
            if (clientes.isFornecedor()) {
                servicoProdutos.addEstoquePendente(token, pdt.getProduto().getId(), pdt.getQuantidadeProduto());
            } else {
                servicoProdutos.addEstoqueReservado(token, pdt.getProduto().getId(), pdt.getQuantidadeProduto());
            }
        }
        produtosDosTitulos.saveAll(listaProdutosTitulo);

        return Optional.of(titulo);
    }

    // ---------- Remover Título ----------
    @Transactional
    public Optional<Titulo> removerTitulo(String token, Long tituloId) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, tituloId)
                .map(titulo -> {

                    if (!titulo.isPago()) {
                        List<ProdutosDosTitulos> itens = produtosDosTitulos.findByTituloId(titulo.getId());

                        for (ProdutosDosTitulos item : itens) {
                            Produto produto = item.getProduto();
                            int quantidade = item.getQuantidadeProduto();

                            if (titulo.getClientes().isFornecedor()) {
                                produto.setEstoquePendente(produto.getEstoquePendente() - quantidade);
                            } else {
                                produto.setEstoqueReservado(produto.getEstoqueReservado() - quantidade);
                                produto.setEstoqueDisponivel(produto.getEstoqueDisponivel() + quantidade);
                            }

                            produtosRepositorio.save(produto);
                        }
                    }

                    repositorio.delete(titulo);
                    return titulo;
                });
    }

    // ---------- Marcar como Pago ----------
    @Transactional
    public Optional<Titulo> quitarTitulo(String token, Long tituloId) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, tituloId)
                .filter(titulo -> !titulo.isPago())
                .map(titulo -> {

                    List<ProdutosDosTitulos> itens = produtosDosTitulos.findByTituloId(titulo.getId());

                    for (ProdutosDosTitulos item : itens) {
                        Produto produto = item.getProduto();
                        int quantidade = item.getQuantidadeProduto();

                        if (titulo.getClientes().isFornecedor()) {
                            servicoProdutos.quitarEstoquePendente(token, produto.getId(), quantidade);
                        } else {
                            servicoProdutos.quitarEstoqueReservado(token, produto.getId(), quantidade);
                        }
                    }

                    titulo.setPago(true);
                    return repositorio.save(titulo);
                });
    }


    // ---------- Editar ----------
    @Transactional
    public Optional<Titulo> editarTitulo(String token, Long tituloId, TituloDTO dto) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        Optional<Titulo> tituloOpt = repositorio.findByAssinanteIdAndId(assinanteId, tituloId)
                .filter(titulo -> !titulo.isPago());
        if (tituloOpt.isEmpty() ) return Optional.empty();
        Titulo titulo = tituloOpt.get();

        Optional<Clientes> pessoaOpt = pessoasRepositorio.findByAssinanteIdAndId(assinanteId, dto.getId());
        if (pessoaOpt.isEmpty()) return Optional.empty();
        Clientes clientes = pessoaOpt.get();

        for (ItemProdutoDTO item : dto.getProdutos()) {
            Optional<Produto> produtoOpt = produtosRepositorio.findByAssinanteIdAndId(assinanteId, item.getProdutoId());
            if (produtoOpt.isEmpty()) return Optional.empty();
            Produto produto = produtoOpt.get();
            if (!clientes.isFornecedor() && produto.getEstoqueDisponivel() < item.getQuantidade()) {
                return Optional.empty();
            }
        }

        List<ProdutosDosTitulos> produtosAntigos = produtosDosTitulos.findByTituloId(titulo.getId());
        for (ProdutosDosTitulos item : produtosAntigos) {
            Produto produto = item.getProduto();
            int quantidade = item.getQuantidadeProduto();

            if (titulo.getClientes().isFornecedor()) {
                servicoProdutos.quitarEstoquePendente(token, produto.getId(), quantidade);
            } else {
                servicoProdutos.quitarEstoqueReservado(token, produto.getId(), quantidade);
                produto.setEstoqueDisponivel(produto.getEstoqueDisponivel() + quantidade);
                produtosRepositorio.save(produto);
            }
        }
        produtosDosTitulos.deleteByTitulo(titulo);

        titulo.setClientes(clientes);

        double valorTotal = 0.0;
        List<ProdutosDosTitulos> listaProdutosTitulo = new ArrayList<>();
        for (ItemProdutoDTO item : dto.getProdutos()) {
            Produto produto = produtosRepositorio.findByAssinanteIdAndId(assinanteId, item.getProdutoId()).get();

            ProdutosDosTitulos pdt = new ProdutosDosTitulos();
            pdt.setTitulo(titulo);
            pdt.setProduto(produto);
            pdt.setQuantidadeProduto(item.getQuantidade());
            listaProdutosTitulo.add(pdt);

            double subtotal = produto.getPreco() * item.getQuantidade();
            if (clientes.isFornecedor()) {
                servicoProdutos.addEstoquePendente(token, produto.getId(), item.getQuantidade());
                valorTotal -= subtotal;
            } else {
                servicoProdutos.addEstoqueReservado(token, produto.getId(), item.getQuantidade());
                valorTotal += subtotal;
            }
        }

        produtosDosTitulos.saveAll(listaProdutosTitulo);
        titulo.setValor(valorTotal);

        return Optional.of(repositorio.save(titulo));
    }



    // ---------- Buscar Titulos (Paginação) ----------
    public Page<Titulo> buscarTitulos(
            String token,
            String cpf,
            String nome,
            LocalDateTime inicio,
            LocalDateTime fim,
            Boolean pago,
            Boolean aReceber,
            Boolean aPagar,
            int pagina,
            int tamanho) {

        Long assinanteId = jwtUtil.extrairAdminId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Titulo> spec = TituloSpecifications.comFiltros(assinanteId, cpf, nome, inicio, fim, pago, aReceber, aPagar);

        return repositorio.findAll(spec, pageable);
    }

}

