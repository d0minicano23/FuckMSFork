function start(mode, type, selection) {
	qm.dispose();
    }

function end(mode, type, selection) {
if (qm.haveItem(4033194)) {
	qm.gainItem(4033194,-1);
	qm.gainItem(4033195,-1);
	qm.gainExp(91);
	qm.gainItem(2000003,10);
	qm.gainItem(2000000,10);
	qm.forceCompleteQuest(20031);
	qm.warp(913070002,"sp");
	} else {
	    	qm.forceStartQuest(20031);
	}
	qm.dispose();
    }