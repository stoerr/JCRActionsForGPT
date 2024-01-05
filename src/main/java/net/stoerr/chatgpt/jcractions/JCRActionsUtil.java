package net.stoerr.chatgpt.jcractions;

import static net.stoerr.chatgpt.jcractions.GPTJCRActionsServlet.COOKIE_AUTHORIZATION;
import static net.stoerr.chatgpt.jcractions.GPTJCRActionsServlet.HEADER_API_KEY;

import java.io.IOException;

import javax.servlet.http.Cookie;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of various utility methods to make the main classes shorter.
 */
class JCRActionsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JCRActionsUtil.class);

    private JCRActionsUtil() {
        throw new IllegalStateException("No instances - utility class");
    }

    /**
     * Checks whether any of the given regexes match the given path.
     */
    static boolean pathIsAllowed(String[] allowedRegexes, String path) {
        if (allowedRegexes == null || allowedRegexes.length == 0) {
            return false;
        }
        for (String pathRegex : allowedRegexes) {
            if (path.matches(pathRegex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether any of the authentication methods succeeds.
     */
    static boolean checkAuthorizationFailure(SlingHttpServletRequest request, SlingHttpServletResponse response, String requiredSecret) throws IOException {
        if (requiredSecret == null || requiredSecret.trim().isEmpty()) {
            logError(response, 500, "No API key configured.");
            return true;
        }
        String secret = request.getHeader(HEADER_API_KEY);
        if (secret == null) { // for easy testing in the browser
            secret = request.getParameter(HEADER_API_KEY);
        }
        if (secret == null) {
            Cookie cookie = request.getCookie(COOKIE_AUTHORIZATION);
            if (cookie != null) {
                secret = cookie.getValue();
            }
        }
        if (secret == null) {
            logError(response, 401, "No API key given in request.");
            return true;
        }
        if (secret.startsWith("Basic ")) {
            secret = secret.substring("Basic ".length());
        }
        if (!secret.trim().equals(requiredSecret.trim())) {
            logError(response, 401, "Invalid API key.");
            return true;
        }
        return false;
    }

    static void logError(SlingHttpServletResponse response, int statuscode, String message)
            throws IOException {
        response.setStatus(404);
        response.getWriter().write(message);
        LOG.info("GPTJCRActionsServlet returning error status {} {}", statuscode, message);
    }

}
