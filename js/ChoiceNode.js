function ChoiceNode(name, parent, choices) {
	var choiceNode = new Node(name,parent);
	
	choiceNode.choices = choices;
	
	return choiceNode;
}