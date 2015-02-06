function SleepNode(parent) {
	var sleepNode = new ActionNode('sleep', parent, [/* a list of ValueNodes */]);
	
	return sleepNode;
}