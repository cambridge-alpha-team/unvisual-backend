function SampleNode(parent) {
	var sampleNode = new ActionNode('sample', parent, [/* a list of ValueNodes */]);
	
	sampleNode.choiceNode = new ChoiceNode('sample name', this, [/* a list of string choices */])
	
	return sampleNode;
}