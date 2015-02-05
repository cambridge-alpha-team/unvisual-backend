function PlayNode(parent) {
	var playNode = new ActionNode('play', parent, [/* a list of ValueNodes */]);
	
	return playNode;
}