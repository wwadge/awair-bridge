Awair <--> PW bridge


This app will read from Awair API and feed data onto Planetwatch (PW). It is designed to run 
headless. 

# Running the app

!!!
Before you run the app, make sure you read the configuration section
!!!

Then:

a) Simplest is via docker: docker run -it -e awair_token=ey... -e pw_username=blah -e pw_password=bar docker.io/wwadge/awair-bridge

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

If you just blindly run the app right away, you're probably going to see it fail.

That's because you MUST pass in some configuration options by either editing
src/main/resources/application.yml or overriding them via env variables (see above)


The key things to configure:
1) awair.token 

This is the key that lets us talk to awair. Open your Awair Home Application, click one of your sensors and press Awair+, Awair APIs Beta, Cloud API, Get API Token. Copy that key that looks like "ey...."

If you have more than 1 key, separate them by a comma: -e awair_token=key1,key2,key3

2) pw.username

This is your PW username eg foo@gmail.com

3) pw.password

This is your PW password eg abcdef

# Running on raspberry pi/docker

It's java so it should run anywhere, but simplest is to use docker:

``
 docker run -d --restart=always -e awair_token=YOUR-AWAIR-TOKEN -e pw_username=foo@gmail.com -e pw_password=bar wwadge/awair-bridge
``

For raspberry pi v3 please try:  wwadge/awair-bridge:armv7


# Troubleshooting

!!
Cloudflare might block request for you forcing you to use a VPN. PW configured
their service to block potential harmful calls, this implies many cloud providers
are banned by their IP address (they also do TLS fingerprinting and user-agent checks
but these have been bypassed in the code)
!!

Try adding ``--debug`` as a command line argument to see more logs.

# Upgrading

- Pull the latest image: docker pull wwadge/awair-bridge
- Kill and restart your docker images:

Find your container id:

``
docker ps   
``

Kill it (use your container ID in the example below): 

``docker kill 12412ca``





