function PlayNode(parent, childNumber) {
	var note = new ValueNode('note', this, 0, 60, 40, 100);
	var amp = new ValueNode('amp', this, 1, 0, 1);
	var release = new ValueNode('release', this, 1, 0, 5);
	var playNode = new ActionNode('play', parent, childNumber, [note, amp, release]);
	
	return playNode;
}