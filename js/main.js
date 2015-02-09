var root = new RootNode();
var activeNode = new TempoNode();
var addCode = false;
var codeTypes = ["loop", "play", "sleep", "fx", "synth", "sample"];
var selectedCodeType;
var loopNumber = 1; //to uniquely name loops
var sonicPi = "";


//tests--------------
var loopA = LoopNode("loop" + loopNumber++, root, 1);
PlayNode(loopA, 0);
//-------------------

//The speech doesn't seem to work---------
var speechNode = (function() {
	  var textNode = document.createTextNode('');
	  $('#speech')[0].appendChild(textNode);
	  return textNode;
});

function say(message) {
	speechNode.textContent = message;
}
//----------------------------------------

//shortcut to make cubelet controlled
Mousetrap.bind(['command+c', 'ctrl+c'], function() {
	
	// return false to prevent default browser behaviour
	// and stop event from bubbling
	return false;
});

//shortcut to add a node
Mousetrap.bind(['command+a', 'ctrl+a'], function() {
	addCode = true;
	selectedCodeType = 0;
	console.log("adding code");
	
	// return false to prevent default browser behaviour
	// and stop event from bubbling
	return false;
});

//shortcut to delete a node
Mousetrap.bind(['command+d', 'ctrl+d'], function() {
	
	// return false to prevent default browser behaviour
	// and stop event from bubbling
	return false;
});

//shortcut to save code
Mousetrap.bind(['command+s', 'ctrl+s'], function() {
	
	// return false to prevent default browser behaviour
	// and stop event from bubbling
	return false;
});

//shortcut to open code
Mousetrap.bind(['command+o', 'ctrl+o'], function() {
	
	// return false to prevent default browser behaviour
	// and stop event from bubbling
	return false;
});

//shortcut to go out of list
Mousetrap.bind(['left'], function() {
	if(addCode) {
		addCode = false;
		console.log("adding code cancelled. activeNode is " + activeNode.name);		
	} else if(activeNode.parent != root) {
		activeNode = activeNode.parent;
		console.log(activeNode.name);
	}
});

//shortcut to go into a list
Mousetrap.bind(['right'], function() {
	if(addCode) {
		switch(selectedCodeType) {
			case 0:	// loop
				activeNode = new LoopNode("loop" + loopNumber++, activeNode.parent, (activeNode.parent.children.indexOf(activeNode) + 1));
				console.log("new loop created");
				break;
			case 1:	// play
				activeNode = new PlayNode(activeNode.parent, (activeNode.parent.children.indexOf(activeNode) + 1));
				console.log("new note created");
				break;
			case 2:	// sleep
				activeNode = new SleepNode(activeNode.parent, (activeNode.parent.children.indexOf(activeNode) + 1));
				console.log("new rest created");
				break;
			case 3:	// fx
				activeNode = new FXNode(activeNode.parent);
				console.log("new FX created");
				break;
			case 4:	// synth
				activeNode = new SynthNode(activeNode.parent, (activeNode.parent.children.indexOf(activeNode) + 1));
				console.log("new synth created");
				break;
			case 5:	// sample
				activeNode = new SampleNode(activeNode.parent, (activeNode.parent.children.indexOf(activeNode) + 1));
				console.log("new sample created");
				break;
			default:	// something's wrong
				console.log("ERROR on addCode.");
				break;
		}
		console.log(activeNode.name);
		addCode = false;
	} else if(activeNode.children.length > 0) {
		activeNode = activeNode.children[0];
		console.log(activeNode.name);
	}
});

//shortcut to go to the next element in a list
Mousetrap.bind(['down'], function() {
	if(addCode) {
		if(selectedCodeType < (codeTypes.length - 1)) {
			selectedCodeType++;
		}	
		console.log(codeTypes[selectedCodeType]);
	} else {
		var n = activeNode.parent.children.indexOf(activeNode);
		if((n + 1) < activeNode.parent.children.length) activeNode = activeNode.parent.children[n+1];
		console.log(activeNode.name);
	}
});

//shortcut to go to the previous element in a list
Mousetrap.bind(['up'], function() {
	if(addCode) {
		if(selectedCodeType > 0) {
			selectedCodeType--;
		}
		console.log(codeTypes[selectedCodeType]);
	} else {
		var n = activeNode.parent.children.indexOf(activeNode);
		if(n != 0) activeNode = activeNode.parent.children[n-1];
		console.log(activeNode.name);
	}
});

if (!String.prototype.startsWith) {
	  Object.defineProperty(String.prototype, 'startsWith', {
	    enumerable: false,
	    configurable: false,
	    writable: false,
	    value: function(searchString, position) {
	      position = position || 0;
	      return this.lastIndexOf(searchString, position) === position;
	    }
	  });
	}

//takes the root and generates code
function generateCode(node) {
	for (child in node.children) {
		if ((child.name).startsWith("loop")) {
			sonicPi = "live_loop :" + child.name + " do \n";
			(node.children).forEach(generateCode);
			sonicPi += "\n end"
		} else if ((child.name).startsWith("play")) {
			
		} else if ((child.name).startsWith("synth")) {
			
		} else if ((child.name).startsWith("sample")) {
			
		} else if ((child.name).startsWith("fx")) {
			
		}
		
	}
}
