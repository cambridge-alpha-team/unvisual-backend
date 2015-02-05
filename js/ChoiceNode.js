function ChoiceNode(name, parent, choices, choice) {
	var choiceNode = new Node(name,parent);
	
	choiceNode.value = choices[choice];
	choiceNode.choices = choices;
	
	return choiceNode;
}