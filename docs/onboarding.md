# Onboarding

Human-oriented map of this repository. Coding agents should treat [`AGENTS.md`](../AGENTS.md) as the dense entry point; humans should also read [`CONTRIBUTING.md`](../CONTRIBUTING.md). How docs are split for both audiences: [documentation-structure.md](documentation-structure.md).

## What this repo is

A **Java specialised line** for **GDS-compliant** frontends: **Spring Boot + Thymeleaf** generate HTML; **GOV.UK Frontend** is the only UI library; **no frontend frameworks** for UI. Exact **HTML parity** against official Frontend fixtures. See [project-purpose.md](project-purpose.md) and [tech-stack.md](tech-stack.md).

**Stack:** Java 25 LTS, Spring Boot 4.1, Thymeleaf, Maven. Component HTML is rendered by native Java ports of Frontend macros (`uk.gov.example.govuk`); page shells use Thymeleaf.

**GOV.UK Frontend is Node + Nunjucks upstream.** Install `govuk-frontend` from npm for fixtures, CSS (via Sass), and JS — do **not** call Nunjucks at request time from this Java line.

**Demo service:** [example-service.md](example-service.md) (fishing rod licence). **Hosting:** [deploying-on-render.md](deploying-on-render.md).

**Official guidance:** search the URLs in [guidance-sources.md](guidance-sources.md).

**Priorities:** frontend web performance → frontend security → reduced maintenance → accessibility → inclusive design ([priorities.md](priorities.md)).

## Components vs patterns

| Kind          | What it is                                                               | How we build it                       | Fixture parity?                                                         |
| ------------- | ------------------------------------------------------------------------ | ------------------------------------- | ----------------------------------------------------------------------- |
| **Component** | Design System building block (button, text input, …)                     | Java renderer matching Frontend HTML  | **Yes** — official `fixtures.json`                                      |
| **Pattern**   | Guidance for a journey or page composition (addresses, check answers, …) | Compose shipped components into pages | **No** — follow Design System guidance; no invented pattern HTML suites |

## Repo map

```text
AGENTS.md
docs/                     # Dual-audience documentation
baseline/                 # Shared performance + OWASP header contract
styles/                   # Sass entry + govuk-overrides → dist/stylesheets/
scripts/                  # Node build helpers (styles)
src/main/java/uk/gov/example/
  govuk/                  # Component renderers + fixture loader
  baseline/               # Java port of baseline/policy.json
  service/                # Rod licence journey model/validation
  web/                    # Controllers, filters, page chrome
  session/                # In-memory sessions
src/main/resources/templates/
src/test/java/…           # Fixture parity + journey + unit tests
Dockerfile / render.yaml  # Render.com demo hosting
```

## Run modes

| Mode    | Command                                                                       |
| ------- | ----------------------------------------------------------------------------- |
| Preview | `npm ci && npm run build:styles && DEMOS_ENABLED=true ./mvnw spring-boot:run` |
| Test    | `npm test` then `./mvnw test`                                                 |
| Verify  | `npm run verify && ./mvnw verify`                                             |
| Deploy  | See [deploying-on-render.md](deploying-on-render.md)                          |

## Testing mindset

1. **Parity checks (primary)** compare Java `Render` output to fixture `html` for every fixture.
2. **Never** edit fixture `html` to make tests pass — fix the renderer.
3. JaCoCo **100%** applies to application packages outside `uk.gov.example.govuk` (that package is gated by fixture parity).

Details: [testing-components.md](testing-components.md).

## Related

- [example-service.md](example-service.md)
- [preview-server.md](preview-server.md)
- [deploying-on-render.md](deploying-on-render.md)
- [styles.md](styles.md) — Sass cascade; no `!important` in service CSS
