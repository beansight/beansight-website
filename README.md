Beansight Website
=================

Description
-----------

Build using version 1 of the [Play! Framework](http://www.playframework.com/documentation/1.2.7/home).

How to run
----------

For your first run, you will need to create a new database structure or import an existing one. To create it, set `jpa.ddl` to `create`.

### Running locally

We have issues running with the `mem`of `db` databases. Prefer using a MySQL database running on your machine.

If you need to emulate an Heroku config option, use something like `$ export CLEARDB_DATABASE_URL=mysql://******`.

### Running on Heroku with the ClearDB add-on

Make sure the following Heroku options are set in your app config (`heroku config`):

  CLEARDB_DATABASE_URL: mysql://******
  PLAY_OPTS: --%prod -Dprecompiled=true
