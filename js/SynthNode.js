function SynthNode(parent, childNumber) {
	var synthNode = new ApplyNode('sample', parent, childNumber);
	
	synthNode.choiceNode = new ChoiceNode('synth name', this, [/* a list of string choices */])
	
	return synthNode;
}