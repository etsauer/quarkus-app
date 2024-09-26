# Dev Cluster Build

1. Provision a new cluster in the product demo system
1. Create a GitHub Oauth App for DevSpaces, and export the client id and secret to your environment.
    ```bash
    export GITHUB_OAUTH_CLIENT_ID=<client id>
    export GITHUB_OAUTH_CLIENT_SECRET=<client secret>
    ```
1. Create a GitHub Oauth App for OpenShift login, and export the client id and secret to your environment.
    ```bash
    export GITHUB_OAUTH_OPENSHIFT=<client id>
    export GITHUB_OAUTH_OPENSHIFT=<client secret>
    ```
1. Apply configs to install Dev Spaces and Pelorus
```bash
oc apply -f .bootstrap/namespaces.yaml
oc apply -f .bootstrap/devspaces-operator.yaml
envsubst < .bootstrap/github-oauth-openshift.yaml | oc apply -f -
envsubst < .bootstrap/github-oauth.yaml | oc apply -f -
envsubst < .bootstrap/github-pat-secret.yaml | oc apply -f -
envsubst < .bootstrap/identity-provider.yaml | oc apply -f -
oc apply -f .bootstrap/devspaces.yaml
oc apply -f .bootstrap/pelorus-operator.yaml
oc apply -f pelorus.yaml
```