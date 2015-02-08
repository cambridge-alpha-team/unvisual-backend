function SampleNode(parent, childNumber) {
	var sampleNode = new ActionNode('sample', parent, childNumber, [/* a list of ValueNodes */]);
	
	sampleNode.choiceNode = new ChoiceNode('sample name', this, [/* a list of string choices */])
	
	return sampleNode;
}