importPackage (Packages.client); 
importPackage (Packages.server); 
importPackage (Packages.constants); 
importPackage (Packages.net.channel); 
importPackage (Packages.tools); 
importPackage (Packages.scripting);
var cserv, c, p, list, meh, type, slot, lulz, equip, item2; 
//cserv = Player's channel server 
//c= Player's client  
//list = The list/array of all character names in the world 
//meh = whether you have chosen to edit inventory (1) or lookup stats (2) 
//type = The inventory type of the chosen item 
//slot = The slot of the chosen item 
//lulz = The selection of what you want to edit in an item (or send/remove etc) 
//equip = a boolean that determines if the item you wish to CREATE is an equip or not 
//item2 = Simply the item when creating a new item 
var ugh=false; 
//ugh = the boolean used to make sure we don't redeclare the slot of an item you selected 
var nigger; 
//the player selected 
var item; 
//the item selected 
var hah = ["Weapon Attack", "Magic Attack", "Strength", "Dexterity", "Intelligence", "Luck", "", "", "Owner", "", "Quantity"]; 
//the array containing all of the names of stats that is called when asking "What do you want to change ____ to" and "___ has been changed to ___" 
var citemid; 
//the item id of the item you wish to create 
var FUCKINGPLAYERS=""; 
//the list of all the players including #L (selections) 
var st=-1; 
//status 
banneditems = [1004001, 1002140, 1003142, 1002419, 1042003, 1062007, 1322013, 1002959, 1042003, 1062007, 1002140, 1003142, 1082223, 1002082, 1002081, 1002080, 1002391, 1002395, 1002448, 1002394, 1002083, 1072012, 1072054, 1072055, 1072056, 1082230, 1102064, 1004000, 1442023, 3019101, 3019102, 3109103, 1322106, 1042223, 1062140, 3019104, 1003274, 2101023, 2101024, 1902805, 1902806, 1902807, 1902808, 1902809, 1902811, 1902912, 1902912, 1902805, 1332300, 1112116, 1002553, 1112226, 1032035, 1032036, 1602002, 1602001, 1602000, 1602003, 1602004, 1602005, 1602006, 1602007, 1602008, 5220001, 01050210, 1050210, 1112586, 1112585, 1112584, 1112583, 1112587, 01112586, 1112585, 1302916, 01302916, 1003627, 01003627, 1142003, 01142003, 1142003, 1142000, 1142001, 1142002, 1142004, 1142005, 1142006, 1142007, 1142008, 1142009, 1142010, 1142006, 01142006, 1302916, 1052976, 1003627, 1012396, 1012394, 1012395, 1012397, 1012398, 1012399, 3018030, 3018032, 3018033, 3018034, 3018035, 3018036, 3018881, 3018882, 3018883, 3018884, 3019995, 3019996, 3019997, 3019998, 3018021, 3018022, 3018023, 3018024, 3018025, 3018026, 3018027, 3018028, 3018029, 3018031];
function start() { //1204004
    status = -1;
    action(1, 0, 0);
}

function action(mode, t, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && cm.haveItem(4032055, 1)) {
	p=cm.getPlayer(); 
    c=p.getClient(); 
    cserv=c.getChannelServer(); 
    meh=1; 
    var playerStr; 
            for (var i=1; i<ServerConstants; i++){ 
                var cserv_= ChannelServer.getInstance(i); 
                var ret = cserv_.getPlayerStorage().getCharacterByName(p.getName()); 
                if (ret != null) { 
                    nigger = ret; 
                    break; 
                } 
            }
		cm.sendNext("Welcome! I am the event prize trader NPC. \r\n You #gDO#k have 1 event ticket.");
		}
		
		else if (status == 0) {
		cm.sendOk("Welcome! I am the event prize trader NPC. \r\n You #rDON'T#k have 1 event ticket.");
		cm.dispose();
		}
		
		if (status == 1) {
		cm.sendSimple("Congrats on winning! What would you like? \r\n #L0#IOC \r\n #L1#20 Nutella's#l \r\n  #L3# GM scrolling #l");
		}
		
		else if (status == 2) {
		if (selection == 0) {
		poop = 1;
		cm.sendGetText("Please type the item ID you wish to have.");
		}
		else if (selection == 1) {
		cm.gainItem(4000313, 20);		
		cm.dispose();
		cm.gainItem(4032055, -1);
		cm.callGM("" + cm.getPlayer().getName() + " has recieved 20 Nutella's as their event prize <3");
		}
		else if (selection == 2) {
		cm.getPlayer().gain1votepoint();
		cm.getPlayer().gain1votepoint();
		cm.getPlayer().dropMessage("You have recieved 2 vote points.");
		cm.gainItem(4032055, -1);		
		cm.dispose();
		cm.callGM("" + cm.getPlayer().getName() + " has recieved 2 vote points as their event prize <3");

		}
		/**else if (selection == 3) {
		poop = 2;
		//cm.sendGetText("Please equip the item you want scrolled and enter the itemID of the item. This process will increase your equip's Weapon Attack by 5. \r\n #r MAKE SURE THIS ITEM IS NOT IN YOUR EQUIP INVENTORY (IF YOU HAVE MORE THAN ONE)");
		cm.sendSimple("Please locate the item.\r\n"+ 
            "#L0#Equip Inventory\r\n#L5#Equipped Items (Does not include cash items)#l");
		}**/
		else if (selection == 3) {
		cm.dispose();
		cm.openNpc(1100005);
		}
		}
		else if (status == 3 && poop == 2) {
		type = MapleInventoryType.getByType(selection!=5?s+1:-1);
		            var herpaderp=""; 

            for (var i=(type.getType()!=-1?0:-20); i<(type.getType()!=-1?nigger.getInventory(type).getSlotLimit(): 20); i++){ 
                item = nigger.getInventory(type).getItem(i); 
                if (item ==null) 
                    continue; 
                if (type.getType != 3 && item != null) 
                herpaderp+="#L"+i+"##v"+item.getItemId()+"##l"; 
            else if (item != null)
                herpaderp+=""+item.getPosition()+"##v"+item.getItemId()+"#"; 
            } 
            if (herpaderp==""){ 
                cm.sendOk("There are no items in this inventory."); 
                cm.dispose(); 
            } 
            else { 
                cm.sendSimple("Here is the list: \r\n"+herpaderp); 
            }
		/*****pooey = cm.getText();
		var pooey = parseInt(pooey);
		currItem = cm.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(pooey);      
		        if (currItem == null) {// Check if item exists (they have it)
            cm.sendOk("This item needs to be equipped.");
            cm.dispose();
            return;
        }
		else {
		editStats(currItem, "watk", 5);
		cm.reloadChar();		
		cm.gainItem(4032055, -1);
		cm.sendOk("Done.");
		cm.dispose();
		cm.callGM("" + cm.getPlayer().getName() + " recieved GM scrolling as their event prize.");
		return;
		}
		}	****/	
		
			}else if (status == 3 && poop == 1) {
		poo = cm.getText();
		if (poo.length() < 7 || poo.length() > 8 || isNaN(poo)) {
		cm.sendOk("This item ID does not exist.");
		cm.dispose();
		}
		else if (!cm.canHold(poo)) {
		cm.sendOk("You do not have room.");
		cm.dispose();
		}
		else {
		cm.sendNext("Are you sure you want this item? \r\n Item ID: [" + poo + "] \r\n Item name: [ #z" + poo + "# ] \r\n Mouse over the item name to see details. \r\n You may need to move the mouse around for an item with no name.");
		}
		}
		else if (status == 4 && poop != 2) {
		var notbanned = true;
		var realitem = false;
		for (var i = 0; i < banneditems.length; i++) {
		if (banneditems[i] == poo) {
		notbanned = false;
		}
		}
		
		if (poo >= 1902000 && poo <= 1939999 || poo >= 2210000 && poo <= 2219999 || poo >= 5030000 && poo <= 5031000) {
		notbanned = false;
		}
		if (notbanned == true) {
		cm.gainItem(poo, 1, cm.getPlayer().getName());
		}
		if (cm.haveItem(poo, 1)) {
 		realitem = true;
		}
		
		
		if (realitem == true && notbanned == true) {
		cm.callGM("" + cm.getPlayer().getName() + " has recieved itemID " + poo + " as their event prize.");		
		cm.gainItem(4032055, -1);
		cm.dispose();
		return;
		}
		if (realitem == false && notbanned == true) {
		cm.sendOk("This is not a real item ID.");
		cm.dispose();
		return;
		}		
		else {
		cm.sendOk("I cannot give out this item. sorry.");
		cm.dispose();
		return;
		}
		}
		
		else if (status == 4 && poop == 2) {
		   if (!ugh){ 
            slot=selection; 
            ugh=true;
}			
			item = cm.getEquip(slot, nigger); 
                if (type.getType()==-1){ 
                    item =cm.getEquipped(slot, nigger); 
                } 
				item.setWatk(item.getWatk() + 5);
		        cm.reloadChar();
                cm.dispose();
		
		}
		}	
		}
		
		
		function editStats(currItem, toEdit, toAdd) {
    var currStats = 0;
    if (toEdit == "str") {
        currStats = currItem.getStr();
    } else if (toEdit == "luk") {
        currStats = currItem.getLuk();
    } else if (toEdit == "dex") {
        currStats = currItem.getDex();
    } else if (toEdit == "int") {
        currStats = currItem.getInt();
    } else if (toEdit == "watk") {
        currStats = currItem.getWatk();
    } else {
        cm.dropMessage("An error occured");
        cm.dispose();
        return;
    }
    
    currStats += toAdd;
    if (currStats > 32767) {
        currStats = 32767;
    }
    MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, currItem.getItemId(), toEdit, currStats);
}