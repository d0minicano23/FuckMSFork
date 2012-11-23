/* Author: Steven(even on ragezone)
 * ID: 2091011
 * Name: SoGong
 * Function: Dojo npc
 * Uncoded NPC, created with TraNpcExtractor v0.3.0
 * TraNpcExtractor was written by Prio from StaticDEV.com
 */
 var status; 

function start() { 
    status = -1; 
    action(1, 0, 0); 
} 

function action(mode, type, selection) { 
    if (mode == 1) { 
        status++; 
    }else{ 
        status--; 
    } 
    if (status == 0) { 
        cm.sendSimple("My master is the strongest being in Mu Lung, and YOU wish to challaenge HIM? Just don't regret it later.\r\n#b#L0#I'll challenge myself to Mu Lung Dojo.#l\r\n#b#L1#I want to recieve Mu Gong's Belt.#l\r\n#b#L2#I want to see what rewards I can get from Mu Lung Dojo.#l\r\n#b#L3#What's Mu Lung Dojo?#l\r\n#b#L4#I want to check how many more times I can do the challenge today.#l\r\n#b#L5#I want to know my Hard Mode points and grade.#l"); 
		
    } else if (status == 1) { 
        cm.sendSimple("You can take on the dojo on three different difficulties: #bNormal, Hard, and Ranked.#lThat way, even shrimps like you can participate. You've gotta be #rLV.90#l for Normal Mode, #rLV.120#l for Hard Mode, and #rLV.130#l for the Ranking Mode. How tough are you feeling?#l\r\n\n\n#b#L0#I'm pretty Normal, let's try that!\r\n#l#b#L1#I haven't gotten beaten up lately. Hard mode!\r\n#l#r#L2#I want to get Ranked!#l"); 
   
    } else if (status == 2) { 
       
	    if (selection == 0) { 
            cm.sendOk("Here I am, in another status. As you can see from the script, this window is in status 2."); 
            cm.dispose(); 
        } else if (selection == 1) { 
            cm.sendOk("Well, sucks to be you, don't it? This window is also in status 2 :) "); 
            cm.dispose(); 
        } 
    } 
}  