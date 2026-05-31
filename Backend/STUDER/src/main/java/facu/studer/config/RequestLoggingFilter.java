package facu.studer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8); // Shortened ID

        // Log incoming request
        logger.info("➡️  Request Start [ID: {}] ---> {} {}",
                requestId,
                request.getMethod(),
                request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            String statusEmoji = getStatusEmoji(status);

            // Log outgoing response
            logger.info("{} Request End   [ID: {}] <--- {} {} | Status: {} | Duration: {}ms",
                    statusEmoji,
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration);
        }
    }

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) {
            return "✅"; // Success
        } else if (status >= 400 && status < 500) {
            return "❌"; // Client Error
        } else if (status >= 500) {
            return "🔥"; // Server Error
        } else {
            return "🔵"; // Other (e.g., redirects)
        }
    }
}