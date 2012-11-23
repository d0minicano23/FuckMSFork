function start() {
    cm.sendSimple ("Buy what you want it's the Warriors Shop.#k\r\n#L0##rWarriors Medals Shop#k\r\n#L1##rWarriors Hats Shop#k\r\n#L2##rWarriors Face Accessory Shop#k\r\n#L3##rWarriors Shoe Shop#k\r\n#L4##rWarriors Cape Shop#k\r\n#L5##rWarriors Top Shop#k\r\n#L6##rWarriors Glove Shop#k\r\n#L7##bWarriors Overall Shop#k\r\n#L8##bWarriors Bottom Shop#k\r\n#L9##bWarriors Shield Shop#k\r\n#L10##bAxe 1 Hand Shop#l\r\n#L11##bAxe 2 Hand Shop#l\r\n#L12#Blunt 1 Hand Shop#l\r\n#L13##bBlunt 2 Hand Shop#l\r\n#L14##bPolearm Hand Shop#k\r\n#L15##bSpear Shop#k\r\n#L16##bSword 1 Hand Shop#k\r\n#L17##bSword 2 hand Shop#k");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        cm.openShop(10100);
      } else if (selection == 1) {
        cm.openShop(10101);
      } else if (selection == 2) {
        cm.openShop(10102);
      } else if (selection == 3) {
        cm.openShop(10105);
      } else if (selection == 4) {
        cm.openShop(10106);
      } else if (selection == 5) {
        cm.openShop(10107);
      } else if (selection == 6) {
        cm.openShop(10108);
      } else if (selection == 7) {
        cm.openShop(10109);
      } else if (selection == 8) {
        cm.openShop(10110);
      } else if (selection == 9) {
        cm.openShop(10112);
      } else if (selection == 10) {
        cm.openShop(10116);
      } else if (selection == 11) {
        cm.openShop(10117);
      } else if (selection == 12) {
        cm.openShop(10118);
      } else if (selection == 13) {
        cm.openShop(10119);
      } else if (selection == 14) {
        cm.openShop(10126);
      } else if (selection == 15) {
        cm.openShop(10127);
      } else if (selection == 16) {
        cm.openShop(10129);
      } else if (selection == 17) {
        cm.openShop(10130);
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