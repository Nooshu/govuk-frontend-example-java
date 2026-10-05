# GOV.UK Frontend example (Java)

> [!IMPORTANT]
> You are free to fork, modify, and maintain this repository for your own use.
>
> This includes using and adapting it within your department, organisation, or project.

> [!WARNING]
> 🚨 **Example repository only**
>
> This repository was created as a demonstration and will not be actively maintained or supported. It is not an official UK government project and is not endorsed, maintained, or supported by any UK government department, the Government Digital Service (GDS), or the GOV.UK Design System team.
>
> I will not be providing ongoing maintenance, updates, security fixes, or technical support.
>
> Use this code at your own risk. You are responsible for reviewing, testing, securing, maintaining, and ensuring the suitability of the code before using it in any service or production environment. I accept no responsibility or liability for any loss, damage, security issue, service failure, or other consequence resulting from its use.
>
> This repository is released under the MIT Licence. See the [LICENSE](LICENSE) file for the full licence terms.

**Specialised Java line** of the GDS-compliant frontend template: **Spring Boot + Thymeleaf** + **[GOV.UK Frontend](https://frontend.design-system.service.gov.uk/)** — **no** React/Vue/Angular/Svelte for UI. Official fixtures enable **100% HTML parity** testing of Java-rendered component HTML.

**Stack:** Java 25 · Spring Boot 4.1 · Thymeleaf · Maven — see [`docs/tech-stack.md`](docs/tech-stack.md).

## What you get

- **Apply for a fishing rod licence** — demo journey from start to confirmation ([`docs/example-service.md`](docs/example-service.md))
- **Component catalogue** at `/components` — every Frontend 6.5.1 component with all fixture variations ([`docs/preview-server.md`](docs/preview-server.md))
- **Render.com** Docker Blueprint for a public blog demo ([`docs/deploying-on-render.md`](docs/deploying-on-render.md))

## Related language lines

| Line              | Repository                                                                                              |
| ----------------- | ------------------------------------------------------------------------------------------------------- |
| TypeScript (Node) | [Nooshu/govuk-frontend-example-typescript](https://github.com/Nooshu/govuk-frontend-example-typescript) |
| Java (this repo)  | [Nooshu/govuk-frontend-example-java](https://github.com/Nooshu/govuk-frontend-example-java)             |

## Priorities

Frontend web performance → frontend security → reduced maintenance → accessibility → inclusive design.

## Who should read what

| You are…            | Start here                                                                                                                                                     |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Human developer** | [`docs/onboarding.md`](docs/onboarding.md) → [`CONTRIBUTING.md`](CONTRIBUTING.md) → [`docs/`](docs/README.md)                                                  |
| **AI coding agent** | [`AGENTS.md`](AGENTS.md) → [`.cursor/skills/gds-compliant-frontend/`](.cursor/skills/gds-compliant-frontend/SKILL.md) → playbooks in [`docs/`](docs/README.md) |

## Quick start

```sh
# Node 22+ (Frontend pin + Sass)
npm ci
npm run build:styles

# Java 25
./mvnw spring-boot:run
# http://127.0.0.1:8080  — set DEMOS_ENABLED=true for /components
```

## Verify

```sh
npm run verify          # docs + Sass + baseline tests
./mvnw verify           # fixture parity + journey tests + JaCoCo
```

## Deploy

See [`docs/deploying-on-render.md`](docs/deploying-on-render.md) for the Blueprint checklist.

## Licence and security

- Code in this repository: [MIT License](LICENSE)
- How to report vulnerabilities: [SECURITY.md](SECURITY.md)
- GOV.UK Design System and Frontend are maintained by GDS; Crown copyright / OGL apply to GOV.UK content patterns as documented on GOV.UK.
