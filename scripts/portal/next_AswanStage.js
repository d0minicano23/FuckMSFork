function enter(pi) {
	switch(pi.getMapId()) {
		case 955000100:
			if (pi.getMap().getAllMonstersThreadsafe().size() == 0) {
				pi.warp(955000200,0);
			 } else {
				pi.playerMessage(5, "Please eliminate all the monsters, to go to Stage 2.");
			}
			break;
		case 955000200:
			if (pi.getMap().getAllMonstersThreadsafe().size() == 0) {
				pi.warp(955000300,0);
			} else {
				pi.playerMessage(5, "Please eliminate all the monsters, to go to Final Stage.");
			}
			break;
}
}

/*
 first beta one lol 

function enter(pi) {
var m = pi.getPlayer().getMapId();
   if (m == 955000100) {
        pi.warp(955000200);
        return true;
    } else {
        pi.warp(955000300);
        return true;
    }
}

*/
