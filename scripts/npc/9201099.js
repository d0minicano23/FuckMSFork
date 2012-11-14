var status = -1;
var keys = Array(16, 17, 18, 20, 21, 22, 36, 44, 45, 46, 47, 48);
var keynames = Array("Q", "W", "F", "T", "Y", "U", "J", "Z", "X", "C", "V", "B"); //just as reference
var slot = Array();
var inv;
/* GM Skills */
var skills;
var skillsnames = Array("Dispel", "Haste", "Bless", "Teleport", "Hyper Body");
var skillsp = Array(1000, 1500, 1800, 1500, 2500);
var allskillsp = 5000;
/* DB Skills */
var DBskills;
var DBskillsnames = Array("Dark Sight", "Channel Karma", "Thorns");
var DBskillsp = Array(100, 500, 1000);
var DBallskillsp = 1350;
/* Explorer Skills AKA EX */
var EXskills;
var EXskillsnames = Array("Rage", "Power Reflection", "Combo Attack", "Power Stance", "Power Guard", "Hyper Body", "Iron Will", "Dragon Strength", "Beholden", "Puppet", "Sharp Eyes", "Dark Harmony", "Meditation", "Heal", "Dark Clarity");
var EXskillsp = Array(500, 500, 1000, 500, 500, 1000, 500, 750, 550, 900, 1000, 950, 250, 1200, 400);
var EXallskillsp = 6500;


function start() {
    action(1, 0, 0);
	skills = Array(9101000, 9101001, 9101003, 9101007, 9101008);
	DBskills = Array(4001003, 4311005, 4341007);
	EXskills = Array(1101006, 1101007, 1111002, 1121002, 1201007, 1301007, 1301006, 1311008, 1321007, 3111002, 3121002, 4121014, 2201001, 2301002, 5101011);
    inv = cm.getInventory(1);
    previous_points = cm.getPlayer().getPoints();
}

function action(mode, type, selection) {
	if (mode != 1) {
		cm.dispose();
		return;
	}
	status++;
    if (status == 0) {
	cm.sendSimple("Hello #r#h ##k! I'm the master of #bskills#k. Which would you like?\r\nYou only have #r" + cm.getPlayer().getPoints() +"#k points left.#b\r\n#L9#Buy GM skills for points#l\r\n#L0#Buy DualBlade skills for points#l\r\n#L1#Buy Explorer skills for points");
    } else if (status == 1) {
	sel = selection;
	if (selection == 0) {
		  var selStr = "Alright, I can trade these skills for points...#eIf you have bought a skill once, you will not be charged points for it thereafter.#n#b\r\n\r\n";
	    for (var i = 0; i < DBskills.length; i++) {
	      selStr += "#L" + i + "##s" + DBskills[i] + "#" + DBskillsnames[i] + " for #e" + DBskillsp[i] + "#n points#n#l\r\n";
	    }
	    selStr += "#L" + DBskills.length + "##rALL skills above#b for #e" + DBallskillsp + "#n points#l\r\n";
	    cm.sendSimple(selStr + "#k");
		/*
		
		
		
		*/
	} else if (selection == 1) {
		 var selStr = "Alright, I can trade these skills for points...#eIf you have bought a skill once, you will not be charged points for it thereafter.#n#b\r\n\r\n";
	    for (var i = 0; i < EXskills.length; i++) {
	      selStr += "#L" + i + "##s" + EXskills[i] + "#" + EXskillsnames[i] + " for #e" + EXskillsp[i] + "#n points#n#l\r\n";
	    }
	    selStr += "#L" + EXskills.length + "##rALL skills above#b for #e" + EXallskillsp + "#n points#l\r\n";
	    cm.sendSimple(selStr + "#k");
		
		/*
		   
		   
		   
		*/
	} else if (selection == 3) {
		cm.dispose();
	} else if (selection == 4) {
		cm.dispose();
	} else if (selection == 5) {
		cm.dispose();
	} else if (selection == 6) {
	cm.dispose();
	} else if (selection == 7) {
	cm.dispose();
	} else if (selection == 8) {
	cm.dispose();
	} else if (selection == 9) {
	    var selStr = "Alright, I can trade these skills for points...#eIf you have bought a skill once, you will not be charged points for it thereafter.#n#b\r\n\r\n";
	    for (var i = 0; i < skills.length; i++) {
	      selStr += "#L" + i + "##s" + skills[i] + "#" + skillsnames[i] + " for #e" + skillsp[i] + "#n points#n#l\r\n";
	    }
	    selStr += "#L" + skills.length + "##rALL skills above#b for #e" + allskillsp + "#n points#l\r\n";
	    cm.sendSimple(selStr + "#k");
	} else if (selection == 10) {
		cm.dispose();
	} else if (selection == 11) {
	     cm.dispose();
	} else if (selection == 12) {
		cm.dispose();
	} else if (selection == 13) {
	    cm.dispose();
	} else if (selection == 16) {
	   cm.dispose();
	} else if (selection == 17) {
	   cm.dispose();
	} else if (selection == 18) {
	   cm.dispose();
	}
	} else if (status == 2) {
		if (sel == 0) {
		      if (selection == DBskills.length) {
			if (cm.getPlayer().getPoints() < DBallskillsp) {
				cm.sendOk("You don't have enough points. You only have " + cm.getPlayer().getPoints());
			} else {
				for (var i = 0; i < DBskills.length; i++) {
					cm.teachSkill(DBskills[i], 1, 0);
				}
				cm.getPlayer().setPoints(cm.getPlayer().getPoints() - DBallskillsp);
				cm.sendOk("Thank you for the purchase~ To use your skills, please click on me again and distribute each skill to a key.");
				cm.logDonator(" has bought all skills costing " + DBallskillsp + ".", previous_points);
			}
			cm.dispose();
			return;
		      }
		      itt = selection;
		      var selStr = "Alright, I can put your skill on these keys...#b\r\n\r\n";
		      for (var i = 0; i < keys.length; i++) {
		        selStr += "#L" + i + "#" + keynames[i] + "#l\r\n";
		      }
		      cm.sendSimple(selStr + "#k");
			  /*
			  
			  
			  
			  */
		} else if (sel == 1) {
		      if (selection == EXskills.length) {
			if (cm.getPlayer().getPoints() < EXallskillsp) {
				cm.sendOk("You don't have enough points. You only have " + cm.getPlayer().getPoints());
			} else {
				for (var i = 0; i < EXskills.length; i++) {
					cm.teachSkill(EXskills[i], 1, 0);
				}
				cm.getPlayer().setPoints(cm.getPlayer().getPoints() - EXallskillsp);
				cm.sendOk("Thank you for the purchase~ To use your skills, please click on me again and distribute each skill to a key.");
				cm.logDonator(" has bought all skills costing " + EXallskillsp + ".", previous_points);
			}
			cm.dispose();
			return;
		      }
		      itt = selection;
		      var selStr = "Alright, I can put your skill on these keys...#b\r\n\r\n";
		      for (var i = 0; i < keys.length; i++) {
		        selStr += "#L" + i + "#" + keynames[i] + "#l\r\n";
		      }
		      cm.sendSimple(selStr + "#k");
			  /*
			  
			  
			  
			  */
		} else if (sel == 4) {
			if (cm.getPlayer().getPoints() < acashp) {
				cm.sendOk("You don't have enough points. You have " + cm.getPlayer().getPoints() + " while I need " + acashp + ".");
			} else if (cm.getPlayer().getCSPoints(1) > (java.lang.Integer.MAX_VALUE - acash)) {
				cm.sendOk("You have too much Cash.");
			} else {
				cm.getPlayer().setPoints(cm.getPlayer().getPoints() - acashp);
				cm.getPlayer().modifyCSPoints(1, acash, true);
				cm.sendOk("There! Thank you for those points, I have given you Cash. Come again~");
				cm.logDonator(" has bought Cash [" + (cm.isGMS() ? (acash/2) : acash) + "] costing " + acashp + ".", previous_points);
			}
			cm.dispose();
			
		} else if (sel == 6) {
			var it = chairs[selection];
			var cp = chairsp[selection];
			if (cm.getPlayer().getPoints() < cp) {
				cm.sendOk("You don't have enough points. You have " + cm.getPlayer().getPoints() + " while I need " + cp + ".");
			} else if (!cm.canHold(it)) {
				cm.sendOk("Please free up inventory.");
			} else {
				cm.gainItem(it, 1);
				cm.getPlayer().setPoints(cm.getPlayer().getPoints() - cp);
				cm.sendOk("There! Thank you for those points. I have given you your chair. Come again~");
				cm.logDonator(" has bought chair [" + it + "] costing " + cp + ".", previous_points);
			}
			cm.dispose();
		} else if (sel == 7) {
			if (cm.getPlayer().getPoints() < hairp) {
				cm.sendOk("You don't have enough points. You only have " + cm.getPlayer().getPoints());
			} else {
				cm.setHair(hairnew[selection]);
				cm.getPlayer().setPoints(cm.getPlayer().getPoints() - hairp);
				cm.sendOk("Thank you for the purchase~");
				cm.logDonator(" has bought hair [" + hairnew[selection] + "] costing " + hairp + ".", previous_points);
			}
			cm.dispose();
		} else if (sel == 8) {
			if (cm.getPlayer().getPoints() < resetp) {
				cm.sendOk("You don't have enough points. You only have " + cm.getPlayer().getPoints());
			} else {
				cm.getPlayer().resetStatsByJob(false);
				cm.getPlayer().setPoints(cm.getPlayer().getPoints() - resetp);
				cm.sendOk("Thank you for the purchase~");
				cm.logDonator(" has bought full AP reset costing " + resetp + ".", previous_points);
			}
			cm.dispose();
		} else if (sel == 9) {
		      if (selection == skills.length) {
			if (cm.getPlayer().getPoints() < allskillsp) {
				cm.sendOk("You don't have enough points. You only have " + cm.getPlayer().getPoints());
			} else {
				for (var i = 0; i < skills.length; i++) {
					cm.teachSkill(skills[i], 1, 0);
				}
				cm.getPlayer().setPoints(cm.getPlayer().getPoints() - allskillsp);
				cm.sendOk("Thank you for the purchase~ To use your skills, please click on me again and distribute each skill to a key.");
				cm.logDonator(" has bought all skills costing " + allskillsp + ".", previous_points);
			}
			cm.dispose();
			return;
		      }
		      itt = selection;
		      var selStr = "Alright, I can put your skill on these keys...#b\r\n\r\n";
		      for (var i = 0; i < keys.length; i++) {
		        selStr += "#L" + i + "#" + keynames[i] + "#l\r\n";
		      }
		      cm.sendSimple(selStr + "#k");
		} else if (sel == 10) {
		      if (cm.getPlayer().getPoints() >= namep && cm.isEligibleName(cm.getText())) {
			cm.getPlayer().setPoints(cm.getPlayer().getPoints() - namep);
			cm.logDonator(" has bought name change from " + cm.getPlayer().getName() + " to " + cm.getText() + " costing " + namep + ".", previous_points);
		 	cm.getClient().getChannelServer().removePlayer(cm.getPlayer().getId(), cm.getPlayer().getName());
			cm.getPlayer().setName(cm.getText());
			cm.getClient().getSession().close();
		      } else {
			cm.sendOk("You either don't have enough points or " + cm.getText() + " is not an eligible name");
		      }
			cm.dispose();
		} else if (sel == 12) {
			if (selection >= 1 && selection <= cm.getPlayer().itemQuantity(5220013)) {
				if (cm.getPlayer().getPoints() > (2147483647 - (selection * 100))) {
					cm.sendOk("You have too many points.");
				} else {
					cm.gainItem(5220013, -selection);
					cm.getPlayer().setPoints(cm.getPlayer().getPoints() + (selection * 100));
					cm.sendOk("You have lost " + selection + " M Coins and gained " + (selection * 100) + " points. Current Points: " + cm.getPlayer().getPoints());
					cm.logDonator(" has redeemed " + selection + " M Coin(s) gaining " + (selection * 100) + ".", previous_points);
				}
			} 
			cm.dispose();
		} else if (sel == 13) {
			if (selection >= 1 && selection <= 100) {
				if (selection > (cm.getPlayer().getPoints() / 100)) {
					cm.sendOk("You can only get max " + (cm.getPlayer().getPoints() / 100) + " M Coins. 1 M Coin = 100 points.");
				} else if (!cm.canHold(5220013, selection)) {
					cm.sendOk("Please make space in CASH tab.");
				} else {
					cm.gainItem(5220013, selection);
					cm.getPlayer().setPoints(cm.getPlayer().getPoints() - (selection * 100));
					cm.sendOk("You have gained " + selection + " M Coins and lost " + (selection * 100) + " points. Current Points: " + cm.getPlayer().getPoints());
					cm.logDonator(" has gained " + selection + " M Coin(s) costing " + (selection * 100) + ".", previous_points);
				}
			}
			cm.dispose();
		} else if (sel == 16) {
			if (selection >= 1 && selection <= cm.getPlayer().itemQuantity(3993003)) {
				if (cm.getPlayer().getPoints() > (2147483647 - (selection * 1000))) {
					cm.sendOk("You have too many points.");
				} else {
					cm.gainItem(3993003, -selection);
					cm.getPlayer().setPoints(cm.getPlayer().getPoints() + (selection * 1000));
					cm.sendOk("You have lost " + selection + " and gained " + (selection * 1000) + " points. Current Points: " + cm.getPlayer().getPoints());
					cm.logDonator(" has redeemed " + selection + " Red Luck Sack(s) gaining " + (selection * 1000) + ".", previous_points);
				}
			} 
			cm.dispose();
		} else if (sel == 17) {
			if (selection >= 1) {
				if (selection > (cm.getPlayer().getPoints() / 1000)) {
					cm.sendOk("You can only get max " + (cm.getPlayer().getPoints() / 1000) + ". 1 Item = 1000 points.");
				} else if (!cm.canHold(3993003, selection)) {
					cm.sendOk("Please make space in SETUP tab.");
				} else {
					cm.gainItem(3993003, selection);
					cm.getPlayer().setPoints(cm.getPlayer().getPoints() - (selection * 1000));
					cm.sendOk("You have gained " + selection + " and lost " + (selection * 1000) + " points. Current Points: " + cm.getPlayer().getPoints());
					cm.logDonator(" has gained " + selection + " Red Luck Sack(s) costing " + (selection * 1000) + ".", previous_points);
				}
			}
			cm.dispose();
		} else if (sel == 18) {
			if (selection == 0) {
				if (cm.getPlayer().getPoints() < pendantp) {
					cm.sendOk("You do not have enough points.");
				} else {
					var marr = cm.getCData("pendant");
					if (marr != null && parseInt(marr) > cm.getCurrentTime()) {
						cm.sendOk("You already have a pendant slot.");
					} else {
						cm.setCData("pendant", "" + (cm.getCurrentTime() + (30 * 24 * 60 * 60 * 1000)));
						cm.forceStartQuest(7830, "1");
						cm.getPlayer().setPoints(cm.getPlayer().getPoints() - pendantp);
						cm.sendOk("You have gained additional pendant slot - 30 days.");
						cm.sendPendant(true);
						cm.getPlayer().fakeRelog();
						cm.logDonator(" has gained Additional Pendant Slot (30 Day) costing " + (pendantp) + ".", previous_points);
					}
				}
			} else {
				if (cm.getPlayer().getPoints() < pendantp_perm) {
					cm.sendOk("You do not have enough points.");
				} else {
					var marr = cm.getCData("pendant");
					if (marr != null && parseInt(marr) > cm.getCurrentTime()) {
						cm.sendOk("You already have a pendant slot.");
					} else {
						cm.setCData("pendant", "" + (cm.getCurrentTime() + (90 * 24 * 60 * 60 * 1000)));
						cm.forceStartQuest(7830, "1");
						cm.getPlayer().setPoints(cm.getPlayer().getPoints() - pendantp_perm);
						cm.sendOk("You have gained additional pendant slot - 90 days.");
						cm.sendPendant(true);
						cm.getPlayer().fakeRelog();
						cm.logDonator(" has gained Additional Pendant Slot (90 Day) costing " + (pendantp) + ".", previous_points);
					}
				}
			}
			cm.dispose();
		}
	} else if (status == 3) {
		  if (sel == 9) {
		    var hadSkill = true;
		    if (cm.getPlayer().getSkillLevel(skills[itt]) <= 0) {
		      hadSkill = false;
		      if (cm.getPlayer().getPoints() < skillsp[itt]) {
		        cm.sendOk("You don't have enough points. You have " + cm.getPlayer().getPoints() + " while I need " + skillsp[itt] + ".");
		        cm.dispose();
		        return;
		      } else {
		        cm.teachSkill(skills[itt], 1, 0);
		        cm.getPlayer().setPoints(cm.getPlayer().getPoints() - skillsp[itt]);
		      }
		    }
		    cm.putKey(keys[selection], 1, skills[itt]);
		    cm.sendOk("There! Thank you for those points. I have given you your skill. Come again~");
		    cm.logDonator(" has bought skill [" + skills[itt] + "] costing " + skillsp[itt] + " on key " + keynames[selection] + " (" + keys[selection] + "). [HadSkill: " + hadSkill + "] ", previous_points);
		  }
		 if (sel == 0) {
		    var hadSkill = true;
		    if (cm.getPlayer().getSkillLevel(DBskills[itt]) <= 0) {
		      hadSkill = false;
		      if (cm.getPlayer().getPoints() < DBskillsp[itt]) {
		        cm.sendOk("You don't have enough points. You have " + cm.getPlayer().getPoints() + " while I need " + DBskillsp[itt] + ".");
		        cm.dispose();
		        return;
		      } else {
		        cm.teachSkill(DBskills[itt], 1, 0);
		        cm.getPlayer().setPoints(cm.getPlayer().getPoints() - DBskillsp[itt]);
		      }
		    }
		    cm.putKey(keys[selection], 1, DBskills[itt]);
		    cm.sendOk("There! Thank you for those points. I have given you your skill. Come again~");
		    cm.logDonator(" has bought skill [" + DBskills[itt] + "] costing " + DBskillsp[itt] + " on key " + keynames[selection] + " (" + keys[selection] + "). [HadSkill: " + hadSkill + "] ", previous_points);
		  }
		  if (sel == 1) {
		    var hadSkill = true;
		    if (cm.getPlayer().getSkillLevel(EXskills[itt]) <= 0) {
		      hadSkill = false;
		      if (cm.getPlayer().getPoints() < EXskillsp[itt]) {
		        cm.sendOk("You don't have enough points. You have " + cm.getPlayer().getPoints() + " while I need " + EXskillsp[itt] + ".");
		        cm.dispose();
		        return;
		      } else {
		        cm.teachSkill(EXskills[itt], 1, 0);
		        cm.getPlayer().setPoints(cm.getPlayer().getPoints() - EXskillsp[itt]);
		      }
		    }
		    cm.putKey(keys[selection], 1, EXskills[itt]);
		    cm.sendOk("There! Thank you for those points. I have given you your skill. Come again~");
		    cm.logDonator(" has bought skill [" + EXskills[itt] + "] costing " + EXskillsp[itt] + " on key " + keynames[selection] + " (" + keys[selection] + "). [HadSkill: " + hadSkill + "] ", previous_points);
		  }
		  cm.dispose();

	}
}