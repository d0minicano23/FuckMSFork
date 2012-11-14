var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendOk("#b< Welcome to Maple Island >#k\r\n\r\nIf you plan to be a job that isn't available in the Character Create screen, become level 10 @npc > Special Advance.\r\n\r\n#L4#Head to Lith Harbor#l\r\n#L5#Stay in Maple Island#l");
            cm.dispose();
        }else if(status == 1){ 
            cm.warp(104000000);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}  