# Pelorus API Prototype

This project is a sample quarkus app made for generating data in a Pelorus setup.

## Deploy Pelorus

Install the Pelorus operator via OperatorHub/OLM, and then apply the following Pelorus CR:

```
oc apply -f pelorus.yaml
```

## Deploy the application

```
oc apply -f kubernetes.yaml
oc adm policy add-cluster-role-to-user cluster-reader system:serviceaccount:pelorus-api:default
```
## Packaging and redeploying the application

The application can be packaged using:

```
./mvnw package -Dnative -Dquarkus.native.container-build=true -Dcommit.id="$(git rev-parse HEAD)" -Dcommit.date="$(git log -1 --format='%ad' --date='format:%a %b %d %H:%M:%S %Y %z')" -Dorigin.url="$(git config --get remote.origin.url)" -Dmaven.test.skip
```

The build also produces a container image tagged with the snapshot version. In order to deploy the image, re-tag it for your repository, and then push it to a registry:

```
podman push quay.io/etsauer/quarkus-app:1.0.0-SNAPSHOT
```

```
oc rollout restart deployment/pelorus-api
```

## Dev Spaces live dev mode

Create appropriate permissions for the pelorus-api deployment to talk to the Pelorus Prometheus instance.

It requires `get` on `namespace`

```bash
oc apply -f pelorus-api-cluster-role.yaml
```

Bind the cluster role to the user or service account that will be running the pelorus-api app

```bash
oc adm policy add-cluster-role-to-user pelorus-api <Your-DevSpaces-User>
```

Run the app in a Dev Spaces terminal:

```bash
oc whoami -t > /tmp/token
export AUTH_TOKEN=/tmp/token
export PELORUS_URL=https://prometheus-pelorus.pelorus.svc:9091/api/v1
quarkus dev -Dquarkus.tls.trust-all=true -Dquarkus.http.host=0.0.0.0
```
