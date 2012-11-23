function start() {
    cm.sendSimple ("Buy what you want its the Pirate Shop.#k\r\n#L0##rPirate Medal Shop#k\r\n#L1##rPirate Hat Shop#k\r\n#L2##rPirate Face Accessory Shop#k\r\n#L3##rPirate Shoe Shop#k\r\n#L4##rPirate Cape Shop#k\r\n#L5##rPirate Glove Shop#k\r\n#L6##rPirate Overall Shop#k\r\n#L7##bPirate Gun Shop#k\r\n#L8##bPirate Knuckle Shop#k\r\n#L9##bPirate Cannon Shop#k");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        cm.openShop(10500);
      } else if (selection == 1) {
        cm.openShop(10501);
      } else if (selection == 2) {
        cm.openShop(10502);
      } else if (selection == 3) {
        cm.openShop(10505);
      } else if (selection == 4) {
        cm.openShop(10506);
      } else if (selection == 5) {
        cm.openShop(10508);
      } else if (selection == 6) {
        cm.openShop(10509);
      } else if (selection == 7) {
        cm.openShop(10524);
      } else if (selection == 8) {
        cm.openShop(10525);
      } else if (selection == 9) {
        cm.openShop(10535);
    } else {
        cm.openNpc(1202010);
    }
}  