# Pelorus API Prototype

This project is a sample quarkus app made for generating data in a Pelorus setup.

## Deploy Pelorus

Install the Pelorus operator via OperatorHub/OLM, and then apply the following Pelorus CR:

```
oc apply -f pelorus.yaml
```

## Deploy the application

```
oc create namespace pelorus-api
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
oc rollout restart deployment/pelorus-api -n pelorus-api
```

## Dev Spaces live dev mode

Create appropriate permissions for the pelorus-api deployment to talk to the Pelorus Prometheus instance.

(Optional) If you're going to run Pelorus in a cluster external to your workspace (e.g. when using Developer Sandbox for Dev Spaces)

```
oc login ... (as a cluster admin)
```

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
oc login (as a developer user)
oc whoami -t > /tmp/token
echo "export PELORUS_URL=https://prometheus-pelorus-pelorus.apps.cluster-d67lc.d67lc.sandbox3014.opentlc.com/api/v1" > /projects/custom-env.sh
# run quarkus dev mode task
```
