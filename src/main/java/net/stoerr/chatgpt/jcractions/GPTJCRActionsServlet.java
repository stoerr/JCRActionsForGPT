package net.stoerr.chatgpt.jcractions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import javax.servlet.Servlet;
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

    /** For local testing in the browser an alternative to authorization. */
    public static final String COOKIE_AUTHORIZATION = "JcrActionsAuthorization";
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
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (checkAuthorizationFailure(request, response)) return;

        String extension = request.getRequestPathInfo().getExtension();

        switch (extension) {
            case "yaml":
                serveOpenAPISpecification(response);
                break;
            case "json":
                serveJSONRepresentation(request, response);
                break;
            case "data":
                serveBinaryData(request, response);
                break;
            default:
                response.setContentType("text/plain");
                response.getWriter().write("Invalid request extension.");
                break;
        }
    }

    /**
     * Returns a full OpenAPI spec of this servlet; we omit /jcractions.yaml since that is not an operation for the GPT.
     */
    private void serveOpenAPISpecification(SlingHttpServletResponse response) throws IOException {
        response.setContentType("text/yaml");
        response.getWriter().write("" +
                "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: GPT JCR Actions API\n" +
                "  description: API to interact with JCR content repository via GPT.\n" +
                "  version: 1.0.0\n" +
                "servers:\n" +
                "  - url: /bin/gpt/jcractions\n" +
                "paths:\n" +
                "  /jcractions.{depth}.json/{path}:\n" +
                "    get:\n" +
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
                "  /jcractions.data/{path}:\n" +
                "    get:\n" +
                "      summary: Returns the binary data of the JCR node\n" +
                "      parameters:\n" +
                "        - in: path\n" +
                "          name: path\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Binary data of the JCR node\n");
    }

    private void serveJSONRepresentation(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        RequestPathInfo requestPathInfo = request.getRequestPathInfo();
        Resource resource = requestPathInfo.getSuffixResource();
        if (resource == null) {
            response.setStatus(404);
            response.getWriter().write("No resource found for " + request.getRequestPathInfo().getResourcePath());
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

    private void serveBinaryData(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        Resource resource = request.getRequestPathInfo().getSuffixResource();
        if (resource == null) {
            response.setStatus(404);
            response.getWriter().write("No resource found for " + request.getRequestPathInfo().getResourcePath());
            return;
        }
        try (InputStream dataStream = resource.adaptTo(InputStream.class)) {
            if (dataStream != null) {
                String mimeType = resource.getResourceMetadata().getContentType();
                if (mimeType == null) {
                    response.setStatus(406);
                    response.getWriter().write("No mimetype available for " + resource.getPath());
                    return;
                }
                response.setContentType(mimeType);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = dataStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            } else {
                response.setStatus(404);
                response.getWriter().write("No binary data available for " + request.getRequestPathInfo().getResourcePath());
            }
        }
    }

    private boolean checkAuthorizationFailure(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String requiredSecret = config.apiKey();
        if (requiredSecret == null || requiredSecret.trim().isEmpty()) {
            response.setStatus(500);
            response.getWriter().write("No API key configured.");
            return true;
        }
        String secret = request.getHeader("X-API-Key");
        if (secret == null) {
            secret = request.getHeader("Authorization");
        }
        if (secret == null) {
            secret = request.getParameter("Authorization");
        }
        if (secret == null) {
            Cookie cookie = request.getCookie(COOKIE_AUTHORIZATION);
            if (cookie != null) {
                secret = cookie.getValue();
            }
        }
        if (secret == null || !secret.trim().equals(requiredSecret.trim())) {
            response.setStatus(401);
            response.getWriter().write("Invalid API key.");
            return true;
        }
        return false;
    }

}
