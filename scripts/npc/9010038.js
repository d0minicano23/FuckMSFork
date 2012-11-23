function start() {
    cm.sendSimple ("Buy what you want its the Magic Shop#k\r\n#L0##rMagic Medal Shop#k\r\n#L1##rMagic Hat Shop#k\r\n#L2##rMagic Face Accessory Shop#k\r\n#L3##rMagic Shoe Shop#k\r\n#L4##rMagic Cape Shop#k\r\n#L5##rMagic Top Shop#k\r\n#L6##rMagic Glove Shop#k\r\n#L7##bMagic Overall Shop #k\r\n#L8##bMagic Bottom Shop#k\r\n#L9##bMagic Shield Shop#k\r\n#L10##bMagic Staff Shop#l\r\n#L11##bMagic Wand Shop#l");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        cm.openShop(10200);
      } else if (selection == 1) {
        cm.openShop(10201);
      } else if (selection == 2) {
        cm.openShop(10202);
      } else if (selection == 3) {
        cm.openShop(10205);
      } else if (selection == 4) {
        cm.openShop(10206);
      } else if (selection == 5) {
        cm.openShop(10207);
      } else if (selection == 6) {
        cm.openShop(10208);
      } else if (selection == 7) {
        cm.openShop(10209);
      } else if (selection == 8) {
        cm.openShop(10210);
      } else if (selection == 9) {
        cm.openShop(10212);
      } else if (selection == 10) {
        cm.openShop(10228);
      } else if (selection == 11) {
        cm.openShop(10231);
    } else {
        cm.openNpc(1202010);
    }
}  