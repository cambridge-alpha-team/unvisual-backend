function SleepNode(parent, childNumber) {
	var sleepNode = new ActionNode('sleep', parent, childNumber, [/* a list of ValueNodes */]);
	
	return sleepNode;
}