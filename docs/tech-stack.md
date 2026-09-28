# Tech stack

**Status: Java** — this is the Java specialised line of [govuk-frontend-example](https://github.com/Nooshu/govuk-frontend-example).

## Two layers

| Layer                          | Stack                                                                                               | Notes                                                                                                                                                                                                  |
| ------------------------------ | --------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **GOV.UK Frontend (upstream)** | **Node** package (`govuk-frontend`), **Nunjucks** macros (`template.njk`), official `fixtures.json` | Fixed by GDS. Node is for install, fixtures, Sass, and optional freshness checks — **not** for request-time HTML in this line.                                                                         |
| **This line (wrapper)**        | **Java 25** LTS, **Spring Boot 4.1**, Spring MVC, **Thymeleaf**, Maven                              | Server-side HTML generated **natively in Java**. Component HTML tracks Frontend macros/`template.njk` and proves **backend ≡ every fixture**. Page shells and journey pages use Thymeleaf. No SPA UIs. |

## Why this shape

| Concern                  | Choice                                                                                                                                                                                                                                       | Avoid                                                                                 |
| ------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| HTTP / routing           | Spring Boot Web (MVC)                                                                                                                                                                                                                        | Client-side routers                                                                   |
| Page documents           | Thymeleaf layouts / fragments                                                                                                                                                                                                                | Hand-pasted full pages; React/Vue/etc.                                                |
| GOV.UK component HTML    | Java renderers in `uk.gov.example.govuk` (StringBuilder ports of `template.njk`)                                                                                                                                                             | Request-time Nunjucks/Node; incomplete third-party wrappers                           |
| Trusted HTML             | Explicit `TrustedHtml` (never a bare `String` for `html` options)                                                                                                                                                                            | Unescaped user input                                                                  |
| Text escaping            | Nunjucks-aligned escaper (`&#39;`, `&#92;`, …)                                                                                                                                                                                               | Only `StringEscapeUtils` / default HTML escaper for fixture text                      |
| Fixtures                 | Jackson + ordered `Params`; load from `node_modules/govuk-frontend/.../fixtures.json`                                                                                                                                                        | Editing fixture `html`; copying fixtures into `src/test/resources` as source of truth |
| Parity gate              | Every fixture: Java `Render` output ≡ fixture `html` (trim outer whitespace only)                                                                                                                                                            | Weakening comparison to chase a pass rate                                             |
| Styles                   | Sass pipeline: `styles/application.scss` → Frontend `@use` → `govuk-overrides.scss` last                                                                                                                                                     | Serving prebuilt `govuk-frontend.min.css`; `!important` in service CSS                |
| Security / cache headers | Java port in [`uk.gov.example.baseline`](../src/main/java/uk/gov/example/baseline/) of [`baseline/policy.json`](../baseline/policy.json); wire via [`BaselineHeadersFilter`](../src/main/java/uk/gov/example/web/BaselineHeadersFilter.java) | Ad-hoc header middleware that drifts from the Node oracle                             |
| Compression              | Brotli (`br`) when advertised; Gzip only as fallback                                                                                                                                                                                         | Gzip-only                                                                             |
| Tests                    | JUnit 5, AssertJ, Jsoup (optional DOM helpers); JaCoCo **100%** on application packages (not `govuk`)                                                                                                                                        | Skipping fixture suites                                                               |

## Commands

```sh
# Node 22+ (Frontend pin, Sass, shared baseline/docs tests)
npm ci
npm run build:styles          # styles/ → dist/stylesheets/application.css
npm test                      # baseline + Sass tests at 100% coverage
npm run verify:docs

# Java 25 + Maven Wrapper
./mvnw verify                 # unit + fixture parity + JaCoCo gate
./mvnw spring-boot:run        # listens on PORT (default 8080); bind all interfaces

# Full local verify (docs + Sass + Java)
npm run verify && ./mvnw verify
```

| Item                            | Value                                                                                                           |
| ------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| Implementation language         | Java 25 LTS                                                                                                     |
| Framework                       | Spring Boot 4.1.x, Spring MVC, Thymeleaf                                                                        |
| Build                           | Maven + Maven Wrapper                                                                                           |
| Templating / component approach | Thymeleaf for page shells; native Java component renderers with fixture parity                                  |
| `govuk-frontend` (Node)         | `6.5.1` — check [latest release](https://github.com/alphagov/govuk-frontend/releases/latest) before upgrades    |
| Sass pipeline                   | `styles/application.scss` → `npm run build:styles` → `dist/stylesheets/application.css`                         |
| Overrides                       | [`styles/govuk-overrides.scss`](../styles/govuk-overrides.scss) last; cascade/specificity only; no `!important` |
| Page template reference         | https://design-system.service.gov.uk/styles/page-template/                                                      |
| Fixture testing guide           | https://frontend.design-system.service.gov.uk/testing-your-html/                                                |
| Preview                         | `/components` catalogue; `/components/:name?fixture=` previews (gated by `DEMOS_ENABLED` in production)         |
| Deploy                          | Docker on Render — see [deploying-on-render.md](deploying-on-render.md)                                         |

## Environment

| Variable        | Default / behaviour                                                     |
| --------------- | ----------------------------------------------------------------------- |
| `PORT`          | `8080` locally; Render injects                                          |
| `DEMOS_ENABLED` | unset/false hides catalogue in production; Render Blueprint sets `true` |
| `NODE_ENV`      | `production` implies demos off unless `DEMOS_ENABLED` overrides         |

## Hard constraints

- GOV.UK Frontend pins a single version; CSS/JS and fixtures must match.
- Request-time HTML must be native Java — do **not** shell out to Node/Nunjucks.
- Backend output must pass **every** official fixture `html` for every shipped component.
- Compile CSS via Sass ([styles.md](styles.md)); `govuk-overrides.scss` last; never `!important` in service CSS.
- No frontend UI frameworks for GOV.UK chrome.
- Coverage: **100%** instructions / branches / complexity for application packages under JaCoCo (`Application` and `uk.gov.example.govuk` excluded — govuk renderers are gated by the fixture parity suite, not line coverage).
- Before every Frontend upgrade: https://github.com/alphagov/govuk-frontend/releases/latest

**Coverage split:** `uk.gov.example.govuk` component HTML is proven by [`RenderFixtureParityTest`](../src/test/java/uk/gov/example/govuk/RenderFixtureParityTest.java) (backend ≡ every official fixture `html`). JaCoCo’s 100% gate applies to the rest of the application packages (`web`, `service`, `baseline`, `session`, `config`).

See [`AGENTS.md`](../AGENTS.md) and [`.cursor/rules/govuk-frontend-java.mdc`](../.cursor/rules/govuk-frontend-java.mdc).
