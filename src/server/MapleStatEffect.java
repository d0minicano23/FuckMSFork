package server;

import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import client.inventory.Item;
import constants.GameConstants;
import constants.skills.*;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import client.MapleDisease;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.MapleStat;
import client.MapleTrait.MapleTraitType;
import client.SkillFactory;
import client.PlayerStats;
import client.Skill;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;
import java.awt.Point;
import java.util.*;
import provider.MapleData;
import provider.MapleDataType;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import java.util.Map.Entry;
import server.MapleCarnivalFactory.MCSkill;
import server.Timer.BuffTimer;
import server.life.MapleLifeFactory;
import server.maps.MapleExtractor;
import server.maps.MechDoor;
import tools.Pair;
import tools.CaltechEval;
import tools.FileoutputUtil;
import tools.Triple;
import tools.packet.CField.EffectPacket;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.BuffPacket;

public class MapleStatEffect implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private Map<MapleStatInfo, Integer> info;
    private Map<MapleTraitType, Integer> traits;
    private boolean overTime, skill, partyBuff = true;
    private EnumMap<MapleBuffStat, Integer> statups;
    private ArrayList<Pair<Integer, Integer>> availableMap;
    private EnumMap<MonsterStatus, Integer> monsterStatus;
    private Point lt, rb;
    private byte level;
//    private List<Pair<Integer, Integer>> randomMorph;
    private List<MapleDisease> cureDebuffs;
    private List<Integer> petsCanConsume, familiars, randomPickup;
    private List<Triple<Integer, Integer, Integer>> rewardItem;
    private byte expR, familiarTarget, recipeUseCount, recipeValidDay, reqSkillLevel, slotCount, effectedOnAlly, effectedOnEnemy, type, preventslip, immortal, bs;
    private short ignoreMob, mesoR, thaw, fatigueChange, lifeId, imhp, immp, inflation, useLevel, indiePdd, indieMdd, incPVPdamage, mobSkill, mobSkillLevel;
    private double hpR, mpR;
    private int sourceid, recipe, moveTo, moneyCon, morphId = 0, expinc, exp, consumeOnPickup, charColor, interval, rewardMeso, totalprob, cosmetic;
    private int expBuff, itemup, mesoup, cashup, berserk, illusion, booster, berserk2, cp, nuffSkill;

    public static MapleStatEffect loadSkillEffectFromData(final MapleData source, final int skillid, final boolean overtime, final int level, final String variables) {
        return loadFromData(source, skillid, true, overtime, level, variables);
    }

    public static MapleStatEffect loadItemEffectFromData(final MapleData source, final int itemid) {
        return loadFromData(source, itemid, false, false, 1, null);
    }

    private static void addBuffStatPairToListIfNotZero(final EnumMap<MapleBuffStat, Integer> list, final MapleBuffStat buffstat, final Integer val) {
        if (val.intValue() != 0) {
            list.put(buffstat, val);
        }
    }

    private static int parseEval(String path, MapleData source, int def, String variables, int level) {
        if (variables == null) {
            return MapleDataTool.getIntConvert(path, source, def);
        } else {
            final MapleData dd = source.getChildByPath(path);
            if (dd == null) {
                return def;
            }
            if (dd.getType() != MapleDataType.STRING) {
                return MapleDataTool.getIntConvert(path, source, def);
            }
            String dddd = MapleDataTool.getString(dd).replace(variables, String.valueOf(level));
            if (dddd.substring(0, 1).equals("-")) { //-30+3*x
                if (dddd.substring(1, 2).equals("u") || dddd.substring(1, 2).equals("d")) { //-u(x/2)
                    dddd = "n(" + dddd.substring(1, dddd.length()) + ")"; //n(u(x/2))
                } else {
                    dddd = "n" + dddd.substring(1, dddd.length()); //n30+3*x
                }
            } else if (dddd.substring(0, 1).equals("=")) { //lol nexon and their mistakes
                dddd = dddd.substring(1, dddd.length());
            }
            return (int) (new CaltechEval(dddd).evaluate());
        }
    }

    private static MapleStatEffect loadFromData(final MapleData source, final int sourceid, final boolean skill, final boolean overTime, final int level, final String variables) {
        final MapleStatEffect ret = new MapleStatEffect();
        ret.sourceid = sourceid;
        ret.skill = skill;
        ret.level = (byte) level;
        if (source == null) {
            return ret;
        }
        ret.info = new EnumMap<>(MapleStatInfo.class);
        for (final MapleStatInfo i : MapleStatInfo.values()) {
            if (i.isSpecial()) {
                ret.info.put(i, parseEval(i.name().substring(0, i.name().length() - 1), source, i.getDefault(), variables, level));
            } else {
                ret.info.put(i, parseEval(i.name(), source, i.getDefault(), variables, level));
            }
        }
        ret.hpR = parseEval("hpR", source, 0, variables, level) / 100.0;
        ret.mpR = parseEval("mpR", source, 0, variables, level) / 100.0;
        ret.ignoreMob = (short) parseEval("ignoreMobpdpR", source, 0, variables, level);
        ret.thaw = (short) parseEval("thaw", source, 0, variables, level);
        ret.interval = parseEval("interval", source, 0, variables, level);
        ret.expinc = parseEval("expinc", source, 0, variables, level);
        ret.exp = parseEval("exp", source, 0, variables, level);
        ret.morphId = parseEval("morph", source, 0, variables, level);
        ret.cp = parseEval("cp", source, 0, variables, level);
        ret.cosmetic = parseEval("cosmetic", source, 0, variables, level);
        ret.slotCount = (byte) parseEval("slotCount", source, 0, variables, level);
        ret.preventslip = (byte) parseEval("preventslip", source, 0, variables, level);
        ret.useLevel = (short) parseEval("useLevel", source, 0, variables, level);
        ret.nuffSkill = parseEval("nuffSkill", source, 0, variables, level);
        ret.familiarTarget = (byte) (parseEval("familiarPassiveSkillTarget", source, 0, variables, level) + 1);
        ret.immortal = (byte) parseEval("immortal", source, 0, variables, level);
        ret.type = (byte) parseEval("type", source, 0, variables, level);
        ret.bs = (byte) parseEval("bs", source, 0, variables, level);
        ret.indiePdd = (short) parseEval("indiePdd", source, 0, variables, level);
        ret.indieMdd = (short) parseEval("indieMdd", source, 0, variables, level);
        ret.expBuff = parseEval("expBuff", source, 0, variables, level);
        ret.cashup = parseEval("cashBuff", source, 0, variables, level);
        ret.itemup = parseEval("itemupbyitem", source, 0, variables, level);
        ret.mesoup = parseEval("mesoupbyitem", source, 0, variables, level);
        ret.berserk = parseEval("berserk", source, 0, variables, level);
        ret.berserk2 = parseEval("berserk2", source, 0, variables, level);
        ret.booster = parseEval("booster", source, 0, variables, level);
        ret.lifeId = (short) parseEval("lifeId", source, 0, variables, level);
        ret.inflation = (short) parseEval("inflation", source, 0, variables, level);
        ret.imhp = (short) parseEval("imhp", source, 0, variables, level);
        ret.immp = (short) parseEval("immp", source, 0, variables, level);
        ret.illusion = parseEval("illusion", source, 0, variables, level);
        ret.consumeOnPickup = parseEval("consumeOnPickup", source, 0, variables, level);
        if (ret.consumeOnPickup == 1) {
            if (parseEval("party", source, 0, variables, level) > 0) {
                ret.consumeOnPickup = 2;
            }
        }
        ret.recipe = parseEval("recipe", source, 0, variables, level);
        ret.recipeUseCount = (byte) parseEval("recipeUseCount", source, 0, variables, level);
        ret.recipeValidDay = (byte) parseEval("recipeValidDay", source, 0, variables, level);
        ret.reqSkillLevel = (byte) parseEval("reqSkillLevel", source, 0, variables, level);
        ret.effectedOnAlly = (byte) parseEval("effectedOnAlly", source, 0, variables, level);
        ret.effectedOnEnemy = (byte) parseEval("effectedOnEnemy", source, 0, variables, level);
        ret.incPVPdamage = (short) parseEval("incPVPDamage", source, 0, variables, level);
        ret.moneyCon = parseEval("moneyCon", source, 0, variables, level);
        ret.moveTo = parseEval("moveTo", source, -1, variables, level);

        ret.charColor = 0;
        String cColor = MapleDataTool.getString("charColor", source, null);
        if (cColor != null) {
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(0, 2));
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(2, 4) + "00");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(4, 6) + "0000");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(6, 8) + "000000");
        }
        ret.traits = new EnumMap<>(MapleTraitType.class);
        for (MapleTraitType t : MapleTraitType.values()) {
            int expz = parseEval(t.name() + "EXP", source, 0, variables, level);
            if (expz != 0) {
                ret.traits.put(t, expz);
            }
        }
        List<MapleDisease> cure = new ArrayList<>(5);
        if (parseEval("poison", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.POISON);
        }
        if (parseEval("seal", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.SEAL);
        }
        if (parseEval("darkness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.DARKNESS);
        }
        if (parseEval("weakness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.WEAKEN);
        }
        if (parseEval("curse", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.CURSE);
        }
        ret.cureDebuffs = cure;
        ret.petsCanConsume = new ArrayList<>();
        for (int i = 0; true; i++) {
            final int dd = parseEval(String.valueOf(i), source, 0, variables, level);
            if (dd > 0) {
                ret.petsCanConsume.add(dd);
            } else {
                break;
            }
        }
        final MapleData mdd = source.getChildByPath("0");
        if (mdd != null && mdd.getChildren().size() > 0) {
            ret.mobSkill = (short) parseEval("mobSkill", mdd, 0, variables, level);
            ret.mobSkillLevel = (short) parseEval("level", mdd, 0, variables, level);
        } else {
            ret.mobSkill = 0;
            ret.mobSkillLevel = 0;
        }
        final MapleData pd = source.getChildByPath("randomPickup");
        if (pd != null) {
            ret.randomPickup = new ArrayList<Integer>();
            for (MapleData p : pd) {
                ret.randomPickup.add(MapleDataTool.getInt(p));
            }
        }
        final MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();
        }
        final MapleData ltc = source.getChildByPath("con");
        if (ltc != null) {
            ret.availableMap = new ArrayList<Pair<Integer, Integer>>();
            for (MapleData ltb : ltc) {
                ret.availableMap.add(new Pair<Integer, Integer>(MapleDataTool.getInt("sMap", ltb, 0), MapleDataTool.getInt("eMap", ltb, 999999999)));
            }
        }
        final MapleData ltb = source.getChildByPath("familiar");
        if (ltb != null) {
            ret.fatigueChange = (short) (parseEval("incFatigue", ltb, 0, variables, level) - parseEval("decFatigue", ltb, 0, variables, level));
            ret.familiarTarget = (byte) parseEval("target", ltb, 0, variables, level);
            final MapleData lta = ltb.getChildByPath("targetList");
            if (lta != null) {
                ret.familiars = new ArrayList<Integer>();
                for (MapleData ltz : lta) {
                    ret.familiars.add(MapleDataTool.getInt(ltz, 0));
                }
            }
        } else {
            ret.fatigueChange = 0;
        }
        int totalprob = 0;
        final MapleData lta = source.getChildByPath("reward");
        if (lta != null) {
            ret.rewardMeso = parseEval("meso", lta, 0, variables, level);
            final MapleData ltz = lta.getChildByPath("case");
            if (ltz != null) {
                ret.rewardItem = new ArrayList<Triple<Integer, Integer, Integer>>();
                for (MapleData lty : ltz) {
                    ret.rewardItem.add(new Triple<Integer, Integer, Integer>(MapleDataTool.getInt("id", lty, 0), MapleDataTool.getInt("count", lty, 0), MapleDataTool.getInt("prop", lty, 0))); // todo: period (in minutes)
                    totalprob += MapleDataTool.getInt("prob", lty, 0);
                }
            }
        } else {
            ret.rewardMeso = 0;
        }
        ret.totalprob = totalprob;
        // start of server calculated stuffs
        if (ret.skill) {
            final int priceUnit = ret.info.get(MapleStatInfo.priceUnit); // Guild skills
            if (priceUnit > 0) {
                final int price = ret.info.get(MapleStatInfo.price);
                final int extendPrice = ret.info.get(MapleStatInfo.extendPrice);
                ret.info.put(MapleStatInfo.price, price * priceUnit);
                ret.info.put(MapleStatInfo.extendPrice, extendPrice * priceUnit);
            }
            switch (sourceid) {
                case Fighter.FINAL_ATTACK_SWORD:
                case Page.FINAL_ATTACK_SWORD:
                case Spearman.FINAL_ATTACK_SPEAR:
                case Hunter.FINAL_ATTACK:
                case Crossbowman.FINAL_ATTACK:
                case DawnWarrior.FINAL_ATTACK:
                case WindArcher.FINAL_ATTACK:
                case FPMage.TELEPORT_MASTERY:
                case ILMage.TELEPORT_MASTERY:
                case Priest.TELEPORT_MASTERY:
                case BattleMage.TELEPORT_MASTERY:
                case Evan.TELEPORT_MASTERY:
                case BlazeWizard.TELEPORT_MASTERY:
                case WildHunter.FINAL_ATTACK:
                case Evan.DRAGON_SPARK:
                case Evan.ONYX_WILL:
                case Hero.ADVANCE_FINAL_ATTACK:
                case Bowmaster.ADVANCE_FINAL_ATTACK:
                case Mercedes.FINAL_ATTACK:
                case Mercedes.ADVANCE_FINAL_ATTACK:
                case Mihile.ROILING_SOUL:
                    ret.info.put(MapleStatInfo.mobCount, 6);
                    break;
                case Mechanic.MISSLE:
                case Mechanic.SIEGE_MODE:
                case Mechanic.SIEGE:
                    ret.info.put(MapleStatInfo.attackCount, 6);
                    ret.info.put(MapleStatInfo.bulletCount, 6);
                    break;
                case Phantom.CARTE_BLANCHE: // TODO: for now, or could it be card stack? (1 count)
                case Phantom.CARTE_NOIRE:
                    ret.info.put(MapleStatInfo.attackCount, 15);
                    break;
            }
            if (GameConstants.isNoDelaySkill(sourceid)) {
                ret.info.put(MapleStatInfo.mobCount, 6);
            }
        }
        if (!ret.skill && ret.info.get(MapleStatInfo.time) > -1) {
            ret.overTime = true;
        } else {
            ret.info.put(MapleStatInfo.time, (ret.info.get(MapleStatInfo.time) * 1000)); // items have their times stored in ms, of course
            ret.info.put(MapleStatInfo.subTime, (ret.info.get(MapleStatInfo.subTime) * 1000));
            ret.overTime = overTime || ret.isMorph() || ret.isPirateMorph() || ret.isFinalAttack() || ret.isAngel() || ret.getSummonMovementType() != null;
        }
        ret.monsterStatus = new EnumMap<>(MonsterStatus.class);
        ret.statups = new EnumMap<>(MapleBuffStat.class);
        if (ret.overTime && ret.getSummonMovementType() == null && !ret.isEnergyCharge()) {
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.WATK, ret.info.get(MapleStatInfo.pad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.WDEF, ret.info.get(MapleStatInfo.pdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MATK, ret.info.get(MapleStatInfo.mad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MDEF, ret.info.get(MapleStatInfo.mdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ACC, ret.info.get(MapleStatInfo.acc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.eva));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.SPEED, sourceid == 32120001 || sourceid == 32101003 ? ret.info.get(MapleStatInfo.x) : ret.info.get(MapleStatInfo.speed));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.JUMP, ret.info.get(MapleStatInfo.jump));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MAXHP, ret.info.get(MapleStatInfo.mhpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MAXMP, ret.info.get(MapleStatInfo.mmpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.BOOSTER, Integer.valueOf(ret.booster));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_LOSS_GUARD, Integer.valueOf(ret.thaw));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.EXPRATE, Integer.valueOf(ret.expBuff)); // EXP
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ACASH_RATE, Integer.valueOf(ret.cashup)); // custom
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.DROP_RATE, Integer.valueOf(GameConstants.getModifier(ret.sourceid, ret.itemup))); // defaults to 2x
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MESO_RATE, Integer.valueOf(GameConstants.getModifier(ret.sourceid, ret.mesoup))); // defaults to 2x
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.BERSERK_FURY, Integer.valueOf(ret.berserk2));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ILLUSION, Integer.valueOf(ret.illusion));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PYRAMID_PQ, Integer.valueOf(ret.berserk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MAXHP, ret.info.get(MapleStatInfo.emhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MAXMP, ret.info.get(MapleStatInfo.emmp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_WATK, ret.info.get(MapleStatInfo.epad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MATK, ret.info.get(MapleStatInfo.emad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_WDEF, ret.info.get(MapleStatInfo.epdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MDEF, ret.info.get(MapleStatInfo.emdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.GIANT_POTION, Integer.valueOf(ret.inflation));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.STR, ret.info.get(MapleStatInfo.str));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.DEX, ret.info.get(MapleStatInfo.dex));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.INT, ret.info.get(MapleStatInfo.int_));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.LUK, ret.info.get(MapleStatInfo.luk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_ATK, ret.info.get(MapleStatInfo.indiePad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_MATK, ret.info.get(MapleStatInfo.indieMad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST, Integer.valueOf(ret.imhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST, Integer.valueOf(ret.immp)); //same one? lol
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST_PERCENT, ret.info.get(MapleStatInfo.indieMhpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST_PERCENT, ret.info.get(MapleStatInfo.indieMmpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST, ret.info.get(MapleStatInfo.indieMhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST, ret.info.get(MapleStatInfo.indieMmp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PVP_DAMAGE, Integer.valueOf(ret.incPVPdamage));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_JUMP, ret.info.get(MapleStatInfo.indieJump));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_SPEED, ret.info.get(MapleStatInfo.indieSpeed));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_ACC, ret.info.get(MapleStatInfo.indieAcc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_AVOID, ret.info.get(MapleStatInfo.indieEva));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_STAT, ret.info.get(MapleStatInfo.indieAllStat));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PVP_ATTACK, ret.info.get(MapleStatInfo.PVPdamage));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.INVINCIBILITY, Integer.valueOf(ret.immortal));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.NO_SLIP, Integer.valueOf(ret.preventslip));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.FAMILIAR_SHADOW, Integer.valueOf(ret.charColor > 0 ? 1 : 0));
            if (sourceid == Corsair.BATTLE_SHIP || ret.isPirateMorph()) { //HACK: add stance :D and also this buffstat has to be the first one..
                ret.statups.put(MapleBuffStat.STANCE, 100); //100% :D:D:D
            }
        }
        if (ret.skill) {
            switch (sourceid) {
                case Magician.MAGIC_GUARD:
                case BlazeWizard.MAGIC_GUARD:
                case Evan.MAGIC_GUARD:
                    ret.statups.put(MapleBuffStat.MAGIC_GUARD, ret.info.get(MapleStatInfo.x));
                    break;            
                case Cleric.INVINCIBLE:
                    ret.statups.put(MapleBuffStat.INVINCIBLE, ret.info.get(MapleStatInfo.x));
                    break;
                case Mechanic.EXTREME:
                case Mechanic.PROTOTYPE:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    break;
                case GM.HIDE:
                case SuperGM.HIDE:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.info.get(MapleStatInfo.x));
                    break;
                case WindArcher.WIND_WALK:
                    ret.statups.put(MapleBuffStat.WIND_WALK, ret.info.get(MapleStatInfo.x));
                    break;
                case Rogue.DARK_SIGHT:
                case NightWalker.DARK_SIGHT:
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.info.get(MapleStatInfo.x)); // d
                    break;
                case DualBlade.ADV_DARK_SIGHT:
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.info.get(MapleStatInfo.y));
                    break;
                case ChiefBandit.PICKPOCKET:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.PICKPOCKET, ret.info.get(MapleStatInfo.x));
                    break;
                case ChiefBandit.MESO_GUARD:
                case ChiefBandit.MESO_GUARD2:
                    ret.statups.put(MapleBuffStat.MESOGUARD, ret.info.get(MapleStatInfo.x));
                    break;
                case Hermit.MESO_UP:
                    ret.statups.put(MapleBuffStat.MESOUP, ret.info.get(MapleStatInfo.x));
                    break;
                case Hermit.SHADOW_PARTNER:
                case ChiefBandit.SHADOW_PARTNER:
                case NightWalker.SHADOW_PARTNER:
                case DualBlade.MIRROR_IMAGE:
                    ret.statups.put(MapleBuffStat.SHADOWPARTNER, ret.info.get(MapleStatInfo.x));
                    break;
                case DawnWarrior.FINAL_ATTACK:
                case WindArcher.FINAL_ATTACK:
                    ret.statups.put(MapleBuffStat.FINALATTACK, ret.info.get(MapleStatInfo.x));
                    break;
                case Evan.ONYX_SHROUD:
                    ret.statups.put(MapleBuffStat.ONYX_SHROUD, ret.info.get(MapleStatInfo.x));
                    break;
                case Hunter.SOUL_ARROW:
                case Crossbowman.SOUL_ARROW:
                case Priest.MYSTIC_DOOR:
                case Mechanic.PORTAL:
                case WindArcher.SOUL_ARROW:
                case WildHunter.SOUL_ARROW:
                    ret.statups.put(MapleBuffStat.SOULARROW, ret.info.get(MapleStatInfo.x));
                    break;
                case Bishop.BUFF_MASTERY:
                case FPArchMage.BUFF_MASTERY:
                case ILArchMage.BUFF_MASTERY:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.BUFF_MASTERY, ret.info.get(MapleStatInfo.x));
                    break;
                case Bishop.ARCANE_AIM:
                case FPArchMage.ARCANE_AIM:
                case ILArchMage.ARCANE_AIM:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.ARCANE_AIM, ret.info.get(MapleStatInfo.x));
                    break;
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case Paladin.BW_HOLY_CHARGE:
                case DawnWarrior.SOUL_CHARGE:
                case WhiteKnight.SWORD_FIRE_CHARGE:
                case WhiteKnight.SWORD_ICE_CHARGE:
                case WhiteKnight.SWORD_LIT_CHARGE:
                    ret.statups.put(MapleBuffStat.WK_CHARGE, ret.info.get(MapleStatInfo.x));
                    break;
                case ILMage.ELEMNT_DECREASE:
                case FPMage.ELEMENT_DECREASE:
                case BlazeWizard.ELEMENTAL_RESET:
                case Evan.ELEMENT_RESET:
                    ret.statups.put(MapleBuffStat.ELEMENT_RESET, ret.info.get(MapleStatInfo.x));
                    break;
                case Bowmaster.CONCENTRATE:
                    ret.statups.put(MapleBuffStat.CONCENTRATE, ret.info.get(MapleStatInfo.x));
                    break;
                case Marauder.ENERGY_CHARGE:
                case ThunderBreaker.ENERGY_CHARGE:
                    ret.statups.put(MapleBuffStat.ENERGY_CHARGE, 0);
                    break;
                case Aran.POLEARM_BOOSTER:
                case Fighter.AXE_BOOSTER:
                case Fighter.SWORD_BOOSTER:
                case Page.BW_BOOSTER:
                case Page.SWORD_BOOSTER:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case Hunter.BOW_BOOSTER:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Assassin.CLAW_BOOSTER:
                case Bandit.DAGGER_BOOSTER:
                case FPMage.SPELL_BOOSTER:
                case ILMage.SPELL_BOOSTER:
                case Brawler.KNUCKLER_BOOSTER:
                case Gunslinger.GUN_BOOSTER:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case NightWalker.CLAW_BOOSTER:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case Mercedes.DBG_BOOSTER:
                case DemonSlayer.BATTLE_IMPACT:
                case Evan.MAGIC_BOOSTER:
                case Priest.MAGIC_BOOSTER:
                case DualBlade.KATARA_BOOSTER:
                case BattleMage.STAFF_BOOSTER:
                case WildHunter.CROSSBOW_BOOSTER:
                case Mechanic.MECHANIC_RAGE:
                case CannonShooter.CANNON_BOOSTER:
                case Phantom.CANE_BOOSTER:
                case Jett.GUN_BOOSTER:
                case Mihile.SWORD_BOOSTER:   
                    ret.statups.put(MapleBuffStat.BOOSTER, ret.info.get(MapleStatInfo.x));
                    break;
                case Mechanic.DICE:
                case Marauder.DICE:
                case Outlaw.DICE:
                case Marauder.DICE2:
                case Outlaw.DICE2:
                case CannonShooter.DIE:
                case CannonShooter.DOUBLE_DOWN:
                case Buccaneer.DOUBLE_DOWN:
                case Corsair.DOUBLE_DOWN:
                case Jett.DICE:
                case Jett.DOUBLE_DOWN:
                case ThunderBreaker.DICE:
                    ret.statups.put(MapleBuffStat.DICE_ROLL, 0);
                    break;
                case Buccaneer.PIRATES_REVENGE:
                case Corsair.PIRATES_REVENGE:
                    ret.info.put(MapleStatInfo.cooltime, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, (int) ret.info.get(MapleStatInfo.damR)); //i think
                    break;
                case Corsair.SPEED_INFUSION:
                case Buccaneer.SPEED_INFUSION:
                case ThunderBreaker.SPEED_INFUSION:
                    ret.statups.put(MapleBuffStat.SPEED_INFUSION, ret.info.get(MapleStatInfo.x));
                    break;
                case DualBlade.TORNADO_SPIN:
                    ret.info.put(MapleStatInfo.time, 1000);
                    ret.statups.put(MapleBuffStat.DASH_SPEED, 100 + ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.DASH_JUMP, ret.info.get(MapleStatInfo.y)); //always 0 but its there
                    break;
                case Pirate.DASH:
                case ThunderBreaker.DASH:
                case Beginner.SPACE_DASH:
                case Noblesse.SPACE_DASH:
                    ret.statups.put(MapleBuffStat.DASH_SPEED, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.DASH_JUMP, ret.info.get(MapleStatInfo.y));
                    break;
                case Fighter.POWER_GUARD:
                case Page.POWER_GUARD:
                    ret.statups.put(MapleBuffStat.POWERGUARD, ret.info.get(MapleStatInfo.x));
                    break;
                case BattleMage.CONVERSION:
                    ret.statups.put(MapleBuffStat.CONVERSION, ret.info.get(MapleStatInfo.x));
                    break;
                case Spearman.HYPER_BODY:
                case GM.HYPER_BODY:
                case SuperGM.HYPER_BODY:
                    ret.statups.put(MapleBuffStat.MAXHP, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.MAXMP, ret.info.get(MapleStatInfo.x));
                    break;
                case Crusader.COMBO:
                case Hero.ADVANCED_COMBO:
                    ret.statups.put(MapleBuffStat.COMBO, 1);
                    break;
                case Aran.COMBO_BARRIER:
                    ret.statups.put(MapleBuffStat.COMBO_BARRIER, ret.info.get(MapleStatInfo.x));
                    break;
                case Outlaw.HOMING_BEACON:
                case Corsair.BULLSEYE:
                case Evan.KILLER_WINGS:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.HOMING_BEACON, ret.info.get(MapleStatInfo.x));
                    break;
                case Aran.COMBO_RECHARGE:
                case DragonKnight.DRAGON_ROAR:
                case DragonKnight.SACRIFICE:
                    ret.hpR = -ret.info.get(MapleStatInfo.x) / 100.0;
                    break;
                case WhiteKnight.HP_RECOVERY:
                    ret.hpR = ret.info.get(MapleStatInfo.x) / 100.0;
                    break;
                case DualBlade.FINAL_CUT:
                    ret.info.put(MapleStatInfo.time, 60 * 1000);
                    ret.hpR = -ret.info.get(MapleStatInfo.x) / 100.0;
                    ret.statups.put(MapleBuffStat.FINAL_CUT, ret.info.get(MapleStatInfo.y));
                    break;
                case FPMage.TELEPORT_MASTERY:
                case ILMage.TELEPORT_MASTERY:
                case Priest.TELEPORT_MASTERY:
                case BattleMage.TELEPORT_MASTERY:
                case Evan.TELEPORT_MASTERY:
                case BlazeWizard.TELEPORT_MASTERY:
                    ret.info.put(MapleStatInfo.mpCon, ret.info.get(MapleStatInfo.y));
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.TELEPORT_MASTERY, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case DualBlade.OWL_SPIRIT:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.OWL_SPIRIT, ret.info.get(MapleStatInfo.y));
                    break;
                case DragonKnight.DRAGON_BLOOD:
                    if (!GameConstants.GMS) { //TODO JUMP
                        ret.statups.put(MapleBuffStat.DRAGONBLOOD, ret.info.get(MapleStatInfo.x));
                    }
                    break;
                case Hero.MAPLE_WARRIOR:
                case Paladin.MAPLE_WARRIOR:
                case DarkKnight.MAPLE_WARRIOR:
                case FPArchMage.MAPLE_WARRIOR:
                case ILArchMage.MAPLE_WARRIOR:
                case Bishop.MAPLE_WARRIOR:
                case Bowmaster.MAPLE_WARRIOR:
                case Marksman.MAPLE_WARRIOR:
                case NightLord.MAPLE_WARRIOR:
                case Shadower.MAPLE_WARRIOR:
                case Corsair.MAPLE_WARRIOR:
                case Buccaneer.MAPLE_WARRIOR:
                case Aran.MAPLE_WARRIOR:
                case CannonShooter.MAPLE_WARRIOR:
                case Jett.MAPLE_WARRIOR:
                case Evan.MAPLE_WARRIOR:
                case DualBlade.MAPLE_WARRIOR:
                case BattleMage.MAPLE_WARRIOR:
                case WildHunter.MAPLE_WARRIOR:
                case Mechanic.MAPLE_WARRIOR:
                case Mercedes.MAPLE_WARRIOR:
                case DemonSlayer.MAPLE_WARRIOR:
                case Phantom.MAPLE_WARRIOR:
                case Mihile.MAPLE_WARRIOR:
                    ret.statups.put(MapleBuffStat.MAPLE_WARRIOR, ret.info.get(MapleStatInfo.x));
                    break;
                case ThunderBreaker.SPARK:
                    ret.statups.put(MapleBuffStat.SPARK, ret.info.get(MapleStatInfo.x));
                    break;
                case Bowmaster.SHARP_EYES:
                case Marksman.SHARP_EYES:
                case WildHunter.SHARP_EYES:
                    ret.statups.put(MapleBuffStat.SHARP_EYES, (ret.info.get(MapleStatInfo.x) << 8) + ret.info.get(MapleStatInfo.criticaldamageMax));
                    break;
                case Evan.MAGIC_RESISTANCE:
                    ret.statups.put(MapleBuffStat.MAGIC_RESISTANCE, ret.info.get(MapleStatInfo.x));
                    break;
                case Magician.ELEMENTAL_WEAKNESS:
                case BlazeWizard.ELEMENTAL_WEAKNESS:
                case Evan.ELEMTENTAL_WEAKNESS:
                case BattleMage.ELEMENTAL_WEAKNESS:
                    ret.statups.put(MapleBuffStat.ELEMENT_WEAKEN, ret.info.get(MapleStatInfo.x));
                    break;
                case Aran.BODY_PRESSURE:
                    ret.statups.put(MapleBuffStat.BODY_PRESSURE, ret.info.get(MapleStatInfo.x));
                    break;
                case Aran.COMBO_ABILITY:
                    ret.statups.put(MapleBuffStat.ARAN_COMBO, 100);
                    break;
                case Mercedes.SPIRIT_SURGE:
                    ret.statups.put(MapleBuffStat.SPIRIT_SURGE, ret.info.get(MapleStatInfo.x));
                    break;
                case Aran.COMBO_DRAIN:
                case BattleMage.BLOOD_DRAIN:
                case DemonSlayer.LEECH_AURA:
                    ret.statups.put(MapleBuffStat.COMBO_DRAIN, ret.info.get(MapleStatInfo.x));
                    break;
                case Aran.SMART_KNOCKBACK:
                    ret.statups.put(MapleBuffStat.SMART_KNOCKBACK, ret.info.get(MapleStatInfo.x));
                    break;
                case Mercedes.ANCIENT_WARDING://TODO LEGEND
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, (int) ret.info.get(MapleStatInfo.damR));
                    break;
                case DragonKnight.POWER_CRASH:
                case Crusader.ARMOR_CRASH:
                case WhiteKnight.MAGIC_CRASH:
                    ret.monsterStatus.put(MonsterStatus.MAGIC_CRASH, Integer.valueOf(1));
                    break;
                case Paladin.DIVINE_SHIELD:
                    ret.statups.put(MapleBuffStat.DIVINE_SHIELD, ret.info.get(MapleStatInfo.x));
                    break;
                case Paladin.ADVANCED_CHARGE:
                case WhiteKnight.COMBAT_ORDERS:
                    ret.statups.put(MapleBuffStat.COMBAT_ORDERS, ret.info.get(MapleStatInfo.x));
                    break;
                case Mercedes.WATER_SHIELD:
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, ret.info.get(MapleStatInfo.terR));
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, ret.info.get(MapleStatInfo.terR));
                    ret.statups.put(MapleBuffStat.WATER_SHIELD, ret.info.get(MapleStatInfo.x));
                    break;
                case Evan.MAGIC_SHIELD:
                    ret.statups.put(MapleBuffStat.MAGIC_SHIELD, ret.info.get(MapleStatInfo.x));
                    break;
                case Evan.SOUL_STONE:
                    ret.statups.put(MapleBuffStat.SOUL_STONE, 1);
                    break;
                case Phantom.FINAL_FEINT:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.SOUL_STONE, 1);
                    break;
                case BattleMage.TWISTER_SPIN:
                    ret.statups.put(MapleBuffStat.TORNADO, ret.info.get(MapleStatInfo.x));
                    break;
                case Outlaw.CROSS_CUT_BLAST:
                     ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.y));
                    break;
                case Priest.HOLY_MAGIC_SHELL:
                    ret.statups.put(MapleBuffStat.HOLY_MAGIC_SHELL, ret.info.get(MapleStatInfo.x));
                    ret.info.put(MapleStatInfo.cooltime, ret.info.get(MapleStatInfo.y));
                    ret.hpR = ret.info.get(MapleStatInfo.z) / 100.0;
                    break;
                case BattleMage.BODY_BOOSTER:
                    ret.info.put(MapleStatInfo.time, 60000);
                    ret.statups.put(MapleBuffStat.BODY_BOOST, (int) ret.level); //lots of variables
                    break;
                case Evan.ELEMENTAL_DECREASE:
                case Evan.SLOW:
                    ret.statups.put(MapleBuffStat.SLOW, ret.info.get(MapleStatInfo.x));
                    break;
                case Rogue.DISORDER:
                case NightWalker.DISORDER:
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.y));
                    break;
                case Corsair.HYPNOTIZE:
                    ret.monsterStatus.put(MonsterStatus.HYPNOTIZE, 1);
                    break;
                case DualBlade.MONSTER_BOMB:
                    ret.monsterStatus.put(MonsterStatus.MONSTER_BOMB, (int) ret.info.get(MapleStatInfo.damage));
                    break;
                case Page.THREATEN:
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.DARKNESS, ret.info.get(MapleStatInfo.z));
                    break;
                case Crusader.AXE_COMA:
                case Crusader.SWORD_COMA:
                case Crusader.SHOUT:
                case WhiteKnight.CHARGE_BLOW:
                case Hunter.ARROW_BOMB:
                case Shadower.BOOMERANG_STEP:
                case Brawler.BACK_SPIN_BLOW:
                case Brawler.DOUBLE_UPPERCUT:
                case Buccaneer.DEMOLITION:
                case Buccaneer.SNATCH:
                case Buccaneer.BARRAGE:
                case Gunslinger.BLANK_SHOT:
                case DawnWarrior.COMA:
                case Aran.ROLLING_SPIN:
                case Evan.DRAGON_THRUST:
                case DualBlade.FLY_ASSULTER:
                case NightLord.NINJA_STORM:
                case Evan.FIRE_BREATH:
                case Bandit.STEAL:
                case WildHunter.RICOCHET:
                case WildHunter.JAGUAR_RAWR:
                case BattleMage.HYPER_DARK_CHAIN:
                case BattleMage.ADV_DARK_CHAIN:
                case BattleMage.DARK_GENESIS:
                case WildHunter.DASH:
                case WildHunter.SONIC:
                case Mechanic.ATOMIC_HAMMER:
                case Mechanic.PUNCH:
                case Marauder.ENERGY_BLAST:
                case ThunderBreaker.ENERGY_BLAST:
                case DualBlade.FLY_ASSULTER2:
                case DarkKnight.MONSTER_MAGNET:
                case Paladin.MONSTER_MAGNET:
                case Hero.MONSTER_MAGNET:
                case 9001020: // what skill ?
                case DemonSlayer.VORTEX:
                case DemonSlayer.RAVEN_STORM:
                case 9101020: // again what skill?
                case ILMage.THUNDER_SPEAR:
                case Priest.RAY:
                case Bowmaster.BROILER:
                case Evan.BLAZE:
                case Evan.BLESSING_OF_ONYX:
                case CannonShooter.BARREL_BOMB:
                case CannonShooter.MONKEY_MADNESS:
                case CannonShooter.MONKEY_WAVE:
                case ILArchMage.CHAIN_LIGHTNING:
                case CannonShooter.MONKEY_WAVE2:
                case Mihile.RADIANT_BUSTER:    
                    ret.monsterStatus.put(MonsterStatus.STUN, 1);
                    break;
                case 90001004: // darkness pot skill
                case DualBlade.FLASHBANG:
                case Crusader.SWORD_PANIC:
                case DawnWarrior.PANIC:
                    ret.monsterStatus.put(MonsterStatus.DARKNESS, ret.info.get(MapleStatInfo.x));
                    break;
                case Shadower.TAUNT:
                case NightLord.TAUNT:
                case WildHunter.STINK_BOMB:
                    ret.monsterStatus.put(MonsterStatus.SHOWDOWN, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.MDEF, ret.info.get(MapleStatInfo.x)); // removed for taunt
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.x)); // removed for taunt
                    break;
                case DemonSlayer.DEMON_CRY:
                    ret.monsterStatus.put(MonsterStatus.SHOWDOWN, ret.info.get(MapleStatInfo.w));
                    ret.monsterStatus.put(MonsterStatus.MDEF, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.MATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.ACC, ret.info.get(MapleStatInfo.x));
                    break;
                case Mercedes.SPIKES_ROYALE:
                    ret.monsterStatus.put(MonsterStatus.WDEF, -ret.info.get(MapleStatInfo.x));
                    break;
                case ILWizard.COLD_BEAM:
                case ILArchMage.GLACIER_CHAIN:
                case ILMage.ICE_STRIKE:
                case Sniper.BLIZZARD:
                case ILMage.ELEMENT_COMPOSITION:
                case ILArchMage.BLIZZARD:
                case Outlaw.ICE_SPLITTER:
                case FPArchMage.PARALYZE:
                case Aran.COMBO_TEMPEST:
                case Evan.ICE_BREATH:
                case 90001006: // freeze pot skill
                case ILArchMage.BIG_BANG:
                    ret.monsterStatus.put(MonsterStatus.FREEZE, 1);
                    ret.info.put(MapleStatInfo.time, ret.info.get(MapleStatInfo.time) * 2); // freezing skills are a little strange
                    break;
                case FPWizard.SLOW:
                case ILWizard.SLOW:
                case BlazeWizard.SLOW:
                case 90001002: // pot skill
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.x));
                    break;
                case CannonShooter.BLAST_BACK:
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.z));
                    break;
                case Hero.ENRAGE:                                                                                                             
                    ret.statups.put(MapleBuffStat.ENRAGE, ret.info.get(MapleStatInfo.x) * 100 + ret.info.get(MapleStatInfo.mobCount));
                    break;
                case Mercedes.UNICORN_SPIKE: //TODO LEGEND: damage increase?
                case Evan.PHANTOM_IMPRINT:
                    ret.monsterStatus.put(MonsterStatus.IMPRINT, ret.info.get(MapleStatInfo.x));
                    break;
                case FPWizard.POISON_BREATH:
                case FPMage.ELEMENT_COMPOSITION:
                case 90001003: //should be poison pot skill?
                case NightLord.VENOMOUS_STAR:
                case Shadower.VENOMOUS_STAB:
                case Hermit.VENOM:
                case ChiefBandit.VENOM:
                case DualBlade.VENOM1:
                case DualBlade.VENOM2:
                case NightWalker.VENOM:
                case DualBlade.TOXIC_VENOM:
                case NightLord.TOXIC_VENOM:
                case Shadower.TOXIC_VENOM:
                    ret.monsterStatus.put(MonsterStatus.POISON, 1);
                    break;
                case Shadower.NINJA_AMBUSH:
                case NightLord.NINJA_AMBUSH:
                    ret.monsterStatus.put(MonsterStatus.NINJA_AMBUSH, (int) ret.info.get(MapleStatInfo.damage));
                    break;
                case Priest.DOOM:
                    ret.monsterStatus.put(MonsterStatus.DOOM, 1);
                    break;
                case BattleMage.REAPER:
                    ret.statups.put(MapleBuffStat.REAPER, 1);                
                    break;
                case Mechanic.SG88:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    break;
                case Mechanic.SATELLITE1:
                case Mechanic.SATELLITE2:
                case Mechanic.SATELLITE3:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.PUPPET, 1);
                    break;
                case DualBlade.MIRROR_TARGET:
                case Bowmaster.ELITE_PUPPET:
                case Marksman.ELITE_PUPPET:
                case Ranger.PUPPET:
                case Sniper.PUPPET:
                case WindArcher.PUPPET:
                case Outlaw.OCTOPUS:
                case Corsair.WRATH_OF_THE_OCTOPI:
                case WildHunter.WILD_TRAP:
                case CannonShooter.ANCHORS:
                case Outlaw.OCTO_CANNON:
                    ret.statups.put(MapleBuffStat.PUPPET, 1);
                    break;
                case Bowmaster.SPIRIT_LINK:
                case Marksman.SPIRIT_LINK:
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, (int) ret.info.get(MapleStatInfo.terR));
                    ret.statups.put(MapleBuffStat.SPIRIT_LINK, 1);
                    break;
                case Corsair.AHOY:
                    ret.info.put(MapleStatInfo.time, 120000);
                    break;
                case Outlaw.ALL_ABOARD1:
                case Outlaw.ALL_ABOARD2:
                case Outlaw.ALL_ABOARD3:
                case Jett.TURRET_DEPLOY:
                case FPArchMage.ELQUINES:
                case Crossbowman.GOLDEN_EAGLE:
                case Hunter.SILVER_HAWK:
                case Sniper.GOLDEN_EAGLE:
                case Ranger.SILVER_HAWK:
                case WildHunter.SILVER_HAWK:
                case Mechanic.ROCK_N_SHOCK:
                case Bowmaster.PHOENIX:
                case Mercedes.ELEMENTAL_KNIGHTS1:
                case Mercedes.ELEMENTAL_KNIGHTS2:
                case Mercedes.ELEMENTAL_KNIGHTS3:
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case Marksman.FROST_PREY:
                case ILArchMage.IFRIT:
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    ret.monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case Mechanic.BOT_EX7:
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.y));
                    break;
                case DarkKnight.BEHOLDER:
                    ret.statups.put(MapleBuffStat.BEHOLDER, (int) ret.level);
                    break;
                case Bishop.BAHAMUT:
                case Outlaw.GAVIOTA:
                case DawnWarrior.SOUL:
                case BlazeWizard.FLAME:
                case BlazeWizard.IFRIT:
                case WindArcher.STORM:
                case NightWalker.DARKNESS:
                case ThunderBreaker.LIGHTNING:
                case Mechanic.HEAL:
                case Mechanic.BOTS_N_TOTS:
                case Mechanic.UNKNOWN_SKILL:
                case WildHunter.RAINING:
                case Hermit.DARK_FLARE:
                case ChiefBandit.DARK_FLARE:
                case NightWalker.DARK_FLARE: 
                case CannonShooter.MILITA:
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    break;
                case Mechanic.AMPLIFIER:
                    ret.info.put(MapleStatInfo.time, 60000);
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    break;
                case DemonSlayer.DARK_META:
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, (int) ret.info.get(MapleStatInfo.damR));
                    ret.statups.put(MapleBuffStat.DARK_METAMORPHOSIS, 6); // mob count
                    break;
                case Priest.HOLY_SYMBOL:
                case SuperGM.HOLY_SYMBOL:
                    ret.statups.put(MapleBuffStat.HOLY_SYMBOL, ret.info.get(MapleStatInfo.x));
                    break;
                case 80001034: //virtue
                case 80001035: //virtue
                case 80001036: //virtue
                    ret.statups.put(MapleBuffStat.VIRTUE_EFFECT, 1);
                    break;
                case ILMage.SEAL:
                case FPMage.SEAL:
                case BlazeWizard.SEAL:
                case 90001005: // pot skill
                    ret.monsterStatus.put(MonsterStatus.SEAL, 1);
                    break;
                case Phantom.PRENOMBRE:
                    ret.info.put(MapleStatInfo.damage, ret.info.get(MapleStatInfo.v));
                    ret.info.put(MapleStatInfo.attackCount, ret.info.get(MapleStatInfo.w));
                    ret.info.put(MapleStatInfo.mobCount, ret.info.get(MapleStatInfo.x));
                    break;
                case DualBlade.SHADOW_MELD:
                    ret.statups.put(MapleBuffStat.WATK, ret.info.get(MapleStatInfo.indiePad));
                    ret.statups.put(MapleBuffStat.SHARP_EYES, (ret.info.get(MapleStatInfo.x) + 100 << 8)); // I guess this is right!
                    break;
                case Hermit.SHADOW_WEB:
                case NightWalker.SHADOW_WEB:
                    ret.monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
                    break;
                case Hermit.SHADOW_STARS:
                case Gunslinger.INFINITY_BLAST:
                case NightWalker.SHADOW_STARS:
                    ret.statups.put(MapleBuffStat.SPIRIT_CLAW, 0);
                    break;
                case FPArchMage.INFINITY:
                case ILArchMage.INFINITY:
                case Bishop.INFINITY:
                    ret.hpR = ret.info.get(MapleStatInfo.y) / 100.0;
                    ret.mpR = ret.info.get(MapleStatInfo.y) / 100.0;
                    ret.statups.put(MapleBuffStat.INFINITY, ret.info.get(MapleStatInfo.x));
                    if (GameConstants.GMS) { //TODO JUMP
                        ret.statups.put(MapleBuffStat.STANCE, (int) ret.info.get(MapleStatInfo.prop));
                    }
                    break;
                case Evan.ONYX_WILL:
                    ret.statups.put(MapleBuffStat.ONYX_WILL, (int) ret.info.get(MapleStatInfo.damage)); //is this the right order
                    ret.statups.put(MapleBuffStat.STANCE, (int) ret.info.get(MapleStatInfo.prop));
                    break;
                case Hero.STANCE:
                case Paladin.STANCE:
                case DarkKnight.STANCE:
                case Aran.FREEZE_STANDING:
                case BattleMage.STANCE2:
                case CannonShooter.SPIRIT:
                case Mihile.STANCE:
                case Mihile.KNIGHTS_WATCH:    
                case BattleMage.STANCE:
                case DualBlade.THORNS:
                    ret.statups.put(MapleBuffStat.STANCE, (int) ret.info.get(MapleStatInfo.prop));
                    break;
                case Bishop.MANA_REFLECTION:
                case FPArchMage.MANA_REFLECTION:
                case ILArchMage.MANA_REFLECTION:
                    ret.statups.put(MapleBuffStat.MANA_REFLECTION, 1);
                    break;
                case Bishop.ADV_BLESS: // holy shield, TODO JUMP         
                    ret.statups.put(MapleBuffStat.HOLY_SHIELD, GameConstants.GMS ? (int) ret.level : ret.info.get(MapleStatInfo.x));
                    break;
                case Bowmaster.HAMSTRING:
                    ret.statups.put(MapleBuffStat.HAMSTRING, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.x));
                    break;
                case WildHunter.BLIND:
                    ret.statups.put(MapleBuffStat.BLIND, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.ACC, ret.info.get(MapleStatInfo.x));
                    break;
                case WildHunter.FELINE:
                    ret.statups.put(MapleBuffStat.SPEED, ret.info.get(MapleStatInfo.z));
                    ret.statups.put(MapleBuffStat.ATTACK_BUFF, ret.info.get(MapleStatInfo.y));
                    ret.statups.put(MapleBuffStat.FELINE_BERSERK, ret.info.get(MapleStatInfo.x));
                    break;
                case Cleric.BLESS:
                case GM.BLESS:
                case SuperGM.BLESS:
                    ret.statups.put(MapleBuffStat.BLESS, (int) ret.level);
                    break;
                case BattleMage.ADV_DARK_AURA:
                    ret.info.put(MapleStatInfo.dot, ret.info.get(MapleStatInfo.damage));
                    ret.info.put(MapleStatInfo.dotTime, 3);
                case BattleMage.DARK_AURA:
                case 32110007://effect
                case BattleMage.BLUE_AURA:
                case BattleMage.ADV_BLUE_AURA:
                case 32110008://effect
                    ret.info.put(MapleStatInfo.time, (sourceid == 32110008 ? 60000 : 2100000000));
                    ret.statups.put(MapleBuffStat.AURA, (int) ret.level);
                    ret.statups.put(MapleBuffStat.BLUE_AURA, (int) ret.level);
                    break;
                case BattleMage.ADV_YELLOW_AURA:
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.speed));
                case BattleMage.YELLOW_AURA:
                case 32110009://effect
                    ret.info.put(MapleStatInfo.time, (sourceid == 32110009 ? 60000 : 2100000000));
                    ret.statups.put(MapleBuffStat.AURA, (int) ret.level);
                    ret.statups.put(MapleBuffStat.YELLOW_AURA, (int) ret.level);
                    break;
                case WildHunter.IRM:
                    ret.statups.put(MapleBuffStat.RAINING_MINES, ret.info.get(MapleStatInfo.x)); //x?
                    break;
                case Mechanic.PERFECT_ARMOR:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.PERFECT_ARMOR, ret.info.get(MapleStatInfo.x));
                    break;
                case DemonSlayer.VENGEANCE:
                    ret.statups.put(MapleBuffStat.PERFECT_ARMOR, ret.info.get(MapleStatInfo.y));
                    break;
                case Mechanic.SATELLITE_SAFETY:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.SATELLITESAFE_PROC, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.SATELLITESAFE_ABSORB, ret.info.get(MapleStatInfo.y));
                    break;
               case Mihile.ENDURING_SPIRIT:
                   ret.statups.put(MapleBuffStat.DEFENCE_R, ret.info.get(MapleStatInfo.x));
                   ret.statups.put(MapleBuffStat.STATUS_RESIST, ret.info.get(MapleStatInfo.y));
                   ret.statups.put(MapleBuffStat.ELEMENT_RESIST, ret.info.get(MapleStatInfo.z));
                    break;
                 case Mihile.ROILING_SOUL:
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.CRITICAL_RATE_BUFF, ret.info.get(MapleStatInfo.y));
                    ret.statups.put(MapleBuffStat.CRITICAL_RATE_BUFF, ret.info.get(MapleStatInfo.z));
                    break;
                 case Mihile.RADIANT: 
                 ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                     break;
                case 80001040: // for what job?
                case 20021110: // for what job?
                case Phantom.TO_THE_SKIES:
                    ret.moveTo = ret.info.get(MapleStatInfo.x);
                    break;
                case CannonShooter.BARREL_ROULETTE:
                    ret.statups.put(MapleBuffStat.BARREL_ROLL, 0);
                    break;
                case Buccaneer.CROSS_BONES:
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    break;
                case 80001089: // Soaring
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.SOARING, 1);
                    break;
                case Phantom.SHROUD_WALK:
                    ret.statups.put(MapleBuffStat.PHANTOM_MOVE, ret.info.get(MapleStatInfo.x));
                    break;
                case Mechanic.FLAME_LAUNCHER:
                case Mechanic.ENHANCED_FLAME_LAUNCHER:
                    ret.info.put(MapleStatInfo.time, 1000);
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, (int) level); //ya wtf
                    break;
                case Mechanic.SIEGE_MODE:
                case Mechanic.SIEGE:
                    ret.info.put(MapleStatInfo.time, 5000);
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, (int) level); //ya wtf
                    break;
                case Mechanic.MISSLE:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, (int) level); //ya wtf
                    break;
                case Beginner.ECHO_OF_HERO:
                case Noblesse.ECHO_OF_HERO:
                case Legend.ECHO_OF_HERO:
                case Citizen.ECHO_OF_HERO:
                case Mihile.ECHO_OF_HERO:
                case Mercedes.ECHO_OF_HERO:
                case Phantom.ECHO_OF_HERO:
                case DemonSlayer.ECHO_OF_HERO:
                    ret.statups.put(MapleBuffStat.ECHO_OF_HERO, ret.info.get(MapleStatInfo.x));
                    break;
                case BattleMage.BOUNDLESS_RAGE:
                    ret.statups.put(MapleBuffStat.BOUNDLESS_RAGE, 1); // for now
                    break;
                case DemonSlayer.BLACK_HEART:					
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, ret.info.get(MapleStatInfo.y));
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, ret.info.get(MapleStatInfo.z));
                    ret.statups.put(MapleBuffStat.DEFENCE_BOOST_R, ret.info.get(MapleStatInfo.x));
                    break;
                    //angelic blessing: HACK, we're actually supposed to use the passives for atk/matk buff
                    case Beginner.DARK_ANGEL:
                        ret.info.put(MapleStatInfo.time, 2100000000);
                        ret.statups.put(MapleBuffStat.ANGEL_ATK, 10);
                        ret.statups.put(MapleBuffStat.ANGEL_MATK, 10);
                        break;
                    case Beginner.ARCANGEL:
                    case Beginner.WHITE_ANGEL:
                        ret.info.put(MapleStatInfo.time, 2100000000);
                        ret.statups.put(MapleBuffStat.ANGEL_ATK, 5);
                        ret.statups.put(MapleBuffStat.ANGEL_MATK, 5);
                        break;
                    case Beginner.WHITE_ANGEL2:
                        ret.info.put(MapleStatInfo.time, 2100000000);
                        ret.statups.put(MapleBuffStat.ANGEL_ATK, 12);
                        ret.statups.put(MapleBuffStat.ANGEL_MATK, 12);
                        break;
                    case Beginner.ICE_KNIGHT2:
                    case Phantom.ICE_KNIGHT:
                    case Mercedes.ICE_KNIGHT:
                    case DemonSlayer.ICE_KNIGHT:
                    case Citizen.ICE_KNIGHT:
                    case Legend.ICE_KNIGHT:
                        ret.statups.put(MapleBuffStat.ICE_SKILL, 1);
                        ret.info.put(MapleStatInfo.time, 2100000000);
                        break;
                    case Beginner.HIDDEN_POTENTIAL_EXPLORER:
                    case Evan.HIDDEN_POTENTIAL_HERO:
                    case Phantom.HIDDEN_POTENTIAL_HERO:
                    case Mercedes.HIDDEN_POTENTIAL_HERO:
                    case DemonSlayer.HIDDEN_POTENTIAL_RESISTANCE:
                    case Citizen.HIDDEN_POTENTIAL_RESISTANCE:
                    case Legend.HIDDEN_POTENTIAL_HERO:
                        ret.statups.put(MapleBuffStat.HIDDEN_POTENTIAL, 1);
                        break;
                    case Beginner.DECENT_MYSTIC_DOOR:
                    case Evan.DECENT_MYSTIC_DOOR:
                    case Phantom.DECENT_MYSTIC_DOOR:
                    case DemonSlayer.DECENT_MYSTIC_DOOR:
                    case Citizen.DECENT_MYSTIC_DOOR:
                    case Legend.DECENT_MYSTIC_DOOR:
                        ret.statups.put(MapleBuffStat.SOULARROW, ret.info.get(MapleStatInfo.x));
                        break;
                    case Beginner.BERSERK_FURY:
                    case Noblesse.BERSERK_FURY:
                    case Citizen.BERSERK_FURY:
                        ret.statups.put(MapleBuffStat.BERSERK_FURY, ret.info.get(MapleStatInfo.x));
                        break;
                    case Beginner.INVINCIBLE_BARRIER:
                    case Noblesse.INVINCIBLE_BARRIER:
                    case Legend.INVICIBLE_BARRIER:
                    case Citizen.INVINCIBLE_BARRIER:
                        ret.statups.put(MapleBuffStat.DIVINE_BODY, 1);
                        break;
                    case Beginner.RECOVERY:
                    case Noblesse.RECOVERY:
                    case Legend.RECOVERY:
                        ret.statups.put(MapleBuffStat.RECOVERY, ret.info.get(MapleStatInfo.x));
                        break;
                    case Citizen.INFILTRATE:
                    case DemonSlayer.INFILTRATE:
                    case Phantom.INFILTRATE: 
                    case Mercedes.INFILTRATE:
                            ret.statups.put(MapleBuffStat.INFILTRATE, ret.info.get(MapleStatInfo.x));
                        break;
                    case Beginner.DECENT_HYPER_BODY:
                    case Evan.DECENT_HYPER_BODY:
                    case Legend.DECENT_HYPER_BODY:
                    case Phantom.DECENT_HYPER_BODY:
                    case DemonSlayer.DECENT_HYPER_BODY:
                    case Citizen.DECENT_HYPER_BODY:
                        ret.statups.put(MapleBuffStat.MAXHP, ret.info.get(MapleStatInfo.x));
                        ret.statups.put(MapleBuffStat.MAXMP, ret.info.get(MapleStatInfo.x));
                        break;
                    case Beginner.DECENT_COMBAT_ORDERS:
                    case Evan.DECENT_COMBAT_ORDERS:
                    case Legend.DECENT_COMBAT_ORDERS:
                    case Phantom.DECENT_COMBAT_ORDERS:
                    case DemonSlayer.DECENT_COMBAT_ORDERS:
                    case Citizen.DECENT_COMBAT_ORDERS:
                        ret.statups.put(MapleBuffStat.COMBAT_ORDERS, ret.info.get(MapleStatInfo.x));
                        break;
                    case Beginner.DECENT_ADVANCE_BLESSING:
                    case Legend.DECENT_ADVANCE_BLESSING:
                    case Citizen.DECENT_ADVANCE_BLESSING:
                    case Phantom.DECENT_ADVANCE_BLESSING:
                    case Mercedes.DECENT_ADVANCE_BLESSING:
                    case DemonSlayer.DECENT_ADVANCE_BLESSING:
                    case Noblesse.DECENT_ADVANCE_BLESSING:   
                        ret.statups.put(MapleBuffStat.HOLY_SHIELD, 1);
                        break;
                    case Beginner.DECENT_SPEED_INFUSION:                   
                    case Legend.DECENT_SPEED_INFUSION:
                    case Citizen.DECENT_SPEED_INFUSION:
                    case Phantom.DECENT_SPEED_INFUSION:
                    case Mercedes.DECENT_SPEED_INFUSION:
                    case DemonSlayer.DECENT_SPEED_INFUSION:
                    case Noblesse.DECENT_SPEED_INFUSION: 
                        ret.statups.put(MapleBuffStat.SPEED_INFUSION, ret.info.get(MapleStatInfo.x));
                        break;
                    case Beginner.ICE_CHOP:
                        ret.monsterStatus.put(MonsterStatus.STUN, 1);
                        break;
                    case Beginner.ICE_SMASH:
                    case Beginner.ICE_CURSE:
                        ret.monsterStatus.put(MonsterStatus.FREEZE, 1);
                        ret.info.put(MapleStatInfo.time, ret.info.get(MapleStatInfo.time) * 2); // freezing skills are a little strange
                        break;
                    case Beginner.DECENT_SHARP_EYES:
                    case Legend.DECENT_SHARP_EYES:
                    case Citizen.DECENT_SHARP_EYES:
                    case Phantom.DECENT_SHARP_EYES:
                    case Mercedes.DECENT_SHARP_EYES:
                    case DemonSlayer.DECENT_SHARP_EYES:
                    case Noblesse.DECENT_SHARP_EYES:
                        ret.statups.put(MapleBuffStat.SHARP_EYES, (ret.info.get(MapleStatInfo.x) << 8) + ret.info.get(MapleStatInfo.y) + ret.info.get(MapleStatInfo.criticaldamageMax));
                        break;
                    case Beginner.SOARING:
                    case Beginner.SOARING2:
                        ret.info.put(MapleStatInfo.time, 2100000000);
                        ret.statups.put(MapleBuffStat.SOARING, 1);
                        break;   
                   case 2022746: //angel bless
                   case 2022747: //d.angel bless
                   case 2022823: // wabr
                        ret.statups.clear(); //no atk/matk
                        ret.statups.put(MapleBuffStat.PYRAMID_PQ, 1); //ITEM_EFFECT buff
                    break;
                default:
                    break;
            }
        }
        if (ret.isPoison()) {
            ret.monsterStatus.put(MonsterStatus.POISON, 1);
        }
        if (ret.isMorph() || ret.isPirateMorph()) {
            ret.statups.put(MapleBuffStat.MORPH, ret.getMorph());
        }

        return ret;
    }

    /**
     * @param applyto
     * @param obj
     * @param attack damage done by the skill
     */
    public final void applyPassive(final MapleCharacter applyto, final MapleMapObject obj) {
        if (makeChanceResult() && !GameConstants.isDemon(applyto.getJob())) { // demon can't heal mp
            switch (sourceid) {
                case FPWizard.MP_EATER:
                case ILWizard.MP_EATER:
                case Cleric.MP_EATER:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                    final MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.getStats().isBoss()) {
                        final int absorbMp = Math.min((int) (mob.getMobMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.getStat().setMp(applyto.getStat().getMp() + absorbMp, applyto);
                            applyto.getClient().getSession().write(EffectPacket.showOwnBuffEffect(sourceid, 1, applyto.getLevel(), level));
                            applyto.getMap().broadcastMessage(applyto, EffectPacket.showBuffeffect(applyto.getId(), sourceid, 1, applyto.getLevel(), level), false);
                        }
                    }
                    break;
            }
        }
    }

    public final boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, info.get(MapleStatInfo.time));
    }

    public final boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos, info.get(MapleStatInfo.time));
    }

    public final boolean applyTo(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary, final Point pos, int newDuration) {
        if (isHeal() && (applyfrom.getMapId() == 749040100 || applyto.getMapId() == 749040100)) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false; //z
        } else if ((isSoaring_Mount() && applyfrom.getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null) || (isSoaring_Normal() && !applyfrom.getMap().canSoar())) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        } else if (sourceid == DualBlade.MIRROR_TARGET && applyfrom.getBuffedValue(MapleBuffStat.SHADOWPARTNER) == null) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        } else if (sourceid == WildHunter.IRM && (applyfrom.getBuffedValue(MapleBuffStat.RAINING_MINES) == null || applyfrom.getBuffedValue(MapleBuffStat.SUMMON) != null || !applyfrom.canSummon())) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        } else if (isShadow() && applyfrom.getJob() / 100 % 10 != 4) { //pirate/shadow = dc
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        } else if (sourceid == WildHunter.RAINING && applyfrom.getMap().isTown()) {
            applyfrom.dropMessage(5, "You may not use this skill in towns.");
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        }
        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);

        final PlayerStats stat = applyto.getStat();
        if (primary) {
            if (info.get(MapleStatInfo.itemConNo) != 0 && !applyto.isClone() && !applyto.inPVP()) {
                if (!applyto.haveItem(info.get(MapleStatInfo.itemCon), info.get(MapleStatInfo.itemConNo), false, true)) {
                    applyto.getClient().getSession().write(CWvsContext.enableActions());
                    return false;
                }
                MapleInventoryManipulator.removeById(applyto.getClient(), GameConstants.getInventoryType(info.get(MapleStatInfo.itemCon)), info.get(MapleStatInfo.itemCon), info.get(MapleStatInfo.itemConNo), false, true);
            }
        } else if (!primary && isResurrection()) {
            hpchange = stat.getMaxHp();
            applyto.setStance(0); //TODO fix death bug, player doesnt spawn on other screen
        }
        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isHeroWill()) {
            applyto.dispelDebuffs();
        } else if (cureDebuffs.size() > 0) {
            for (final MapleDisease debuff : cureDebuffs) {
                applyfrom.dispelDebuff(debuff);
            }
        } else if (isMPRecovery()) {
            final int toDecreaseHP = ((stat.getMaxHp() / 100) * 10);
            if (stat.getHp() > toDecreaseHP) {
                hpchange += -toDecreaseHP; // -10% of max HP
                mpchange += ((toDecreaseHP / 100) * getY());
            } else {
                hpchange = stat.getHp() == 1 ? 0 : stat.getHp() - 1;
            }
        }
        final Map<MapleStat, Integer> hpmpupdate = new EnumMap<>(MapleStat.class);
        if (hpchange != 0) {
            if (hpchange < 0 && (-hpchange) > stat.getHp() && !applyto.hasDisease(MapleDisease.ZOMBIFY)) {
                applyto.getClient().getSession().write(CWvsContext.enableActions());
                return false;
            }
            stat.setHp(stat.getHp() + hpchange, applyto);
        }
        if (mpchange != 0) {
            if (mpchange < 0 && (-mpchange) > stat.getMp()) {
                applyto.getClient().getSession().write(CWvsContext.enableActions());
                return false;
            }
            //short converting needs math.min cuz of overflow
            if ((mpchange < 0 && GameConstants.isDemon(applyto.getJob())) || !GameConstants.isDemon(applyto.getJob())) { // heal
                stat.setMp(stat.getMp() + mpchange, applyto);
            }
            hpmpupdate.put(MapleStat.MP, Integer.valueOf(stat.getMp()));
        }
        hpmpupdate.put(MapleStat.HP, Integer.valueOf(stat.getHp()));

        applyto.getClient().getSession().write(CWvsContext.updatePlayerStats(hpmpupdate, true, applyto));
        if (expinc != 0) {
            applyto.gainExp(expinc, true, true, false);
            applyto.getClient().getSession().write(EffectPacket.showForeignEffect(20));
        } else if (sourceid / 10000 == 238) {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final int mobid = ii.getCardMobId(sourceid);
            if (mobid > 0) {
                final boolean done = applyto.getMonsterBook().monsterCaught(applyto.getClient(), mobid, MapleLifeFactory.getMonsterStats(mobid).getName());
                applyto.getClient().getSession().write(CWvsContext.getCard(done ? sourceid : 0, 1));
            }
        } else if (isReturnScroll()) {
            applyReturnScroll(applyto);
        } else if (useLevel > 0 && !skill) {
            applyto.setExtractor(new MapleExtractor(applyto, sourceid, useLevel * 50, 1440)); //no clue about time left
            applyto.getMap().spawnExtractor(applyto.getExtractor());
        } else if (isMistEruption()) {
            int i = info.get(MapleStatInfo.y);
            for (MapleMist m : applyto.getMap().getAllMistsThreadsafe()) {
                if (m.getOwnerId() == applyto.getId() && m.getSourceSkill().getId() == FPMage.POISON_MIST) {
                    if (m.getSchedule() != null) {
                        m.getSchedule().cancel(false);
                        m.setSchedule(null);
                    }
                    if (m.getPoisonSchedule() != null) {
                        m.getPoisonSchedule().cancel(false);
                        m.setPoisonSchedule(null);
                    }
                    applyto.getMap().broadcastMessage(CField.removeMist(m.getObjectId(), true));
                    applyto.getMap().removeMapObject(m);

                    i--;
                    if (i <= 0) {
                        break;
                    }
                }
            }
        } else if (cosmetic > 0) {
            if (cosmetic >= 30000) {
                applyto.setHair(cosmetic);
                applyto.updateSingleStat(MapleStat.HAIR, cosmetic);
            } else if (cosmetic >= 20000) {
                applyto.setFace(cosmetic);
                applyto.updateSingleStat(MapleStat.FACE, cosmetic);
            } else if (cosmetic < 100) {
                applyto.setSkinColor((byte) cosmetic);
                applyto.updateSingleStat(MapleStat.SKIN, cosmetic);
            }
            applyto.equipChanged();
        } else if (bs > 0) {
            if (!applyto.inPVP()) {
                return false;
            }
            final int x = Integer.parseInt(applyto.getEventInstance().getProperty(String.valueOf(applyto.getId())));
            applyto.getEventInstance().setProperty(String.valueOf(applyto.getId()), String.valueOf(x + bs));
            applyto.getClient().getSession().write(CField.getPVPScore(x + bs, false));
        } else if (info.get(MapleStatInfo.iceGageCon) > 0) {
            if (!applyto.inPVP()) {
                return false;
            }
            final int x = Integer.parseInt(applyto.getEventInstance().getProperty("icegage"));
            if (x < info.get(MapleStatInfo.iceGageCon)) {
                return false;
            }
            applyto.getEventInstance().setProperty("icegage", String.valueOf(x - info.get(MapleStatInfo.iceGageCon)));
            applyto.getClient().getSession().write(CField.getPVPIceGage(x - info.get(MapleStatInfo.iceGageCon)));
            applyto.applyIceGage(x - info.get(MapleStatInfo.iceGageCon));
        } else if (recipe > 0) {
            if (applyto.getSkillLevel(recipe) > 0 || applyto.getProfessionLevel((recipe / 10000) * 10000) < reqSkillLevel) {
                return false;
            }
            applyto.changeSingleSkillLevel(SkillFactory.getCraft(recipe), Integer.MAX_VALUE, recipeUseCount, (long) (recipeValidDay > 0 ? (System.currentTimeMillis() + recipeValidDay * 24L * 60 * 60 * 1000) : -1L));
        } else if (isComboRecharge()) {
            applyto.setCombo((short) Math.min(30000, applyto.getCombo() + info.get(MapleStatInfo.y)));
            applyto.setLastCombo(System.currentTimeMillis());
            applyto.getClient().getSession().write(CField.rechargeCombo(applyto.getCombo()));
            SkillFactory.getSkill(Aran.COMBO_ABILITY).getEffect(10).applyComboBuff(applyto, applyto.getCombo());
        } else if (isDragonBlink()) {
            final MaplePortal portal = applyto.getMap().getPortal(Randomizer.nextInt(applyto.getMap().getPortals().size()));
            if (portal != null) {
                applyto.getClient().getSession().write(CField.dragonBlink(portal.getId()));
                applyto.getMap().movePlayer(applyto, portal.getPosition());
                applyto.checkFollow();
            }
        } else if (isSpiritClaw() && !applyto.isClone()) {
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            boolean itemz = false;
            for (int i = 0; i < use.getSlotLimit(); i++) { // impose order...
                Item item = use.getItem((byte) i);
                if (item != null) {
                    if (GameConstants.isRechargable(item.getItemId()) && item.getQuantity() >= 100) {
                        MapleInventoryManipulator.removeFromSlot(applyto.getClient(), MapleInventoryType.USE, (short) i, (short) 100, false, true);
                        itemz = true;
                        break;
                    }
                }
            }
            if (!itemz) {
                return false;
            }
        } else if (isSpiritBlast() && !applyto.isClone()) {
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            boolean itemz = false;
            for (int i = 0; i < use.getSlotLimit(); i++) { // impose order...
                Item item = use.getItem((byte) i);
                if (item != null) {
                    if (GameConstants.isBullet(item.getItemId()) && item.getQuantity() >= 100) {
                        MapleInventoryManipulator.removeFromSlot(applyto.getClient(), MapleInventoryType.USE, (short) i, (short) 100, false, true);
                        itemz = true;
                        break;
                    }
                }
            }
            if (!itemz) {
                return false;
            }
        } else if (cp != 0 && applyto.getCarnivalParty() != null) {
            applyto.getCarnivalParty().addCP(applyto, cp);
            applyto.CPUpdate(false, applyto.getAvailableCP(), applyto.getTotalCP(), 0);
            for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                chr.CPUpdate(true, applyto.getCarnivalParty().getAvailableCP(), applyto.getCarnivalParty().getTotalCP(), applyto.getCarnivalParty().getTeam());
            }
        } else if (nuffSkill != 0 && applyto.getParty() != null) {
            final MCSkill skil = MapleCarnivalFactory.getInstance().getSkill(nuffSkill);
            if (skil != null) {
                final MapleDisease dis = skil.getDisease();
                for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                    if (applyto.getParty() == null || chr.getParty() == null || (chr.getParty().getId() != applyto.getParty().getId())) {
                        if (skil.targetsAll || Randomizer.nextBoolean()) {
                            if (dis == null) {
                                chr.dispel();
                            } else if (skil.getSkill() == null) {
                                chr.giveDebuff(dis, 1, 30000, dis.getDisease(), 1);
                            } else {
                                chr.giveDebuff(dis, skil.getSkill());
                            }
                            if (!skil.targetsAll) {
                                break;
                            }
                        }
                    }
                }
            }
        } else if ((effectedOnEnemy > 0 || effectedOnAlly > 0) && primary && applyto.inPVP()) {
            final int eventType = Integer.parseInt(applyto.getEventInstance().getProperty("type"));
            if (eventType > 0 || effectedOnEnemy > 0) {
                for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                    if (chr.getId() != applyto.getId() && (effectedOnAlly > 0 ? (chr.getTeam() == applyto.getTeam()) : (chr.getTeam() != applyto.getTeam() || eventType == 0))) {
                        applyTo(applyto, chr, false, pos, newDuration);
                    }
                }
            }
        } else if (mobSkill > 0 && mobSkillLevel > 0 && primary && applyto.inPVP()) {
            if (effectedOnEnemy > 0) {
                final int eventType = Integer.parseInt(applyto.getEventInstance().getProperty("type"));
                for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                    if (chr.getId() != applyto.getId() && (chr.getTeam() != applyto.getTeam() || eventType == 0)) {
                        chr.disease(mobSkill, mobSkillLevel);
                    }
                }
            } else {
                if (sourceid == 2910000 || sourceid == 2910001) { //red flag
                    applyto.getClient().getSession().write(EffectPacket.showOwnBuffEffect(sourceid, 13, applyto.getLevel(), level));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.showBuffeffect(applyto.getId(), sourceid, 13, applyto.getLevel(), level), false);

                    applyto.getClient().getSession().write(EffectPacket.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Effect", 0, 0));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Effect", 0, 0), false);
                    if (applyto.getTeam() == (sourceid - 2910000)) { //restore duh flag
                        if (sourceid == 2910000) {
                            applyto.getEventInstance().broadcastPlayerMsg(-7, "The Red Team's flag has been restored.");
                        } else {
                            applyto.getEventInstance().broadcastPlayerMsg(-7, "The Blue Team's flag has been restored.");
                        }
                        applyto.getMap().spawnAutoDrop(sourceid, applyto.getMap().getGuardians().get(sourceid - 2910000).left);
                    } else {
                        applyto.disease(mobSkill, mobSkillLevel);
                        if (sourceid == 2910000) {
                            applyto.getEventInstance().setProperty("redflag", String.valueOf(applyto.getId()));
                            applyto.getEventInstance().broadcastPlayerMsg(-7, "The Red Team's flag has been captured!");
                            applyto.getClient().getSession().write(EffectPacket.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Tail/Red", 600000, 0));
                            applyto.getMap().broadcastMessage(applyto, EffectPacket.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Tail/Red", 600000, 0), false);
                        } else {
                            applyto.getEventInstance().setProperty("blueflag", String.valueOf(applyto.getId()));
                            applyto.getEventInstance().broadcastPlayerMsg(-7, "The Blue Team's flag has been captured!");
                            applyto.getClient().getSession().write(EffectPacket.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Tail/Blue", 600000, 0));
                            applyto.getMap().broadcastMessage(applyto, EffectPacket.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Tail/Blue", 600000, 0), false);
                        }
                    }
                } else {
                    applyto.disease(mobSkill, mobSkillLevel);
                }
            }
        } else if (randomPickup != null && randomPickup.size() > 0) {
            MapleItemInformationProvider.getInstance().getItemEffect(randomPickup.get(Randomizer.nextInt(randomPickup.size()))).applyTo(applyto);
        }
        for (Entry<MapleTraitType, Integer> t : traits.entrySet()) {
            applyto.getTrait(t.getKey()).addExp(t.getValue(), applyto);
        }
        final SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null && (sourceid != BattleMage.REAPER || (applyfrom.getBuffedValue(MapleBuffStat.REAPER) != null && !primary)) && !applyto.isClone()) {
            int summId = sourceid;
            if (sourceid == Ranger.PUPPET) {
                final Skill elite = SkillFactory.getSkill(Bowmaster.ELITE_PUPPET);
                if (applyfrom.getTotalSkillLevel(elite) > 0) {
                    return elite.getEffect(applyfrom.getTotalSkillLevel(elite)).applyTo(applyfrom, applyto, primary, pos, newDuration);
                }
            } else if (sourceid == Sniper.PUPPET) {
                final Skill elite = SkillFactory.getSkill(Marksman.ELITE_PUPPET);
                if (applyfrom.getTotalSkillLevel(elite) > 0) {
                    return elite.getEffect(applyfrom.getTotalSkillLevel(elite)).applyTo(applyfrom, applyto, primary, pos, newDuration);
                }
            }
            final MapleSummon tosummon = new MapleSummon(applyfrom, summId, getLevel(), new Point(pos == null ? applyfrom.getTruePosition() : pos), summonMovementType);
            if (!tosummon.isPuppet()) {
                applyfrom.getCheatTracker().resetSummonAttack();
            }
            applyfrom.cancelEffect(this, true, -1, statups);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(tosummon);
            tosummon.addHP(info.get(MapleStatInfo.x).shortValue());
            if (isBeholder()) {
                tosummon.addHP((short) 1);
            } else if (sourceid == DualBlade.MIRROR_TARGET) { // cancel a buff, when another one is used.
                applyfrom.cancelEffectFromBuffStat(MapleBuffStat.SHADOWPARTNER);
            } else if (sourceid == BattleMage.REAPER) {
                return true; //no buff
            } else if (sourceid == Mechanic.ROCK_N_SHOCK) {
                List<Integer> count = new ArrayList<>();
                final List<MapleSummon> ss = applyfrom.getSummonsReadLock();
                try {
                    for (MapleSummon s : ss) {
                        if (s.getSkill() == sourceid) {
                            count.add(s.getObjectId());
                        }
                    }
                } finally {
                    applyfrom.unlockSummonsReadLock();
                }
                if (count.size() != 3) {
                    return true; //no buff until 3
                }
                applyfrom.getClient().getSession().write(CField.skillCooldown(sourceid, getCooldown(applyfrom)));
                applyfrom.addCooldown(sourceid, System.currentTimeMillis(), getCooldown(applyfrom) * 1000);
                applyfrom.getMap().broadcastMessage(CField.teslaTriangle(applyfrom.getId(), count.get(0), count.get(1), count.get(2)));
            } else if (sourceid == Mechanic.SG88) {
                applyfrom.getClient().getSession().write(CWvsContext.enableActions()); //doubt we need this at all
            }
        } else if (isMechDoor()) {
            int newId = 0;
            boolean applyBuff = false;
            if (applyto.getMechDoors().size() >= 2) {
                final MechDoor remove = applyto.getMechDoors().remove(0);
                newId = remove.getId();
                applyto.getMap().broadcastMessage(CField.removeMechDoor(remove, true));
                applyto.getMap().removeMapObject(remove);
            } else {
                for (MechDoor d : applyto.getMechDoors()) {
                    if (d.getId() == newId) {
                        applyBuff = true;
                        newId = 1;
                        break;
                    }
                }
            }
            final MechDoor door = new MechDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), newId);
            applyto.getMap().spawnMechDoor(door);
            applyto.addMechDoor(door);
            applyto.getClient().getSession().write(CWvsContext.mechPortal(door.getTruePosition()));
            if (!applyBuff) {
                return true; //do not apply buff until 2 doors spawned
            }
        }
        if (primary && availableMap != null) {
            for (Pair<Integer, Integer> e : availableMap) {
                if (applyto.getMapId() < e.left || applyto.getMapId() > e.right) {
                    applyto.getClient().getSession().write(CWvsContext.enableActions());
                    return true;
                }
            }
        }
        if (overTime && !isEnergyCharge()) {
            applyBuffEffect(applyfrom, applyto, primary, newDuration);
        }
        if (skill) {
            removeMonsterBuff(applyfrom);
        }
        if (primary) {
            if ((overTime || isHeal()) && !isEnergyCharge()) {
                applyBuff(applyfrom, newDuration);
            }
            if (isMonsterBuff()) {
                applyMonsterBuff(applyfrom);
            }
        }
        if (isMagicDoor()) { // Magic Door
            MapleDoor door = new MapleDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), sourceid); // Current Map door
            if (door.getTownPortal() != null) {

                applyto.getMap().spawnDoor(door);
                applyto.addDoor(door);

                MapleDoor townDoor = new MapleDoor(door); // Town door
                applyto.addDoor(townDoor);
                door.getTown().spawnDoor(townDoor);

                if (applyto.getParty() != null) { // update town doors
                    applyto.silentPartyUpdate();
                }
            } else {
                applyto.dropMessage(5, "You may not spawn a door because all doors in the town are taken.");
            }
        } else if (isMist()) {
            final Rectangle bounds = calculateBoundingBox(pos != null ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
            final MapleMist mist = new MapleMist(bounds, applyfrom, this);
            applyfrom.getMap().spawnMist(mist, getDuration(), false);

        } else if (isTimeLeap()) { // Time Leap
            for (MapleCoolDownValueHolder i : applyto.getCooldowns()) {
                if (i.skillId != Buccaneer.TIME_LEAP) {
                    applyto.removeCooldown(i.skillId);
                    applyto.getClient().getSession().write(CField.skillCooldown(i.skillId, 0));
                }
            }
        } else {
            for (WeakReference<MapleCharacter> chrz : applyto.getClones()) {
                if (chrz.get() != null) {
                    applyTo(chrz.get(), chrz.get(), primary, pos, newDuration);
                }
            }
        }
        if (fatigueChange != 0 && applyto.getSummonedFamiliar() != null && (familiars == null || familiars.contains(applyto.getSummonedFamiliar().getFamiliar()))) {
            applyto.getSummonedFamiliar().addFatigue(applyto, fatigueChange);
        }
        if (rewardMeso != 0) {
            applyto.gainMeso(rewardMeso, false);
        }
        if (rewardItem != null && totalprob > 0) {
            for (Triple<Integer, Integer, Integer> reward : rewardItem) {
                if (MapleInventoryManipulator.checkSpace(applyto.getClient(), reward.left, reward.mid, "") && reward.right > 0 && Randomizer.nextInt(totalprob) < reward.right) { // Total prob
                    if (GameConstants.getInventoryType(reward.left) == MapleInventoryType.EQUIP) {
                        final Item item = MapleItemInformationProvider.getInstance().getEquipById(reward.left);
                        item.setGMLog("Reward item (effect): " + sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.addbyItem(applyto.getClient(), item);
                    } else {
                        MapleInventoryManipulator.addById(applyto.getClient(), reward.left, reward.mid.shortValue(), "Reward item (effect): " + sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                    }
                }
            }
        }
        if (familiarTarget == 2 && applyfrom.getParty() != null && primary) { //to party
            for (MaplePartyCharacter mpc : applyfrom.getParty().getMembers()) {
                if (mpc.getId() != applyfrom.getId() && mpc.getChannel() == applyfrom.getClient().getChannel() && mpc.getMapid() == applyfrom.getMapId() && mpc.isOnline()) {
                    MapleCharacter mc = applyfrom.getMap().getCharacterById(mpc.getId());
                    if (mc != null) {
                        applyTo(applyfrom, mc, false, null, newDuration);
                    }
                }
            }
        } else if (familiarTarget == 3 && primary) {
            for (MapleCharacter mc : applyfrom.getMap().getCharactersThreadsafe()) {
                if (mc.getId() != applyfrom.getId()) {
                    applyTo(applyfrom, mc, false, null, newDuration);
                }
            }
        }
        return true;
    }

    public final boolean applyReturnScroll(final MapleCharacter applyto) {
        if (moveTo != -1) {
            if (applyto.getMap().getReturnMapId() != applyto.getMapId() || sourceid == 2031010 || sourceid == 2030021) {
                MapleMap target;
                if (moveTo == 999999999) {
                    target = applyto.getMap().getReturnMap();
                } else {
                    target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
                    if (target.getId() / 10000000 != 60 && applyto.getMapId() / 10000000 != 61) {
                        if (target.getId() / 10000000 != 21 && applyto.getMapId() / 10000000 != 20) {
                            if (target.getId() / 10000000 != applyto.getMapId() / 10000000) {
                                return false;
                            }
                        }
                    }
                }
                applyto.changeMap(target, target.getPortal(0));
                return true;
            }
        }
        return false;
    }

    private boolean isSoulStone() {
        return skill && sourceid == Evan.SOUL_STONE || sourceid == Phantom.FINAL_FEINT;
    }
    

    private void applyBuff(final MapleCharacter applyfrom, int newDuration) {
        if (isSoulStone() && sourceid != Phantom.FINAL_FEINT) {
            if (applyfrom.getParty() != null) {
                int membrs = 0;
                for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
                    if (!chr.isClone() && chr.getParty() != null && chr.getParty().getId() == applyfrom.getParty().getId() && chr.isAlive()) {
                        membrs++;
                    }
                }
                List<MapleCharacter> awarded = new ArrayList<>();
                while (awarded.size() < Math.min(membrs, info.get(MapleStatInfo.y))) {
                    for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
                        if (chr != null && !chr.isClone() && chr.isAlive() && chr.getParty() != null && chr.getParty().getId() == applyfrom.getParty().getId() && !awarded.contains(chr) && Randomizer.nextInt(info.get(MapleStatInfo.y)) == 0) {
                            awarded.add(chr);
                        }
                    }
                }
                for (MapleCharacter chr : awarded) {
                    applyTo(applyfrom, chr, false, null, newDuration);
                    chr.getClient().getSession().write(EffectPacket.showOwnBuffEffect(sourceid, 2, applyfrom.getLevel(), level));
                    chr.getMap().broadcastMessage(chr, EffectPacket.showBuffeffect(chr.getId(), sourceid, 2, applyfrom.getLevel(), level), false);
                }
            }
        } else if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff() || applyfrom.inPVP())) {
            final Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
            final List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));

            for (final MapleMapObject affectedmo : affecteds) {
                final MapleCharacter affected = (MapleCharacter) affectedmo;

                if (affected.getId() != applyfrom.getId() && (isGmBuff() || (applyfrom.inPVP() && affected.getTeam() == applyfrom.getTeam() && Integer.parseInt(applyfrom.getEventInstance().getProperty("type")) != 0) || (applyfrom.getParty() != null && affected.getParty() != null && applyfrom.getParty().getId() == affected.getParty().getId()))) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
                        applyTo(applyfrom, affected, false, null, newDuration);
                        affected.getClient().getSession().write(EffectPacket.showOwnBuffEffect(sourceid, 2, applyfrom.getLevel(), level));
                        affected.getMap().broadcastMessage(affected, EffectPacket.showBuffeffect(affected.getId(), sourceid, 2, applyfrom.getLevel(), level), false);
                    }
                    if (isTimeLeap()) {
                        for (MapleCoolDownValueHolder i : affected.getCooldowns()) {
                            if (i.skillId != Buccaneer.TIME_LEAP) {
                                affected.removeCooldown(i.skillId);
                                affected.getClient().getSession().write(CField.skillCooldown(i.skillId, 0));
                            }
                        }
                    }
                }
            }
        }
    }

    private void removeMonsterBuff(final MapleCharacter applyfrom) {
        List<MonsterStatus> cancel = new ArrayList<>();
        switch (sourceid) {
            case Crusader.ARMOR_CRASH:
            case DragonKnight.POWER_CRASH:
            case WhiteKnight.MAGIC_CRASH:
                cancel.add(MonsterStatus.WEAPON_DEFENSE_UP);
                cancel.add(MonsterStatus.MAGIC_DEFENSE_UP);
                cancel.add(MonsterStatus.WEAPON_ATTACK_UP);
                cancel.add(MonsterStatus.MAGIC_ATTACK_UP);
                cancel.add(MonsterStatus.DAMAGE_IMMUNITY); // I think this is damage reflect
                break;
            default:
                return;
        }
        final Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        final List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        int i = 0;

        for (final MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (MonsterStatus stat : cancel) {
                    ((MapleMonster) mo).cancelStatus(stat);
                }
            }
            i++;
            if (i >= info.get(MapleStatInfo.mobCount)) {
                break;
            }
        }
    }

    public final void applyMonsterBuff(final MapleCharacter applyfrom) {
        final Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        final boolean pvp = applyfrom.inPVP();
        final MapleMapObjectType objType = pvp ? MapleMapObjectType.PLAYER : MapleMapObjectType.MONSTER;
        final List<MapleMapObject> affected = sourceid == Mechanic.BOT_EX7 ? applyfrom.getMap().getMapObjectsInRange(applyfrom.getTruePosition(), Double.POSITIVE_INFINITY, Arrays.asList(objType)) : applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(objType));
        int i = 0;

        for (final MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (Map.Entry<MonsterStatus, Integer> stat : getMonsterStati().entrySet()) {
                    if (pvp) {
                        MapleCharacter chr = (MapleCharacter) mo;
                        MapleDisease d = MonsterStatus.getLinkedDisease(stat.getKey());
                        if (d != null) {
                            chr.giveDebuff(d, stat.getValue(), getDuration(), d.getDisease(), 1);
                        }
                    } else {
                        MapleMonster mons = (MapleMonster) mo;
                        if (sourceid == Mechanic.BOT_EX7 && mons.getStats().isBoss()) {
                            break;
                        }
                        mons.applyStatus(applyfrom, new MonsterStatusEffect(stat.getKey(), stat.getValue(), sourceid, null, false), isPoison(), isSubTime(sourceid) ? getSubTime() : getDuration(), true, this);
                    }
                }
                if (pvp && skill) {
                    MapleCharacter chr = (MapleCharacter) mo;
                    handleExtraPVP(applyfrom, chr);
                }
            }
            i++;
            if (i >= info.get(MapleStatInfo.mobCount) && sourceid != Mechanic.BOT_EX7) {
                break;
            }
        }
    }

    public final boolean isSubTime(final int source) {
        switch (source) {
            case Page.THREATEN:
            case Mercedes.ELEMENTAL_KNIGHTS1:
            case Mercedes.ELEMENTAL_KNIGHTS2:
            case Mercedes.ELEMENTAL_KNIGHTS3:
            case DemonSlayer.VENGEANCE:
            case DemonSlayer.DEMON_CRY:
            case DemonSlayer.DARK_META:
                return true;
        }
        return false;
    }

    public final void handleExtraPVP(MapleCharacter applyfrom, MapleCharacter chr) {
        if (sourceid == Priest.DOOM || sourceid == Buccaneer.SNATCH || sourceid == Page.THREATEN || (GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 104)) {
            final long starttime = System.currentTimeMillis();

            final int localsourceid = sourceid == Buccaneer.SNATCH ? 90002000 : sourceid; // what fucking skill is that?
            
            final Map<MapleBuffStat, Integer> localstatups = new EnumMap<>(MapleBuffStat.class);
            if (sourceid == Priest.DOOM) {
                localstatups.put(MapleBuffStat.MORPH, 7);
            } else if (sourceid == Page.THREATEN) {
                localstatups.put(MapleBuffStat.THREATEN_PVP, (int) level);
            } else if (sourceid == Buccaneer.SNATCH) {
                localstatups.put(MapleBuffStat.SNATCH, 1);
            } else {
                localstatups.put(MapleBuffStat.MORPH, info.get(MapleStatInfo.x));
            }
            chr.getClient().getSession().write(BuffPacket.giveBuff(localsourceid, getDuration(), localstatups, this));
            chr.registerEffect(this, starttime, BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime, localstatups), isSubTime(sourceid) ? getSubTime() : getDuration()), localstatups, false, getDuration(), applyfrom.getId());
        }
    }

    public final Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft) {
        return calculateBoundingBox(posFrom, facingLeft, lt, rb, info.get(MapleStatInfo.range));
    }

    public final Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft, int addedRange) {
        return calculateBoundingBox(posFrom, facingLeft, lt, rb, info.get(MapleStatInfo.range) + addedRange);
    }

    public static Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft, final Point lt, final Point rb, final int range) {
        if (lt == null || rb == null) {
            return new Rectangle((facingLeft ? (-200 - range) : 0) + posFrom.x, (-100 - range) + posFrom.y, 200 + range, 100 + range);
        }
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(lt.x * -1 + posFrom.x + range, rb.y + posFrom.y);
            mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
        }
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    public final double getMaxDistanceSq() { //lt = infront of you, rb = behind you; not gonna distanceSq the two points since this is in relative to player position which is (0,0) and not both directions, just one
        final int maxX = Math.max(Math.abs(lt == null ? 0 : lt.x), Math.abs(rb == null ? 0 : rb.x));
        final int maxY = Math.max(Math.abs(lt == null ? 0 : lt.y), Math.abs(rb == null ? 0 : rb.y));
        return (maxX * maxX) + (maxY * maxY);
    }

    public final void setDuration(int d) {
        this.info.put(MapleStatInfo.time, d);
    }

    public final void silentApplyBuff(final MapleCharacter chr, final long starttime, final int localDuration, final Map<MapleBuffStat, Integer> statup, final int cid) {
        chr.registerEffect(this, starttime, BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime, statup),
                ((starttime + localDuration) - System.currentTimeMillis())), statup, true, localDuration, cid);

        final SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            final MapleSummon tosummon = new MapleSummon(chr, this, chr.getTruePosition(), summonMovementType);
            if (!tosummon.isPuppet()) {
                chr.getCheatTracker().resetSummonAttack();
                chr.getMap().spawnSummon(tosummon);
                chr.addSummon(tosummon);
                tosummon.addHP(info.get(MapleStatInfo.x).shortValue());
                if (isBeholder()) {
                    tosummon.addHP((short) 1);
                }
            }
        }
    }

    public final void applyComboBuff(final MapleCharacter applyto, short combo) {
        final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
        stat.put(MapleBuffStat.ARAN_COMBO, (int) combo);
        applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, 99999, stat, this)); // Hackish timing, todo find out

        final long starttime = System.currentTimeMillis();
//	final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
//	final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + 99999) - System.currentTimeMillis()));
        applyto.registerEffect(this, starttime, null, applyto.getId());
    }

    public final void applyEnergyBuff(final MapleCharacter applyto, final boolean infinity, int targets) {
        final long starttime = System.currentTimeMillis();
        if (infinity) {
            applyto.getClient().getSession().write(BuffPacket.giveEnergyChargeTest(0, info.get(MapleStatInfo.time) / 1000, targets));
            applyto.registerEffect(this, starttime, null, applyto.getId());
        } else {
            final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
            stat.put(MapleBuffStat.ENERGY_CHARGE, 10000);
            applyto.cancelEffect(this, true, -1, stat);
            applyto.getMap().broadcastMessage(applyto, BuffPacket.giveEnergyChargeTest(applyto.getId(), 10000, info.get(MapleStatInfo.time) / 1000), false);
            final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime, stat);
            final ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, ((starttime + info.get(MapleStatInfo.time)) - System.currentTimeMillis()));
            applyto.registerEffect(this, starttime, schedule, stat, false, info.get(MapleStatInfo.time), applyto.getId());

        }
    }

    private void applyBuffEffect(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary, final int newDuration) {
        int localDuration = newDuration;
        if (primary) {
            localDuration = Math.max(newDuration, alchemistModifyVal(applyfrom, localDuration, false));
        }
        Map<MapleBuffStat, Integer> localstatups = statups, maskedStatups = null;
        boolean normal = true, showEffect = primary;
        int maskedDuration = 0;
        switch (sourceid) {
            case Shadower.SHADOWER_INSTINCT: {
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.ANGEL_ATK, info.get(MapleStatInfo.x) + (info.get(MapleStatInfo.kp) * applyfrom.currentBattleshipHP()));
                applyfrom.setBattleshipHP(0);
                applyfrom.refreshBattleshipHP();
                break;
            }
            case CannonShooter.BARREL_ROULETTE: {
                final int zz = Randomizer.nextInt(4) + 1;
                applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto.getId(), sourceid, zz, -1, (int)level), false);
                applyto.getClient().getSession().write(CField.EffectPacket.showOwnDiceEffect(sourceid, zz, -1, (int)level));
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.BARREL_ROLL, zz);
                break;
            }
            case Outlaw.ALL_ABOARD1:
            case Outlaw.ALL_ABOARD2:
            case Outlaw.ALL_ABOARD3: {
                if (applyfrom.getTotalSkillLevel(Corsair.AHOY) > 0) {
                    SkillFactory.getSkill(Corsair.AHOY).getEffect(applyfrom.getTotalSkillLevel(Corsair.AHOY)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                }
                break;
            }
            case Mechanic.DICE:
            case ThunderBreaker.DICE:
            case Marauder.DICE:
            case Marauder.DICE2:
            case Outlaw.DICE:
            case CannonShooter.DIE:
            case Jett.DICE:
            case Outlaw.DICE2:{
                final int zz = Randomizer.nextInt(6) + 1;
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), sourceid, zz, -1, level), false);
                applyto.getClient().getSession().write(EffectPacket.showOwnDiceEffect(sourceid, zz, -1, level));
                if (zz <= 1) {
                    return;
                }
                localstatups = new EnumMap<>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.DICE_ROLL, zz);
                applyto.getClient().getSession().write(BuffPacket.giveDice(zz, sourceid, localDuration, localstatups));
                normal = false;
                showEffect = false;
                break;
            }
            case Jett.DOUBLE_DOWN:
            case Buccaneer.DOUBLE_DOWN:
            case Corsair.DOUBLE_DOWN:
            case CannonShooter.DOUBLE_DOWN: {
                final int zz = Randomizer.nextInt(6) + 1;
                final int zz2 = makeChanceResult() ? (Randomizer.nextInt(6) + 1) : 0;
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), sourceid, zz, zz2 > 0 ? -1 : 0, level), false);
                applyto.getClient().getSession().write(EffectPacket.showOwnDiceEffect(sourceid, zz, zz2 > 0 ? -1 : 0, level));
                if (zz <= 1 && zz2 <= 1) {
                    return;
                }
                final int buffid = zz == zz2 ? (zz * 100) : (zz <= 1 ? zz2 : (zz2 <= 1 ? zz : (zz * 10 + zz2)));
                if (buffid >= 100) { //just because of animation lol
                    applyto.dropMessage(-6, "[Double Lucky Dice] You have rolled a Double Down! (" + (buffid / 100) + ")");
                } else if (buffid >= 10) {
                    applyto.dropMessage(-6, "[Double Lucky Dice] You have rolled two dice. (" + (buffid / 10) + " and " + (buffid % 10) + ")");
                }
                localstatups = new EnumMap<>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.DICE_ROLL, buffid);
                applyto.getClient().getSession().write(BuffPacket.giveDice(zz, sourceid, localDuration, localstatups));
                normal = false;
                showEffect = false;
                break;
            }
            case Phantom.JUDGEMENT_DRAW1:
            case Phantom.JUDGEMENT_DRAW2:
                 int zz = Randomizer.nextInt(this.sourceid == Phantom.JUDGEMENT_DRAW1 ? 2 : 5) + 1;
                 int skillid = Phantom.CARTE_BLANCHE;
                   if (applyto.getSkillLevel(Phantom.CARTE_NOIRE) > 0) {
                            skillid = Phantom.CARTE_NOIRE;
                   }
                 applyto.setCardStack((byte)0);
                 applyto.resetRunningStack();
                 applyto.addRunningStack(skillid == Phantom.CARTE_BLANCHE ? 5 : 10);
                 applyto.getMap().broadcastMessage(applyto, CField.gainCardStack(applyto.getId(), applyto.getRunningStack(), skillid == Phantom.CARTE_NOIRE ? 2 : 1, skillid, 0, skillid == Phantom.CARTE_BLANCHE ? 5 : 10), true);
                 applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto.getId(), this.sourceid, zz, -1, this.level), false);
                 applyto.getClient().getSession().write(CField.EffectPacket.showOwnDiceEffect(this.sourceid, zz, -1, this.level));
                 localstatups = new EnumMap(MapleBuffStat.class);
                 localstatups.put(MapleBuffStat.JUDGMENT_DRAW, Integer.valueOf(zz));
                     if (zz == 5) {
                            localstatups.put(MapleBuffStat.ABSORB_DAMAGE_HP, this.info.get(MapleStatInfo.z));
                     }
                 applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                 normal = false;
                 showEffect = false;
                   break;
            case WildHunter.JAGUAR: {
                applyto.clearLinkMid();
                MapleBuffStat theBuff = null;
                int theStat = info.get(MapleStatInfo.y);
                switch (Randomizer.nextInt(6)) {
                    case 0:
                        theBuff = MapleBuffStat.CRITICAL_RATE_BUFF;
                        break;
                    case 1:
                        theBuff = MapleBuffStat.MP_BUFF;
                        break;
                    case 2:
                        theBuff = MapleBuffStat.DAMAGE_TAKEN_BUFF;
                        theStat = info.get(MapleStatInfo.x);
                        break;
                    case 3:
                        theBuff = MapleBuffStat.DODGE_CHANGE_BUFF;
                        theStat = info.get(MapleStatInfo.x);
                        break;
                    case 4:
                        theBuff = MapleBuffStat.DAMAGE_BUFF;
                        break;
                    case 5:
                        theBuff = MapleBuffStat.ATTACK_BUFF;
                        break;
                }
                localstatups = new EnumMap<>(MapleBuffStat.class);
                localstatups.put(theBuff, theStat);
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }
            case Beginner.DECENT_SPEED_INFUSION:
            case Noblesse.DECENT_SPEED_INFUSION:
            case Evan.DECENT_SPEED_INFUSION:
            case Legend.DECENT_SPEED_INFUSION:
            case Mercedes.DECENT_SPEED_INFUSION:
            case Citizen.DECENT_SPEED_INFUSION:
            case DemonSlayer.DECENT_SPEED_INFUSION:
            case Buccaneer.SPEED_INFUSION:
            case ThunderBreaker.SPEED_INFUSION:
            case Pirate.DASH:
            case DualBlade.TORNADO_SPIN:
            case Phantom.DECENT_SPEED_INFUSION:
            case ThunderBreaker.DASH: {
                applyto.getClient().getSession().write(BuffPacket.givePirate(statups, localDuration / 1000, sourceid));
                if (!applyto.isHidden()) {
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignPirate(statups, localDuration / 1000, applyto.getId(), sourceid), false);
                }
                normal = false;
                break;
            }
            case Phantom.ARIA:
                localstatups.put(MapleBuffStat.DAMAGE_BUFF, info.get(MapleStatInfo.damR));
               // applyto.getClient().getSession().write(BuffPacket.giveAriaBuff(level, sourceid, localDuration));
               // System.out.println("Applying aria buff");
                break;
            case Outlaw.HOMING_BEACON:
            case Evan.KILLER_WINGS:
            case Corsair.BULLSEYE: {
                if (applyto.getFirstLinkMid() > 0) {
                    applyto.getClient().getSession().write(BuffPacket.cancelHoming());
                    applyto.getClient().getSession().write(BuffPacket.giveHoming(sourceid, applyto.getFirstLinkMid(), 1));
                } else {
                    return;
                }
                normal = false;
                break;
            }
            case FPArchMage.ARCANE_AIM:
            case ILArchMage.ARCANE_AIM:
            case Bishop.ARCANE_AIM:
                if (applyto.getFirstLinkMid() > 0) {
                   applyto.getClient().getSession().write(BuffPacket.giveArcane(applyto.getAllLinkMid(), localDuration));
                } else {
                    return;
                }
                normal = false;
                break;
            case DemonSlayer.INFILTRATE:
            case Citizen.INFILTRATE:
            case Phantom.INFILTRATE:
            case Mercedes.INFILTRATE:{
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.INFILTRATE, 0);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case WindArcher.WIND_WALK: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARKSIGHT, 1); // HACK..
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case DualBlade.ADV_DARK_SIGHT:
            case Phantom.GHOST_WALK:
            case NightWalker.DARK_SIGHT: {
                if (applyto.isHidden()) {
                    return; //don't even apply the buff
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARKSIGHT, 0);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Mercedes.WATER_SHIELD: {
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.WATER_SHIELD, info.get(MapleStatInfo.x));
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Mercedes.SPIRIT_SURGE: {
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SPIRIT_SURGE, info.get(MapleStatInfo.x));
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case BattleMage.TWISTER_SPIN: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.TORNADO, info.get(MapleStatInfo.x));
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case BattleMage.BODY_BOOSTER: {
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                Pair<MapleBuffStat, Integer> statt;
                int sourcez = 0;
                if (applyfrom.getStatForBuff(MapleBuffStat.DARK_AURA) != null) {
                    sourcez = BattleMage.DARK_AURA;
                    statt = new Pair<>(MapleBuffStat.DARK_AURA, (int) (level + 10 + applyto.getTotalSkillLevel(sourcez))); //i think
                } else if (applyfrom.getStatForBuff(MapleBuffStat.YELLOW_AURA) != null) {
                    sourcez = BattleMage.YELLOW_AURA;
                    statt = new Pair<>(MapleBuffStat.YELLOW_AURA, (int) applyto.getTotalSkillLevel(sourcez));
                } else if (applyfrom.getStatForBuff(MapleBuffStat.BLUE_AURA) != null) {
                    sourcez = BattleMage.BLUE_AURA;
                    localDuration = 10000;
                    statt = new Pair<>(MapleBuffStat.BLUE_AURA, (int) applyto.getTotalSkillLevel(sourcez));
                } else {
                    return;
                }
                localstatups = new EnumMap<>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.BODY_BOOST, (int) level);
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, localstatups, this));
                localstatups.put(statt.left, statt.right);
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(statt.left, statt.right);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourcez, localDuration, stat, this));
                normal = false;
                break;
            }
            case BattleMage.DARK_AURA: {
                if (applyfrom.getTotalSkillLevel(BattleMage.ADV_DARK_AURA) > 0) {
                    SkillFactory.getSkill(BattleMage.ADV_DARK_AURA).getEffect(applyfrom.getTotalSkillLevel(BattleMage.ADV_DARK_AURA)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
            }
            case 32110007:
            case BattleMage.ADV_DARK_AURA: {
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                final EnumMap<MapleBuffStat, Integer> statt = new EnumMap<>(MapleBuffStat.class);
                statt.put(sourceid == 32110007 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, (int) (sourceid == BattleMage.ADV_DARK_AURA ? applyfrom.getTotalSkillLevel(BattleMage.DARK_AURA) : level));
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid == BattleMage.ADV_DARK_AURA ? BattleMage.DARK_AURA : sourceid, localDuration, statt, this));
                statt.clear();
                statt.put(MapleBuffStat.DARK_AURA, info.get(MapleStatInfo.x));
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, statt, this));
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), statt, this), false);
                normal = false;
                break;
            }
//            case 22131001: {//magic shield
//              final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_SHIELD, x));
//              applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
//              break;
//             }
            case BattleMage.BLUE_AURA: { // blue aura
                if (applyfrom.getTotalSkillLevel(BattleMage.ADV_BLUE_AURA) > 0) {
                    SkillFactory.getSkill(BattleMage.ADV_BLUE_AURA).getEffect(applyfrom.getTotalSkillLevel(BattleMage.ADV_BLUE_AURA)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
            }
            case 32110008: {
                localDuration = 10000;
            }
            case BattleMage.ADV_BLUE_AURA: {
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                final EnumMap<MapleBuffStat, Integer> statt = new EnumMap<>(MapleBuffStat.class);
                statt.put(sourceid == 32110008 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, (int) (sourceid == BattleMage.ADV_BLUE_AURA ? applyfrom.getTotalSkillLevel(BattleMage.BLUE_AURA) : level));
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid == BattleMage.ADV_BLUE_AURA ? BattleMage.BLUE_AURA : sourceid, localDuration, statt, this));
                statt.clear();
                statt.put(MapleBuffStat.BLUE_AURA, (int) level);
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, statt, this));
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), statt, this), false);
                normal = false;
                break;
            }
            case BattleMage.YELLOW_AURA: {
                if (applyfrom.getTotalSkillLevel(BattleMage.ADV_YELLOW_AURA) > 0) {
                    SkillFactory.getSkill(BattleMage.ADV_YELLOW_AURA).getEffect(applyfrom.getTotalSkillLevel(BattleMage.ADV_YELLOW_AURA)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
            }
            case 32110009:
            case BattleMage.ADV_YELLOW_AURA: {
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                final EnumMap<MapleBuffStat, Integer> statt = new EnumMap<>(MapleBuffStat.class);
                statt.put(sourceid == 32110009 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, (int) (sourceid == BattleMage.ADV_YELLOW_AURA ? applyfrom.getTotalSkillLevel(BattleMage.YELLOW_AURA) : level));
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid == BattleMage.ADV_YELLOW_AURA ? BattleMage.YELLOW_AURA : sourceid, localDuration, statt, this));
                statt.clear();
                statt.put(MapleBuffStat.YELLOW_AURA, (int) level);
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, statt, this));
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), statt, this), false);
                normal = false;
                break;
            }
            case WhiteKnight.BW_LIT_CHARGE: {
                if (applyto.getBuffedValue(MapleBuffStat.WK_CHARGE) != null && applyto.getBuffSource(MapleBuffStat.WK_CHARGE) != sourceid) {
                    localstatups = new EnumMap<>(MapleBuffStat.class);
                    localstatups.put(MapleBuffStat.LIGHTNING_CHARGE, 1);
                } else if (!applyto.isHidden()) {
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.WK_CHARGE, 1);
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                }
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }
            case Mechanic.SIEGE: {
                if (applyto.getBuffedValue(MapleBuffStat.MECH_CHANGE) != null && applyto.getBuffSource(MapleBuffStat.MECH_CHANGE) == Mechanic.MISSLE) {
                    SkillFactory.getSkill(Mechanic.SIEGE_MODE).getEffect(level).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.MECH_CHANGE, 1);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Mechanic.FLAME_LAUNCHER:
            case Mechanic.ENHANCED_FLAME_LAUNCHER:
            case Mechanic.SIEGE_MODE:
            case Mechanic.MISSLE:{
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.MECH_CHANGE, 1);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Paladin.DIVINE_SHIELD: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.DIVINE_SHIELD, 1);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Crusader.COMBO:
            case DawnWarrior.COMBO:{ 
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.COMBO, 0);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Hunter.SOUL_ARROW:
            case Crossbowman.SOUL_ARROW:
            case WindArcher.SOUL_ARROW: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SOULARROW, 0);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Priest.HOLY_MAGIC_SHELL:
                if (GameConstants.GMS) { //TODO JUMP
                    applyto.cancelEffectFromBuffStat(MapleBuffStat.BLESS);
                }
                break;
            case Hermit.SHADOW_PARTNER:
            case DualBlade.MIRROR_IMAGE:
            case ChiefBandit.SHADOW_PARTNER:
            case NightWalker.SHADOW_PARTNER: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SHADOWPARTNER, info.get(MapleStatInfo.x));
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case ThunderBreaker.SPARK: {
                localstatups = new EnumMap<>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.SPARK, info.get(MapleStatInfo.x));
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }
            case DualBlade.FINAL_CUT: {
                localstatups = new EnumMap<>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.FINAL_CUT, info.get(MapleStatInfo.y));
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }
            case Crossbowman.GOLDEN_EAGLE:    
            case Sniper.GOLDEN_EAGLE: {
                if (applyfrom.getTotalSkillLevel(Marksman.SPIRIT_LINK) > 0) {
                    SkillFactory.getSkill(Marksman.SPIRIT_LINK).getEffect(applyfrom.getTotalSkillLevel(Marksman.SPIRIT_LINK)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                }
                break;
            }
            case Hunter.SILVER_HAWK:    
            case Ranger.SILVER_HAWK: {
                if (applyfrom.getTotalSkillLevel(Bowmaster.SPIRIT_LINK) > 0) {
                    SkillFactory.getSkill(Bowmaster.SPIRIT_LINK).getEffect(applyfrom.getTotalSkillLevel(Bowmaster.SPIRIT_LINK)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                }
                break;
            }
            case WhiteKnight.BW_ICE_CHARGE:
            case WhiteKnight.BW_FIRE_CHARGE:
            case Paladin.BW_HOLY_CHARGE:
            case Mihile.RADIANT:    
            case DawnWarrior.SOUL_CHARGE:
            case Aran.SNOW_CHARGE1:
            case Aran.SNOW_CHARGE2:
            case ThunderBreaker.LIGHTNING_CHARGE: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.WK_CHARGE, 1);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case Bowmaster.SPIRIT_LINK:
            case Marksman.SPIRIT_LINK: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SPIRIT_LINK, 0);
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case DemonSlayer.DARK_META: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARK_METAMORPHOSIS, 6); // mob count
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case ILArchMage.INFINITY:
            case FPArchMage.INFINITY:
            case Bishop.INFINITY: {
                maskedDuration = alchemistModifyVal(applyfrom, 4000, false);
                break;
            }
            case DualBlade.OWL_SPIRIT: {
                localstatups = new EnumMap<>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.OWL_SPIRIT, info.get(MapleStatInfo.y));
                applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, localstatups, this));
                applyto.setBattleshipHP(info.get(MapleStatInfo.x)); //a variable that wouldnt' be used by a db
                normal = false;
                break;
            }
            case Hero.ENRAGE:
                applyto.handleOrbconsume(10);
                break;
            case 2022746: //angel bless
            case 2022747: //d.angel bless
            case 2022823: // wabr
                if (applyto.isHidden()) {
                    break;
                }
                applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), maskedStatups == null ? localstatups : maskedStatups, this), false);
                break;
            case Mechanic.PROTOTYPE:
                if (applyfrom.getTotalSkillLevel(Mechanic.PROTOTYPE) > 0) {
                    SkillFactory.getSkill(Mechanic.PROTOTYPE).getEffect(applyfrom.getTotalSkillLevel(Mechanic.PROTOTYPE)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }

            //fallthrough intended
            default:
                if (isPirateMorph()) {
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.MORPH, getMorph(applyto));
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                    applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, stat, this));
                    maskedStatups = new EnumMap<>(localstatups);
                    maskedStatups.remove(MapleBuffStat.MORPH);
                    normal = false;
                } else if (isMorph()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    if (isIceKnight()) {
                        //odd
                        final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                        stat.put(MapleBuffStat.ICE_KNIGHT, 2);
                        applyto.getClient().getSession().write(BuffPacket.giveBuff(0, localDuration, stat, this));
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.MORPH, getMorph(applyto));
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isInflation()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.GIANT_POTION, (int) inflation);
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (charColor > 0) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.FAMILIAR_SHADOW, 1);
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isMonsterRiding()) {
                    localDuration = 2100000000;
                    localstatups = new EnumMap<>(statups);
                    localstatups.put(MapleBuffStat.MONSTER_RIDING, 1);
                    final int mountid = parseMountInfo(applyto, sourceid);
                    final int mountid2 = parseMountInfo_Pure(applyto, sourceid);
                    if (mountid != 0 && mountid2 != 0) {
                        final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                        stat.put(MapleBuffStat.MONSTER_RIDING, 0);
                        applyto.cancelEffectFromBuffStat(MapleBuffStat.POWERGUARD);
                        applyto.cancelEffectFromBuffStat(MapleBuffStat.MANA_REFLECTION);
                        applyto.getClient().getSession().write(BuffPacket.giveMount(mountid2, sourceid, stat));
                        applyto.getMap().broadcastMessage(applyto, BuffPacket.showMonsterRiding(applyto.getId(), stat, mountid, sourceid), false);
                    } else {
                        return;
                    }
                    normal = false;
                } else if (isSoaring()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.SOARING, 1);
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (berserk > 0) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.PYRAMID_PQ, 0);
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isBerserkFury() || berserk2 > 0) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.BERSERK_FURY, 1);
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isDivineBody()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.DIVINE_BODY, 1);
                    applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                }
                break;
        }
        if (showEffect && !applyto.isHidden()) {
            applyto.getMap().broadcastMessage(applyto, EffectPacket.showBuffeffect(applyto.getId(), sourceid, 1, applyto.getLevel(), level), false);
        }
        if (isMechPassive()) {
            applyto.getClient().getSession().write(EffectPacket.showOwnBuffEffect(sourceid - 1000, 1, applyto.getLevel(), level, (byte) 1));
        }
        if (!isMonsterRiding() && !isMechDoor() && getSummonMovementType() == null) {
            applyto.cancelEffect(this, true, -1, localstatups);
        }
        // Broadcast effect to self
        if (normal && localstatups.size() > 0) {
            applyto.getClient().getSession().write(BuffPacket.giveBuff((skill ? sourceid : -sourceid), localDuration, maskedStatups == null ? localstatups : maskedStatups, this));
        }
        final long starttime = System.currentTimeMillis();
        final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime, localstatups);
        final ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, maskedDuration > 0 ? maskedDuration : localDuration);
        applyto.registerEffect(this, starttime, schedule, localstatups, false, localDuration, applyfrom.getId());
    }

    public static int parseMountInfo(final MapleCharacter player, final int skillid) {
        switch (skillid) {
            case 80001000:
            case Beginner.MONSTER_RIDER: // Monster riding
            case 11004: // Monster riding
            case Noblesse.MONSTER_RIDER:
            case Legend.MONSTER_RIDER:
            case Evan.MONSTER_RIDER:
            case Mercedes.MONSTER_RIDER:
            case Citizen.MONSTER_RIDER:
            case DemonSlayer.MONSTER_RIDER:
            case Phantom.MONSTER_RIDER:
                if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -119) != null) {
                    return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118).getItemId();
                }
                return parseMountInfo_Pure(player, skillid);
            default:
                return GameConstants.getMountItem(skillid, player);
        }
    }

    public static int parseMountInfo_Pure(final MapleCharacter player, final int skillid) {
        switch (skillid) {
            case 80001000:
            case Beginner.MONSTER_RIDER: // Monster riding
            case 11004: // Monster riding
            case Noblesse.MONSTER_RIDER:
            case Legend.MONSTER_RIDER:
            case Evan.MONSTER_RIDER:
            case Mercedes.MONSTER_RIDER:
            case Citizen.MONSTER_RIDER:
            case DemonSlayer.MONSTER_RIDER:
            case Phantom.MONSTER_RIDER:
                if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -19) != null) {
                    return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
                }
                return 0;
            default:
                return GameConstants.getMountItem(skillid, player);
        }
    }

    private int calcHPChange(final MapleCharacter applyfrom, final boolean primary) {
        int hpchange = 0;
        if (info.get(MapleStatInfo.hp) != 0) {
            if (!skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, info.get(MapleStatInfo.hp), true);
                } else {
                    hpchange += info.get(MapleStatInfo.hp);
                }
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange /= 2;
                }
            } else { // assumption: this is heal
                hpchange += makeHealHP(info.get(MapleStatInfo.hp) / 100.0, applyfrom.getStat().getTotalMagic(), 3, 5);
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange = -hpchange;
                }
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getStat().getCurrentMaxHp() * hpR) / (applyfrom.hasDisease(MapleDisease.ZOMBIFY) ? 2 : 1);
        }
        // actually receivers probably never get any hp when it's not heal but whatever
        if (primary) {
            if (info.get(MapleStatInfo.hpCon) != 0) {
                hpchange -= info.get(MapleStatInfo.hpCon);
            }
        }
        switch (this.sourceid) {
            case ChiefBandit.CHAKRA:
                final PlayerStats stat = applyfrom.getStat();
                int v42 = getY() + 100;
                int v38 = Randomizer.rand(1, 100) + 100;
                hpchange = (int) ((v38 * stat.getLuk() * 0.033 + stat.getDex()) * v42 * 0.002);
                hpchange += makeHealHP(getY() / 100.0, applyfrom.getStat().getTotalLuk(), 2.3, 3.5);
                break;
        }
        return hpchange;
    }

    private static int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(final MapleCharacter applyfrom, final boolean primary) {
        int mpchange = 0;
        if (info.get(MapleStatInfo.mp) != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, info.get(MapleStatInfo.mp), false); // recovery up doesn't apply for mp
            } else {
                mpchange += info.get(MapleStatInfo.mp);
            }
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getStat().getCurrentMaxMp(applyfrom.getJob()) * mpR);
        }
        if (GameConstants.isDemon(applyfrom.getJob())) {
            mpchange = 0;
        }
        if (primary) {
            if (info.get(MapleStatInfo.mpCon) != 0 && !GameConstants.isDemon(applyfrom.getJob())) {
                boolean free = false;
                if (applyfrom.getJob() == 411 || applyfrom.getJob() == 412) {
                    final Skill expert = SkillFactory.getSkill(4110012);
                    if (applyfrom.getTotalSkillLevel(expert) > 0) {
                        final MapleStatEffect eff = expert.getEffect(applyfrom.getTotalSkillLevel(expert));
                        if (eff.makeChanceResult()) {
                            free = true;
                        }
                    }
                }
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mpchange = 0;
                } else if (!free) {
                    mpchange -= (info.get(MapleStatInfo.mpCon) - (info.get(MapleStatInfo.mpCon) * applyfrom.getStat().mpconReduce / 100)) * (applyfrom.getStat().mpconPercent / 100.0);
                }
            } else if (info.get(MapleStatInfo.forceCon) != 0 && GameConstants.isDemon(applyfrom.getJob())) {
                if (applyfrom.getBuffedValue(MapleBuffStat.BOUNDLESS_RAGE) != null) {
                    mpchange = 0;
                } else {
                    mpchange -= info.get(MapleStatInfo.forceCon);
                }
            }
        }

        return mpchange;
    }

    public final int alchemistModifyVal(final MapleCharacter chr, final int val, final boolean withX) {
        if (!skill) { // RecoveryUP only used for hp items and skills
            return (val * (100 + (withX ? chr.getStat().RecoveryUP : chr.getStat().BuffUP)) / 100);
        }
        return (val * (100 + (withX ? chr.getStat().RecoveryUP : (chr.getStat().BuffUP_Skill + (getSummonMovementType() == null ? 0 : chr.getStat().BuffUP_Summon)))) / 100);
    }

    public final void setSourceId(final int newid) {
        sourceid = newid;
    }

    public final boolean isGmBuff() {
        switch (sourceid) {
            case 10001075: //Empress Prayer
            case 9001000: // GM dispel
            case 9001001: // GM haste
            case 9001002: // GM Holy Symbol
            case 9001003: // GM Bless
            case 9001005: // GM resurrection
            case 9001008: // GM Hyper body

            case 9101000:
            case 9101001:
            case 9101002:
            case 9101003:
            case 9101005:
            case 9101008:
                return true;
            default:
                return GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1005;
        }
    }

    public final boolean isInflation() {
        return inflation > 0;
    }

    public final int getInflation() {
        return inflation;
    }

    public final boolean isEnergyCharge() {
        return skill && (sourceid == Marauder.ENERGY_CHARGE || sourceid == ThunderBreaker.ENERGY_CHARGE);
    }

    public boolean isMonsterBuff() {
        switch (sourceid) {
            case 1201006: // threaten
            case 2101003: // fp slow
            case 2201003: // il slow
            case 5011002:
            case 12101001: // cygnus slow
            case 2211004: // il seal
            case 2111004: // fp seal
            case 12111002: // cygnus seal
            case 2311005: // doom
            case 4111003: // shadow web
            case 14111001: // cygnus web
            case 4121004: // Ninja ambush
            case 4221004: // Ninja ambush
            case 22151001:
            case 22121000:
            case 22161002:
            case 4321002:
            case 4341003:
            case 90001002:
            case 90001003:
            case 90001004:
            case 90001005:
            case 90001006:
            case 1111007:
            case 1211009:
            case 1311007:
            case 35111005:
            case 32120000:
            case 32120001:
                return skill;
        }
        return false;
    }

    public final void setPartyBuff(boolean pb) {
        this.partyBuff = pb;
    }

    private boolean isPartyBuff() {
        if (lt == null || rb == null || !partyBuff) {
            return isSoulStone() && sourceid != 24111002;
        }
        switch (sourceid) {
            case 1211003:
            case 1211004:
            case 1211005:
            case 1211006:
            case 1211007:
            case 1211008:
            case 1221003:
            case 1221004:
            case 11111007:
            case 51111003:    
            case 12101005:  
            case 4311001:
            case 4331003:
            case 4341002:
            case 35121005:
                return false;
        }
        if (GameConstants.isNoDelaySkill(sourceid)) {
            return false;
        }
        return true;
    }

    public final boolean isArcane() {
        return skill && (sourceid == Bishop.ARCANE_AIM || sourceid == FPArchMage.ARCANE_AIM || sourceid == ILArchMage.ARCANE_AIM);
    }

    public final boolean isHeal() {
        return skill && (sourceid == Cleric.HEAL || sourceid == SuperGM.HEAL_PLUS_DISPEL || sourceid == 9001000);
    }

    public final boolean isResurrection() {
        return skill && (sourceid == 9001005 || sourceid == SuperGM.RESURRECTION || sourceid == Bishop.RESURRECTION);
    }

    public final boolean isTimeLeap() {
        return skill && sourceid == Buccaneer.TIME_LEAP;
    }

    public final int getHp() {
        return info.get(MapleStatInfo.hp);
    }

    public final int getMp() {
        return info.get(MapleStatInfo.mp);
    }
    
    public final int getDOTStack() {
        return info.get(MapleStatInfo.dotSuperpos);
    }

    public final double getHpR() {
        return hpR;
    }

    public final double getMpR() {
        return mpR;
    }

    public final int getMastery() {
        return info.get(MapleStatInfo.mastery);
    }

    public final int getWatk() {
        return info.get(MapleStatInfo.pad);
    }

    public final int getMatk() {
        return info.get(MapleStatInfo.mad);
    }

    public final int getWdef() {
        return info.get(MapleStatInfo.pdd);
    }

    public final int getMdef() {
        return info.get(MapleStatInfo.mdd);
    }

    public final int getAcc() {
        return info.get(MapleStatInfo.acc);
    }

    public final int getAvoid() {
        return info.get(MapleStatInfo.eva);
    }

    public final int getSpeed() {
        return info.get(MapleStatInfo.speed);
    }

    public final int getJump() {
        return info.get(MapleStatInfo.jump);
    }

    public final int getSpeedMax() {
        return info.get(MapleStatInfo.speedMax);
    }

    public final int getPassiveSpeed() {
        return info.get(MapleStatInfo.psdSpeed);
    }

    public final int getPassiveJump() {
        return info.get(MapleStatInfo.psdJump);
    }

    public final int getDuration() {
        return info.get(MapleStatInfo.time);
    }

    public final int getSubTime() {
        return info.get(MapleStatInfo.subTime);
    }

    public final boolean isOverTime() {
        return overTime;
    }

    public final Map<MapleBuffStat, Integer> getStatups() {
        return statups;
    }

    public final boolean sameSource(final MapleStatEffect effect) {
        boolean sameSrc = this.sourceid == effect.sourceid;
        switch (this.sourceid) { // All these are passive skills, will have to cast the normal ones.
            case BattleMage.ADV_DARK_AURA: // Advanced Dark Aura
                sameSrc = effect.sourceid == 32001003;
                break;
            case BattleMage.ADV_BLUE_AURA: // Advanced Blue Aura
                sameSrc = effect.sourceid == 32111012;
                break;
            case BattleMage.ADV_YELLOW_AURA: // Advanced Yellow Aura
                sameSrc = effect.sourceid == 32101003;
                break;
            case Mechanic.EXTREME: // Extreme Mech
                sameSrc = effect.sourceid == 35001002;
                break;
            case Mechanic.SIEGE_MODE: // Mech: Siege Mode
                sameSrc = effect.sourceid == 35111004;
                break;
        }
        return effect != null && sameSrc && this.skill == effect.skill;
    }

    public final int getCr() {
        return info.get(MapleStatInfo.cr);
    }

    public final int getT() {
        return info.get(MapleStatInfo.t);
    }

    public final int getU() {
        return info.get(MapleStatInfo.u);
    }

    public final int getV() {
        return info.get(MapleStatInfo.v);
    }

    public final int getW() {
        return info.get(MapleStatInfo.w);
    }

    public final int getX() {
        return info.get(MapleStatInfo.x);
    }

    public final int getY() {
        return info.get(MapleStatInfo.y);
    }

    public final int getZ() {
        return info.get(MapleStatInfo.z);
    }

    public final int getDamage() {
        return info.get(MapleStatInfo.damage);
    }

    public final int getPVPDamage() {
        return info.get(MapleStatInfo.PVPdamage);
    }

    public final int getAttackCount() {
        return info.get(MapleStatInfo.attackCount);
    }

    public final int getBulletCount() {
        return info.get(MapleStatInfo.bulletCount);
    }

    public final int getBulletConsume() {
        return info.get(MapleStatInfo.bulletConsume);
    }

    public final int getMobCount() {
        return info.get(MapleStatInfo.mobCount);
    }

    public final int getMoneyCon() {
        return moneyCon;
    }

    public final int getCooltimeReduceR() {
        return info.get(MapleStatInfo.coolTimeR);
    }

    public final int getMesoAcquisition() {
        return info.get(MapleStatInfo.mesoR);
    }

    public final int getCooldown(final MapleCharacter chra) {
        if (chra.getStat().coolTimeR > 0) {
            return Math.max(0, ((info.get(MapleStatInfo.cooltime) * (100 - (chra.getStat().coolTimeR / 100))) - chra.getStat().reduceCooltime));
        }
        return Math.max(0, (info.get(MapleStatInfo.cooltime) - chra.getStat().reduceCooltime));
    }

    public final Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }

    public final int getBerserk() {
        return berserk;
    }

    public final boolean isHide() {
        return skill && (sourceid == 9001004 || sourceid == 9101004);
    }

    public final boolean isDragonBlood() {
        return skill && sourceid == 1311008;
    }

    public final boolean isRecovery() {
        return skill && (sourceid == 1001 || sourceid == 10001001 || sourceid == 20001001 || sourceid == 20011001 || sourceid == 20021001 || sourceid == 11001 || sourceid == 35121005);
    }

    public final boolean isBerserk() {
        return skill && sourceid == 1320006;
    }

    public final boolean isBeholder() {
        return skill && sourceid == 1321007;
    }

    public final boolean isMPRecovery() {
        return skill && sourceid == 5101005;
    }

    public final boolean isInfinity() {
        return skill && (sourceid == 2121004 || sourceid == 2221004 || sourceid == 2321004);
    }

    public final boolean isMonsterRiding_() {
        return skill && (sourceid == 1004 || sourceid == 10001004 || sourceid == 20001004 || sourceid == 20011004 || sourceid == 11004 || sourceid == 20021004 || sourceid == 80001000);
    }

    public final boolean isMonsterRiding() {
        return skill && (isMonsterRiding_() || GameConstants.getMountItem(sourceid, null) != 0);
    }

    public final boolean isMagicDoor() {
        return skill && (sourceid == 2311002 || sourceid % 10000 == 8001);
    }

    public final boolean isMesoGuard() {
        return skill && sourceid == 4211005;
    }

    public final boolean isMechDoor() {
        return skill && sourceid == 35101005;
    }

    public final boolean isComboRecharge() {
        return skill && sourceid == 21111009;
    }

    public final boolean isDragonBlink() {
        return skill && sourceid == 22141004;
    }

    public final boolean isCharge() {
        switch (sourceid) {
            case 1211003:
            case 1211008:
            case 11111007:
            case 51111003:  
            case 12101005:
            case 15101006:
            case 21111005:
                return skill;
        }
        return false;
    }

    public final boolean isPoison() {
        return info.get(MapleStatInfo.dot) > 0 && info.get(MapleStatInfo.dotTime) > 0;
    }

    public boolean isMist() {
        return skill && (sourceid == 2111003 || sourceid == 4221006 || sourceid == 12111005 || sourceid == 14111006 || sourceid == 22161003 || sourceid == 32121006 || sourceid == 1076 || sourceid == 11076); // poison mist, smokescreen and flame gear, recovery aura
    }

    private boolean isSpiritClaw() {
        return skill && sourceid == 4111009 || sourceid == 14111007 || sourceid == 5201008;
    }

    private boolean isSpiritBlast() {
        return skill && sourceid == 5201008;
    }
    
    private boolean isDispel() {
        return skill && (sourceid == 2311001 || sourceid == 9001000 || sourceid == 9101000);
    }

    private boolean isHeroWill() {
        switch (sourceid) {
            case 1121011:
            case 1221012:
            case 1321010:
            case 2121008:
            case 2221008:
            case 2321009:
            case 3121009:
            case 3221008:
            case 4121009:
            case 4221008:
            case 5121008:
            case 5221010:
            case 21121008:
            case 22171004:
            case 4341008:
            case 32121008:
            case 33121008:
            case 35121008:
            case 5321008:
            case 23121008:
            case 24121009:
            case 5721002:
                return skill;
        }
        return false;
    }

    public final boolean isAranCombo() {
        return sourceid == 21000000;
    }

    public final boolean isCombo() {
        switch (sourceid) {
            case 1111002:
            case 11111001: // Combo
                return skill;
        }
        return false;
    }

    public final boolean isPirateMorph() {
        switch (sourceid) {
            case 13111005:
            case 15111002:
            case 5111005:
            case 5121003:
                return skill;
        }
        return false;
    }

    public final boolean isMorph() {
        return morphId > 0;
    }

    public final int getMorph() {
        switch (sourceid) {
            case 15111002:
            case 5111005:
                return 1000;
            case 5121003:
                return 1001;
            case 5101007:
                return 1002;
            case 13111005:
                return 1003;
        }
        return morphId;
    }

    public final boolean isDivineBody() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1010;
    }

    public final boolean isDivineShield() {
        switch (sourceid) {
            case 1220013:
                return skill;
        }
        return false;
    }

    public final boolean isBerserkFury() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1011;
    }

    public final int getMorph(final MapleCharacter chr) {
        final int morph = getMorph();
        switch (morph) {
            case 1000:
            case 1001:
            case 1003:
                return morph + (chr.getGender() == 1 ? 100 : 0);
        }
        return morph;
    }

    public final byte getLevel() {
        return level;
    }

    public final SummonMovementType getSummonMovementType() {
        if (!skill) {
            return null;
        }
        switch (sourceid) {
            case 3211002: // puppet sniper
            case 3111002: // puppet ranger
            case 33111003:
            case 13111004: // puppet cygnus
            case 5211001: // octopus - pirate
            case 5220002: // advanced octopus - pirate
            case 4341006:
            case 35111002:
            case 35111005:
            case 35111011:
            case 35121009:
            case 35121010:
            case 35121011:
            case 4111007: //dark flare
            case 4211007: //dark flare
            case 14111010: //dark flare  
            case 33101008:
            case 35121003:
            case 3120012:
            case 3220012:
            case 5321003:
            case 5321004:
            case 5320011:
            case 5211014:
            case 5711001: // turret
                return SummonMovementType.STATIONARY;
            case 3211005: // golden eagle
            case 3111005: // golden hawk
            case 3101007:
            case 3201007:
            case 33111005:
            case 3221005: // frostprey
            case 3121006: // phoenix
            case 23111008:
            case 23111009:   
            case 23111010:
                return SummonMovementType.CIRCLE_FOLLOW;
            case 5211002: // bird - pirate
                return SummonMovementType.CIRCLE_STATIONARY;
            case 32111006: //reaper
            case 5211011:
            case 5211015:
            case 5211016:
                return SummonMovementType.WALK_STATIONARY;
            case 1321007: // beholder
            case 2121005: // elquines
            case 2221005: // ifrit
            case 2321003: // bahamut
            case 12111004: // Ifrit
            case 11001004: // soul
            case 12001004: // flame
            case 13001004: // storm
            case 14001005: // darkness
            case 15001004: // lightning
            case 35111001:
            case 35111010:
            case 35111009:

                return SummonMovementType.FOLLOW;
        }
        if (isAngel()) {
            return SummonMovementType.FOLLOW;
        }
        return null;
    }

    public final boolean isAngel() {
        return GameConstants.isAngel(sourceid);
    }

    public final boolean isSkill() {
        return skill;
    }

    public final int getSourceId() {
        return sourceid;
    }

    public final boolean isIceKnight() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1105;
    }

    public final boolean isSoaring() {
        return isSoaring_Normal() || isSoaring_Mount();
    }

    public final boolean isSoaring_Normal() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1026;
    }

    public final boolean isSoaring_Mount() {
        return skill && ((GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1142) || sourceid == 80001089);
    }

    public final boolean isFinalAttack() {
        switch (sourceid) {
            case 13101002:
            case 11101002:
                return skill;
        }
        return false;
    }

    public final boolean isMistEruption() {
        switch (sourceid) {
            case 2121003:
                return skill;
        }
        return false;
    }

    public final boolean isShadow() {
        switch (sourceid) {
            case 4111002: // shadowpartner
            case 14111000: // cygnus
            case 4211008:
            case 4331002:
                return skill;
        }
        return false;
    }

    public final boolean isMechPassive() {
        switch (sourceid) {
            //case 35121005:
            case 35121013:
                return true;
        }
        return false;
    }

    /**
     *
     * @return true if the effect should happen based on it's probablity, false
     * otherwise
     */
    public final boolean makeChanceResult() {
        return info.get(MapleStatInfo.prop) >= 100 || Randomizer.nextInt(100) < info.get(MapleStatInfo.prop);
    }

    public final int getProb() {
        return info.get(MapleStatInfo.prop);
    }

    public final short getIgnoreMob() {
        return ignoreMob;
    }

    public final int getEnhancedHP() {
        return info.get(MapleStatInfo.emhp);
    }

    public final int getEnhancedMP() {
        return info.get(MapleStatInfo.emmp);
    }

    public final int getEnhancedWatk() {
        return info.get(MapleStatInfo.epad);
    }

    public final int getEnhancedWdef() {
        return info.get(MapleStatInfo.pdd);
    }

    public final int getEnhancedMatk() {
        return info.get(MapleStatInfo.emad);
    }

    public final int getEnhancedMdef() {
        return info.get(MapleStatInfo.emdd);
    }

    public final int getDOT() {
        return info.get(MapleStatInfo.dot);
    }

    public final int getDOTTime() {
        return info.get(MapleStatInfo.dotTime);
    }

    public final int getCriticalMax() {
        return info.get(MapleStatInfo.criticaldamageMax);
    }

    public final int getCriticalMin() {
        return info.get(MapleStatInfo.criticaldamageMin);
    }

    public final int getASRRate() {
        return info.get(MapleStatInfo.asrR);
    }

    public final int getTERRate() {
        return info.get(MapleStatInfo.terR);
    }

    public final int getDAMRate() {
        return info.get(MapleStatInfo.damR);
    }

    public final int getHpToDamage() {
        return info.get(MapleStatInfo.mhp2damX);
    }

    public final int getMpToDamage() {
        return info.get(MapleStatInfo.mmp2damX);
    }

    public final int getLevelToDamage() {
        return info.get(MapleStatInfo.lv2damX);
    }

    public final int getLevelToWatk() {
        return info.get(MapleStatInfo.lv2pdX);
    }

    public final int getLevelToMatk() {
        return info.get(MapleStatInfo.lv2mdX);
    }

    public final int getEXPLossRate() {
        return info.get(MapleStatInfo.expLossReduceR);
    }

    public final int getBuffTimeRate() {
        return info.get(MapleStatInfo.bufftimeR);
    }

    public final int getSuddenDeathR() {
        return info.get(MapleStatInfo.suddenDeathR);
    }

    public final int getPercentAcc() {
        return info.get(MapleStatInfo.accR);
    }

    public final int getPercentAvoid() {
        return info.get(MapleStatInfo.evaR);
    }

    public final int getSummonTimeInc() {
        return info.get(MapleStatInfo.summonTimeR);
    }

    public final int getMPConsumeEff() {
        return info.get(MapleStatInfo.mpConEff);
    }

    public final short getMesoRate() {
        return mesoR;
    }

    public final int getEXP() {
        return exp;
    }

    public final int getAttackX() {
        return info.get(MapleStatInfo.padX);
    }

    public final int getMagicX() {
        return info.get(MapleStatInfo.madX);
    }

    public final int getPercentHP() {
        return info.get(MapleStatInfo.mhpR);
    }

    public final int getPercentMP() {
        return info.get(MapleStatInfo.mmpR);
    }

    public final int getConsume() {
        return consumeOnPickup;
    }

    public final int getSelfDestruction() {
        return info.get(MapleStatInfo.selfDestruction);
    }

    public final int getCharColor() {
        return charColor;
    }

    public final List<Integer> getPetsCanConsume() {
        return petsCanConsume;
    }

    public final boolean isReturnScroll() {
        return skill && (sourceid == 80001040 || sourceid == 20021110 || sourceid == 20031203);
    }

    public final boolean isMechChange() {
        switch (sourceid) {
            case 35111004: //siege
            case 35001001: //flame
            case 35101009:
            case 35121013:
            case 35121005:
                return skill;
        }
        return false;
    }

    public final int getRange() {
        return info.get(MapleStatInfo.range);
    }

    public final int getER() {
        return info.get(MapleStatInfo.er);
    }

    public final int getPrice() {
        return info.get(MapleStatInfo.price);
    }

    public final int getExtendPrice() {
        return info.get(MapleStatInfo.extendPrice);
    }

    public final int getPeriod() {
        return info.get(MapleStatInfo.period);
    }

    public final int getReqGuildLevel() {
        return info.get(MapleStatInfo.reqGuildLevel);
    }

    public final byte getEXPRate() {
        return expR;
    }

    public final short getLifeID() {
        return lifeId;
    }

    public final short getUseLevel() {
        return useLevel;
    }

    public final byte getSlotCount() {
        return slotCount;
    }

    public final int getStr() {
        return info.get(MapleStatInfo.str);
    }

    public final int getStrX() {
        return info.get(MapleStatInfo.strX);
    }

    public final int getDex() {
        return info.get(MapleStatInfo.dex);
    }

    public final int getDexX() {
        return info.get(MapleStatInfo.dexX);
    }

    public final int getInt() {
        return info.get(MapleStatInfo.int_);
    }

    public final int getIntX() {
        return info.get(MapleStatInfo.intX);
    }

    public final int getLuk() {
        return info.get(MapleStatInfo.luk);
    }

    public final int getLukX() {
        return info.get(MapleStatInfo.lukX);
    }

    public final int getMaxHpX() {
        return info.get(MapleStatInfo.mhpX);
    }

    public final int getMaxMpX() {
        return info.get(MapleStatInfo.mmpX);
    }

    public final int getAccX() {
        return info.get(MapleStatInfo.accX);
    }

    public final int getMPConReduce() {
        return info.get(MapleStatInfo.mpConReduce);
    }

    public final int getIndieMHp() {
        return info.get(MapleStatInfo.indieMhp);
    }

    public final int getIndieMMp() {
        return info.get(MapleStatInfo.indieMmp);
    }

    public final int getIndieAllStat() {
        return info.get(MapleStatInfo.indieAllStat);
    }

    public final byte getType() {
        return type;
    }

    public int getBossDamage() {
        return info.get(MapleStatInfo.bdR);
    }

    public int getInterval() {
        return interval;
    }

    public ArrayList<Pair<Integer, Integer>> getAvailableMaps() {
        return availableMap;
    }

    public int getWDEFRate() {
        return info.get(MapleStatInfo.pddR);
    }

    public int getMDEFRate() {
        return info.get(MapleStatInfo.mddR);
    }

    public static class CancelEffectAction implements Runnable {

        private final MapleStatEffect effect;
        private final WeakReference<MapleCharacter> target;
        private final long startTime;
        private final Map<MapleBuffStat, Integer> statup;

        public CancelEffectAction(final MapleCharacter target, final MapleStatEffect effect, final long startTime, final Map<MapleBuffStat, Integer> statup) {
            this.effect = effect;
            this.target = new WeakReference<>(target);
            this.startTime = startTime;
            this.statup = statup;
        }

        @Override
        public void run() {
            final MapleCharacter realTarget = target.get();
            if (realTarget != null && !realTarget.isClone()) {
                realTarget.cancelEffect(effect, false, startTime, statup);
            }
        }
    }
    
    public final boolean isUnstealable() {
        for (MapleBuffStat b : statups.keySet()) {
            if (b == MapleBuffStat.MAPLE_WARRIOR) {
                return true;
            }
        }
        return  sourceid == 4221013;
    }
}
