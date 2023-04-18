#!/bin/bash
export IDENTITY_SERVER_CERT=./certificates/certificate.cer
export IDENTITY_SERVER_PRIVATE_KEY=./certificates/mykey.pem.pkcs8

java -jar ./identity-service/build/libs/IdentityService.jar "$@"