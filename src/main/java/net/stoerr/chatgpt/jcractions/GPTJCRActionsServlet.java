package net.stoerr.chatgpt.jcractions;

import java.io.IOException;
import javax.servlet.Servlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=net.stoerr.chatgpt JCR Actions Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/gpt/jcractions",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET
        })
public class GPTJCRActionsServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String extension = request.getRequestPathInfo().getExtension();

        switch (extension) {
            case "yaml":
                serveOpenAPISpecification(request, response);
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

    private void serveOpenAPISpecification(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/yaml");
        response.getWriter().write("OpenAPI spec content here...");
    }

    private void serveJSONRepresentation(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("JSON content for " + request.getRequestPathInfo().getResourcePath() + " here...");
    }

    private void serveBinaryData(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("binary/data-type"); // set appropriate mime type
        response.getWriter().write("Binary data for " + request.getRequestPathInfo().getResourcePath() + " here...");
    }

}