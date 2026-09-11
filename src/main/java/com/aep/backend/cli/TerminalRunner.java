package com.aep.backend.cli;

import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.domain.departamento.service.DepartamentoService;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Profile("cli")
@RequiredArgsConstructor
public class TerminalRunner implements ApplicationRunner {

    private final SolicitacaoService solicitacaoService;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;
    private final DepartamentoService departamentoService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void run(ApplicationArguments args) {
        Scanner scanner = new Scanner(System.in);
        CidadaoMenu cidadaoMenu = new CidadaoMenu(scanner, solicitacaoService, categoriaService, usuarioService, authenticationManager);
        AtendenteMenu atendenteMenu = new AtendenteMenu(scanner, solicitacaoService, departamentoService);
        GestorMenu gestorMenu = new GestorMenu(scanner, solicitacaoService);

        while (true) {
            System.out.println("\n=== Sistema de Denúncias Ambientais ===");
            System.out.println("[1] Cidadão");
            System.out.println("[2] Atendente / Gestor");
            System.out.println("[0] Sair");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> cidadaoMenu.exibir();
                case "2" -> menuLogin(scanner, atendenteMenu, gestorMenu);
                case "0" -> {
                    System.out.println("Encerrando.");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void menuLogin(Scanner scanner, AtendenteMenu atendenteMenu, GestorMenu gestorMenu) {
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

        switch (usuario.getPerfil()) {
            case GESTOR -> gestorMenu.exibir(usuario);
            case ATENDENTE -> atendenteMenu.exibir(usuario);
            default -> System.out.println("Acesso negado. Use o menu Cidadão.");
        }
    }
}
