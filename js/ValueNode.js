function ValueNode(name, parent, defaultValue, min, max) {
	var valueNode = new Node(name, parent);
	valueNode.defaultValue = defaultValue;
	valueNode.cubelet = 0;
	valueNode.Min = min;
	valueNode.Max = max;
	
	return valueNode;
}

