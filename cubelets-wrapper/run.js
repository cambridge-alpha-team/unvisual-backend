var cubelets = require('./node-cubelets');
var BlockValueEventResponse = require('./node-cubelets/response/blockValueEvent.js');


// Connecting to Cubelet

var scanner = new cubelets.BluetoothScanner('Cubelet-RYB');

scanner.scan();

scanner.on('pass', function(connection, name, config) {

  connection.on('open', function() {
    console.error('Connection open');
  });

  connection.on('close', function() {
    console.error('Connection closed');
  });

  console.error('Connecting...');


  var construction = new cubelets.Construction(connection);

  construction.connect(function() {
    console.error('Construction connected, sending discover()');
    construction.discover();
  });


  // When construction changes, subscribe to value events from each new block

  function pretty(x) {
    if (x.type) {
      return {id: x.id, type: x.type};
    } else if (x.length) {
      return x.map(pretty);
    } else {
      return x;
    }
  }

  construction.on('change', function() {
    process.stderr.write("\u001b[2J\u001b[0;0H"); // clear the terminal
    console.error('Construction changed:');
    console.error('The origin is \n', pretty(construction.origin()), '\n');
    console.error('The direct neighbors are near \n', pretty(construction.near()), '\n');
    console.error('The other cubelets are far \n', pretty(construction.far()), '\n');

    construction.all().forEach(function(cubelet) {
      var id = cubelet.id;
      if (!values.hasOwnProperty(id)) {
        values[id] = null;
        grabCubelets.push(id);

        var command = new cubelets.RegisterBlockValueEventCommand(id);
        connection.postCommand(command);
      }
    });

    lastChange = new Date();
    sent = false;
  });


  // When we receive a value, work out which cubelet it was,
  // and output updated values dict

  var values = {};
  var grabCubelets = [];

  construction.on('value', function(response) {
    if (response.type.code === 'b') {
      var r = new BlockValueEventResponse(response.data, 'b');

      var index = grabCubelets.indexOf(r.id);
      if (index !== -1) {
        grabCubelets.splice(index, 1);
      }

      values[r.id] = r.value;
      console.log(values);
    }
  });


  // If subscribing to a block failed the first time, try it again.
  // This never works!

  setInterval(function() {
    if (!connection.isOpen()) return;

    grabCubelets.forEach(function(id) {
      var command = new cubelets.RegisterBlockValueEventCommand(id);
      connection.postCommand(command);
      console.error("grabbing " + id);
    });
  }, 1000);

});

