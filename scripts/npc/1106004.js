function action(mode, type, selection) {
	if (cm.job == 5000) {
		cm.gainItem(4033194,1);
		cm.gainItem(4033195,1);
		cm.playerMessage("You recieve Dusty Old Potion Box.");
	} else {
	    cm.playerMessage("The Black Witch is being fought by someone else.");
	}
	cm.dispose();
}