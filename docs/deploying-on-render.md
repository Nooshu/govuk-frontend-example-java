# Deploying on Render.com

Host the Java example service on [Render](https://render.com) as a public demo (for example for a blog post). This line is a Spring Boot process that needs Node-built Sass and `govuk-frontend` assets at runtime, so use a **Docker** Web Service (Render has no native JVM runtime).

Authoritative Render docs: [Docker](https://render.com/docs/docker), [Blueprints](https://render.com/docs/blueprint-spec).

## What this repo already includes

| File                                                         | Role                                                                |
| ------------------------------------------------------------ | ------------------------------------------------------------------- |
| [`render.yaml`](../render.yaml)                              | Blueprint: free Docker web service, `/health`, `DEMOS_ENABLED=true` |
| [`Dockerfile`](../Dockerfile)                                | Multi-stage: Node Sass → Maven package → Temurin 25 JRE             |
| `GET /health`                                                | Plain `ok` for Render health checks                                 |
| Spring `server.port=${PORT:8080}` / `server.address=0.0.0.0` | Honours Render’s `PORT`; binds all interfaces                       |

Build artefacts kept at runtime: `app.jar`, `node_modules/govuk-frontend`, `dist/stylesheets/application.css`, `baseline/policy.json`.

## Prerequisites

1. A [GitHub](https://github.com) account with this repo (or a fork) pushed to `main`.
2. A [Render](https://render.com) account (free tier is enough for a demo).
3. Local checks green before you deploy: `npm ci && npm run verify && ./mvnw verify` (optional but recommended).

## Post-merge deploy checklist (few clicks)

1. **Push `main`** to GitHub (including `Dockerfile` and `render.yaml`).
2. Open the [Render Dashboard](https://dashboard.render.com/) → **New +** → **Blueprint**.
3. Select this repository; confirm Render detects `render.yaml`.
4. Review the service name `govuk-frontend-example-java`, free plan, Docker runtime, health `/health`.
5. Click **Apply** / **Create** and wait until status is **Live** (first build installs npm + Maven; allow several minutes).
6. Open the `.onrender.com` URL from the dashboard.
7. Verify:
   - [ ] `https://<service>.onrender.com/health` returns `ok`
   - [ ] Start page loads with GOV.UK styling (`/`)
   - [ ] Yellow **Important** demo banner is visible
   - [ ] `/components` catalogue works (`DEMOS_ENABLED=true` in Blueprint)
   - [ ] View source / headers show `noindex, nofollow`; `/robots.txt` disallows `/`
   - [ ] A form POST in the licence journey retains the session cookie

## Option A — Blueprint (recommended)

Uses the committed [`render.yaml`](../render.yaml). Follow the checklist above.

## Option B — Manual Web Service

1. **New +** → **Web Service**.
2. Connect the GitHub repo and branch `main`.
3. Environment: **Docker**.
4. Health Check Path: `/health`.
5. Environment variables:

   | Key             | Value                       | Notes                                    |
   | --------------- | --------------------------- | ---------------------------------------- |
   | `DEMOS_ENABLED` | `true`                      | Keeps `/components` on for the blog demo |
   | `JAVA_OPTS`     | `-XX:MaxRAMPercentage=75.0` | Optional heap hint for free tier         |
   | `PORT`          | _(omit)_                    | Render injects this                      |

6. Create and wait for the first deploy.

## Environment variables

| Variable        | Required | Default / behaviour                                       |
| --------------- | -------- | --------------------------------------------------------- |
| `PORT`          | Injected | Render sets this; Spring reads `server.port=${PORT:8080}` |
| `DEMOS_ENABLED` | No       | Blueprint sets `true` so catalogue/previews stay on       |
| `JAVA_OPTS`     | No       | Extra JVM flags                                           |

HTTPS terminates at Render. The app treats `X-Forwarded-Proto: https` as secure transport for HSTS and Secure cookies.

## Free plan behaviour

- The service may **spin down** after idle time; the first request after idle can take ~30–60s (cold start). Mention that in a blog post if you link the demo.
- **In-memory sessions** reset when the instance restarts — fine for an example, not for a real multi-instance service.
- Disk is ephemeral.

## Local Docker parity

```sh
docker build -t govuk-frontend-example-java .
docker run --rm -p 8080:8080 -e DEMOS_ENABLED=true -e PORT=8080 govuk-frontend-example-java
# open http://127.0.0.1:8080
```

Without Docker:

```sh
npm ci
npm run build:styles
./mvnw -DskipTests package
DEMOS_ENABLED=true PORT=8080 java -jar target/govuk-frontend-example-java-*.jar
```

## Troubleshooting

| Symptom                            | Likely fix                                                                  |
| ---------------------------------- | --------------------------------------------------------------------------- |
| Build fails on `npm ci`            | Lockfile committed; Node 22 in the Dockerfile styles stage                  |
| Build fails on Maven / Java        | Dockerfile uses Temurin **25**; keep `java.version` 25 in `pom.xml`         |
| Deploy live but connection refused | Confirm the process binds `0.0.0.0` and reads `PORT`                        |
| HTML without GOV.UK CSS            | Image must include `dist/stylesheets/application.css` from the styles stage |
| Missing Frontend assets            | Image must include `node_modules/govuk-frontend`                            |
| `/components` 404                  | Set `DEMOS_ENABLED=true`                                                    |
| Health check failing               | Path must be exactly `/health`                                              |
| Session lost between requests      | Expected after free-tier spin-down                                          |

## Related

- [example-service.md](example-service.md) — what the demo contains
- [tech-stack.md](tech-stack.md) — Java + Node tooling roles
- [frontend-security.md](frontend-security.md) — headers and cookies behind a reverse proxy
