/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package constants;


public class ServerConstants {

    public static final String HOST = "25.222.122.82";
    public static final int EXP_RATE = 10;
    public static final int MESO_RATE = 10;
    public static final int DROP_RATE = 10;
    public static final int TRAIT_RATE = 1;
    public static final int CASH_RATE = 1;
    public static final int NUM_CHANNELS = 2;
    public static final int USER_LIMIT = 100;
    public static final int LOGIN_FLAGS = 2;
    public static final String SERVER_MESSAGE = "WELCOME!";
    public static final String SERVER_NAME = "NAME!";
    public static final String EVENT_MESSAGE = "EVENT MESSAGE!";
    public static final int FLAGS = 0;
    public static final String EVENTS ="AswanOffSeason,PVP,CygnusBattle,ArkariumBattle,"
            + "ScarTarBattle,VonLeonBattle,HillaBattle,Ghost,OrbisPQ,Romeo,Juliet,Pirate,"
            + "Amoria,Ellin,CWKPQ,DollHouse,BossBalrog_EASY,BossBalrog_NORMAL,HorntailBattle,"
            + "Nibergen,PinkBeanBattle,ZakumBattle,NamelessMagicMonster,Dunas,Dunas2,2095_tokyo,"
            + "ZakumPQ,LudiPQ,KerningPQ,ProtectTylus,WitchTower_EASY,WitchTower_Med,"
            + "WitchTower_Hard,Vergamot,ChaosHorntail,ChaosZakum,CoreBlaze,BossQuestEASY,"
            + "BossQuestMed,BossQuestHARD,BossQuestHELL,Ravana_EASY,Ravana_HARD,"
            + "Ravana_MED,GuildQuest,Aufhaven,Dragonica,Rex,MonsterPark,KentaPQ";
    public static final int MAX_CHARACTERS = 15;
    public static boolean RELEASE = true;
    public static final short MAPLE_VERSION = (short) 117;
    public static final String MAPLE_PATCH = "1";
    public static final boolean BLOCK_CS = false;
    public static boolean ADMIN_SERVER = false;
    public static final int MIN_MTS = 100; //lowest amount an item can be, GMS = 110
    public static final int MTS_BASE = 0; //+amount to everything, GMS = 500, MSEA = 1000
    public static final int MTS_TAX = 5; //+% to everything, GMS = 10
    public static final int MTS_MESO = 10000; //mesos needed, GMS = 5000
    public static final int CURRENCY = 4001055; //maybe chg to something else
    public static final String DB_LINK = "jdbc:mysql://localhost:3306/fuckms?autoReconnect=true";
    public static final String SQL_USER = "root", SQL_PASSWORD = "";
    public static final String PORT = "8585";
}