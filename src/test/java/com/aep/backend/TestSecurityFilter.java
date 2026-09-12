package com.aep.backend;

import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.usuario.entity.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class TestSecurityFilter implements Filter {

    private boolean hasRole(Authentication auth, String role) {
        if (auth == null) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario usuario) {
            PerfilUsuario perfil = usuario.getPerfil();
            if (perfil != null && perfil.name().equalsIgnoreCase(role)) {
                return true;
            }
        }

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities == null) {
            return false;
        }

        for (GrantedAuthority authority : authorities) {
            String value = authority.getAuthority();
            if (role.equalsIgnoreCase(value) || ("ROLE_" + role).equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated();

        Object securityCtxAttr = null;
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        if (session != null) {
            securityCtxAttr = session.getAttribute("SPRING_SECURITY_CONTEXT");
        }
        if (!authenticated && securityCtxAttr instanceof org.springframework.security.core.context.SecurityContext reqCtx) {
            SecurityContextHolder.getContext().setAuthentication(reqCtx.getAuthentication());
            auth = reqCtx.getAuthentication();
            authenticated = auth != null && auth.isAuthenticated();
        }

        if (!authenticated && req.getUserPrincipal() != null) {
            java.security.Principal p = req.getUserPrincipal();
            List<GrantedAuthority> inferredAuthorities = new ArrayList<>();
            if (req.isUserInRole("GESTOR") || req.isUserInRole("ROLE_GESTOR")) {
                inferredAuthorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            }
            if (req.isUserInRole("ATENDENTE") || req.isUserInRole("ROLE_ATENDENTE")) {
                inferredAuthorities.add(new SimpleGrantedAuthority("ROLE_ATENDENTE"));
            }
            Authentication built = new UsernamePasswordAuthenticationToken(p, "N/A", inferredAuthorities);
            SecurityContextHolder.getContext().setAuthentication(built);
            auth = built;
            authenticated = true;
        }

        boolean hasRoleGestor = hasRole(auth, "GESTOR");
        boolean hasRoleAtendente = hasRole(auth, "ATENDENTE");

        try {
            if (path.startsWith("/auth/")
                    || path.startsWith("/solicitacoes/publicas")
                    || path.startsWith("/solicitacoes/anonimas")
                    || path.startsWith("/solicitacoes/anonima")
                    || (path.startsWith("/usuarios/cadastrar") && method.equalsIgnoreCase("POST"))) {
                chain.doFilter(request, response);
                return;
            }

            if (path.equals("/logs") || path.startsWith("/logs")) {
                if (!authenticated || !hasRoleGestor) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.equals("/usuarios") && method.equalsIgnoreCase("POST")) {
                if (!authenticated || !hasRoleGestor) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.startsWith("/usuarios/") && method.equalsIgnoreCase("DELETE")) {
                if (!authenticated || !hasRoleGestor) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.equals("/solicitacoes") && method.equalsIgnoreCase("GET")) {
                if (!authenticated || (!hasRoleAtendente && !hasRoleGestor)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.equals("/solicitacoes") && method.equalsIgnoreCase("POST")) {
                if (!authenticated) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.startsWith("/solicitacoes/minhas")) {
                if (!authenticated) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.startsWith("/solicitacoes/protocolo")) {
                if (!authenticated) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if ((path.matches("/solicitacoes/.+/movimentacoes") || path.matches("/solicitacoes/.+/logs"))
                    && method.equalsIgnoreCase("GET")) {
                if (!authenticated || (path.matches("/solicitacoes/.+/logs") && !hasRoleGestor)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.matches("/solicitacoes/.+/status") && method.equalsIgnoreCase("PATCH")) {
                if (!authenticated || (!hasRoleAtendente && !hasRoleGestor)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (path.startsWith("/solicitacoes/")) {
                if (!authenticated) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}