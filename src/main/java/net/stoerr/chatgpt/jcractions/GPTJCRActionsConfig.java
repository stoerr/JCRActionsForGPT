package net.stoerr.chatgpt.jcractions;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "stoerr.net ChatGPT JCR Actions Configuration",
        description = "Configuration for the GPTJCRActions Servlet: This servlet enables specific interactions with the JCR (Java Content Repository) in an Apache Sling/AEM environment. It allows a GPT (Generative Pre-trained Transformer) to read and interpret JCR node properties and file contents. The servlet serves requests at /bin/public/gpt/jcractions and supports various operations, including fetching JSON data and binary content of JCR nodes.")
public @interface GPTJCRActionsConfig {

    @AttributeDefinition(name = "Read Allowed Path Regex", description = "Regular expressions for allowing read access. These should match the full paths to access. If none given / nothing matches no read access is allowed.")
    String[] readAllowedPathRegex() default {};

    @AttributeDefinition(name = "API Key", description = "API key for security - has to match the API key configured in the GPT as API Key for Auth Type Basic.")
    String apiKey() default "";

}
