# Specification for Apache Sling Servlet Serving /bin/gpt/jcractions

## Overview

This document outlines the specifications for implementing an Apache Sling servlet that serves the
endpoint `/bin/gpt/jcractions`. The servlet is part of an OSGi bundle deployable on Apache Sling / Adobe Experience
Manager (AEM) and provides actions for a GPT to read the JCR content repository on configured paths.

## Requirements

- The servlet must be an OSGi component deployable on Apache Sling / AEM.
- It should allow a GPT to read properties of JCR nodes and contents of stored files.
- Configuration must include setting up paths for reading, and an API key for security.
- The servlet should be accessible over the internet via HTTPS.

## Configuration

- OSGi configuration "ChatGPT JCR Actions" should be set up.
- Configure regular expressions for read access on full paths.
- Configure the servlet to be accessible at `/bin/gpt/jcractions`.

## Security

- Implement API key authentication for accessing the servlet.

## General Notes

The servlet should use the package net.stoerr.chatgpt.jcractions and inherit from SlingAllMethodsServlet, but only
implement the GET method. The name should be GPTJCRActionsServlet.

## API

### GET /bin/gpt/jcractions.yaml

Returns the OpenAPI specification for the servlet.

### GET /bin/gpt/jcractions.json/{path}

Returns the JSON representation of the JCR node at the given path.

### GET /bin/gpt/jcractions.data/{path}

Returns the binary data of the JCR node at the given path. Sets the Content-Type header to the mime type of the binary.
