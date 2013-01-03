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
package client;

import constants.GameConstants;
import java.util.ArrayList;
import java.util.List;
import constants.skills.*;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.Element;
import tools.Pair;

public class Skill {

    private String name = "";
    private final List<MapleStatEffect> effects = new ArrayList<MapleStatEffect>();
    private List<MapleStatEffect> pvpEffects = null;
    private List<Integer> animation = null;
    private final List<Pair<Integer, Byte>> requiredSkill = new ArrayList<Pair<Integer, Byte>>();
    private Element element = Element.NEUTRAL;
    private int id, animationTime = 0, masterLevel = 0, maxLevel = 0, delay = 0, trueMax = 0, eventTamingMob = 0, skillType = 0; //4 is alert
    private boolean invisible = false, chargeskill = false, timeLimited = false, combatOrders = false, pvpDisabled = false, magic = false, casterMove = false, pushTarget = false, pullTarget = false;

    public Skill(final int id) {
        super();
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static final Skill loadFromData(final int id, final MapleData data, final MapleData delayData) {
        Skill ret = new Skill(id);

        boolean isBuff = false;
        final int skillType = MapleDataTool.getInt("skillType", data, -1);
        final String elem = MapleDataTool.getString("elemAttr", data, null);
        if (elem != null) {
            ret.element = Element.getFromChar(elem.charAt(0));
        }
        ret.skillType = skillType;
        ret.invisible = MapleDataTool.getInt("invisible", data, 0) > 0;
        ret.timeLimited = MapleDataTool.getInt("timeLimited", data, 0) > 0;
        ret.combatOrders = MapleDataTool.getInt("combatOrders", data, 0) > 0;
        ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
        if ((id == 22111001 || id == 22140000 || id == 22141002)) {
            ret.masterLevel = 5; //hack
        }
        ret.eventTamingMob = MapleDataTool.getInt("eventTamingMob", data, 0);
        final MapleData inf = data.getChildByPath("info");
        if (inf != null) {
            ret.pvpDisabled = MapleDataTool.getInt("pvp", inf, 1) <= 0;
            ret.magic = MapleDataTool.getInt("magicDamage", inf, 0) > 0;
            ret.casterMove = MapleDataTool.getInt("casterMove", inf, 0) > 0;
            ret.pushTarget = MapleDataTool.getInt("pushTarget", inf, 0) > 0;
            ret.pullTarget = MapleDataTool.getInt("pullTarget", inf, 0) > 0;
        }
        final MapleData effect = data.getChildByPath("effect");
        if (skillType == 2) {
            isBuff = true;
        } else if (skillType == 3) { //final attack
            ret.animation = new ArrayList<Integer>();
            ret.animation.add(0);
            isBuff = effect != null;
        } else {
            MapleData action_ = data.getChildByPath("action");
            final MapleData hit = data.getChildByPath("hit");
            final MapleData ball = data.getChildByPath("ball");

            boolean action = false;
            if (action_ == null) {
                if (data.getChildByPath("prepare/action") != null) {
                    action_ = data.getChildByPath("prepare/action");
                    action = true;
                }
            }
            isBuff = effect != null && hit == null && ball == null;
            if (action_ != null) {
                String d = null;
                if (action) { //prepare
                    d = MapleDataTool.getString(action_, null);
                } else {
                    d = MapleDataTool.getString("0", action_, null);
                }
                if (d != null) {
                    isBuff |= d.equals("alert2");
                    final MapleData dd = delayData.getChildByPath(d);
                    if (dd != null) {
                        for (MapleData del : dd) {
                            ret.delay += Math.abs(MapleDataTool.getInt("delay", del, 0));
                        }
                        if (ret.delay > 30) { //then, faster(2) = (10+2)/16 which is basically 3/4
                            ret.delay = (int) Math.round(ret.delay * 11.0 / 16.0); //fastest(1) lolol
                            ret.delay -= (ret.delay % 30); //round to 30ms
                        }
                    }
                    if (SkillFactory.getDelay(d) != null) { //this should return true always
                        ret.animation = new ArrayList<Integer>();
                        ret.animation.add(SkillFactory.getDelay(d));
                        if (!action) {
                            for (MapleData ddc : action_) {
                                if (!MapleDataTool.getString(ddc, d).equals(d)) {
                                    String c = MapleDataTool.getString(ddc);
                                    if (SkillFactory.getDelay(c) != null) {
                                        ret.animation.add(SkillFactory.getDelay(c));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            switch (id) {
                case Hero.RUSH:
                case Paladin.RUSH:
                case DarkKnight.RUSH:
                case Paladin.ADVANCED_CHARGE:
                case DragonKnight.SACRIFICE:
                case FPMage.EXPLOSION:
                case FPMage.POISON_MIST:
                case Cleric.HEAL:
                case Ranger.MORTAL_BLOW:
                case Sniper.MORTAL_BLOW:
                case Assassin.DRAIN:
                case Hermit.SHADOW_WEB:
                case Bandit.STEAL:
                case Shadower.SMOKE_SCREEN:
                case SuperGM.HEAL_PLUS_DISPEL:
                case Hero.MONSTER_MAGNET:
                case Paladin.MONSTER_MAGNET:
                case DarkKnight.MONSTER_MAGNET:
                case Gunslinger.RECOIL_SHOT:
                case Marauder.ENERGY_DRAIN:
                case BlazeWizard.FLAME_GEAR:
                case NightWalker.SHADOW_WEB:
                case NightWalker.POISON_BOMB:
                case NightWalker.VAMPIRE:
                case Evan.RECOVERY_AURA:
                case BattleMage.PARTY_SHIELD:
                case Bishop.BIG_BANG:
                case ILArchMage.BIG_BANG:
                case FPArchMage.BIG_BANG:
                case ChiefBandit.CHAKRA:
                case Beginner.OZ_FLAME:
                case Beginner.OZ_FLAME_GEAR:
                case Phantom.TEMPEST:
                    isBuff = false;
                    break;
                case Beginner.RECOVERY:
                case Beginner.NIMBLE_FEET:
                case Beginner.MONSTER_RIDER:
                case Beginner.ECHO_OF_HERO:
                case Swordsman.IRON_BODY:
                case Fighter.AXE_BOOSTER:
                case Fighter.POWER_GUARD:
                case Fighter.RAGE:
                case Fighter.SWORD_BOOSTER:
                case Crusader.ARMOR_CRASH:
                case Crusader.COMBO:
                case Hero.ENRAGE:
                case Hero.HEROS_WILL:
                case Hero.MAPLE_WARRIOR:
                case Hero.STANCE:
                case Page.BW_BOOSTER:
                case Page.POWER_GUARD:
                case Page.SWORD_BOOSTER:
                case Page.THREATEN:
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case WhiteKnight.MAGIC_CRASH:
                case Paladin.BW_HOLY_CHARGE:
                case Paladin.HEROS_WILL:
                case Paladin.MAPLE_WARRIOR:
                case Paladin.STANCE:
                case Spearman.HYPER_BODY:
                case Spearman.IRON_WILL:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case DragonKnight.DRAGON_BLOOD:
                case DragonKnight.POWER_CRASH:
                case DarkKnight.AURA_OF_BEHOLDER:
                case DarkKnight.BEHOLDER:
                case DarkKnight.HEROS_WILL:
                case DarkKnight.HEX_OF_BEHOLDER:
                case DarkKnight.MAPLE_WARRIOR:
                case DarkKnight.STANCE:
                case Magician.MAGIC_GUARD:
                case Magician.MAGIC_ARMOR:
                case FPWizard.MEDITATION:
                case FPWizard.SLOW:
                case FPMage.SEAL:
                case FPMage.SPELL_BOOSTER:
                case FPArchMage.HEROS_WILL:
                case FPArchMage.INFINITY:
                case FPArchMage.MANA_REFLECTION:
                case FPArchMage.MAPLE_WARRIOR:
                case ILWizard.MEDITATION:
                case ILMage.SEAL:
                case ILWizard.SLOW:
                case ILMage.SPELL_BOOSTER:
                case ILArchMage.HEROS_WILL:                
                case ILArchMage.INFINITY:
                case ILArchMage.MANA_REFLECTION:
                case ILArchMage.MAPLE_WARRIOR:
                case Cleric.INVINCIBLE:
                case Cleric.BLESS:
                case Priest.DISPEL:
                case Priest.DOOM:
                case Priest.HOLY_SYMBOL:
                case Priest.MYSTIC_DOOR:
                case Bishop.HEROS_WILL:
                case Bishop.HOLY_SHIELD:
                case Bishop.INFINITY:
                case Bishop.MANA_REFLECTION:
                case Bishop.MAPLE_WARRIOR:
                case Archer.FOCUS:
                case Hunter.BOW_BOOSTER:
                case Hunter.SOUL_ARROW:
                case Ranger.PUPPET:
                case Bowmaster.CONCENTRATE:
                case Bowmaster.HEROS_WILL:
                case Bowmaster.MAPLE_WARRIOR:
                case Bowmaster.SHARP_EYES:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Crossbowman.SOUL_ARROW:
                case Sniper.PUPPET:
                case Marksman.BLIND:
                case Marksman.HEROS_WILL:
                case Marksman.MAPLE_WARRIOR:
                case Marksman.SHARP_EYES:
                case Rogue.DARK_SIGHT:
                case Assassin.CLAW_BOOSTER:
                case Assassin.HASTE:
                case Hermit.MESO_UP:
                case Hermit.SHADOW_PARTNER:
                case Hermit.SHADOW_STARS:
                case NightLord.HEROS_WILL:
                case NightLord.MAPLE_WARRIOR:
                case NightLord.NINJA_AMBUSH:
                case NightLord.SHADOW_STARS:
                case Bandit.DAGGER_BOOSTER:
                case Bandit.HASTE:
                case ChiefBandit.MESO_GUARD:
                case ChiefBandit.PICKPOCKET:
                case Shadower.HEROS_WILL:
                case Shadower.MAPLE_WARRIOR:
                case Shadower.NINJA_AMBUSH:
                case Pirate.DASH:
                case Marauder.TRANSFORMATION:
                case Buccaneer.SUPER_TRANSFORMATION:
                case Corsair.BATTLE_SHIP:
                case GM.HIDE:
                case SuperGM.HASTE:
                case SuperGM.HOLY_SYMBOL:
                case SuperGM.BLESS:
                case SuperGM.HIDE:
                case SuperGM.HYPER_BODY:
                case Noblesse.BLESSING_OF_THE_FAIRY:
                case Noblesse.ECHO_OF_HERO:
                case Noblesse.MONSTER_RIDER:
                case Noblesse.NIMBLE_FEET:
                case Noblesse.RECOVERY:
                case DawnWarrior.COMBO:
                case DawnWarrior.FINAL_ATTACK:
                case DawnWarrior.IRON_BODY:
                case DawnWarrior.RAGE:
                case DawnWarrior.SOUL:
                case DawnWarrior.SOUL_CHARGE:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.ELEMENTAL_RESET:
                case BlazeWizard.FLAME:
                case BlazeWizard.IFRIT:
                case BlazeWizard.MAGIC_ARMOR:
                case BlazeWizard.MAGIC_GUARD:
                case BlazeWizard.MEDITATION:
                case BlazeWizard.SEAL:
                case BlazeWizard.SLOW:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case WindArcher.EAGLE_EYE:
                case WindArcher.FINAL_ATTACK:
                case WindArcher.FOCUS:
                case WindArcher.PUPPET:
                case WindArcher.SOUL_ARROW:
                case WindArcher.STORM:
                case WindArcher.WIND_WALK:
                case NightWalker.CLAW_BOOSTER:
                case NightWalker.DARKNESS:
                case NightWalker.DARK_SIGHT:
                case NightWalker.HASTE:
                case NightWalker.SHADOW_PARTNER:
                case ThunderBreaker.DASH:
                case ThunderBreaker.ENERGY_CHARGE:
                case ThunderBreaker.ENERGY_DRAIN:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case ThunderBreaker.LIGHTNING:
                case ThunderBreaker.SPARK:
                case ThunderBreaker.LIGHTNING_CHARGE:
                case ThunderBreaker.SPEED_INFUSION:
                case ThunderBreaker.TRANSFORMATION:
                case Legend.BLESSING_OF_THE_FAIRY:
                case Legend.AGILE_BODY:
                case Legend.ECHO_OF_HERO:
                case Legend.RECOVERY:
                case Legend.MONSTER_RIDER:
                case Aran.MAPLE_WARRIOR:
                case Aran.HEROS_WILL:
                case Aran.POLEARM_BOOSTER:
                case Aran.COMBO_DRAIN:
                case Aran.SNOW_CHARGE1:
                case Aran.SNOW_CHARGE2:
                case Aran.BODY_PRESSURE:
                case Aran.SMART_KNOCKBACK:
                case Aran.COMBO_BARRIER:
                case Aran.COMBO_ABILITY:
                case Phantom.JUDGEMENT_DRAW1:
                case Phantom.JUDGEMENT_DRAW2:
                case CannonShooter.BARREL_ROULETTE:
                case Evan.ELEMENT_RESET:
                case Evan.MAGIC_SHIELD:
                case Evan.MAGIC_BOOSTER:
                case Evan.MONSTER_RIDER:
                case Beginner.SOARING:
                case Noblesse.SOARING:
                case Legend.SOARING:
                case Evan.SOARING:
                case Mercedes.SOARING:
                case Citizen.SOARING:
                case DemonSlayer.SOARING:
                case Phantom.SOARING:
                case Beginner.HIDDEN_POTENTIAL_EXPLORER:
                case Noblesse.HIDDEN_POTENTIAL_CK:
                case Legend.HIDDEN_POTENTIAL_HERO:
                case Evan.HIDDEN_POTENTIAL_HERO:
                case Mercedes.HIDDEN_POTENTIAL_HERO:
                case Citizen.HIDDEN_POTENTIAL_RESISTANCE:
                case DemonSlayer.HIDDEN_POTENTIAL_RESISTANCE:
                case Phantom.HIDDEN_POTENTIAL_HERO:
                case Paladin.DIVINE_SHIELD:
                case FPArchMage.ARCANE_AIM:
                case FPArchMage.BUFF_MASTERY:
                case Evan.KILLER_WINGS:
                case Evan.MAGIC_RESISTANCE:
                case Evan.ONYX_SHROUD:
                case Evan.PHANTOM_IMPRINT:
                case Evan.MAPLE_WARRIOR:
                case Evan.HEROS_WILL:
                case Evan.SOUL_STONE:
                case Evan.ONYX_BLESSING:
                case Evan.ONYX_WILL:
                case ILArchMage.ARCANE_AIM:
                case ILArchMage.BUFF_MASTERY:
                case Bishop.ARCANE_AIM:
                case Bishop.BUFF_MASTERY:
                case Phantom.CANE_BOOSTER:
                case DemonSlayer.DARK_META:
                case BattleMage.DARK_AURA:
                case BattleMage.YELLOW_AURA:
                case BattleMage.ADV_BLUE_AURA:
                case 32110007: // arua effect
                case 32110008: // aura effect
                case 32110009: // aura effect
                case BattleMage.BODY_BOOSTER:
                case BattleMage.REAPER:
                case BattleMage.BLUE_AURA:
                case BattleMage.ADV_DARK_AURA:
                case BattleMage.ADV_YELLOW_AURA:
                case BattleMage.TWISTER_SPIN:
                case WildHunter.JAGUAR: //jaguar
                case WildHunter.WILD_TRAP: //puppet
                case Mechanic.FLAME_LAUNCHER:
                case Mechanic.PROTOTYPE: //TEMP. mech
                case Mechanic.PORTAL:
                case Mechanic.PERFECT_ARMOR:
                case Mechanic.ENHANCED_FLAME_LAUNCHER:
                case Mechanic.SATELLITE1:
                case Mechanic.ROCK_N_SHOCK:
                case Mechanic.SIEGE:
                case Mechanic.BOT_EX7:
                case Mechanic.SATELLITE2:
                case Mechanic.SATELLITE3:
                case Mechanic.HEAL:
                case Mechanic.DICE: //dice
                case Mechanic.EXTREME:
                case Mechanic.SG88:
                case Mechanic.MISSLE: //missile
                case Mechanic.SATELLITE_SAFETY: //satellite safety
                case Mechanic.BOTS_N_TOTS:
                case Mechanic.AMPLIFIER:
                case Mechanic.SIEGE_MODE:
                case DualBlade.TORNADO_SPIN: //tornado spin
                case DualBlade.FINAL_CUT:
                case Marauder.DICE:
                case Buccaneer.PIRATES_REVENGE:
                case Outlaw.DICE:
                case Corsair.PIRATES_REVENGE:
                case CannonShooter.DIE:
                case CannonShooter.DOUBLE_DOWN:
                case CannonShooter.ANCHORS:
                case CannonShooter.MILITA:
                case Jett.DICE:
                case DualBlade.HEROS_WILL:
                case DualBlade.MAPLE_WARRIOR:
                case DualBlade.KATARA_BOOSTER:
                case Jett.DOUBLE_DOWN:
                case CannonShooter.HEROS_WILL:
		case CannonShooter.CANNON_BOOSTER:
                case WhiteKnight.COMBAT_ORDERS:
                case DualBlade.ADV_DARK_SIGHT: 
                case DualBlade.SHADOW_MELD:    
                case 80001089:
                    isBuff = true;
                    break;
            }
        }
        ret.chargeskill = data.getChildByPath("keydown") != null;
        //some skills have old system, some new		
	final MapleData level = data.getChildByPath("common");
        if (level != null) {
            ret.maxLevel = MapleDataTool.getInt("maxLevel", level, 1); //10 just a failsafe, shouldn't actually happens
            ret.trueMax = ret.maxLevel + (ret.combatOrders ? 2 : 0);
            for (int i = 1; i <= ret.trueMax; i++) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff, i, "x"));
            }

        } else {
            for (final MapleData leve : data.getChildByPath("level")) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(leve, id, isBuff, Byte.parseByte(leve.getName()), null));
            }
            ret.maxLevel = ret.effects.size();
            ret.trueMax = ret.effects.size();
        }
        final MapleData level2 = data.getChildByPath("PVPcommon");
        if (level2 != null) {
            ret.pvpEffects = new ArrayList<MapleStatEffect>();
            for (int i = 1; i <= ret.trueMax; i++) {
                ret.pvpEffects.add(MapleStatEffect.loadSkillEffectFromData(level2, id, isBuff, i, "x"));
            }
        }
        final MapleData reqDataRoot = data.getChildByPath("req");
        if (reqDataRoot != null) {
            for (final MapleData reqData : reqDataRoot.getChildren()) {
                ret.requiredSkill.add(new Pair<Integer, Byte>(Integer.parseInt(reqData.getName()), (byte) MapleDataTool.getInt(reqData, 1)));
            }
        }
        ret.animationTime = 0;
        if (effect != null) {
            for (final MapleData effectEntry : effect) {
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
            }
        }
        return ret;
    }

    public MapleStatEffect getEffect(final int level) {
        if (effects.size() < level) {
            if (effects.size() > 0) { //incAllskill
                return effects.get(effects.size() - 1);
            }
            return null;
        } else if (level <= 0) {
            return effects.get(0);
        }
        return effects.get(level - 1);
    }

    public MapleStatEffect getPVPEffect(final int level) {
        if (pvpEffects == null) {
            return getEffect(level);
        }
        if (pvpEffects.size() < level) {
            if (pvpEffects.size() > 0) { //incAllskill
                return pvpEffects.get(pvpEffects.size() - 1);
            }
            return null;
        } else if (level <= 0) {
            return pvpEffects.get(0);
        }
        return pvpEffects.get(level - 1);
    }

    public int getSkillType() {
        return skillType;
    }

    public List<Integer> getAllAnimation() {
        return animation;
    }

    public int getAnimation() {
        if (animation == null) {
            return -1;
        }
        return animation.get(Randomizer.nextInt(animation.size()));
    }

    public boolean isPVPDisabled() {
        return pvpDisabled;
    }

    public boolean isChargeSkill() {
        return chargeskill;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public boolean hasRequiredSkill() {
        return requiredSkill.size() > 0;
    }

    public List<Pair<Integer, Byte>> getRequiredSkills() {
        return requiredSkill;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getTrueMax() {
        return trueMax;
    }

    public boolean combatOrders() {
        return combatOrders;
    }

    public boolean canBeLearnedBy(int job) {
        int jid = job;
        int skillForJob = id / 10000;
        
        if (skillForJob == 2001) {
            return GameConstants.isEvan(job); //special exception for beginner -.-
        } else if (skillForJob == 0) {
            return GameConstants.isAdventurer(job); //special exception for beginner
        } else if (skillForJob == 1000) {
            return GameConstants.isKOC(job); //special exception for beginner
        } else if (skillForJob == 2000) {
            return GameConstants.isAran(job); //special exception for beginner
        } else if (skillForJob == 3000) {
            return GameConstants.isResist(job); //special exception for beginner
        } else if (skillForJob == 1) {
            return GameConstants.isCannon(job); //special exception for beginner
        } else if (skillForJob == 3001) {
            return GameConstants.isDemon(job); //special exception for beginner
        } else if (skillForJob == 2002) {
            return GameConstants.isMercedes(job); //special exception for beginner
        } else if (skillForJob == 508) {
            return GameConstants.isJett(job); //special exception for beginner
	} else if (skillForJob == 2003) {
            return GameConstants.isPhantom(job); //special exception for beginner
        } else if (jid / 100 != skillForJob / 100) { // wrong job
            return false;
        } else if (jid / 1000 != skillForJob / 1000) { // wrong job
            return false;
        } else if (GameConstants.isPhantom(skillForJob) && !GameConstants.isPhantom(job)) {
            return false;
	} else if (GameConstants.isJett(skillForJob) && !GameConstants.isJett(job)) {
            return false;
	} else if (GameConstants.isCannon(skillForJob) && !GameConstants.isCannon(job)) {
            return false;
        } else if (GameConstants.isDemon(skillForJob) && !GameConstants.isDemon(job)) {
            return false;
        } else if (GameConstants.isAdventurer(skillForJob) && !GameConstants.isAdventurer(job)) {
            return false;
        } else if (GameConstants.isKOC(skillForJob) && !GameConstants.isKOC(job)) {
            return false;
        } else if (GameConstants.isAran(skillForJob) && !GameConstants.isAran(job)) {
            return false;
        } else if (GameConstants.isEvan(skillForJob) && !GameConstants.isEvan(job)) {
            return false;
        } else if (GameConstants.isMercedes(skillForJob) && !GameConstants.isMercedes(job)) {
            return false;
        } else if (GameConstants.isResist(skillForJob) && !GameConstants.isResist(job)) {
            return false;
        } else if ((jid / 10) % 10 == 0 && (skillForJob / 10) % 10 > (jid / 10) % 10) { // wrong 2nd job
            return false;
        } else if ((skillForJob / 10) % 10 != 0 && (skillForJob / 10) % 10 != (jid / 10) % 10) { //wrong 2nd job
            return false;
        } else if (skillForJob % 10 > jid % 10) { // wrong 3rd/4th job
            return false;
        }
        return true;
    }

    public boolean isTimeLimited() {
        return timeLimited;
    }
    public boolean isFourthJobSkill(int skillid) {
         switch (skillid / 10000) {
             case 112:
             case 122:
             case 132:
             case 212:
             case 222:
             case 232:
             case 312:
             case 322:
             case 412:
             case 422:
             case 434:
             case 512:
             case 522:    
             return true;
         }
         return false;
    }
    
    public boolean isThirdJobSkill(int skillid) {
        switch (skillid / 10000) {
            case 111:
            case 121:
            case 131:
            case 211:
            case 221:
            case 231:
            case 311:
            case 321:
            case 411:
            case 421:
            case 511:
            case 521:
                return true;
        }
        return false;
    }
    public boolean isSecondJobSkill(int skillid) {
        switch(skillid / 10000) {
            case 110:
            case 120:
            case 130:
            case 210:
            case 220:
            case 230:
            case 310:
            case 320:
            case 410:
            case 420:
            case 510:
            case 520:
                return true;
        }
        return false;
    }
    public boolean isFourthJob() {
		switch (id) { // I guess imma make an sql table to store these, so that we could max them all out.
			case 3220010:
			case 3120011:
			case 33120010:
                        case 5220014:
                        case 23120011:
                        case 23121008:
                        case 33121005:
			//case 32120009:
			case 5321006:
			case 21120011:
			case 22181004:
			case 4340010:
                        case 4320005:
                        case 5720008:
                        case 5120012:
                        case 5320007:
                        //case 24121004://Aria Phantom skill
                        case 5211009:    
				return false;
		}			
        //resurrection has master level while ult.strafe does not.. wtf, impossible to tell from WZ
        if ((id / 10000) == 2312) { //all 10 skills.
            return true;
        }if (id == 24121009 || id == 24121010) { //LOL WTF U LOSER
            return true;
        }
        if ((getMaxLevel() <= 15 && !invisible && getMasterLevel() <= 0)) {
            return false;
        }
        if (id / 10000 >= 2210 && id / 10000 < 3000) { //evan skill
            return ((id / 10000) % 10) >= 7 || getMasterLevel() > 0;
        }
        if (id / 10000 >= 430 && id / 10000 <= 434) { //db skill
            return ((id / 10000) % 10) == 4 || getMasterLevel() > 0;
        }
        return ((id / 10000) % 10) == 2 && id < 90000000 && !isBeginnerSkill();
    }

    public Element getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public int getMasterLevel() {
        return masterLevel;
    }

    public int getDelay() {
        return delay;
    }

    public int getTamingMob() {
        return eventTamingMob;
    }

    public boolean isBeginnerSkill() {
        int jobId = id / 10000;
        return GameConstants.isBeginnerJob(jobId);
    }

    public boolean isMagic() {
        return magic;
    }

    public boolean isMovement() {
        return casterMove;
    }

    public boolean isPush() {
        return pushTarget;
    }

    public boolean isPull() {
        return pullTarget;
    }

    public boolean isSpecialSkill() {
        int jobId = id / 10000;
        return jobId == 900 || jobId == 800 || jobId == 9000 || jobId == 9200 || jobId == 9201 || jobId == 9202 || jobId == 9203 || jobId == 9204;
    }
}
