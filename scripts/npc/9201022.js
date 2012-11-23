function start() {
    cm.sendSimple ("Buy what you want its the Archer Shop#k\r\n#L0##rArcher Medal Shop#k\r\n#L1##rArcher Hat Shop#k\r\n#L2##rArcher Face Accessory#k\r\n#L3##rArcher Shoe Shop#k\r\n#L4##rArcher Cape Shop#k\r\n#L5##rArcher Top Shop#k\r\n#L6##rArcher Glove Shop#k\r\n#L7##bArcher Overall Shop#k\r\n#L8##bArcher Bottom Shop#k\r\n#L9##bArcher Bow Shop#k\r\n#L10##bArcher CrossBow Shop#l\r\n#L11##bMercedes Dual Bow Shop#l\r\n#L12##bMercedes Arrows#l");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        cm.openShop(10300);
      } else if (selection == 1) {
        cm.openShop(10301);
      } else if (selection == 2) {
        cm.openShop(10302);
      } else if (selection == 3) {
        cm.openShop(10305);
      } else if (selection == 4) {
        cm.openShop(10306);
      } else if (selection == 5) {
        cm.openShop(10307);
      } else if (selection == 6) {
        cm.openShop(10308);
      } else if (selection == 7) {
        cm.openShop(10309);
      } else if (selection == 8) {
        cm.openShop(10310);
      } else if (selection == 9) {
        cm.openShop(10320);
      } else if (selection == 10) {
        cm.openShop(10322);
      } else if (selection == 11) {
        cm.openShop(10334);
      } else if (selection == 12) {
        cm.openShop(10335);
    } else {
        cm.openNpc(1202010);
    }
}  