# unvisual-backend
Java &amp; node.js backend.

Pulls in [unvisual-frontend](https://github.com/cambridge-alpha-team/unvisual-frontend) as a submodule. (Hopefully.)

## Getting the server running

1. Install [Maven](http://maven.apache.org/download.cgi) if you don't already have it (try `mvn -v` to find out).
2. Clone this repository, making sure to initialise the frontend submodule, either by using the `--recursive` flag when cloning, or running `git submodule update --init --recursive` after cloning.
3. Change directory to `{unvisual-backend}/java-backend/oscSender` and run `mvn install`.
4. Change directory to `{unvisual-backend}/java-backend/nodeCubeletsConnect` and run `mvn install`.
5. Change directory to `{unvisual-backend}/java-backend/nativeServer`.
6. Run `mvn package` then you can run the `*-jar-with-dependencies.jar` file under `target/`.
  * If you want to serve the a different front end to the one packaged in the jar, then pass the location of this front end as the first argument.
  * If you want to use the cubelets, you have to (at the moment) give a location for a front end to serve and then the program reading in the cubelets values.
7. Make sure that you have started Sonic Pi (i.e. you have the Sonic Pi app running, for now).
8. Go to [localhost:8000](http://localhost:8000) to use the project.


## Getting Cubelets to work

- Install [Node.js](http://nodejs.org/)

- Install the [Sonic Pi](http://sonic-pi.net) application.

- Clone this repository, and install `node-cubelets`:

    ```
    $ cd cubelets-wrapper/node-cubelets
    $ sudo npm install -g
    $ cd ..
    $ npm install underscore async
    $ npm install serialport hashish bluetooth-serial-port request temp
    ```

### Tips

The Bluetooth Cubelet can be a little quirky sometimes.

To ensure a successful connection, do:

1. Turn on the Cubelets.
2. Wait a few seconds, then start `run.js`.

