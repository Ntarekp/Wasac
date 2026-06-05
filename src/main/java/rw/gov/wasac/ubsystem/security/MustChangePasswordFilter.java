package rw.gov.wasac.ubsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rw.gov.wasac.ubsystem.user.User;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PATHS = Set.of(
            "/api/auth/me",
            "/api/auth/profile",
            "/api/auth/change-password"
    );

    private final SecurityService securityService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!requiresPasswordChangeCheck(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            User user = securityService.getCurrentUser();
            if (Boolean.TRUE.equals(user.getMustChangePassword())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"message\":\"Password change required before accessing this resource.\",\"mustChangePassword\":true}"
                );
                return;
            }
        } catch (Exception ignored) {
            // Unauthenticated requests are handled by Spring Security.
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresPasswordChangeCheck(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        if (ALLOWED_PATHS.contains(path)) {
            return false;
        }
        return path.startsWith("/api/");
    }
}
