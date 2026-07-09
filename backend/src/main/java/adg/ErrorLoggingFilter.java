package adg;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ErrorLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ErrorLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        int status = response.getStatus();
        if (status >= 400) {
            log.warn("HTTP {} {} {}ms", status, request.getRequestURI(), System.currentTimeMillis() - start);
        }
    }
}