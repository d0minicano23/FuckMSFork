package client.messages.commands;

//import client.MapleInventory;
//import client.MapleInventoryType;
import client.MapleBuffStat;
import client.inventory.Item;
import server.RankingWorker;
import client.MapleCharacter;
import constants.Rank.PlayerGMRank;
import client.MapleClient;
import client.MapleStat;
import client.inventory.Equip;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import client.messages.commands.CommandExecute.PokemonExecute;
import client.messages.commands.CommandExecute.TradeExecute;
import com.mysql.jdbc.Connection;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.World;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import scripting.NPCScriptManager;
import server.ItemInformation;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.RankingWorker.RankingInformation;
import server.life.MapleMonster;


import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.SavedLocationType;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

/**
 *
 * @author Emilyx3
 */
public class PlayerCommand {

    public static PlayerGMRank getPlayerLevelRequired(){
        return PlayerGMRank.NORMAL;
    }

    public static class STR extends DistributeStatCommands {

        public STR(){
            stat = MapleStat.STR;
        }
    }

    public static class DEX extends DistributeStatCommands {

        public DEX(){
            stat = MapleStat.DEX;
        }
    }

    public static class INT extends DistributeStatCommands {

        public INT(){
            stat = MapleStat.INT;
        }
    }

    public static class LUK extends DistributeStatCommands {

        public LUK(){
            stat = MapleStat.LUK;
        }
    }

    public abstract static class DistributeStatCommands extends CommandExecute {

        protected MapleStat stat = null;
        private static int statLim = 999;

        private void setStat(MapleCharacter player, int amount){
            switch (stat){
                case STR:
                    player.getStat().setStr((short) amount, player);
                    player.updateSingleStat(MapleStat.STR, player.getStat().getStr());
                    break;
                case DEX:
                    player.getStat().setDex((short) amount, player);
                    player.updateSingleStat(MapleStat.DEX, player.getStat().getDex());
                    break;
                case INT:
                    player.getStat().setInt((short) amount, player);
                    player.updateSingleStat(MapleStat.INT, player.getStat().getInt());
                    break;
                case LUK:
                    player.getStat().setLuk((short) amount, player);
                    player.updateSingleStat(MapleStat.LUK, player.getStat().getLuk());
                    break;
            }
        }

        private int getStat(MapleCharacter player){
            switch (stat){
                case STR:
                    return player.getStat().getStr();
                case DEX:
                    return player.getStat().getDex();
                case INT:
                    return player.getStat().getInt();
                case LUK:
                    return player.getStat().getLuk();
                default:
                    throw new RuntimeException(); //Will never happen.
            }
        }

        @Override
        public int execute(MapleClient c, String[] splitted){
            if (splitted.length < 2){
                c.getPlayer().dropMessage(5, "Invalid number entered.");
                return 0;
            }
            int change = 0;
            try {
                change = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException nfe){
                c.getPlayer().dropMessage(5, "Invalid number entered.");
                return 0;
            }
            if (change <= 0){
                c.getPlayer().dropMessage(5, "You must enter a number greater than 0.");
                return 0;
            }
            if (c.getPlayer().getRemainingAp() < change){
                c.getPlayer().dropMessage(5, "You don't have enough AP for that.");
                return 0;
            }
            if (getStat(c.getPlayer()) + change > statLim){
                c.getPlayer().dropMessage(5, "The stat limit is " + statLim + ".");
                return 0;
            }
            setStat(c.getPlayer(), getStat(c.getPlayer()) + change);
            c.getPlayer().setRemainingAp((short) (c.getPlayer().getRemainingAp() - change));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
            c.getPlayer().dropMessage(5, StringUtil.makeEnumHumanReadable(stat.name()) + " has been raised by " + change + ".");
            return 1;
        }
    }

    public static class Mob extends CommandExecute {

        public int execute(MapleClient c, String[] splitted){
            MapleMonster mob = null;
            for (final MapleMapObject monstermo : c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 100000, Arrays.asList(MapleMapObjectType.MONSTER))){
                mob = (MapleMonster) monstermo;
                if (mob.isAlive()){
                    c.getPlayer().dropMessage(6, "Monster " + mob.toString());
                    break; //only one
                }
            }
            if (mob == null){
                c.getPlayer().dropMessage(6, "No monster was found.");
            }
            return 1;
        }
    }
    
        public static class rb extends CommandExecute {
        
        @Override
       public int execute(MapleClient c, String[] splitted){
            if (splitted.length != 2){
                               c.getPlayer().dropMessage(6, "Sorry, you must be level 200 to rebirth.");
               c.getPlayer().dropMessage(6, "Cygnus - @rb koc");
               c.getPlayer().dropMessage(6, "Evan - @rb evan");
               c.getPlayer().dropMessage(6, "Aran - @rb aran");
               c.getPlayer().dropMessage(6, "Dual blade - @rb db");
               c.getPlayer().dropMessage(6, "Explorer - @rb exp");
               c.getPlayer().dropMessage(6, "Mechanic - @rb mech");
               c.getPlayer().dropMessage(6, "Wild Hunter - @rb wh");
               c.getPlayer().dropMessage(6, "Battle Mage - @rb bam");
               c.getPlayer().dropMessage(6, "Demon Slayer - @rb ds");
               c.getPlayer().dropMessage(6, "Cannon Master - @rb cs");
               c.getPlayer().dropMessage(6, "Mercedes - @rb mr");
               c.getPlayer().dropMessage(6, "Phantom - @rb phantom");
               c.getPlayer().dropMessage(6, "Jett - @rb Jett");
               c.getPlayer().dropMessage(6, "Mihile - @rb Mihile");
           }
            
            if (c.getPlayer().getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null){
             c.getPlayer().dropMessage(5, "Please disable Shadow Partner/Mirror Image before rebirthing or changing job.");   
               } else {
           if (splitted[1].equalsIgnoreCase("koc") && (c.getPlayer().getLevel() >= 200)){
               c.getPlayer().doCRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Koc]");
           } else if (splitted[1].equalsIgnoreCase("evan") && (c.getPlayer().getLevel() >= 200 )){
               c.getPlayer().doERB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getSession().write(CField.getCharInfo(c.getPlayer()));
               c.getPlayer().getMap().removePlayer(c.getPlayer());
               c.getPlayer().getMap().addPlayer(c.getPlayer());
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Evan]");
           } else if (splitted[1].equalsIgnoreCase("aran") && (c.getPlayer().getLevel() >= 200)){
               c.getPlayer().doARB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Aran]");
           } else if (splitted[1].equalsIgnoreCase("db") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doDBRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Dual blade]");
           } else if (splitted[1].equalsIgnoreCase("exp") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doEXPRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Explorer]");
           } else if (splitted[1].equalsIgnoreCase("mech") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doMRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Mechanic]");
           } else if (splitted[1].equalsIgnoreCase("wh") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doWHRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Wild Hunter]");
           } else if (splitted[1].equalsIgnoreCase("bam") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doBAMRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Battle Mage]");
           } else if (splitted[1].equalsIgnoreCase("ds") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doDSRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Demon Slayer]");
           } else if (splitted[1].equalsIgnoreCase("cs") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doCANNONRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Cannoneer]");
           } else if (splitted[1].equalsIgnoreCase("mr") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doMERCRB();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Mercedes]");
           } else if (splitted[1].equalsIgnoreCase("phantom") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doPhantom();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Phantom]");
           } else if (splitted[1].equalsIgnoreCase("Jett") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doJett();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Jett]");
          } else if (splitted[1].equalsIgnoreCase("Mihile") && c.getPlayer().getLevel() >= 200){
               c.getPlayer().doMihile();
               c.getPlayer().gainDamage(1000, 5000);
               c.getPlayer().dropMessage(6, "Done! You have rebirthed to [Mihile]");
           } else if (splitted[1].equalsIgnoreCase("help")){
               c.getPlayer().dropMessage(6, "Sorry, you must be level 200 to rebirth.");
               c.getPlayer().dropMessage(6, "Cygnus - @rb koc");
               c.getPlayer().dropMessage(6, "Evan - @rb evan");
               c.getPlayer().dropMessage(6, "Aran - @rb aran");
               c.getPlayer().dropMessage(6, "Dual blade - @rb db");
               c.getPlayer().dropMessage(6, "Explorer - @rb exp");
               c.getPlayer().dropMessage(6, "Mechanic - @rb mech");
               c.getPlayer().dropMessage(6, "Wild Hunter - @rb wh");
               c.getPlayer().dropMessage(6, "Battle Mage - @rb bam");
               c.getPlayer().dropMessage(6, "Demon Slayer - @rb ds");
               c.getPlayer().dropMessage(6, "Cannon Master - @rb cs");
               c.getPlayer().dropMessage(6, "Mercedes - @rb mr");
               c.getPlayer().dropMessage(6, "Phantom - @rb phantom");
               c.getPlayer().dropMessage(6, "Jett - @rb Jett");
               c.getPlayer().dropMessage(6, "Mihile - @rb Mihile");
            } else {
           /*    c.getPlayer().dropMessage(6, "Sorry, you must be level 200 to rebirth.");
               c.getPlayer().dropMessage(6, "Cygnus - @rb koc");
               c.getPlayer().dropMessage(6, "Evan - @rb evan");
               c.getPlayer().dropMessage(6, "Aran - @rb aran");
               c.getPlayer().dropMessage(6, "Dual blade - @rb db");
               c.getPlayer().dropMessage(6, "Explorer - @rb exp");
               c.getPlayer().dropMessage(6, "Mechanic - @rb mech");
               c.getPlayer().dropMessage(6, "Wild Hunter - @rb wh");
               c.getPlayer().dropMessage(6, "Battle Mage - @rb bam");
               c.getPlayer().dropMessage(6, "Demon Slayer - @rb ds");
               c.getPlayer().dropMessage(6, "Cannon Master - @rb cs");
               c.getPlayer().dropMessage(6, "Mercedes - @rb mr");
               c.getPlayer().dropMessage(6, "Phantom - @rb phantom");
               c.getPlayer().dropMessage(6, "Jett - @rb Jett");
               c.getPlayer().dropMessage(6, "Mihile - @rb Mihile");*/
           }

           }
         return 1;
        }
        }
               public static class Marry extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted){
            if (c.getPlayer().getMeso() >= 100000000){
            if (splitted.length < 3){
                c.getPlayer().dropMessage(6, "Need <name> <itemid>");
                return 0;
            }
            int itemId = Integer.parseInt(splitted[2]);
            if (!GameConstants.isEffectRing(itemId)){
                c.getPlayer().dropMessage(6, "Invalid itemID.");
            } else {
                c.getPlayer().gainMeso(-100000000, true);
                MapleCharacter fff = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (fff == null){
                    c.getPlayer().dropMessage(6, "Player must be online");
                } else {
                    int[] ringID = {MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};
                    try {
                        MapleCharacter[] chrz = {fff, c.getPlayer()};
                        for (int i = 0; i < chrz.length; i++){
                            Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId, ringID[i]);
                            if (eq == null){
                                c.getPlayer().dropMessage(6, "Invalid itemID.");
                                return 0;
                            }
                            MapleInventoryManipulator.addbyItem(chrz[i].getClient(), eq.copy());
                            chrz[i].dropMessage(6, "Successfully married with " + chrz[i == 0 ? 1 : 0].getName());
                        }
                        MapleRing.addToDB(itemId, c.getPlayer(), fff.getName(), fff.getId(), ringID);
                    } catch (SQLException e){
                    }
                }
            }

        } else {
         c.getPlayer().dropMessage(5, "A ring costs 100m! it must be a crush, friend or a marriage ring!");       
            }
 return 1;
    }
        }
               
public static class checkap extends CommandExecute {
        @Override
            public int execute(MapleClient c, String[] splitted){
                  c.getPlayer().dropMessage(6, "You currently have: " + c.getPlayer().getRemainingAp() + " AP");
                  return 1;
            }
} 

            
public static class dmgnotice extends CommandExecute {
        @Override
            public int execute(MapleClient c, String[] splitted){
                if (c.getPlayer().getshowdamage() == 0){
                  c.getPlayer().setshowdamage(1);
                  c.getPlayer().dropMessage(6, "Your extra Damage notice is now: Enabled! (" + c.getPlayer().getshowdamage() + ")");    
                } else {
                  c.getPlayer().setshowdamage(0);
                  c.getPlayer().dropMessage(6, "Your extra Damage notice is now: Disabled! (" + c.getPlayer().getshowdamage() + ")");    
                }

                  return 1;
            }
}

    public static class DmgCheck extends CommandExecute {
        @Override
            public int execute(MapleClient c, String[] splitted){
                  c.getPlayer().dropMessage(6, "You currently have: " + c.getPlayer().getDamage() + " extra Damage!");
                  return 1;
            }
}
    
	public static class BEvent extends CommandExecute {

            @Override
        public int execute(MapleClient c, String[] splitted){
            if (c.getPlayer().getClient().getChannelServer().eventOn){
                  MapleCharacter victim;
                try {
                    victim = c.getPlayer();
                    if (victim.getClient().getChannel() != c.getChannelServer().eventChannel){
                        c.getPlayer().dropMessage(5, "Please go to the channel where it's being hosted on before trying to warp there.");
                        
                        // WorldLocation loc = new WorldLocation(40000, 2);
                        int map = c.getChannelServer().eventMap;
                        //MapleMap target = c.getChannelServer().getMapFactory().getMap(map);
                      //  c.getPlayer().changeMap(target, target.getPortal(0));
                    //    String ip = c.getChannelServer().getIP(c.getChannelServer().eventChannel);
                     //   c.getPlayer().getMap().removePlayer(c.getPlayer());
                       // victim.setMap(target);
                       // String[] socket = ip.split(":");
                        int ch = World.Find.findChannel(splitted[1]);
                        if (c.getPlayer().getTrade() != null){
                             MapleTrade.cancelTrade( c.getPlayer().getTrade(), c.getPlayer().getClient(),  c.getPlayer());
                        }
                        try {
                            //WorldChannelInterface wci = c.getChannelServer().getInstance(c.getChannel()).getWorldInterface();
                           // wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
                            //wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
                        } catch (Exception e){
                           // c.getChannelServer().reconnectWorld();
                        }
                        c.getPlayer().saveToDB(true, false);
                        if (c.getPlayer().getCheatTracker() != null){
                            c.getPlayer().getCheatTracker().dispose();
                        }
                        ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
                        c.updateBLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                        try {
                          // byte[] packet = c.getPlayer().getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
                          c.getPlayer().changeChannel(ch);
                        } catch (NumberFormatException e){
                            throw new RuntimeException(e);
                        }
                    } else {
                        int map = c.getChannelServer().eventMap;
                        MapleMap target = c.getChannelServer().getMapFactory().getMap(map);
                        c.getPlayer().changeMap(target, target.getPortal(0));
                    }
                } catch (/* Remote */Exception e){
                    c.getPlayer().dropMessage(6, "Something went wrong " + e.getMessage());
                }
            } else {
                c.getPlayer().dropMessage(6, "There is no event currently on.");
            }
                return 0;
        }
        }


    
    
    
    
    public static class search extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted){
            if (splitted.length == 1){
                c.getPlayer().dropMessage(6, splitted[0] + ": <ITEM>");
            } else if (splitted.length == 2){
                c.getPlayer().dropMessage(6, "Provide something to search.");
            } else {
                String type = splitted[1];
                String search = StringUtil.joinStringFrom(splitted, 2);
                MapleData data = null;
                MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/" + "String.wz"));
                c.getPlayer().dropMessage(6, "<<Type: " + type + " | Search: " + search + ">>");
                 if (type.equalsIgnoreCase("ITEM")){
                    List<String> retItems = new ArrayList<String>();
                    for (ItemInformation itemPair : MapleItemInformationProvider.getInstance().getAllItems()){
                        if (itemPair != null && itemPair.name != null && itemPair.name.toLowerCase().contains(search.toLowerCase())){
                            retItems.add(itemPair.itemId + " - " + itemPair.name);
                        }
                    }
                    if (retItems != null && retItems.size() > 0){
                        for (String singleRetItem : retItems){
                            c.getPlayer().dropMessage(6, singleRetItem);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "No Items Found");
                    }
                
                } else {
                    c.getPlayer().dropMessage(6, "Sorry, that search call is unavailable");
                }
            }
            return 0;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public abstract static class OpenNPCCommand extends CommandExecute {

        protected int npc = -1;
        private static int[] npcs = { //Ish yur job to make sure these are in order and correct ;(
            9270035,
            9010018,
            9000000,
            9000030,
            9010000,
            9000085,
            9000018,
            9201094};

        @Override
        public int execute(MapleClient c, String[] splitted){
            if (npc != 6 && npc != 5 && npc != 4 && npc != 3 && npc != 1 && c.getPlayer().getMapId() != 910000000){ //drpcash can use anywhere
                if (c.getPlayer().getLevel() < 10 && c.getPlayer().getJob() != 200){
                    c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                    return 0;
                }
                if (c.getPlayer().isInBlockedMap()){
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                    return 0;
                }
            } else if (npc == 1){
                if (c.getPlayer().getLevel() < 70){
                    c.getPlayer().dropMessage(5, "You must be over level 70 to use this command.");
                    return 0;
                }
            }
            if (c.getPlayer().hasBlockedInventory()){
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return 0;
            }
            NPCScriptManager.getInstance().start(c, npcs[npc]);
            return 1;
        }
    }

    public static class npc extends OpenNPCCommand {

        public npc(){
            npc = 0;
        }
    }

    public static class DCash extends OpenNPCCommand {

        public DCash(){
            npc = 1;
        }
    }

    public static class Event extends OpenNPCCommand {

        public Event(){
            npc = 2;
        }
    }

    public static class CheckDrop extends OpenNPCCommand {

        public CheckDrop(){
            npc = 4;
        }
    }

    public static class Pokedex extends OpenNPCCommand {

        public Pokedex(){
            npc = 5;
        }
    }

    public static class Pokemon extends OpenNPCCommand {

        public Pokemon(){
            npc = 6;
        }
    }
    
    public static class freesmega extends OpenNPCCommand {

        public freesmega(){
            npc = 7;
        }
    }

    public static class FM extends CommandExecute {

        public int execute(MapleClient c, String[] splitted){
            for (int i : GameConstants.blockedMaps){
                if (c.getPlayer().getMapId() == i){
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                    return 0;
                }
            }
            if (c.getPlayer().getLevel() < 10 && c.getPlayer().getJob() != 200){
                c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                return 0;
            }
            if (c.getPlayer().hasBlockedInventory() || c.getPlayer().getMap().getSquadByMap() != null || c.getPlayer().getEventInstance() != null || c.getPlayer().getMap().getEMByMap() != null || c.getPlayer().getMapId() >= 990000000/* || FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())*/){
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return 0;
            }
            if ((c.getPlayer().getMapId() >= 680000210 && c.getPlayer().getMapId() <= 680000502) || (c.getPlayer().getMapId() / 1000 == 980000 && c.getPlayer().getMapId() != 980000000) || (c.getPlayer().getMapId() / 100 == 1030008) || (c.getPlayer().getMapId() / 100 == 922010) || (c.getPlayer().getMapId() / 10 == 13003000)){
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return 0;
            }
            c.getPlayer().saveLocation(SavedLocationType.FREE_MARKET, c.getPlayer().getMap().getReturnMap().getId());
            MapleMap map = c.getChannelServer().getMapFactory().getMap(910000000);
            c.getPlayer().changeMap(map, map.getPortal(0));
            return 1;
        }
    }

    public static class EA extends CommandExecute {

        public int execute(MapleClient c, String[] splitted){
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(CWvsContext.enableActions());
            return 1;
        }
    }

    public static class TSmega extends CommandExecute {

        public int execute(MapleClient c, String[] splitted){
            c.getPlayer().setSmega();
            return 1;
        }
    }

    public static class Ranking extends CommandExecute {

        public int execute(MapleClient c, String[] splitted){
            if (splitted.length < 4){ //job start end
                c.getPlayer().dropMessage(5, "Use @ranking [job] [start number] [end number] where start and end are ranks of the players");
                final StringBuilder builder = new StringBuilder("JOBS: ");
                for (String b : RankingWorker.getJobCommands().keySet()){
                    builder.append(b);
                    builder.append(" ");
                }
                c.getPlayer().dropMessage(5, builder.toString());
            } else {
                int start = 1, end = 20;
                try {
                    start = Integer.parseInt(splitted[2]);
                    end = Integer.parseInt(splitted[3]);
                } catch (NumberFormatException e){
                    c.getPlayer().dropMessage(5, "You didn't specify start and end number correctly, the default values of 1 and 20 will be used.");
                }
                if (end < start || end - start > 20){
                    c.getPlayer().dropMessage(5, "End number must be greater, and end number must be within a range of 20 from the start number.");
                } else {
                    final Integer job = RankingWorker.getJobCommand(splitted[1]);
                    if (job == null){
                        c.getPlayer().dropMessage(5, "Please use @ranking to check the job names.");
                    } else {
                        final List<RankingInformation> ranks = RankingWorker.getRankingInfo(job.intValue());
                        if (ranks == null || ranks.size() <= 0){
                            c.getPlayer().dropMessage(5, "Please try again later.");
                        } else {
                            int num = 0;
                            for (RankingInformation rank : ranks){
                                if (rank.rank >= start && rank.rank <= end){
                                    if (num == 0){
                                        c.getPlayer().dropMessage(6, "Rankings for " + splitted[1] + " - from " + start + " to " + end);
                                        c.getPlayer().dropMessage(6, "--------------------------------------");
                                    }
                                    c.getPlayer().dropMessage(6, rank.toString());
                                    num++;
                                }
                            }
                            if (num == 0){
                                c.getPlayer().dropMessage(5, "No ranking was returned.");
                            }
                        }
                    }
                }
            }
            return 1;
        }
    }

    public static class Check extends CommandExecute {

        public int execute(MapleClient c, String[] splitted){
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getCSPoints(1) + " Cash.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getPoints() + " donation points.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getVPoints() + " voting points.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getIntNoRecord(GameConstants.BOSS_PQ) + " Boss Party Quest points.");
            c.getPlayer().dropMessage(6, "The time is currently " + FileoutputUtil.CurrentReadable_TimeGMT() + " GMT.");
            return 1;
        }
    }

    public static class Help extends CommandExecute {

        public int execute(MapleClient c, String[] splitted){
            c.getPlayer().dropMessage(5, "@str, @dex, @int, @luk <amount to add>");
            c.getPlayer().dropMessage(5, "@mob < Information on the closest monster >");
            c.getPlayer().dropMessage(5, "@check < Displays various information >");
            c.getPlayer().dropMessage(5, "@fm < Warp to FM >");
            c.getPlayer().dropMessage(5, "@search item < You may search up an item. >");
            /*c.getPlayer().dropMessage(5, "@changesecondpass - Change second password, @changesecondpass <current Password> <new password> <Confirm new password> ");*/
            c.getPlayer().dropMessage(5, "@npc < Universal Town Warp / Event NPC >");
            c.getPlayer().dropMessage(5, "@freesmega < Free Smega NPC / Pot Scroll Seller >");
            c.getPlayer().dropMessage(5, "@dcash < Universal Cash Item Dropper >");
            /*if (!GameConstants.GMS){
            c.getPlayer().dropMessage(5, "@pokedex < Universal Information >");
            c.getPlayer().dropMessage(5, "@pokemon < Universal Monsters Information >");
            c.getPlayer().dropMessage(5, "@challenge < playername, or accept/decline or block/unblock >");
            }*/
            c.getPlayer().dropMessage(5, "@tsmega < Toggle super megaphone on/off >");
            c.getPlayer().dropMessage(5, "@ea < If you are unable to attack or talk to NPC >");
            /*c.getPlayer().dropMessage(5, "@clearslot < Cleanup that trash in your inventory >");*/
            c.getPlayer().dropMessage(5, "@ranking < Use @ranking for more details >");
            c.getPlayer().dropMessage(5, "@DmgCheck < Shows you your damage >");
            c.getPlayer().dropMessage(5, "@rb < Use @rb help for more details >");
            c.getPlayer().dropMessage(5, "@dmgnotice < Disable the red notice >");
            c.getPlayer().dropMessage(5, "@marry < make a ring >");
            return 1;
        }
    }

    public static class TradeHelp extends TradeExecute {

        public int execute(MapleClient c, String[] splitted){
            c.getPlayer().dropMessage(-2, "[System] : <@offerequip, @offeruse, @offersetup, @offeretc, @offercash> <quantity> <name of the item>");
            return 1;
        }
    }

    public abstract static class OfferCommand extends TradeExecute {

        protected int invType = -1;

        public int execute(MapleClient c, String[] splitted){
            if (splitted.length < 3){
                c.getPlayer().dropMessage(-2, "[Error] : <quantity> <name of item>");
            } else if (c.getPlayer().getLevel() < 70){
                c.getPlayer().dropMessage(-2, "[Error] : Only level 70+ may use this command");
            } else {
                int quantity = 1;
                try {
                    quantity = Integer.parseInt(splitted[1]);
                } catch (Exception e){ //swallow and just use 1
                }
                String search = StringUtil.joinStringFrom(splitted, 2).toLowerCase();
                Item found = null;
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (Item inv : c.getPlayer().getInventory(MapleInventoryType.getByType((byte) invType))){
                    if (ii.getName(inv.getItemId()) != null && ii.getName(inv.getItemId()).toLowerCase().contains(search)){
                        found = inv;
                        break;
                    }
                }
                if (found == null){
                    c.getPlayer().dropMessage(-2, "[Error] : No such item was found (" + search + ")");
                    return 0;
                }
                if (GameConstants.isPet(found.getItemId()) || GameConstants.isRechargable(found.getItemId())){
                    c.getPlayer().dropMessage(-2, "[Error] : You may not trade this item using this command");
                    return 0;
                }
                if (quantity > found.getQuantity() || quantity <= 0 || quantity > ii.getSlotMax(found.getItemId())){
                    c.getPlayer().dropMessage(-2, "[Error] : Invalid quantity");
                    return 0;
                }
                if (!c.getPlayer().getTrade().setItems(c, found, (byte) -1, quantity)){
                    c.getPlayer().dropMessage(-2, "[Error] : This item could not be placed");
                    return 0;
                } else {
                    c.getPlayer().getTrade().chatAuto("[System] : " + c.getPlayer().getName() + " offered " + ii.getName(found.getItemId()) + " x " + quantity);
                }
            }
            return 1;
        }
    }

    public static class OfferEquip extends OfferCommand {

        public OfferEquip(){
            invType = 1;
        }
    }

    public static class OfferUse extends OfferCommand {

        public OfferUse(){
            invType = 2;
        }
    }

    public static class OfferSetup extends OfferCommand {

        public OfferSetup(){
            invType = 3;
        }
    }

    public static class OfferEtc extends OfferCommand {

        public OfferEtc(){
            invType = 4;
        }
    }

    public static class OfferCash extends OfferCommand {

        public OfferCash(){
            invType = 5;
        }
    }

    public static class BattleHelp extends PokemonExecute {

        public int execute(MapleClient c, String[] splitted){
            c.getPlayer().dropMessage(-3, "(...I can use @use <attack name> to take down the enemy...)");
            c.getPlayer().dropMessage(-3, "(...I can use @info to check out the stats of my battle...)");
            c.getPlayer().dropMessage(-3, "(...I can use @ball <basic, great, ultra> to use an ball, but only if I have it...)");
            c.getPlayer().dropMessage(-3, "(...I can use @run if I don't want to fight anymore...)");
            c.getPlayer().dropMessage(-4, "(...This is a tough choice! What do I do?...)"); //last msg they see
            return 1;
        }
    }


    public static class Info extends PokemonExecute {

        public int execute(MapleClient c, String[] splitted){
            NPCScriptManager.getInstance().start(c, 9000021); //no checks are needed
            return 1;
        }
    }

 
}
