# Pelorus API Prototype

This project is a sample quarkus app made for generating data in a Pelorus setup.

## Deploy Pelorus

Install the Pelorus operator via OperatorHub/OLM, and then apply the following Pelorus CR:

```
oc apply -f pelorus.yaml
```

## Packaging and running the application

The application can be packaged using:

```
./mvnw package -Dnative -Dquarkus.native.container-build=true -Dcommit.id="$(git rev-parse HEAD)" -Dcommit.date="$(git log -1 --format='%ad' --date='format:%a %b %d %H:%M:%S %Y %z')"
```

The build also produces a container image tagged with the snapshot version. In order to deploy the image, re-tag it for your repository, and then push it to a registry:

```
podman push quay.io/etsauer/quarkus-app:1.0.0-SNAPSHOT
```

## Run the application

```
oc apply -f kubernetes.yaml
```
