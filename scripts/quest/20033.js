function start(mode, type, selection) {
	qm.dispose();
    }

function end(mode, type, selection) {
   	qm.gainExp(932);
	qm.gainItem(2000000,30);
	qm.gainItem(2000003,30);
	qm.forceCompleteQuest(20033);
	qm.warp(913070050,"sp");
	qm.dispose();
    }