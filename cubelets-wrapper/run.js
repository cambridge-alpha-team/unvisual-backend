var cubelets = require('./node-cubelets');
var BlockValueEventResponse = require('./node-cubelets/response/blockValueEvent.js');

var PATH = null; // '/dev/cu.Cubelet-RYB-AMP-SPP';



// Connecting to Cubelet

function withConnection(cb) {
  if (PATH) {
    var connection = new cubelets.SerialConnection({
      path: PATH,
    });
    console.error("Using " + PATH);
    cb(connection);

  } else {
    var scanner = new cubelets.BluetoothScanner('Cubelet-RYB');
    scanner.scan();

    console.error("Scanning...");

    scanner.on('pass', function(connection, name, config) {
      cb(connection);
    });
  }
}


// Once we have a connection...

withConnection(function(connection) {

  connection.on('open', function() {
    console.error('Connection open');
  });

  connection.on('close', function() {
    console.error('Connection closed');
  });

  console.error('Connecting...');


  var construction = new cubelets.Construction(connection);

  var hasMap = false;

  construction.connect(function() {
    console.error('Construction connected');

    function discover() {
      if (hasMap) return;
      construction.discover();
      setTimeout(discover, 5000);

      console.log('Sending discover...')
    }

    discover();
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

  var seenIds = [];

  construction.on('change', function() {
    hasMap = true;

    process.stderr.write("\u001b[2J\u001b[0;0H"); // clear the terminal
    console.error('Construction changed:');
    console.error('The origin is \n', pretty(construction.origin()), '\n');
    console.error('The direct neighbors are near \n', pretty(construction.near()), '\n');
    console.error('The other cubelets are far \n', pretty(construction.far()), '\n');

    var currentIds = [];

    construction.all().forEach(function(cubelet, index) {
      var id = cubelet.id;
      if (id === construction.origin().id) return;

      if (seenIds.indexOf(id) === -1) {
        seenIds.push(id);
        currentIds.push(id);

        var command = new cubelets.RegisterBlockValueEventCommand(id);
        connection.postCommand(command);
        console.error("Subscribing to " + id + "...");
      }
    });

    Object.keys(values, function(id) {
      if (currentIds.indexOf(id) > -1) {
        delete values[id];
      }
    });

    process.stdout.write(JSON.stringify(values) + "\n");

    lastChange = new Date();
    sent = false;
  });


  // When we receive a value, work out which cubelet it was,
  // and output updated values dict

  var values = {};

  connection.on('response', function(response) {
    if (response.type === cubelets.Responses.BLOCK_VALUE_EVENT) {
      values[response.id] = response.value;
      process.stdout.write(JSON.stringify(values) + "\n");
    }
  });

});

