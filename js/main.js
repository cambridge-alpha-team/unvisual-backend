var root = new RootNode();
var activeNode = new TempoNode();

//tests--------------
var loopA = LoopNode("loop a");
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
	
	// return false to prevent default browser behavior
	// and stop event from bubbling
	return false;
});

//shortcut to add a node
Mousetrap.bind(['command+a', 'ctrl+a'], function() {
	
	// return false to prevent default browser behavior
	// and stop event from bubbling
	return false;
});

//shortcut to delete a node
Mousetrap.bind(['command+d', 'ctrl+d'], function() {
	
	// return false to prevent default browser behavior
	// and stop event from bubbling
	return false;
});

//shortcut to go out of list
Mousetrap.bind(['left'], function() {
	if(activeNode.parent != root) {
		activeNode = activeNode.parent;
		console.log(activeNode.name);
	}
});

//shortcut to go into a list
Mousetrap.bind(['right'], function() {
	if(activeNode.children.length > 0) {
		activeNode = activeNode.children[0];
		console.log(activeNode.name);
	}
});

//shortcut to go to the next element in a list
Mousetrap.bind(['down'], function() {
	var n = activeNode.parent.children.indexOf(activeNode);
	activeNode = activeNode.parent.children[n+1];
	console.log(activeNode.name);
});

//shortcut to go to the previous element in a list
Mousetrap.bind(['up'], function() {
	var n = activeNode.parent.children.indexOf(activeNode);
	activeNode = activeNode.parent.children[n-1];
	console.log(activeNode.name);
});

