# Ideas for further features

- XPath Queries

- Try to enable the GPT to process or at least display images:
    - return images as base64 URL
    - return a direct URL to the image. (A bit annoying since it'd put the hidden URL to this server into the browser
      cache / history.)

- For security: implement a servlet filter for all requests that determines whether *any* request comes via serveo and
  blocks it if it's not authorized.
