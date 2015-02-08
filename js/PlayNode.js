function PlayNode(parent, childNumber) {
	var playNode = new ActionNode('play', parent, childNumber, [/* a list of ValueNodes */]);
	
	return playNode;
}