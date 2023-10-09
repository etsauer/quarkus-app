FROM quay.io/etsauer/quarkus-app:1.0.0-SNAPSHOT

ARG LAST_COMMIT_DATE_TIME
ARG LAST_COMMIT_SHA

LABEL io.openshift.build.commit.date=${LAST_COMMIT_DATE_TIME}
LABEL io.openshift.build.commit.id=${LAST_COMMIT_SHA}
LABEL io.openshift.build.source-location="https://github.com/etsauer/quarkus-app"