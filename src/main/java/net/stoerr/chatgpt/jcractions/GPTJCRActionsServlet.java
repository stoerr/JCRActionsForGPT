package net.stoerr.chatgpt.jcractions;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import javax.servlet.Servlet;
import org.osgi.service.component.annotations.Component;
import java.io.IOException;

@Component(service = Servlet.class,
           property = {
               "sling.servlet.methods=" + HttpConstants.METHOD_GET,
               "sling.servlet.paths=/bin/gpt/jcractions"
           })
public class GPTJCRActionsServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write("GPT JCR Actions Servlet is operational.");
    }

}