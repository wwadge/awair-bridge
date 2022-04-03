Awair <--> PW bridge


This app will read from Awair API and feed data onto Planetwatch (PW). It is designed to run 
headless. 

# Running the app

!!!
Before you run the app, make sure you read the configuration section
!!!

Then:

a) Simplest is via docker: docker run -it -e awair_token=ey... -e pw_initialRefreshToken=eya... docker.io/wwadge/awair-bridge

the -e options are overrides to any property in the main config (src/main/resources/application.yml). For example to set awair.fetchrate you can set
the environmental property -e awair_fetchrate=900

Any property can be overridden by an environment variable of the same name, with the characters changed to upper case, and the dots changed to underscores. This means that if we want to override any property, you can do it by setting an environment variable.



b) Build/run it (if you prefer building the code from source):

Build the code via `./gradlew build`

So later you can run:

`java -jar build/libs/awair-bridge-<version>.jar`

Build the code and run it right away:

`./gradlew bootRun` 

(or ./gradlew build then run )

There is no need to install gradle or java, it is self-hosting.


# Configure the app

If you just blindly run the app right away, you're probably going to see it fail with an invalid token: 

``400 Bad Request: "{"error":"invalid_grant","error_description":"Invalid refresh token"}"``

That's because you MUST pass in some configuration options by either editing
src/main/resources/application.yml or overriding them via env variables (see above)


The key things to configure:
1) awair.token 

This is the key that lets us talk to awair. Open your Awair Home Application, click one of your sensors and press Awair+, Awair APIs Beta, Cloud API, Get API Token. Copy that key that looks like "ey...."

2) pw.initialRefreshToken 

This is annoying because we want to run headless but PW have not yet enabled the right configuration to make it easy to just use a username/password combination. What we do here is to login first elsewhere and grab the refresh token, thereafter we keep refreshing that token without the need for a UI. 

Therefore for one time only you need to:

a) Run the alternate tool: https://github.com/Sheherezadhe/awair-uploader

b) Before you login, open developer tools and go on network tab

c) Login, then look for the request named "token". Click response and it should look like this:

{
"access_token": "eyJhbGciOiJS...",
"expires_in": 1800,
"refresh_expires_in": 0,
"refresh_token": "eyJhbG...",
"token_type": "Bearer",
"id_token": "eyJhbG.."
not-before-policy": 0,
"session_state": "...",
"scope": "openid profile offline_access email"
}

Copy the refresh_token into pw.initialRefreshToken.

Where's this refreshed token kept? You have some options controlled by 
persistence options:

persistence.type = memory  | local | google | firebase

memory is simplest and means just keep in memory. If you restart the app, you might 
still have a stale refresh token which might fail.

local means the token is stored on a file on disk

google means the app will run as a cloud run function in Google Run

firebase means the app will run on some server and the token is stored in firebase DB which is
free for the data storage requirement.

# Running on raspberry pi/docker

It's java so it should run anywhere, but simplest is to use docker:

``
sudo docker run -d --restart=always -v ~/awair-bridge:/data -it -e persistence.type=local -e persistence.localFile=/data/token.json -e awair_token=YOUR-AWAIR-TOKEN  -e pw.initialRefreshToken=YOUR-FIRST-REFRESH-TOKEN wwadge/awair-bridge
``

For raspberry pi v3 please try:  wwadge/awair-bridge:armv7

# Running on google cloud


1. Create new firebase project (https://firebase.google.com/). Note down your project-id to set later on.
2. Firestore DB -> create database. Keep production mode selected. Any location.
3. Click rules and enable full access as follows:

```
   rules_version = '2';
   service cloud.firestore {
        match /databases/{database}/documents {
              match /{document=**} {
                allow read, write: if true;
        }
       }
   }
```

If you're not running under "google cloud", you will need a firebase credentials key:

Under cog-wheel icon next to project overview -> project settings -> service accounts: Generate new secure key. Move the generated file somewhere accessible.

Now to configure the app:

``
persistence.type = firebase
persistence.firestoreProjectId = what you set in step #1
persistence.serviceAccountFile = location of the file of step 5
``

# Troubleshooting

!!
Cloudflare might block request for you forcing you to use a VPN. PW configured
their service to block potential harmful calls, this implies many cloud providers
are banned by their IP address (they also do TLS fingerprinting and user-agent checks
but these have been bypassed in the code)
!!

Try adding ``--debug`` as a command line argument to see more logs.

You might see: 
```
 s.c.a.AnnotationConfigApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataBridgeImpl' defined in file [/app/classes/com/pw/awairbridge/service/impl/DataBridgeImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.pw.awairbridge.service.impl.DataBridgeImpl]: Constructor threw exception; nested exception is org.springframework.web.client.ResourceAccessException: I/O error on POST request for "https://login.planetwatch.io/auth/realms/Planetwatch/protocol/openid-connect/token": Unexpected end of file from server; nested exception is java.net.SocketException: Unexpected end of file from server
```

or code 1020 or http status code 403.