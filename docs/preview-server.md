# Preview server

Local server for human parity checks and pattern demos.

## Status

**Java / Spring Boot** — see [tech-stack.md](tech-stack.md).

```sh
npm ci
npm run build:styles
PATH=/opt/homebrew/opt/openjdk@25/bin:$PATH ./mvnw spring-boot:run
```

Listens on `PORT` (default `8080`). Open:

| Path                                 | Purpose                                                              |
| ------------------------------------ | -------------------------------------------------------------------- |
| `/`                                  | Start page                                                           |
| `/components`                        | Component catalogue (requires `DEMOS_ENABLED=true` or local default) |
| `/components/:name?fixture=`         | Fixture preview with parity banner                                   |
| `/components/:name/fixture?fixture=` | Raw fixture HTML fragment                                            |
| `/health`                            | Liveness                                                             |

## Expectations

- Homepage lists components (and patterns) as **links only** — no embedded live demos.
- A preview surface per component renders **only the selected** fixture, with a parity banner vs official `html`.
- A raw-fixture surface returns an HTML **fragment** for automation.
- Preview and fixture surfaces are Development / Testing only (gated by `DEMOS_ENABLED` in production).
- Preview responses use the same [`baseline/`](../baseline/) headers as production. On local HTTP, HSTS is not sent.
- Optional health / readiness endpoints follow the stack’s normal conventions; missing optional infra should not block Frontend-only preview.

## After code changes

Rebuild styles with `npm run build:styles` when Sass changes; restart `./mvnw spring-boot:run` (or rely on Spring DevTools if enabled). Hard-refresh the browser. Confirm focus states, header/footer, and a failing-form example during visual QA after Frontend upgrades ([upgrading-govuk-frontend.md](upgrading-govuk-frontend.md)).
