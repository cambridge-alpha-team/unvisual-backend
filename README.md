# unvisual-backend
Java &amp; node.js backend.

Pulls in [unvisual-frontend](https://github.com/cambridge-alpha-team/unvisual-frontend) as a submodule.

If you just want to use it, you can ignore the heading "Compiling the server" heading. That is in case you want to extend the backend in some way.
If you are only changing the front-end you can specify a custom location to serve, just use "-f {unvisual-frontend}".

## Running the server

1. Run java -jar unvisual.jar
2. Make sure that you have started Sonic Pi (i.e. you have the Sonic Pi app running, for now).
3. Go to [localhost:8000](http://localhost:8000) to use the project.

## Compiling the server

1. Install [Maven](http://maven.apache.org/download.cgi) if you don't already have it (try `mvn -v` to find out).
2. Clone this repository, making sure to initialise the frontend submodule, either by using the `--recursive` flag when cloning, or running `git submodule update --init --recursive` after cloning.
3. Change directory to `{unvisual-backend}/java-backend/oscSender` and run `mvn install`.
4. Change directory to `{unvisual-backend}/java-backend/nodeCubeletsConnect` and run `mvn install`. If unit tests fail, see the README.md in that directory.
5. Change directory to `{unvisual-backend}/java-backend/nativeServer`.
6. Run `mvn package` then you can run the `*-jar-with-dependencies.jar` file under `target/`.
  * You can use the flag "-h" to get help on its command line options.
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
