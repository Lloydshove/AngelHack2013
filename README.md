AngelHack2013
=============

A place for us to share our AngelHack stuff


MONGO
=====
I run my mongoDB using :-
 ./mongod --dbpath ../../mongoDbForFlickr


Git Setup
=========
lloyds-air:AngelHack2013 lloyd$ git remote -v
heroku	git@heroku.com:goodspot.git (fetch)
heroku	git@heroku.com:goodspot.git (push)
origin	https://github.com/Lloydshove/AngelHack2013.git (fetch)
origin	https://github.com/Lloydshove/AngelHack2013.git (push)


Pushing to Heoku
================
git push heroku master


Local Dev
=========
To Switch between localhost and heroku
# set PORT=8080 in environment variables when running from intelliJ
# change goodspot.js to connect to localhost:8080
# change Datamanager.java and uncomment DB db = getRemoteDb() to just use the local connection instead