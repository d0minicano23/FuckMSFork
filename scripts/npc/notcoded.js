function action(mode, type, selection) {
	if (cm.getNpc() >= 9901000) {
		cm.sendNext("Hello #h0#, I am in the Hall of Fame for reaching LEVEL 200.");
	} else {
		id=cm.getNpc();
		cm.sendNext("Hello, I am not coded yet. But my npc id is " + id + ".");

	}
	cm.safeDispose();
}