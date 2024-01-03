package net.stoerr.chatgpt.jcractions;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "ChatGPT JCR Actions Configuration")
public @interface GPTJCRActionsConfig {

    @AttributeDefinition(name = "Read Paths", description = "Regular expressions for read access on full paths.")
    String[] readPaths() default {};

    @AttributeDefinition(name = "API Key", description = "API key for security.")
    String apiKey() default "";

}