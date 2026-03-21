# GWT_Keezenspel


## To start the application:
1) in the server folder run the Application file, this is a springboot application.
2) in a prompt run the code: ```mvn gwt:codeserver -pl *-client -am``` to start the Code Server
3) go to : http://localhost:4200/

## Deploying to Raspberry Pi

Run `./deploy.sh` to build, upload, and restart the service.

The context path (`/keezen`) is **not** set in `application.yaml` — it is only applied on the Pi via an external override file. Create `/opt/keezen/application-override.yaml` on the Pi:

```yaml
server:
  servlet:
    context-path: /keezen
```

This keeps local development and tests working without any prefix, while nginx on the Pi routes `https://<tunnel-url>/keezen/*` to this service. See the GameRoom README for the full nginx and cloudflared setup.


## This project is used to learn 
- Google Webtoolkit, which is used to convert Java code to JavaScript. Which at my current job is used for creating webpages.
- HTML, CSS and JavaScript
- OpenAPI
- Unit testing
- Selenium automated browser testing

The SpringBootApplication server is automatically restarted before each Selenium test.

When a test fails a screenshot is taken.

- Mockito
- Continuous Integration
