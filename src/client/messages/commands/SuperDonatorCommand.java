/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import constants.Rank.PlayerGMRank;
import handling.world.World;
import tools.StringUtil;
import tools.packet.CWvsContext;

/**
 *
 * @author Emilyx3
 */
public class SuperDonatorCommand {

    public static PlayerGMRank getPlayerLevelRequired(){
        return PlayerGMRank.SUPERDONATOR;
    }

         public static class Say extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted){
             if (splitted.length > 1){
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                if (!c.getPlayer().isGM()){
                    sb.append("SuperDonor ");
                }
                sb.append(c.getPlayer().getName());
                sb.append("] ");
                sb.append(StringUtil.joinStringFrom(splitted, 1));
                World.Broadcast.broadcastMessage(CWvsContext.serverNotice(5, sb.toString()));
            } else {
                c.getPlayer().dropMessage(6, "Syntax: say <message>");
                return 0;
            }
            return 1;
        }
    }
                  public static class Ban extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted){
			if (splitted.length < 3){
                c.getPlayer().dropMessage(5, "[Syntax] %ban <IGN> <Reason>");
                return 0;
            }
			String originalReason = StringUtil.joinStringFrom(splitted, 2);
			String reason = c.getPlayer().getName() + " banned " + splitted[1] + ": " + originalReason;
			MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
			if (target != null){
				String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
				reason += " (IP: " + ip + ")";
				target.ban(reason, false);
				World.Broadcast.broadcastSmega(CWvsContext.serverNotice(0, "[SuperDonor Ban] " + c.getPlayer().getName() + " has banned " + target + " for " + originalReason));
			} else {
				if (MapleCharacter.ban(splitted[1], reason, false)){
					c.getPlayer().dropMessage(5, "Offline Banned " + splitted[1]);
				} else {
					c.getPlayer().dropMessage(5, "Failed to ban " + splitted[1]);
				}
                        }
                              return 0;
        }
         }
}