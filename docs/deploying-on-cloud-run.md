# Deploying on Google Cloud Run

Host this Java example on [Cloud Run](https://cloud.google.com/run/docs). Spring Boot reads `server.port=${PORT:8080}` and binds `0.0.0.0`. Cloud Run’s default port is 8080. `server.forward-headers-strategy=framework` trusts `X-Forwarded-Proto`.

You need a Google Cloud project with billing enabled and the [gcloud CLI](https://cloud.google.com/sdk/docs/install). Cloud Run can scale to zero. Build minutes, Artifact Registry storage, and requests above the free tier are billed. The image build runs Maven and takes longer than the Node examples.

This service’s name is `govuk-frontend-example-java`, region `europe-west2` (London).

## What this repo includes

| File                                                            | Role                                              |
| --------------------------------------------------------------- | ------------------------------------------------- |
| [`Dockerfile`](../Dockerfile)                                   | Node Sass, Maven package, then a Temurin 25 JRE   |
| [`scripts/deploy-cloud-run.sh`](../scripts/deploy-cloud-run.sh) | Enables APIs and deploys from source              |
| `GET /health`                                                   | Plain `ok` for startup, readiness, and liveness   |

## Deploy

From the repo root, with a project selected:

```sh
gcloud config set project PROJECT_ID
./scripts/deploy-cloud-run.sh
```

| Variable        | Default                        |
| --------------- | ------------------------------ |
| `PROJECT`       | gcloud’s active project        |
| `REGION`        | `europe-west2`                 |
| `SERVICE`       | `govuk-frontend-example-java`  |
| `MEMORY`        | `1Gi`                          |
| `CPU`           | `1`                            |
| `MIN_INSTANCES` | `0`                            |
| `MAX_INSTANCES` | `3`                            |

The script sets `DEMOS_ENABLED=true` (the catalogue is off when that variable is unset) and `JAVA_OPTS=-XX:MaxRAMPercentage=75.0`.

### After deploy checklist

- [ ] `https://<service>-<hash>.<region>.run.app/health` returns `ok`
- [ ] Start page loads with GOV.UK styling
- [ ] `/components` works
- [ ] A form POST in the licence journey keeps the answers on the next page

## Run the image locally

```sh
docker build -t govuk-frontend-example-java .
docker run --rm -p 8080:8080 -e PORT=8080 -e DEMOS_ENABLED=true govuk-frontend-example-java
```

Open <http://127.0.0.1:8080>.
