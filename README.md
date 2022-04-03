Awair <--> PW bridge


This app will read from Awair API and feed data onto Planetwatch (PW). It is designed to run 
headless

# Running the app

Before you run the app, make sure you read the configuration section!

Then:

a) Simplest is via docker: docker run -it -e awair_token=ey... -e pw_initialRefreshToken=eya... docker.io/wwadge/awair-bridge

the -e options are overrides to any property in the main config (src/main/resources/application.yml). For example to set awair.fetchrate you can set
the environmental property -e awair_fetchrate=900


b) Build/run: ./gradlew bootRun (or ./gradlew build then run java -jar build/libs/awair-bridge-0.0.1-SNAPSHOT.jar)




# Configure the app

All settings are in src/main/resources/application.yml - please look at the comments
within that file.

The key things to configure:
1) awair.token 

Open your Awair Home Application, click one of your sensors and press Awair+, Awair APIs Beta, Cloud API, Get API Token. Copy that key "ey...."

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
5. Under cog-wheel icon next to project overview -> project settings -> service accounts: Generate new secure key. Move the generated file somewhere accessible.

Now to configure the app:
persistence.type = firebase
persistence.firestoreProjectId = what you set in step #1
persistence.serviceAccountFile = location of the file of step 5
