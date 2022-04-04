#!/bin/bash

# Keycloak Authorization Code Flow with Proof Key for Code Exchange (PKCE)
#
# Dependencies:
#
#   'brew install jq pup'
#
#   https://stedolan.github.io/jq/
#   https://github.com/ericchiang/pup


### ----------------------------

usage()
{
  printf 'Usage  : %s -a %s -r %s -c %s -l %s -u %s\n' "${0##*/}" \
    "<AUTHORITY>" "<REALM>" "<CLIENT_ID>" "<REDIRECT_URL>" "<USERNAME>"

  printf 'Example: %s -a "%s" -r "%s" -c "%s" -l "%s" -u "%s"\n' "${0##*/}" \
    "https://keycloak.example.com/auth" \
    "myrealm" \
    "myclient" \
    "https://myapp.example.com/" \
    "myusername"

  printf '\nAccepts password from stdin, env AUTHORIZATION_CODE_LOGIN_PASSWORD, or prompt.\n'
  exit 2
}

while getopts 'a:r:c:l:u:?h' c
do
  case $c in
    a) authority=$OPTARG ;;
    r) realm=$OPTARG ;;
    c) clientId=$OPTARG ;;
    l) redirectUrl=$OPTARG ;;
    u) username=$OPTARG ;;
    h|?) usage ;;
  esac
done

[[ -z $authority || -z $realm || -z $clientId || -z $redirectUrl || -z $username ]] && usage

password="$AUTHORIZATION_CODE_LOGIN_PASSWORD"
[[ -z $password ]] && read -rp "password: " -s password


### ----------------------------


base64url() { tr -d '[:space:]' | tr -- '+/' '-_' | tr -d = ; }

sha256sum() { printf "%s" "$1" | openssl dgst -binary -sha256 | openssl base64 -e | base64url ; }

codeVerifier=$(openssl rand -base64 96 | base64url)

cookieJar=$(mktemp "${TMPDIR:-/tmp}/cookie.jar.XXXX")
trap 'rm "$cookieJar"' EXIT

loginForm=$(curl -sSL --get --cookie "$cookieJar" --cookie-jar "$cookieJar" \
--ciphers ECDHE-RSA-AES128-GCM-SHA256 \
  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) awair-uploader/0.0.1 Chrome/96.0.4664.110 Electron/16.0.7 Safari/537.36' \
  --data-urlencode "client_id=${clientId}" \
  --data-urlencode "redirect_uri=$redirectUrl" \
  --data-urlencode "scope=openid offline_access" \
  --data-urlencode "response_type=code" \
  --data-urlencode "code_challenge=$(sha256sum "$codeVerifier")" \
  --data-urlencode "code_challenge_method=S256" \
  "$authority/realms/$realm/protocol/openid-connect/auth" \
  | pup '#kc-form-login attr{action}')


loginForm=${loginForm//\&amp;/\&}


codeUrl=$(curl -sS --cookie "$cookieJar" --cookie-jar "$cookieJar" \
--ciphers ECDHE-RSA-AES128-GCM-SHA256 \
  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) awair-uploader/0.0.1 Chrome/96.0.4664.110 Electron/16.0.7 Safari/537.36' \
  --data-urlencode "username=$username" \
  --data-urlencode "password=$password" \
  --write-out "%{redirect_url}" \
  "$loginForm")

code=${codeUrl##*code=}

refreshToken=$(curl -sS --cookie "$cookieJar" --cookie-jar "$cookieJar" \
--ciphers ECDHE-RSA-AES128-GCM-SHA256 \
  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) awair-uploader/0.0.1 Chrome/96.0.4664.110 Electron/16.0.7 Safari/537.36' \
  --data-urlencode "client_id=$clientId" \
  --data-urlencode "redirect_uri=$redirectUrl" \
  --data-urlencode "code=$code" \
  --data-urlencode "code_verifier=$codeVerifier" \
  --data-urlencode "grant_type=authorization_code" \
  "$authority/realms/$realm/protocol/openid-connect/token" \
  | jq -r ".refresh_token")

printf "%s" "$refreshToken"
