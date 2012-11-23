function action(mode, type, selection) {
	if (cm.job == 5000) {
   	cm.gainExp(1022);
	cm.gainItem(2000000,40);
	cm.gainItem(2000003,40);
	cm.forceCompleteQuest(20034);	
	cm.forceCompleteQuest(20035);
	cm.warp(913070051,"sp");
		cm.playerMessage("Complete.");
	} else {
	    cm.playerMessage("The Black Witch is being fought by someone else.");
	}
	cm.dispose();
}