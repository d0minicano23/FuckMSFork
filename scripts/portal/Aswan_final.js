importPackage(Packages.client);

function enter(pi) {
	switch(pi.getMapId()) {
		case 955000300:
			if (pi.getMap().getAllMonstersThreadsafe().size() == 0) {
				pi.getPlayer().gainExp(30000, true, true, true);
				pi.getPlayer().addHonourExp(100 * pi.getPlayer().getHonourLevel());
				pi.getPlayer().dropMessage(5, 100 * pi.getPlayer().getHonourLevel()+" Honor Exp gained.");
				pi.warp(262010000,0);
				pi.worldMessage(6, "[Azwan] " + pi.getPlayer().getName() + " finished the Azwan Liberation of Hilla's Gang in Channel "+ pi.getClient().getChannel() +".");
			 } else {
				pi.playerMessage(5, "Please eliminate all the monsters, to get your reward!");
			}
			break;
}
}