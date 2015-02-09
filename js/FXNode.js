function FXNode(parent) {
	var fxNode = new ApplyNode("fx", parent, 0, []);
	while (parent.children.length > 1) {
		fxNode.children.push(parent.children.pop());
		fxNode.children[fxNode.children.length-1].parent = fxNode;
	}
	fxNode.children.reverse();
	
	return fxNode;
}

FXNode.prototype.remove = function() {
};
FXNode.prototype.addPlay = function() {
};
FXNode.prototype.addSleep = function() {
};
FXNode.prototype.addSample = function() {
};
FXNode.prototype.addSynth = function() {
};