package net.stoerr.chatgpt.jcractions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component(service = Servlet.class,
        configurationPolicy = ConfigurationPolicy.OPTIONAL,
        property = {
                Constants.SERVICE_DESCRIPTION + "=net.stoerr.chatgpt JCR Actions Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/public/gpt/jcractions",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET
        })
@Designate(ocd = GPTJCRActionsConfig.class)
public class GPTJCRActionsServlet extends SlingAllMethodsServlet {

    /**
     * For local testing in the browser an alternative to authorization.
     */
    public static final String COOKIE_AUTHORIZATION = "JcrActionsAuthorization";

    /**
     * The header we normally receive the API key for authorization from ChatGPT.
     * Using the "Authorization" header is in conflict with Apache Sling's own authorization.
     */
    public static final String HEADER_API_KEY = "X-JcrActions-Api-Key";

    private static final Logger LOG = LoggerFactory.getLogger(GPTJCRActionsServlet.class);
    /**
     * Those are metadata and not relevant for the resource content.
     */
    private static final Collection<String> ignoredMetadataAttributes = new HashSet<>(Arrays.asList("jcr:uuid", "jcr:lastModified",
            "jcr:lastModifiedBy", "jcr:created", "jcr:createdBy", "jcr:isCheckedOut", "jcr:baseVersion",
            "jcr:versionHistory", "jcr:predecessors", "jcr:mergeFailed", "jcr:mergeFailed", "jcr:configuration",
            "jcr:activity", "jcr:etag", "rep:hold", "rep:retentionPolicy", "rep:versions",
            "jcr:data", // handled with /jcractions.data
            // AEM specific:
            "cq:lastModified", "cq:lastModifiedBy", "cq:lastReplicated", "cq:lastReplicatedBy",
            "cq:lastReplicationAction", "cq:lastReplicationStatus",
            "cq:lastRolledout", "cq:lastRolledoutBy", "cq:lastRolledoutAction", "cq:lastRolledoutStatus"
    ));

    private volatile transient GPTJCRActionsConfig config;

    private final transient Gson gson = new Gson();

    @Activate
    @Modified
    protected void activate(GPTJCRActionsConfig config) {
        this.config = config;
    }

    @Deactivate
    protected void deactivate() {
        this.config = null;
    }

    @Override
    protected void doHead(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        // don't want to do the request twice - the normal head operation does this.
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        LOG.info("GPTJCRActionsServlet.doGet({})", request.getRequestURI());

        String extension = request.getRequestPathInfo().getExtension();
        if (null == extension || "yaml".equals(extension)) {
            serveOpenAPISpecification(request, response);
            return;
        }
        if (checkAuthorizationFailure(request, response)) return;

        switch (extension) {
            case "json":
                serveJSONRepresentation(request, response);
                break;
            case "data":
                serveBinaryData(request, response);
                break;
            default:
                logError(response, 404, "Invalid request extension " + extension + ".");
                break;
        }
    }

    @Override
    protected void doOptions(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        super.doOptions(request, response);
    }

    /**
     * Returns a full OpenAPI spec of this servlet; we omit /jcractions.yaml since that is not an operation for the GPT.
     */
    private void serveOpenAPISpecification(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("text/yaml");
        String url = request.getRequestURL().toString();
        url = url.replaceFirst("/bin/.*", "")
                .replaceFirst("^http://", "https://");
        String spec = "" +
                "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: GPT JCR Actions API\n" +
                "  description: API to interact with JCR content repository via GPT.\n" +
                "  version: 1.0.0\n" +
                "servers:\n" +
                "  - url: THEURL\n" +
                "paths:\n" +
                "  /bin/public/gpt/jcractions.{depth}.json/{path}:\n" +
                "    get:\n" +
                "      operationId: readJson\n" +
                "      x-openai-isConsequential: false\n" +
                "      summary: Returns JSON representation of the JCR node up to given children depth\n" +
                "      parameters:\n" +
                "        - in: path\n" +
                "          name: path\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: string\n" +
                "        - in: path\n" +
                "          name: depth\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: integer\n" +
                "            format: int32\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: JSON content of the JCR node\n" +
                "  /bin/public/gpt/jcractions.data/{path}:\n" +
                "    get:\n" +
                "      operationId: readData\n" +
                "      x-openai-isConsequential: false\n" +
                "      summary: Returns the binary data of the JCR node\n" +
                "      parameters:\n" +
                "        - in: path\n" +
                "          name: path\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Binary data of the JCR node\n";
        response.getWriter().write(spec.replace("THEURL", url));
    }

    private void serveJSONRepresentation(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        RequestPathInfo requestPathInfo = request.getRequestPathInfo();
        if (!pathIsAllowed(requestPathInfo.getSuffix())) {
            logError(response, 403, "Access to " + request.getRequestPathInfo().getResourcePath() + " not allowed in configuration.");
            return;
        }
        Resource resource = requestPathInfo.getSuffixResource();
        if (resource == null) {
            logError(response, 404, "No resource found for " + request.getRequestPathInfo().getResourcePath());
            return;
        }
        // find and parse numeric selector as depth
        int depth = Stream.of(requestPathInfo.getSelectors())
                .filter(s -> s.matches("\\d+"))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(0);
        response.setContentType("application/json");
        JsonObject json = toJsonObject(resource, depth);
        response.getWriter().write(gson.toJson(json));
    }

    private boolean pathIsAllowed(String path) {
        if (config.readAllowedPathRegex() == null || config.readAllowedPathRegex().length == 0) {
            return false;
        }
        for (String pathRegex : config.readAllowedPathRegex()) {
            if (path.matches(pathRegex)) {
                return true;
            }
        }
        return false;
    }

    private JsonObject toJsonObject(Resource resource, int depth) {
        JsonObject json = new JsonObject();
        resource.getValueMap().entrySet().stream()
                .filter(entry -> !ignoredMetadataAttributes.contains(entry.getKey()))
                .forEach(entry -> addProperty(json, entry.getKey(), entry.getValue()));
        if (depth > 0) {
            resource.getChildren().forEach(child -> json.add(child.getName(), toJsonObject(child, depth - 1)));
        }
        return json;
    }

    private void addProperty(JsonObject json, String key, Object value) {
        // if it's an array, we add all elements as json array
        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            json.add(key, gson.toJsonTree(array));
        } else if (value instanceof Number) {
            json.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            json.addProperty(key, (Boolean) value);
        } else if (value instanceof String) {
            json.addProperty(key, (String) value);
        } else {
            json.addProperty(key, String.valueOf(value));
        }
    }

    private boolean checkAuthorizationFailure(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String requiredSecret = config.apiKey();
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

    private void logError(SlingHttpServletResponse response, int statuscode, String message)
            throws IOException {
        response.setStatus(404);
        response.getWriter().write(message);
        LOG.info("GPTJCRActionsServlet returning error status {} {}", statuscode, message);
    }

    private void serveBinaryData(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        RequestPathInfo requestPathInfo = request.getRequestPathInfo();
        if (!pathIsAllowed(requestPathInfo.getSuffix())) {
            logError(response, 403, "Access to " + request.getRequestPathInfo().getResourcePath() + " not allowed in configuration.");
            return;
        }
        Resource resource = requestPathInfo.getSuffixResource();
        if (resource == null) {
            logError(response, 404, "No resource found for " + requestPathInfo.getResourcePath());
            return;
        }
        try (InputStream dataStream = resource.adaptTo(InputStream.class)) {
            if (dataStream != null) {
                String mimeType = resource.getResourceMetadata().getContentType();
                if (mimeType == null) {
                    logError(response, 406, "No mimetype available for " + resource.getPath());
                    return;
                }
                response.setContentType(mimeType);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = dataStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            } else {
                logError(response, 404, "No binary data available for " + requestPathInfo.getResourcePath());
            }
        }
    }

}
