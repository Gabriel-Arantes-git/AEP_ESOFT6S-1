package com.aep.backend.cli;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.solicitacao.dto.SolicitacaoRequest;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.dto.UsuarioRequest;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.service.UsuarioService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Scanner;

public class CidadaoMenu {

    private final Scanner scanner;
    private final SolicitacaoService solicitacaoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;

    public CidadaoMenu(Scanner scanner, SolicitacaoService solicitacaoService, CategoriaService categoriaService,
                        UsuarioService usuarioService, AuthenticationManager authenticationManager) {
        this.scanner = scanner;
        this.solicitacaoService = solicitacaoService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
    }

    public void exibir() {
        while (true) {
            System.out.println("\n--- Cidadão ---");
            System.out.println("[1] Abrir solicitação (identificado)");
            System.out.println("[2] Abrir solicitação (anônimo)");
            System.out.println("[3] Consultar protocolo");
            System.out.println("[4] Cadastrar-se");
            System.out.println("[0] Voltar");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> abrirSolicitacaoIdentificado();
                case "2" -> abrirSolicitacaoAnonima();
                case "3" -> consultarProtocolo();
                case "4" -> cadastrarCidadao();
                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void abrirSolicitacaoIdentificado() {
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        Usuario usuario;
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha));
            usuario = (Usuario) auth.getPrincipal();
        } catch (Exception e) {
            System.out.println("Erro: E-mail ou senha inválidos.");
            return;
        }

        SolicitacaoRequest request = coletarDadosSolicitacao(false);
        if (request == null) return;

        try {
            Solicitacao salva = solicitacaoService.criar(request, usuario);
            System.out.println("Solicitação registrada. Protocolo: " + salva.getProtocolo());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void abrirSolicitacaoAnonima() {
        SolicitacaoRequest dadosBase = coletarDadosSolicitacao(true);
        if (dadosBase == null) return;

        System.out.print("Nome para contato (opcional, Enter para pular): ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail para contato (opcional, Enter para pular): ");
        String emailContato = scanner.nextLine().trim();

        SolicitacaoRequest request = new SolicitacaoRequest(
                dadosBase.categoriaId(), dadosBase.descricao(), dadosBase.bairro(),
                dadosBase.logradouro(), dadosBase.referencia(), true,
                nome.isBlank() ? null : nome, emailContato.isBlank() ? null : emailContato,
                dadosBase.latitude(), dadosBase.longitude(), dadosBase.cep());

        try {
            Solicitacao salva = solicitacaoService.criar(request, null);
            System.out.println("Solicitação registrada. Protocolo: " + salva.getProtocolo());
            System.out.println("Guarde o protocolo — é o único meio de acompanhamento.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private SolicitacaoRequest coletarDadosSolicitacao(boolean anonimo) {
        List<Categoria> categorias = categoriaService.listarAtivas();
        System.out.println("\nCategorias disponíveis:");
        for (int i = 0; i < categorias.size(); i++) {
            System.out.printf("[%d] %s%n", i + 1, categorias.get(i).getNome());
        }
        System.out.print("Categoria: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= categorias.size()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Categoria inválida.");
            return null;
        }
        CategoriaResumo categoria = CategoriaResumo.from(categorias.get(idx));

        System.out.print("Bairro: ");
        String bairro = scanner.nextLine().trim();
        System.out.print("Logradouro (opcional): ");
        String logradouro = scanner.nextLine().trim();
        System.out.print("Referência (opcional): ");
        String referencia = scanner.nextLine().trim();

        if (anonimo) System.out.println("Descrição (mínimo 50 caracteres para denúncia anônima):");
        else System.out.println("Descrição:");
        System.out.print("> ");
        String descricao = scanner.nextLine().trim();

        return new SolicitacaoRequest(
                categoria.id(), descricao, bairro,
                logradouro.isBlank() ? null : logradouro,
                referencia.isBlank() ? null : referencia,
                anonimo, null, null, null, null, null);
    }

    private void consultarProtocolo() {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            SolicitacaoPrinter.imprimirSolicitacao(s);
            SolicitacaoPrinter.imprimirHistorico(solicitacaoService.buscarHistorico(s.getId()));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cadastrarCidadao() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine().trim();
        System.out.print("Telefone: ");
        String telefone = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        try {
            usuarioService.cadastrar(new UsuarioRequest(nome, email, cpf, telefone, senha, PerfilUsuario.CIDADAO));
            System.out.println("Cadastro realizado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
