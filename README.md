Beansight Website
=================

Description
-----------

Source code of the http://beansight.com website, a prediction exchange platform.

The website features a social infrastructure: user profiles, following/followed, custom timeline, social login and sharing. User generated content are predictions (called "insights"), they have categories, tags, comments. The website has administration tools.

Built using version 1 of the [Play! Framework](http://www.playframework.com/documentation/1.2.7/home) with a MySQL database.


Authors
-------

Cyril Dorsaz <cyril.dorsaz@gmail.com>
Guillaume Wolf <wolf.guillaume@gmail.com>
Jean-Baptiste Claramonte <Jeanbaptiste.claramonte@gmail.com>
Steren Giannini <steren.giannini@gmail.com>

Visual design by Colorz <http://www.colorz.fr/>

(c) All right reserved.


How to run
----------

For your first run, you will need to create a new database structure or import an existing one. To create it, set `jpa.ddl` to `create`.

### Running locally

We have issues running with the `mem`of `db` databases. Prefer using a MySQL database running on your machine: Create a MySQL database named `beansight` with a user named `beansight`and password `beansight`.

At first startup, the database tables need to be created. To do this, edit `application.conf` and replace `jpa.ddl=none` with `jpa.ddl=create`, then launch the Play! app by typing `play run`.

If for testing purposes, you need to emulate an Heroku config options, use env viriables, i.e. something like `$ export CLEARDB_DATABASE_URL=mysql://******`.

### Running on Heroku with the ClearDB add-on

Make sure the following Heroku options are set in your app config (`heroku config`):

  CLEARDB_DATABASE_URL: mysql://******
  PLAY_OPTS: --%prod -Dprecompiled=true
