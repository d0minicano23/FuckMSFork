function action(mode, type, selection) {
	if (cm.job == 5000) {
		cm.forceCompleteQuest(20030);
		cm.warp(913070001,"sp");
		cm.playerMessage("Story Start !");
	} else {
	    cm.playerMessage("The Black Witch is being fought by someone else.");
	}
	cm.dispose();
}