function ValueNode(name, parent, childNumber, defaultValue, min, max) {
	var valueNode = new Node(name, parent, childNumber);
	valueNode.defaultValue = defaultValue;
	valueNode.cubelet = 0;
	valueNode.Min = min;
	valueNode.Max = max;
	
	return valueNode;
}

