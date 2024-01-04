package net.stoerr.chatgpt.jcractions;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
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

    private volatile transient GPTJCRActionsConfig config;

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
        response.setContentType("application/yaml");
        response.getWriter().write("" +
                "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: GPT JCR Actions API\n" +
                "  description: API to interact with JCR content repository via GPT.\n" +
                "  version: 1.0.0\n" +
                "servers:\n" +
                "  - url: /bin/gpt/jcractions\n" +
                "paths:\n" +
                "  /jcractions.json/{path}:\n" +
                "    get:\n" +
                "      summary: Returns JSON representation of the JCR node\n" +
                "      parameters:\n" +
                "        - in: path\n" +
                "          name: path\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: string\n" +
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
        response.setContentType("application/json");
        Resource resource = request.getRequestPathInfo().getSuffixResource();
        JsonObject json = new JsonObject();
        resource.getValueMap().forEach((key, value) -> json.addProperty(key, String.valueOf(value)));
        response.getWriter().write(new Gson().toJson(json));
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

}
