importPackage(java.lang);
importPackage(java.util);
importPackage(Packages.tools);
importPackage(Packages.server.quest);
importPackage(Packages.client);
importPackage(Packages.scripting);
importPackage(Packages.handling.channel);
importPackage(Packages.handling);
importPackage(Packages.handling.word);
var status = -1;
var partymembers;

function start() {
partymembers = cm.getPartyMembers();
    status = -1;
    action (1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0) {
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    }
	
    
    if (status == 0) {
        var date = Calendar.getInstance().get(Calendar.YEAR)%100+"/"+StringUtil.getLeftPaddedStr(Calendar.getInstance().get(Calendar.MONTH)+"", "0", 2)+"/"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (cm.getPlayer().getKeyValue("AswanOffSeason_LastDate") == null) {
            cm.getPlayer().setKeyValue("AswanOffSeason_LastDate", date);
        }
        if (cm.getQuestStatus(7963) == 0 || !cm.getPlayer().getKeyValue("AswanOffSeason_LastDate").equals(date)) {
            cm.forceStartQuest(7963, "0");
            cm.getPlayer().setKeyValue("AswanOffSeason_LastDate", date);
        }
         cm.sendSimple("#e<Aswan Liberation of Hilla's Gang> #n\r\n\r\nAzwan region still has wandering remnants of Hilla's gang.#b\r\n\#e#bRemaining times you can play today: "+ (5-Integer.parseInt (cm.getQuestCustomData (7963))) +" times "+ "#n\n\r\n#L0#I want to pop a cap in her gang!#l");
    } else if (status == 1) {
        if (selection == 0) {
            if (cm.getMap(955000100).getCharactersSize() >= 1) {
			cm.sendNext("There is currently someone doing doing this and he/she is in Round 1.");
			cm.dispose();
                        return;
            }
            if (cm.getMap(955000200).getCharactersSize() >= 1) {
			cm.sendNext("There is currently someone doing doing this and he/she is in Round 2.");
			cm.dispose();
                        return;
            }
            if (cm.getMap(955000300).getCharactersSize() >= 1) {
			cm.sendNext("There is currently someone doing doing this and he/she is in the Final Round..");
			cm.dispose();
                return;
            }
            if (cm.getPlayer().getLevel() < 40) {
                cm.sendOk("You may not enter because you lack being level 40+.");
                cm.dispose();
                return;
            }
            if (cm.getQuestCustomData(7963).equals("5")) {
                cm.sendOk("You have already done this 5 times today, please, come back tomorrow!");
                cm.dispose();
                return;
            }
            /*if (cm.getPlayer().getParty() != null) {
                var em = cm.getEventManager("AswanOffSeason");
                if (!cm.isLeader()) {
                    cm.sendOk("Only the party leader can begin.");
                    cm.dispose();
                    return;
                }
                if (!cm.allMembersHere()) {
                    cm.sendOk("You need all your members here!");
                    cm.dispose();
                    return;
                }
                var it = cm.getPartyMembers().iterator();
                var levelPass = true;
                var limitPass = true;
                while (it.hasNext()) {
                    var chr = it.next();
                    if (!checkLevel(chr.getLevel(), 40, 200)) {
                        levelPass = false;
                        break;
                    }
                    if (chr.getQuestNAdd(MapleQuest.getInstance(7963)).getCustomData().equals("5")) {
                        limitPass = false;
                        break;
                    }
                }
                if (!levelPass) {
                   cm.sendOk("You must be between level 40 and 200 to play.");
                    cm.dispose();
                    return;
                }
                if (!limitPass) {
                    cm.sendOk("You already did it 5 times today");
                    cm.dispose();
                    return;
                }
                //
					    //var marr = cm.getQuestRecord(7963);
	    //var data = //marr.getCustomData();
				//var time = parseInt(data)
                var em = cm.getEventManager("AswanOffSeason");
				//var eim = em.newInstance("AswanOffSeason");
                //eim.setProperty("Global_StartMap", 955000100+"");
               // eim.setProperty("Global_ExitMap", 262000000+"");
               // eim.setProperty("Global_MinPerson", 1+"");
                //eim.setProperty("Global_RewardMap", 262000000+"");
               // eim.setProperty("CurrentStage", "1");
               // eim.startEventTimer(1200000);
			   //cm.sendOk("You have already went to Horntail in the past 24 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (24 * 3600000)));
              /*  cm.prepareAswanMob(955000100, em);
				cm.prepareAswanMob(955000200, em);
				cm.prepareAswanMob(955000300, em);
				var it2 = cm.getPartyMembers();
                var quest = MapleQuest.getInstance(7963);
                    var count = Integer.parseInt(chr.getQuestNAdd(quest).getCustomData());
                    quest.forceStartHillaGang(it2, 2100, (count+1)+"");
cm.worldMessage(6, "[Azwan] " + cm.getPlayer().getName() + "'s party has started the Azwan Liberation of Hilla's Gang in Channel "+ cm.getClient().getChannel() +".");
					cm.warpPartyWithExp(955000100, 100);      
                                cm.prepareAswanMob(955000100, em);
				cm.prepareAswanMob(955000200, em);// i shouldn't be doing this, but im to lazy 
				cm.prepareAswanMob(955000300, em);// to add it into portal script lols
                //eim.registerPlayer(cm.getPlayer());
                cm.forceStartQuest(7963, (Integer.parseInt(cm.getQuestCustomData(7963))+1)+"");
				cm.worldMessage(6, "[Azwan] " + cm.getPlayer().getName() + " has started the Azwan Liberation of Hilla's Gang in Channel "+ cm.getClient().getChannel() +".");
				cm.warp(955000100,0);
				cm.dispose();
            } else {
                */
                if (!checkLevel(cm.getPlayer().getLevel(), 40, 255)) {
                    cm.sendOk("Incorrect level, must be 40-200.");
                    cm.dispose();
                    return;
                }
                var em = cm.getEventManager("AswanOffSeason");
				//var eim = em.newInstance("AswanOffSeason");
                //eim.setProperty("Global_StartMap", 955000100+"");
               // eim.setProperty("Global_ExitMap", 262000000+"");
                //eim.setProperty("Global_MinPerson", 1+"");
               // eim.setProperty("Global_RewardMap", 262000000+"");
               // eim.setProperty("CurrentStage", "1");
               // eim.startEventTimer(1200000);
                cm.prepareAswanMob(955000100, em);
				cm.prepareAswanMob(955000200, em);// i shouldn't be doing this, but im to lazy 
				cm.prepareAswanMob(955000300, em);// to add it into portal script lols
                //eim.registerPlayer(cm.getPlayer());
                cm.forceStartQuest(7963, (Integer.parseInt(cm.getQuestCustomData(7963))+1)+"");
				cm.worldMessage(6, "[Azwan] " + cm.getPlayer().getName() + " has enetered the Azwan Liberation of Hilla's Gang in Channel "+ cm.getClient().getChannel() +".");
				cm.warp(955000100,0);
				cm.dispose();
            }
        } else {
            cm.sendOk("Unfinished state, ERROR.");
            cm.dispose();
        }
    }

function checkLevel(cur, min, max) {
    return (cur >= min && cur <= max);
}