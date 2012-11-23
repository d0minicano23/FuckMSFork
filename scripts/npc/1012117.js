function start() {
    cm.sendSimple ("Buy what you want it's the common shop.#k\r\n#L0##rCommon Medals Shop#k\r\n#L1##rCommon Hats Shop#k\r\n#L2##rFace Accessory Shop#k\r\n#L3##rEye Accessory Shop#k\r\n#L4##rEar Accessory Shop#k\r\n#L5##rCommon Shoes Shop#k\r\n#L6##rCommon Cape Shop#k\r\n#L7##bCommon Tops Shop#k\r\n#L8##bCommon Glove shop#k\r\n#L9##bCommon Overall Shop#k\r\n#L10##bCommon Bottom Shop#l\r\n#L11##bCommon Shield Shop#l\r\n#L12#Mixed Accesories Shop#l\r\n#L13##bAxe 1 Hand Shop#l\r\n#L14##bAxe 2 Hand Shop#k\r\n#L15##bBlunt 1 Hand Shop#k\r\n#L16##bBlunt 2 Hand Shop#k\r\n#L17##dClaw Shop#k\r\n#L18##dDagger Shop#k\r\n#L19##dPoleArm Shop#k\r\n#L20##dSpear Shop#k\r\n#L21##dStaff Shop#k\r\n#L22##dSword 1 hand shop#k\r\n#L23##dSword 2 hand Shop#k\r\n#L24##dWand Shop#k\r\n#L25##dShovel Shop#k\r\n#L26##dPickaxe Shop#k\r\n#L27##dDualBow + 2 Kataras Shop#k\r\n#L28##dArrows + Card Shop#k\r\n#L29##dCane Shop#k\r\n#L30##dBareHands Shop");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        cm.openShop(10000);
      } else if (selection == 1) {
        cm.openShop(10001);
      } else if (selection == 2) {
        cm.openShop(10002);
      } else if (selection == 3) {
        cm.openShop(10003);
      } else if (selection == 4) {
        cm.openShop(10004);
      } else if (selection == 5) {
        cm.openShop(10005);
      } else if (selection == 6) {
        cm.openShop(10006);
      } else if (selection == 7) {
        cm.openShop(10007);
      } else if (selection == 8) {
        cm.openShop(10008);
      } else if (selection == 9) {
        cm.openShop(10009);
      } else if (selection == 10) {
        cm.openShop(10010);
      } else if (selection == 11) {
        cm.openShop(10012);
      } else if (selection == 12) {
        cm.openShop(10013);
      } else if (selection == 13) {
        cm.openShop(10016);
      } else if (selection == 14) {
        cm.openShop(10017);
      } else if (selection == 15) {
        cm.openShop(10018);
      } else if (selection == 16) {
        cm.openShop(10019);
      } else if (selection == 17) {
        cm.openShop(10021);
      } else if (selection == 18) {
        cm.openShop(10023);
      } else if (selection == 19) {
        cm.openShop(10026);
      } else if (selection == 20) {
        cm.openShop(10027);
      } else if (selection == 21) {
        cm.openShop(10028);
      } else if (selection == 22) {
        cm.openShop(10029);
      } else if (selection == 23) {
        cm.openShop(10030);
      } else if (selection == 24) {
        cm.openShop(10031);
      } else if (selection == 25) {
        cm.openShop(10032);
      } else if (selection == 26) {
        cm.openShop(10033);
      } else if (selection == 27) {
        cm.openShop(10034);
      } else if (selection == 28) {
        cm.openShop(10035);
      } else if (selection == 29) {
        cm.openShop(10036);
      } else if (selection == 30) {
        cm.openShop(10039);
		/*
      } else if (selection == 31) {
        cm.openShop(10030);
      } else if (selection == 32) {
        cm.openShop(10031);
      } else if (selection == 33) {
        cm.openShop(10032);
      } else if (selection == 34) {
        cm.openShop(10033);
      } else if (selection == 35) {
        cm.openShop(10034);
      } else if (selection == 36) {
        cm.openShop(10035);
      } else if (selection == 37) {
        cm.openShop(100320);
      } else if (selection == 38) {
        cm.openShop(10037);
      } else if (selection == 39) {
        cm.openShop(10051);
		*/
    } else {
        cm.openNpc(1202010);
    }
}  