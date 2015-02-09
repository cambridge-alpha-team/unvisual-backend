function PlayNode(parent, childNumber) {
	var playNode = new ActionNode('play', parent, childNumber);
	ValueNode('note', playNode, 0, 60, 40, 100);
	ValueNode('amp', playNode, 1, 1, 0, 1);
	ValueNode('release', playNode, 2, 1, 0, 5);
	
	return playNode;
}