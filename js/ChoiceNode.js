function ChoiceNode(name, parent, childNumber, choices, choice) {
	var choiceNode = new Node(name,parent, childNumber);
	
	choiceNode.value = choices[choice];
	choiceNode.choices = choices;
	
	return choiceNode;
}