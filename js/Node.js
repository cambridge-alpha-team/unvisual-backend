function Node(name, parent, childNumber, children) {
 	this.name = name;
	this.parent = parent || null;
	this.children = children || [];
	if(childNumber != null) {
		parent.children.splice(childNumber, 0, this);
	} else if(parent != null) {
		parent.children.push(this);
	}
}

