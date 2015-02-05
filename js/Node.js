function Node(name, parent, children) {
 	this.name = name;
	this.parent = parent || null;
	//this is private because not all nodes have typical children
	this._children = children || [];
}

