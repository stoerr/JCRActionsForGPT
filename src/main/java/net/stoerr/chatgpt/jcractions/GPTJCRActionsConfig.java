package net.stoerr.chatgpt.jcractions;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "stoerr.net ChatGPT JCR Actions Configuration",
        description = "Configuration for the GPTJCRActions Servlet: This servlet enables specific interactions with the JCR (Java Content Repository) in an Apache Sling/AEM environment. It allows a GPT (Generative Pre-trained Transformer) to read and interpret JCR node properties and file contents. The servlet serves requests at /bin/gpt/jcractions and supports various operations, including fetching JSON data and binary content of JCR nodes.")
public @interface GPTJCRActionsConfig {

    @AttributeDefinition(name = "Read Paths", description = "Regular expressions for read access. These should match the full paths to access. If empty, no read access is allowed.")
    String[] readPaths() default {};

    @AttributeDefinition(name = "API Key", description = "API key for security - has to match the API key configured in the GPT as API Key for Auth Type Basic.")
    String apiKey() default "";

}
