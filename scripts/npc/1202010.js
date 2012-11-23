/* Author: Burblish
 * ID: 1202010
 * Name: Pudin
 * Function: Palace Guard
 * Now a Coded NPC, created with TraNpcExtractor v0.3.0
 * TraNpcExtractor was written by Prio from StaticDEV.com
 */
function start() {
    cm.sendSimple ("Chose a Category.#k\r\n#L0##rMagican Shop#k\r\n#L1##rWarriors Shop#k\r\n#L2##rThief Shop#k\r\n#L3##rPirate Shop#k\r\n#L4##rArcher Shop#k\r\n#L5##rCommon Shop#k");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        cm.openNpc(9010038);
      } else if (selection == 1) {
        cm.openNpc (1012124);
      } else if (selection == 2) {
        cm.openNpc (9330026);
      } else if (selection == 3) {
        cm.openNpc (9201157);
      } else if (selection == 4) {
        cm.openNpc (9201022);
      } else if (selection == 5) {
        cm.openNpc (1012117);
    } else {
        cm.dispose();
    }
}  