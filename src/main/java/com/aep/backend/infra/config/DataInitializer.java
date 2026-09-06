package com.aep.backend.infra.config;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.repository.SlaRepository;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoriaRepository categoriaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final SlaRepository slaRepository;

    @Override
    public void run(ApplicationArguments args) {
        criarUsuario("Administrador", "admin@gov", "00000000000", "admin", PerfilUsuario.GESTOR);
        criarUsuario("Atendente", "atendente@gov", "00000000001", "123", PerfilUsuario.ATENDENTE);
        criarUsuario("Usuário Teste", "teste123@gmail.com", "00000000002", "teste", PerfilUsuario.CIDADAO);
        criarCategorias();
        criarDepartamentos();
        criarSlaConfigs();
    }

    private void criarUsuario(String nome, String email, String cpf, String senha, PerfilUsuario perfil) {
        if (usuarioRepository.findByEmail(email).isPresent()) return;
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
    }

    private void criarCategorias() {
        Map.of(
                "Lixo Irregular", "Descarte irregular de lixo em locais não autorizados",
                "Queimada", "Queimada ilegal ou fogo descontrolado",
                "Manutenção Urbana", "Problemas de infraestrutura: buracos, iluminação, etc.",
                "Poluição", "Poluição de rios, ar ou solo"
        ).forEach((nome, desc) -> {
            if (!categoriaRepository.existsByNome(nome))
                categoriaRepository.save(new Categoria(nome, desc));
        });
    }

    private void criarDepartamentos() {
        Map.of(
                "Prefeitura Municipal", "Serviços gerais de infraestrutura urbana",
                "COPEL", "Companhia Paranaense de Energia Elétrica",
                "SANEPAR", "Companhia de Saneamento do Paraná",
                "COMPAGAS", "Companhia Paranaense de Gás",
                "SESP", "Secretaria de Estado da Segurança Pública",
                "SEMA", "Secretaria de Estado do Meio Ambiente"
        ).forEach((nome, desc) -> {
            if (!departamentoRepository.existsByNome(nome))
                departamentoRepository.save(new DepartamentoDestino(nome, desc));
        });
    }

    private void criarSlaConfigs() {
        List.of(
                new SlaConfig(Prioridade.BAIXA,  168, "7 dias — impacto local e baixo risco"),
                new SlaConfig(Prioridade.MEDIA,   72, "3 dias — impacto moderado"),
                new SlaConfig(Prioridade.ALTA,    24, "24 horas — risco à saúde ou segurança"),
                new SlaConfig(Prioridade.CRITICA,  4, "4 horas — risco imediato, emergência")
        ).forEach(sla -> {
            if (!slaRepository.existsByPrioridade(sla.getPrioridade()))
                slaRepository.save(sla);
        });
    }
}
