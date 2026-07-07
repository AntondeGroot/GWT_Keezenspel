# GWT_Keezenspel

<img src="readme-images/keezenspel_construction.svg" alt="Keezenspel construction" width="600">

## To start the application (legacy GWT):
1) in the server folder run the Application file, this is a springboot application.
2) in a prompt run the code: ```mvn gwt:codeserver -pl *-client -am``` to start the Code Server
3) go to : http://localhost:4200/

## Running the Angular app (the new primary UI)
The Spring backend can serve the built Angular app (this is what `./deploy.sh` ships):

```
cd frontend && npm run build
mvn spring-boot:run -pl *-server -am -Penv-angular   # Angular served at http://localhost:4200/
```

For frontend hot-reload dev, `cd frontend && npm start` runs ng serve on 4201 and proxies the
API to the backend on 4200. To run the legacy GWT app alongside, start the backend on another
port: `mvn spring-boot:run -pl *-server -am -Dspring-boot.run.arguments="--server.port=4201"`.

## Deploying to Raspberry Pi

Run `./deploy.sh` to build the Angular app, package it into the server jar (`env-angular`),
upload, and restart the service.

**The app is reached at `/keezen`, but nginx strips that prefix before the backend.** The
Angular build sets `base-href=/keezen/`, and the app derives the prefix from `<base href>` at
runtime (`basePath()` in `src/app/base-path.ts`) so the browser requests `/keezen/games`,
`/keezen/gamestates/…/stream`, `/keezen/study-icon.svg`, etc. The nginx `location /keezen/`
block **strips** `/keezen/` (trailing slash on `proxy_pass`) and forwards `/games`, `/main-*.js`,
… to the backend:

```nginx
location /keezen/ {
  proxy_pass http://127.0.0.1:4200/;   # trailing slash → strips /keezen/
  proxy_set_header Host $host;
  # For the SSE streams (/gamestates, /chat) add if updates don't flow:
  # proxy_http_version 1.1; proxy_buffering off; proxy_read_timeout 3600s;
}
```

So the backend must serve at the **root — no context-path** (`deploy.sh` writes an empty
override). A context-path of `/keezen` here would double the prefix and 404 every request.
Local dev/tests run at the root too (default base-href `/` → `basePath()` returns `''`).


## This project is used to learn 
- Google Webtoolkit, which is used to convert Java code to JavaScript. Which at my current job is used for creating webpages.
- HTML, CSS and JavaScript
- OpenAPI
- Unit testing
- Selenium automated browser testing

The SpringBootApplication server is automatically all tests and shut down afterwards.

When a test fails a screenshot is taken.

- Mockito
- Continuous Integration
