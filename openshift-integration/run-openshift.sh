#!/bin/bash -e

DIR=`dirname $?`
DIR=`readlink -f $DIR`

OC_CONFIG=$DIR/openshift-config2

export KEYCLOAK_URL=https://secure-keycloak-myproject.127.0.0.1.nip.io/auth

cd /home/st/dev/go/src/github.com/openshift/origin
export PATH="$( source hack/lib/init.sh; echo "${OS_OUTPUT_BINPATH}/$( os::build::host_platform )/" ):${PATH}"

oc cluster up --base-dir=$OC_CONFIG --write-config=true --tag=latest

for i in kube-apiserver openshift-apiserver openshift-controller-manager; do
    cp $DIR/webhook.yaml $OC_CONFIG/$i
    sed -i "s|KEYCLOAK_URL|$KEYCLOAK_URL|" $OC_CONFIG/$i/webhook.yaml
done

sed -i 's|"webhookTokenAuthenticators":null|"webhookTokenAuthenticators":[{"configFile": "webhook.yaml"}]|' $OC_CONFIG/kube-apiserver/master-config.yaml

for i in openshift-apiserver openshift-controller-manager; do
    sed -i 's|webhookTokenAuthenticators: null|webhookTokenAuthenticators:\n  - configFile: "webhook.yaml"|' $OC_CONFIG/$i/master-config.yaml
done

oc cluster up --base-dir=$OC_CONFIG --tag=latest

oc login -u system:admin
oc project myproject

oc new-app -f $DIR/keycloak-https.json \
-p KEYCLOAK_USER=admin \
-p KEYCLOAK_PASSWORD=admin
