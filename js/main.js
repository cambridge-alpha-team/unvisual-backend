var root = new RootNode();
var activeNode = new TempoNode();
var addCode = false;
var codeTypes = ["loop", "play", "sleep", "fx", "synth", "sample"];
var selectedCodeType;
var loopNumber = 1; //to uniquely name loops

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
		if(selectedCodeType == 0) {
			activeNode = new LoopNode("loop" + loopNumber++, activeNode.parent, (activeNode.parent.children.indexOf(activeNode) + 1));
			console.log("new loop created");
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

//takes the root and generates code
function generateCode() {
	
}
