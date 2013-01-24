 package handling.channel.handler;
 
 import client.MapleBuffStat;
 import client.MapleCharacter;
 import client.MapleClient;
 import client.PlayerStats;
 import client.Skill;
 import client.SkillFactory;
 import client.anticheat.CheatTracker;
 import client.anticheat.CheatingOffense;
 import client.inventory.Item;
 import client.inventory.MapleInventory;
 import client.inventory.MapleInventoryType;
 import client.status.MonsterStatus;
 import client.status.MonsterStatusEffect;
 import constants.GameConstants;
 import constants.skills.*;
 import handling.channel.ChannelServer;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.locks.Lock;
 import org.apache.mina.common.IoSession;
 import server.MapleStatEffect;
 import server.Randomizer;
 import server.life.Element;
 import server.life.ElementalEffectiveness;
 import server.life.MapleMonster;
 import server.life.MapleMonsterStats;
 import server.maps.Event_PyramidSubway;
 import server.maps.MapleMap;
 import server.maps.MapleMapItem;
 import server.maps.MapleMapObject;
 import server.maps.MapleMapObjectType;
 import tools.AttackPair;
 import tools.Pair;
 import tools.data.LittleEndianAccessor;
 import tools.packet.CField;
 import tools.packet.CWvsContext;
 
 public class DamageParse{
    
   public static void applyAttack(AttackInfo attack, Skill theSkill, MapleCharacter player, int attackCount, double maxDamagePerMonster, MapleStatEffect effect, AttackType attack_type){
     if (!player.isAlive()) {
       player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
       return;
     }
                if(GameConstants.isDualBlade(player.getJob())){
                  Skill ads = SkillFactory.getSkill(DualBlade.ADV_DARK_SIGHT);
                  int bof = player.getTotalSkillLevel(ads);
                     if (bof > 0) {
                            MapleStatEffect eff = ads.getEffect(bof);
                         if (Randomizer.nextInt(100) >= eff.getProb()) {                           
                                player.dispelSkill(Rogue.DARK_SIGHT);    
                      }
                     } 
                }
     if ((attack.real) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) {
       player.getCheatTracker().checkAttack(attack.skill, attack.lastAttackTickCount);
     }
     if (attack.skill != 0) {
       if (effect == null) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
       if (GameConstants.isMulungSkill(attack.skill)) {
         if (player.getMapId() / 10000 != 92502)
         {
           return;
         }
         if (player.getMulungEnergy() < 10000) {
           return;
         }
         player.mulung_EnergyModify(false);
       }
       else if (GameConstants.isPyramidSkill(attack.skill)) {
         if (player.getMapId() / 1000000 != 926){
           return;
         }
         if ((player.getPyramidSubway() == null) || (!player.getPyramidSubway().onSkillUse(player))) {
           return;
         }
       }
       else if (GameConstants.isInflationSkill(attack.skill)) {
         if (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null)
           return;
       }
       else if ((attack.targets > effect.getMobCount()) && (attack.skill != WhiteKnight.CHARGE_BLOW) && (attack.skill != Paladin.ADVANCED_CHARGE)) {
         player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
         return;
       }
     }
     if (player.getClient().getChannelServer().isAdminOnly()) {
       player.dropMessage(-1, new StringBuilder().append("Animation: ").append(Integer.toHexString((attack.display & 0x8000) != 0 ? attack.display - 32768 : attack.display)).toString());
     }
     boolean useAttackCount = (attack.skill != ChiefBandit.MESO_EXPLOSION) && (attack.skill != Marksman.SNIPE) && (attack.skill != Mercedes.LIGHTING_EDGE) && ((attack.skill != DragonKnight.DRAGON_BUSTER) || (player.getJob() != 132)) && (attack.skill != Sniper.STRIFE);
 
     if ((attack.hits > 0) && (attack.targets > 0)){
       if (!player.getStat().checkEquipDurabilitys(player, -1)) {
         player.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
         return;
       }
     }
            int totDamage = 0;
            final MapleMap map = player.getMap();
            if (attack.skill == ChiefBandit.MESO_EXPLOSION) { // meso explosion
                for (AttackPair oned : attack.allDamage) {
                    if (oned.attack != null) {
                        continue;
                    }
                    final MapleMapObject mapobject = map.getMapObject(oned.objectid, MapleMapObjectType.ITEM);

                if (mapobject != null) {
                    final MapleMapItem mapitem = (MapleMapItem) mapobject;
                    mapitem.getLock().lock();
                    try {
                        if (mapitem.getMeso() > 0) {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            map.removeMapObject(mapitem);
                            map.broadcastMessage(CField.explodeDrop(mapitem.getObjectId()));
                            mapitem.setPickedUp(true);
                        } else {
                            player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
                            return;
                        }
                    } finally {
                        mapitem.getLock().unlock();
                    }
                } else {
                    player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
                    return; // etc explosion, exploding nonexistant things, etc.
                }
            }
        }

     int totDamageToOneMonster = 0;
     long hpMob = 0L;
     PlayerStats stats = player.getStat();
     int CriticalDamage = stats.passive_sharpeye_percent();
     int ShdowPartnerAttackPercentage = 0;

     if ((attack_type == AttackType.RANGED_WITH_SHADOWPARTNER) || (attack_type == AttackType.NON_RANGED_WITH_MIRROR)) {
       MapleStatEffect shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.SHADOWPARTNER);
       if (shadowPartnerEffect != null) {
         ShdowPartnerAttackPercentage += shadowPartnerEffect.getX();
       }
       attackCount /= 2;
     }
     ShdowPartnerAttackPercentage *= (CriticalDamage + 100) / 100;
     if (attack.skill == Shadower.ASSASSINATE) {
       ShdowPartnerAttackPercentage *= 10;
     }
 
     double maxDamagePerHit = 0.0D;
     MapleMonster monster;
     for (AttackPair oned : attack.allDamage) {
       monster = map.getMonsterByOid(oned.objectid);
 
       if ((monster != null) && (monster.getLinkCID() <= 0)) {
         totDamageToOneMonster = 0;
         hpMob = monster.getMobMaxHp();
         MapleMonsterStats monsterstats = monster.getStats();
         int fixeddmg = monsterstats.getFixedDamage();
         boolean Tempest = (monster.getStatusSourceID(MonsterStatus.FREEZE) == Aran.COMBO_TEMPEST) || (attack.skill == 1221011);
 
         if ((!Tempest) && (!player.isGM())) {
           if (((player.getJob() >= 3200) && (player.getJob() <= 3212) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) || (attack.skill == 3221007) || (attack.skill == 23121003) || (((player.getJob() < 3200) || (player.getJob() > 3212)) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT))))
             maxDamagePerHit = CalculateMaxWeaponDamagePerHit(player, monster, attack, theSkill, effect, maxDamagePerMonster, Integer.valueOf(CriticalDamage));
           else {
             maxDamagePerHit = 1.0D;
           }
         }
         byte overallAttackCount = 0;
 
         int criticals = 0;
         for (Pair eachde : oned.attack) {
           Integer eachd = (Integer)eachde.left;
           overallAttackCount = (byte)(overallAttackCount + 1);
           if (((Boolean)eachde.right).booleanValue()) {
             criticals++;
           }
           if ((useAttackCount) && (overallAttackCount - 1 == attackCount)) {
             maxDamagePerHit = maxDamagePerHit / 100.0D * (ShdowPartnerAttackPercentage * (monsterstats.isBoss() ? stats.bossdam_r : stats.dam_r) / 100.0D);
           }
 
           if (fixeddmg != -1) {
             if (monsterstats.getOnlyNoramlAttack())
               eachd = Integer.valueOf(attack.skill != 0 ? 0 : fixeddmg);
             else {
               eachd = Integer.valueOf(fixeddmg);
             }
           }
           else if (monsterstats.getOnlyNoramlAttack())
             eachd = Integer.valueOf(attack.skill != 0 ? 0 : Math.min(eachd.intValue(), (int)maxDamagePerHit));
           else if (!player.isGM()) {
             if (Tempest) {
               if (eachd.intValue() > monster.getMobMaxHp()) {
                 eachd = Integer.valueOf((int)Math.min(monster.getMobMaxHp(), 2147483647L));
                 player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE);
               }
             }
             else if (((player.getJob() >= 3200) && (player.getJob() <= 3212) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) || (attack.skill == 23121003) || (((player.getJob() < 3200) || (player.getJob() > 3212)) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)))) {
               if (eachd.intValue() > maxDamagePerHit) {
                 player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE, new StringBuilder().append("[Damage: ").append(eachd).append(", Expected: ").append(maxDamagePerHit).append(", Mob: ").append(monster.getId()).append("] [Job: ").append(player.getJob()).append(", Level: ").append(player.getLevel()).append(", Skill: ").append(attack.skill).append("]").toString());
                 if (attack.real) {
                   player.getCheatTracker().checkSameDamage(eachd.intValue(), maxDamagePerHit);
                 }
                 if (eachd.intValue() > maxDamagePerHit * 2.0D) {
                   player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_2, new StringBuilder().append("[Damage: ").append(eachd).append(", Expected: ").append(maxDamagePerHit).append(", Mob: ").append(monster.getId()).append("] [Job: ").append(player.getJob()).append(", Level: ").append(player.getLevel()).append(", Skill: ").append(attack.skill).append("]").toString());
                   eachd = Integer.valueOf((int)(maxDamagePerHit * 2.0D));
                   if (eachd.intValue() >= 2499999) {
                     player.getClient().getSession().close();
                     return;
                   }
                 }
               }
             }
             else if (eachd.intValue() > maxDamagePerHit) {
               eachd = Integer.valueOf((int)maxDamagePerHit);
             }
 
           }
 
           if (player == null) {
             return;
           }
           totDamageToOneMonster += eachd.intValue();
 
           if (((eachd.intValue() == 0) || (monster.getId() == 9700021)) && (player.getPyramidSubway() != null)) {
             player.getPyramidSubway().onMiss(player);
           }
         }
         totDamage += totDamageToOneMonster;
         player.checkMonsterAggro(monster);
 
         if ((GameConstants.getAttackDelay(attack.skill, theSkill) >= 100) && (!GameConstants.isNoDelaySkill(attack.skill)) && (attack.skill != Hunter.ARROW_BOMB) && (!monster.getStats().isBoss()) && (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange))) {
           player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, new StringBuilder().append("[Distance: ").append(player.getTruePosition().distanceSq(monster.getTruePosition())).append(", Expected Distance: ").append(GameConstants.getAttackRange(effect, player.getStat().defRange)).append(" Job: ").append(player.getJob()).append("]").toString());
         }
 
         if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
           switch (attack.skill) {
           case 0:
           case Rogue.DOUBLE_STAB:
           case Bandit.SAVAGE_BLOW:
           case ChiefBandit.ASSAULTER:
           case 4211004:
           case 4221003:
           case Shadower.BOOMERANG_STEP:
             handlePickPocket(player, monster, oned);
           }
         }
 
         if ((totDamageToOneMonster > 0) || (attack.skill == Paladin.HEAVENS_HAMMER) || (attack.skill == Aran.COMBO_TEMPEST)) {
           if (GameConstants.isDemon(player.getJob())) {
             player.handleForceGain(monster.getObjectId(), attack.skill);
           }
           if ((GameConstants.isPhantom(player.getJob())) && (attack.skill != Phantom.CARTE_NOIRE) && (attack.skill != Phantom.CARTE_BLANCHE)) {
                player.handleCardStack();
           }
           if (attack.skill != Paladin.HEAVENS_HAMMER)
             monster.damage(player, totDamageToOneMonster, true, attack.skill);
           else {
             monster.damage(player, monster.getStats().isBoss() ? 500000L : monster.getHp() - 1L, true, attack.skill);
           }
 
           if (monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
             player.addHP(-(7000 + Randomizer.nextInt(8000)));
           }
           player.onAttack(monster.getMobMaxHp(), monster.getMobMaxMp(), attack.skill, monster.getObjectId(), totDamage, 0);
           switch (attack.skill)
           {
           case Rogue.DISORDER:
           case Rogue.DOUBLE_STAB:
           case Rogue.LUCKY_SEVEN:
           case Hermit.AVENGER:
           case Hermit.DARK_FLARE:
           case ChiefBandit.DARK_FLARE:
           case ChiefBandit.ASSAULTER:
           case Shadower.ASSASSINATE:
           case Shadower.BOOMERANG_STEP:
           case DualBlade.TRIPLE_STAB:
           case DualBlade.FATAL_BLOW:
           case DualBlade.SLASH_STORM:
           case DualBlade.BLOODY_STORM:
           case DualBlade.UPPER_STAB:
           case DualBlade.FLY_ASSULTER2:
           case DualBlade.CHAINS_OF_HELL:
           case DualBlade.FINAL_CUT:
           case DualBlade.BLADE_FURY:
           case 4341005:
           case DualBlade.PHANTOM_BLOW:
           case NightWalker.LUCKY_SEVEN:
           case NightWalker.AVENGER:
           case NightWalker.QUAD_STAR: 
           case Hermit.TRIPLE_THROW:
           case NightLord.QUAD_STAR: 
             int[] skills = {DualBlade.VENOM1, DualBlade.VENOM2, DualBlade.TOXIC_VENOM, NightLord.TOXIC_VENOM, Shadower.TOXIC_VENOM, NightWalker.VENOM, ChiefBandit.VENOM, Hermit.VENOM};
             for (int i : skills) {
               Skill skill = SkillFactory.getSkill(i);
               if (player.getTotalSkillLevel(skill) > 0) {
                 MapleStatEffect venomEffect = skill.getEffect(player.getTotalSkillLevel(skill));
                 if (!venomEffect.makeChanceResult()) break;
                 monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.POISON, Integer.valueOf(1), i, null, false), true, venomEffect.getDuration(), true, venomEffect); break;
               }
             }
 
             break;
           case Bandit.STEAL:
             monster.handleSteal(player);
             break;
           case 21000002:
           case 21100001:
           case 21100002:
           case 21100004:
           case 21110002:
           case 21110003:
           case 21110004:
           case 21110006:
           case 21110007:
           case 21110008:
           case 21120002:
           case 21120005:
           case 21120006:
           case 21120009:
           case 21120010:
             if ((player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) && (!monster.getStats().isBoss())) {
               MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.WK_CHARGE);
               if (eff != null) {
                 monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.SPEED, Integer.valueOf(eff.getX()), eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
               }
             }
             if ((player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) == null) || (monster.getStats().isBoss())) break;
             MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BODY_PRESSURE);
             
             if ((eff != null) && (eff.makeChanceResult()) && (!monster.isBuffed(MonsterStatus.NEUTRALISE))) {
               monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.NEUTRALISE, Integer.valueOf(1), eff.getSourceId(), null, false), false, eff.getX() * 1000, true, eff);
             }
             break;
           }

           if (totDamageToOneMonster > 0) {
             Item weapon_ = player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte)-11);
             if (weapon_ != null) {
               MonsterStatus stat = GameConstants.getStatFromWeapon(weapon_.getItemId());
               if ((stat != null) && (Randomizer.nextInt(100) < GameConstants.getStatChance())) {
                 MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(stat, Integer.valueOf(GameConstants.getXForStat(stat)), GameConstants.getSkillForStat(stat), null, false);
                 monster.applyStatus(player, monsterStatusEffect, false, 10000L, false, null);
               }
             }
             if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
               MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BLIND);
 
               if ((eff != null) && (eff.makeChanceResult())) {
                 MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.ACC, Integer.valueOf(eff.getX()), eff.getSourceId(), null, false);
                 monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, true, eff);
               }
             }
 
             if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
               MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.HAMSTRING);
 
               if ((eff != null) && (eff.makeChanceResult())) {
                 MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.SPEED, Integer.valueOf(eff.getX()), 3121007, null, false);
                 monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, true, eff);
               }
             }
             if ((player.getJob() == 121) || (player.getJob() == 122)) {
               Skill skill = SkillFactory.getSkill(1211006);
               if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill)) {
                 MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
                 MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.FREEZE, Integer.valueOf(1), skill.getId(), null, false);
                 monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 2000, true, eff);
               }
             }                       
           }
           if ((effect != null) && (effect.getMonsterStati().size() > 0) && 
             (effect.makeChanceResult()))
             for (Map.Entry z : effect.getMonsterStati().entrySet())
               monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus)z.getKey(), (Integer)z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
         }
       }
     }

     if ((attack.skill == 4331003) && ((hpMob <= 0L) || (totDamageToOneMonster < hpMob))) {
       return;
     }
     if ((hpMob > 0L) && (totDamageToOneMonster > 0)) {
       player.afterAttack(attack.targets, attack.hits, attack.skill);
     }
     if ((attack.skill != 0) && ((attack.targets > 0) || ((attack.skill != 4331003) && (attack.skill != 4341002))) && (!GameConstants.isNoDelaySkill(attack.skill))) {
       effect.applyTo(player, attack.position);
     }
     if ((totDamage > 1) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) {
       CheatTracker tracker = player.getCheatTracker();
 
       tracker.setAttacksWithoutHit(true);
       if (tracker.getAttacksWithoutHit() > 1000)
         tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
     }
   }
 //TODO: FIX MAGIC ATTACK DAMAGE
   public static final void applyAttackMagic(AttackInfo attack, Skill theSkill, MapleCharacter player, MapleStatEffect effect, double maxDamagePerHit){
     if (!player.isAlive()) {
       player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
       return;
     }
     if ((attack.real) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) {
       player.getCheatTracker().checkAttack(attack.skill, attack.lastAttackTickCount);
     }
 
     if ((attack.hits > effect.getAttackCount()) || (attack.targets > effect.getMobCount())) {
       player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
       return;
     }
     if ((attack.hits > 0) && (attack.targets > 0) && 
       (!player.getStat().checkEquipDurabilitys(player, -1))) {
       player.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
       return;
     }
 
     if (GameConstants.isMulungSkill(attack.skill)) {
       if (player.getMapId() / 10000 != 92502)
       {
         return;
       }
       if (player.getMulungEnergy() < 10000) {
         return;
       }
       player.mulung_EnergyModify(false);
     }
     else if (GameConstants.isPyramidSkill(attack.skill)) {
       if (player.getMapId() / 1000000 != 926)
       {
         return;
       }
       if ((player.getPyramidSubway() == null) || (!player.getPyramidSubway().onSkillUse(player))) {
         return;
       }
     }
     else if ((GameConstants.isInflationSkill(attack.skill)) && 
       (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null)) {
       return;
     }
 
     if (player.getClient().getChannelServer().isAdminOnly()) {
       player.dropMessage(-1, new StringBuilder().append("Animation: ").append(Integer.toHexString((attack.display & 0x8000) != 0 ? attack.display - 32768 : attack.display)).toString());
     }
     PlayerStats stats = player.getStat();
     Element element = player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null ? Element.NEUTRAL : theSkill.getElement();
 
     double MaxDamagePerHit = 0.0D;
     int totDamage = 0;
 
     int CriticalDamage = stats.passive_sharpeye_percent();
     Skill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob(player.getJob()));
     int eaterLevel = player.getTotalSkillLevel(eaterSkill);
 
     MapleMap map = player.getMap();
 
     for (AttackPair oned : attack.allDamage) {
       MapleMonster monster = map.getMonsterByOid(oned.objectid);
 
       if ((monster != null) && (monster.getLinkCID() <= 0)) {
         boolean Tempest = (monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006) && (!monster.getStats().isBoss());
         int totDamageToOneMonster = 0;
         MapleMonsterStats monsterstats = monster.getStats();
         int fixeddmg = monsterstats.getFixedDamage();
         if ((!Tempest) && (!player.isGM())) {
           if ((!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)))
             MaxDamagePerHit = CalculateMaxMagicDamagePerHit(player, theSkill, monster, monsterstats, stats, element, Integer.valueOf(CriticalDamage), maxDamagePerHit, effect);
           else {
             MaxDamagePerHit = 1.0D;
           }
         }
         byte overallAttackCount = 0;
 
         for (Pair eachde : oned.attack) {
           Integer eachd = (Integer)eachde.left;
           overallAttackCount = (byte)(overallAttackCount + 1);
           if (fixeddmg != -1) {
             eachd = Integer.valueOf(monsterstats.getOnlyNoramlAttack() ? 0 : fixeddmg);
           }
           else if (monsterstats.getOnlyNoramlAttack())
             eachd = Integer.valueOf(0);
           else if (!player.isGM())
           {
             if (Tempest)
             {
               if (eachd.intValue() > monster.getMobMaxHp()) {
                 eachd = Integer.valueOf((int)Math.min(monster.getMobMaxHp(), 2147483647L));
                 player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_MAGIC);
               }
             } else if ((!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) {
               if (eachd.intValue() > MaxDamagePerHit) {
                 player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_MAGIC, new StringBuilder().append("[Damage: ").append(eachd).append(", Expected: ").append(MaxDamagePerHit).append(", Mob: ").append(monster.getId()).append("] [Job: ").append(player.getJob()).append(", Level: ").append(player.getLevel()).append(", Skill: ").append(attack.skill).append("]").toString());
                 if (attack.real) {
                   player.getCheatTracker().checkSameDamage(eachd.intValue(), MaxDamagePerHit);
                 }
                 if (eachd.intValue() > MaxDamagePerHit * 2.0D)
                 {
                   player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_MAGIC_2, new StringBuilder().append("[Damage: ").append(eachd).append(", Expected: ").append(MaxDamagePerHit).append(", Mob: ").append(monster.getId()).append("] [Job: ").append(player.getJob()).append(", Level: ").append(player.getLevel()).append(", Skill: ").append(attack.skill).append("]").toString());
                   eachd = Integer.valueOf((int)(MaxDamagePerHit * 2.0D));
 
                   if (eachd.intValue() >= 2499999) {
                     player.getClient().getSession().close();
                     return;
                   }
                 }
               }
             }
             else if (eachd.intValue() > MaxDamagePerHit) {
               eachd = Integer.valueOf((int)MaxDamagePerHit);
             }
 
           }
 
           totDamageToOneMonster += eachd.intValue();
         }
         totDamage += totDamageToOneMonster;
         player.checkMonsterAggro(monster);
 
         if ((GameConstants.getAttackDelay(attack.skill, theSkill) >= 100) && (!GameConstants.isNoDelaySkill(attack.skill)) && (!monster.getStats().isBoss()) && (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange))) {
           player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, new StringBuilder().append("[Distance: ").append(player.getTruePosition().distanceSq(monster.getTruePosition())).append(", Expected Distance: ").append(GameConstants.getAttackRange(effect, player.getStat().defRange)).append(" Job: ").append(player.getJob()).append("]").toString());
         }
         if ((attack.skill == 2301002) && (!monsterstats.getUndead())) {
           player.getCheatTracker().registerOffense(CheatingOffense.HEAL_ATTACKING_UNDEAD);
           return;
         }


         if (totDamageToOneMonster  > 0) {
           monster.damage(player, totDamageToOneMonster , true, attack.skill);
           if (monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)) {
             player.addHP(-(7000 + Randomizer.nextInt(8000)));
           }
           if (player.getBuffedValue(MapleBuffStat.SLOW) != null) {
             MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.SLOW);
 
             if ((eff != null) && (eff.makeChanceResult()) && (!monster.isBuffed(MonsterStatus.SPEED))) {
               monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.SPEED, Integer.valueOf(eff.getX()), eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
             }
 
           }
 
           player.onAttack(monster.getMobMaxHp(), monster.getMobMaxMp(), attack.skill, monster.getObjectId(), totDamage, 0);
 
           switch (attack.skill) {
           case 2221003:
             monster.setTempEffectiveness(Element.ICE, effect.getDuration());
             break;
           case 2121003:
             monster.setTempEffectiveness(Element.FIRE, effect.getDuration());
           }
 
           if ((effect != null) && (effect.getMonsterStati().size() > 0) && 
             (effect.makeChanceResult())) {
             for (Map.Entry z : effect.getMonsterStati().entrySet()) {
               monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus)z.getKey(), (Integer)z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
             }
           }
 
           if (eaterLevel > 0) {
             eaterSkill.getEffect(eaterLevel).applyPassive(player, monster);
           }
         }
       }
     }
     if (attack.skill != 2301002) {
       effect.applyTo(player);
     }
 
     if ((totDamage > 1) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) {
       CheatTracker tracker = player.getCheatTracker();
       tracker.setAttacksWithoutHit(true);
 
       if (tracker.getAttacksWithoutHit() > 1000)
         tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
     }
   }

 
   private static final double CalculateMaxMagicDamagePerHit(MapleCharacter chr, Skill skill, MapleMonster monster, MapleMonsterStats mobstats, PlayerStats stats, Element elem, Integer sharpEye, double maxDamagePerMonster, MapleStatEffect attackEffect)
   {
     int dLevel = Math.max(mobstats.getLevel() - chr.getLevel(), 0) * 2;
     int HitRate = Math.min((int)Math.floor(Math.sqrt(stats.getAccuracy())) - (int)Math.floor(Math.sqrt(mobstats.getEva())) + 100, 100);
     if (dLevel > HitRate) {
       HitRate = dLevel;
     }
     HitRate -= dLevel;
     if ((HitRate <= 0) && ((!GameConstants.isBeginnerJob(skill.getId() / 10000)) || (skill.getId() % 10000 != 1000))) {
       return 0.0D;
     }
 
     int CritPercent = sharpEye.intValue();
     ElementalEffectiveness ee = monster.getEffectiveness(elem);
     double elemMaxDamagePerMob;
     switch (ee) {
     case IMMUNE:
       elemMaxDamagePerMob = 1.0D;
       break;
     default:
       elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * ee.getValue(), stats);
     }
 
     int MDRate = monster.getStats().getMDRate();
     MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.MDEF);
     if (pdr != null) {
       MDRate += pdr.getX().intValue();
     }
     elemMaxDamagePerMob -= elemMaxDamagePerMob * (Math.max(MDRate - stats.ignoreTargetDEF - attackEffect.getIgnoreMob(), 0) / 100.0D);
 
     elemMaxDamagePerMob += elemMaxDamagePerMob / 100.0D * CritPercent;
 
     elemMaxDamagePerMob *= (monster.getStats().isBoss() ? chr.getStat().bossdam_r : chr.getStat().dam_r) / 100.0D;
     MonsterStatusEffect imprint = monster.getBuff(MonsterStatus.IMPRINT);
     if (imprint != null) {
       elemMaxDamagePerMob += elemMaxDamagePerMob * imprint.getX().intValue() / 100.0D;
     }
     elemMaxDamagePerMob += elemMaxDamagePerMob * chr.getDamageIncrease(monster.getObjectId()) / 100.0D;
     if (GameConstants.isBeginnerJob(skill.getId() / 10000)) {
       switch (skill.getId() % 10000) {
       case 1000:
         elemMaxDamagePerMob = 40.0D;
         break;
       case 1020:
         elemMaxDamagePerMob = 1.0D;
         break;
       case 1009:
         elemMaxDamagePerMob = monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L : monster.getMobMaxHp();
       }
     }
 
     switch (skill.getId()) {
     case 32001000:
     case 32101000:
     case 32111002:
     case 32121002:
       elemMaxDamagePerMob *= 1.5D;
     }
 
     if (elemMaxDamagePerMob > 999999.0D)
       elemMaxDamagePerMob = 999999.0D;
     else if (elemMaxDamagePerMob <= 0.0D) {
       elemMaxDamagePerMob = 1.0D;
     }
 
     return elemMaxDamagePerMob;
   }
 
        private static final double ElementalStaffAttackBonus(final Element elem, double elemMaxDamagePerMob, final PlayerStats stats) {
            switch (elem) {
            case FIRE:
                return (elemMaxDamagePerMob / 100) * (stats.element_fire + stats.getElementBoost(elem));
            case ICE:
                return (elemMaxDamagePerMob / 100) * (stats.element_ice + stats.getElementBoost(elem));
            case LIGHTING:
                return (elemMaxDamagePerMob / 100) * (stats.element_light + stats.getElementBoost(elem));
            case POISON:
                return (elemMaxDamagePerMob / 100) * (stats.element_psn + stats.getElementBoost(elem));
            default:
                return (elemMaxDamagePerMob / 100) * (stats.def + stats.getElementBoost(elem));
            }
        }

        private static void handlePickPocket(final MapleCharacter player, final MapleMonster mob, AttackPair oned) {
              final int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();

              for (final Pair<Integer, Boolean> eachde : oned.attack) {
                  final Integer eachd = eachde.left;
                  if (player.getStat().pickRate >= 100 || Randomizer.nextInt(99) < player.getStat().pickRate) {
                      player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachd / (double) 20000) * (double) maxmeso, (double) 1), maxmeso), new Point((int) (mob.getTruePosition().getX() + Randomizer.nextInt(100) - 50), (int) (mob.getTruePosition().getY())), mob, player, false, (byte) 0);
                  }
              }
          }
   private static double CalculateMaxWeaponDamagePerHit(MapleCharacter player, MapleMonster monster, AttackInfo attack, Skill theSkill, MapleStatEffect attackEffect, double maximumDamageToMonster, Integer CriticalDamagePercent)
   {
     int dLevel = Math.max(monster.getStats().getLevel() - player.getLevel(), 0) * 2;
     int HitRate = Math.min((int)Math.floor(Math.sqrt(player.getStat().getAccuracy())) - (int)Math.floor(Math.sqrt(monster.getStats().getEva())) + 100, 100);
     if (dLevel > HitRate) {
       HitRate = dLevel;
     }
     HitRate -= dLevel;
     if ((HitRate <= 0) && ((!GameConstants.isBeginnerJob(attack.skill / 10000)) || (attack.skill % 10000 != 1000)) && (!GameConstants.isPyramidSkill(attack.skill)) && (!GameConstants.isMulungSkill(attack.skill)) && (!GameConstants.isInflationSkill(attack.skill))) {
       return 0.0D;
     }
     if ((player.getMapId() / 1000000 == 914) || (player.getMapId() / 1000000 == 927)) {
       return 999999.0D;
     }
 
      List<Element> elements = new ArrayList<Element>();
     boolean defined = false;
     int CritPercent = CriticalDamagePercent.intValue();
     int PDRate = monster.getStats().getPDRate();
     MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.WDEF);
     if (pdr != null) {
       PDRate += pdr.getX().intValue();
     }
     if (theSkill != null) {
       elements.add(theSkill.getElement());
       if (GameConstants.isBeginnerJob(theSkill.getId() / 10000)) {
         switch (theSkill.getId() % 10000) {
         case 1000:
           maximumDamageToMonster = 40.0D;
           defined = true;
           break;
         case 1020:
           maximumDamageToMonster = 1.0D;
           defined = true;
           break;
         case 1009:
           maximumDamageToMonster = monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L : monster.getMobMaxHp();
           defined = true;
         }
       }
 
       switch (theSkill.getId()) {
       case 1311005:
         PDRate = monster.getStats().isBoss() ? PDRate : 0;
         break;
       case 3221001:
       case 33101001:
         maximumDamageToMonster *= attackEffect.getMobCount();
         defined = true;
         break;
       case 3101005:
         defined = true;
         break;
       case 32001000:
       case 32101000:
       case 32111002:
       case 32121002:
         maximumDamageToMonster *= 1.5D;
         break;
       case 1221009:
       case 3221007:
       case 4331003:
       case 23121003:
         if (monster.getStats().isBoss()) break;
         maximumDamageToMonster = monster.getMobMaxHp();
         defined = true; break;
       case 1221011:
       case 21120006:
         maximumDamageToMonster = monster.getStats().isBoss() ? 500000L : monster.getHp() - 1L;
         defined = true;
         break;
       case 3211006:
         if (monster.getStatusSourceID(MonsterStatus.FREEZE) != 3211003) break;
         defined = true;
         maximumDamageToMonster = 999999.0D;
       }
 
     }
 
     double elementalMaxDamagePerMonster = maximumDamageToMonster;
     if ((player.getJob() == 311) || (player.getJob() == 312) || (player.getJob() == 321) || (player.getJob() == 322))
     {
       Skill mortal = SkillFactory.getSkill((player.getJob() == 311) || (player.getJob() == 312) ? 3110001 : 3210001);
       if (player.getTotalSkillLevel(mortal) > 0) {
         MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
         if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
           elementalMaxDamagePerMonster = 999999.0D;
           defined = true;
           if (mort.getZ() > 0)
             player.addHP(player.getStat().getMaxHp() * mort.getZ() / 100);
         }
       }
     }
     else if ((player.getJob() == 221) || (player.getJob() == 222))
     {
       Skill mortal = SkillFactory.getSkill(2210000);
       if (player.getTotalSkillLevel(mortal) > 0) {
         MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
         if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
           elementalMaxDamagePerMonster = 999999.0D;
           defined = true;
         }
       }
     }
     if ((!defined) || ((theSkill != null) && ((theSkill.getId() == 33101001) || (theSkill.getId() == 3221001)))) {
       if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
         int chargeSkillId = player.getBuffSource(MapleBuffStat.WK_CHARGE);
 
         switch (chargeSkillId) {
         case 1211003:
         case 1211004:
           elements.add(Element.FIRE);
           break;
         case 1211005:
         case 1211006:
         case 21111005:
           elements.add(Element.ICE);
           break;
         case 1211007:
         case 1211008:
         case 15101006:
           elements.add(Element.LIGHTING);
           break;
         case 1221003:
         case 1221004:
         case 11111007:
           elements.add(Element.HOLY);
           break;
         case 12101005:
         }
 
       }
 
       if (player.getBuffedValue(MapleBuffStat.LIGHTNING_CHARGE) != null) {
         elements.add(Element.LIGHTING);
       }
       if (player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null)
         elements.clear();
       double elementalEffect;
       if (elements.size() > 0)
       {
         switch (attack.skill) {
         case 3111003:
         case 3211003:
           elementalEffect = attackEffect.getX() / 100.0D;
           break;
         default:
           elementalEffect = 0.5D / elements.size();
         }
 
           for (Element element : elements) {
                    switch (monster.getEffectiveness(element)) {
                        case IMMUNE:
                            elementalMaxDamagePerMonster = 1;
                            break;
                        case WEAK:
                            elementalMaxDamagePerMonster *= (1.0 + elementalEffect + player.getStat().getElementBoost(element));
                            break;
                        case STRONG:
                            elementalMaxDamagePerMonster *= (1.0 - elementalEffect - player.getStat().getElementBoost(element));
                            break;
                    }
                }
            }
            
 
       elementalMaxDamagePerMonster -= elementalMaxDamagePerMonster * (Math.max(PDRate - Math.max(player.getStat().ignoreTargetDEF, 0) - Math.max(attackEffect == null ? 0 : attackEffect.getIgnoreMob(), 0), 0) / 100.0D);
 
       elementalMaxDamagePerMonster += elementalMaxDamagePerMonster / 100.0D * CritPercent;
 
       MonsterStatusEffect imprint = monster.getBuff(MonsterStatus.IMPRINT);
       if (imprint != null) {
         elementalMaxDamagePerMonster += elementalMaxDamagePerMonster * imprint.getX().intValue() / 100.0D;
       }
 
       elementalMaxDamagePerMonster += elementalMaxDamagePerMonster * player.getDamageIncrease(monster.getObjectId()) / 100.0D;
       elementalMaxDamagePerMonster *= ((monster.getStats().isBoss()) && (attackEffect != null) ? player.getStat().bossdam_r + attackEffect.getBossDamage() : player.getStat().dam_r) / 100.0D;
     }
     if (elementalMaxDamagePerMonster > 999999.0D) {
       if (!defined)
         elementalMaxDamagePerMonster = 999999.0D;
     }
     else if (elementalMaxDamagePerMonster <= 0.0D) {
       elementalMaxDamagePerMonster = 1.0D;
     }
     return elementalMaxDamagePerMonster;
   }
 
    public static final AttackInfo DivideAttack(final AttackInfo attack, final int rate) {
                 attack.real = false;
                   if (rate <= 1) {
                      return attack; //lol
                   }
                   for (AttackPair p : attack.allDamage) {
                     if (p.attack != null) {
                        for (Pair<Integer, Boolean> eachd : p.attack) {
                               eachd.left /= rate; //too ex.
                         }
                      }
                    }
                  return attack;
             }
 
   public static final AttackInfo Modify_AttackCrit(AttackInfo attack, MapleCharacter chr, int type, MapleStatEffect effect)
   {
     int CriticalRate;
     boolean shadow;
     List damages;
     List damage;
     if ((attack.skill != 4211006) && (attack.skill != 3211003) && (attack.skill != 4111004)) {
       CriticalRate = chr.getStat().passive_sharpeye_rate() + (effect == null ? 0 : effect.getCr());
       shadow = (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) && ((type == 1) || (type == 2));
       damages = new ArrayList(); damage = new ArrayList();
 
       for (AttackPair p : attack.allDamage) {
         if (p.attack != null) {
           int hit = 0;
           int mid_att = shadow ? p.attack.size() / 2 : p.attack.size();
                      //TODO:THIS IS WHERE YOU PROBABLY ADD SHADOW MELD 100% CRIT EFFECT!
           int toCrit = (attack.skill == 4221001) || (attack.skill == 3221007) || (attack.skill == 23121003) || (attack.skill == 4341005) || (attack.skill == 4331006) || (attack.skill == 21120005) ? mid_att : 0;
           if (toCrit == 0) {
             for (Pair eachd : p.attack) {
               if ((!((Boolean)eachd.right).booleanValue()) && (hit < mid_att)) {
                 if ((((Integer)eachd.left).intValue() > 999999) || (Randomizer.nextInt(100) < CriticalRate)) {
                   toCrit++;
                 }
                 damage.add(eachd.left);
               }
               hit++;
             }
             if (toCrit == 0) {
               damage.clear();
               continue;
             }
             Collections.sort(damage);
             for (int i = damage.size(); i > damage.size() - toCrit; i--) {
               damages.add(damage.get(i - 1));
             }
             damage.clear();
           }
           hit = 0;
           for (Pair eachd : p.attack) {
             if (!((Boolean)eachd.right).booleanValue()) {
               if (attack.skill == 4221001)
                 eachd.right = Boolean.valueOf(hit == 3);
               else if ((attack.skill == 3221007) || (attack.skill == 23121003) || (attack.skill == 21120005) || (attack.skill == 4341005) || (attack.skill == 4331006) || (((Integer)eachd.left).intValue() > 999999))
                 eachd.right = Boolean.valueOf(true);
               else if (hit >= mid_att) {
                 eachd.right = ((Pair)p.attack.get(hit - mid_att)).right;
               }
               else {
                 eachd.right = Boolean.valueOf(damages.contains(eachd.left));
               }
             }
             hit++;
           }
           damages.clear();
         }
       }
     }
     return attack;
   }
 
   public static final AttackInfo parseDmgMa(LittleEndianAccessor lea, MapleCharacter chr){
    
                try {
       AttackInfo ret = new AttackInfo();
 
       lea.skip(1);
       ret.tbyte = lea.readByte();
 
       ret.targets = (byte)(ret.tbyte >>> 4 & 0xF);
       ret.hits = (byte)(ret.tbyte & 0xF);
       ret.skill = lea.readInt();
       if (ret.skill >= 91000000) {
         return null;
       }
       lea.skip(GameConstants.GMS ? 9 : 17);
              
       if (GameConstants.isMagicChargeSkill(ret.skill))
         ret.charge = lea.readInt();
       else {
         ret.charge = -1;
       }
       ret.unk = lea.readByte();
       ret.display = lea.readUShort();
 
       lea.skip(4);
       lea.skip(1);
       ret.speed = lea.readByte();
       ret.lastAttackTickCount = lea.readInt();
       lea.skip(4);
 
       ret.allDamage = new ArrayList();
 
       for (int i = 0; i < ret.targets; i++) {
         int oid = lea.readInt();
 
         lea.skip(18);
 
         List allDamageNumbers = new ArrayList();
 
         for (int j = 0; j < ret.hits; j++) {
           int damage = lea.readInt();
           allDamageNumbers.add(new Pair(Integer.valueOf(damage), Boolean.valueOf(false)));
         }
 
         lea.skip(4);
         ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
       }
       if (lea.available() >= 4L) {
         ret.position = lea.readPos();
       }
       return ret;
     } catch (Exception e) {
       e.printStackTrace();
     }return null;
   }
 
   public static final AttackInfo parseDmgM(LittleEndianAccessor lea, MapleCharacter chr){
     AttackInfo ret = new AttackInfo();
     lea.skip(1);
     ret.tbyte = lea.readByte();
 
     ret.targets = (byte)(ret.tbyte >>> 4 & 0xF);
     ret.hits = (byte)(ret.tbyte & 0xF);
     ret.skill = lea.readInt();
     if (ret.skill >= 91000000) {
       return null;
     }
     lea.skip(9);
        switch (ret.skill) {
            case 24121000:// mille
            case 24121005://tempest
            case 5101004: // Corkscrew
            case 15101003: // Cygnus corkscrew
            case 5201002: // Gernard
            case 14111006: // Poison bomb
            case 4341002:
            case 4341003:
            case 5301001:
            case 5300007:
            case 31001000: // grim scythe
            case 31101000: // soul eater
            case 31111005: // carrion breath
                ret.charge = lea.readInt();
                break;
            default:
                ret.charge = 0;
                break;
        }
 
     ret.unk = lea.readByte();
     ret.display = lea.readUShort();
     lea.skip(4);
     lea.skip(1);
     
     if ((ret.skill == 5300007) || (ret.skill == 5101012) || (ret.skill == 5081001) || (ret.skill == 15101010)) {
       lea.readInt();
     }
     if(ret.skill == Phantom.TEMPEST){
        lea.readInt();
     }
    // if((ret.skill == Priest.TELEPORT_MASTERY) || (ret.skill == FPMage.TELEPORT_MASTERY) || (ret.skill == ILMage.TELEPORT_MASTERY) || (ret.skill == BattleMage.TELEPORT_MASTERY)
    //   || (ret.skill == Evan.TELEPORT_MASTERY) || (ret.skill == BlazeWizard.TELEPORT_MASTERY) || (ret.skill == Aran.BODY_PRESSURE)){
     //    lea.skip(9);
    // }          
     ret.speed = lea.readByte();
     ret.lastAttackTickCount = lea.readInt();
      
     if(ret.skill == BattleMage.TWISTER_SPIN){
          lea.skip(4);
     } else {
          lea.skip(8);
     }
 
     ret.allDamage = new ArrayList();
 
     if (ret.skill == ChiefBandit.MESO_EXPLOSION) {
       return parseMesoExplosion(lea, ret, chr);
     }
 
     if (ret.skill == Phantom.MILLIE) {
       lea.readInt();
     }
     for (int i = 0; i < ret.targets; i++) {
       int oid = lea.readInt();
 
       lea.skip(18);
 
       List allDamageNumbers = new ArrayList();
 
       for (int j = 0; j < ret.hits; j++) {
         int damage = lea.readInt();
 
         allDamageNumbers.add(new Pair(Integer.valueOf(damage), Boolean.valueOf(false)));
       }
       lea.skip(4);
       ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
     }
     ret.position = lea.readPos();
     return ret;
   }
 
   public static final AttackInfo parseDmgR(LittleEndianAccessor lea, MapleCharacter chr)
   {
     AttackInfo ret = new AttackInfo();
     lea.skip(1);
     ret.tbyte = lea.readByte();
 
     ret.targets = (byte)(ret.tbyte >>> 4 & 0xF);
     ret.hits = (byte)(ret.tbyte & 0xF);
     ret.skill = lea.readInt();
     if (ret.skill >= 91000000) {
       return null;
     }
     lea.skip(10);
  switch (ret.skill) {
            case 3121004: // Hurricane
            case 3221001: // Pierce
            case 5221004: // Rapidfire
            case 5721001: // Rapidfire
            case 13111002: // Cygnus Hurricane
            case 33121009:
            case 35001001:
            case 5711002:
            case 35101009:
            case 23121000:
            case 5311002:
            case 24121000:
                lea.skip(4); // extra 4 bytes
                break;
        }
 
     ret.charge = -1;
     ret.unk = lea.readByte();
     ret.display = lea.readUShort();
     lea.skip(4);
     lea.skip(1);
     if (ret.skill == 23111001) {
       lea.skip(4);
       lea.skip(4);
 
       lea.skip(4);
     }
     ret.speed = lea.readByte();
     ret.lastAttackTickCount = lea.readInt();
     lea.skip(4);
     ret.slot = (byte)lea.readShort();
     ret.csstar = (byte)lea.readShort();
     ret.AOE = lea.readByte();
 
     ret.allDamage = new ArrayList();
 
     for (int i = 0; i < ret.targets; i++) {
       int oid = lea.readInt();
 
       lea.skip(18);
 
       List allDamageNumbers = new ArrayList();
       for (int j = 0; j < ret.hits; j++) {
         int damage = lea.readInt();
         allDamageNumbers.add(new Pair(Integer.valueOf(damage), Boolean.valueOf(false)));
       }
 
       lea.skip(4);
 
       ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
     }
     lea.skip(4);
     ret.position = lea.readPos();
 
     return ret;
   }
 
    public static final AttackInfo parseMesoExplosion(final LittleEndianAccessor lea, final AttackInfo ret, final MapleCharacter chr) {
        //System.out.println(lea.toString(true));
        byte bullets;
        if (ret.hits == 0) {
            lea.skip(4);
            bullets = lea.readByte();
            for (int j = 0; j < bullets; j++) {
                ret.allDamage.add(new AttackPair(Integer.valueOf(lea.readInt()), null));
                lea.skip(1);
            }
            lea.skip(2); // 8F 02
            return ret;
        }
        int oid;
        List<Pair<Integer, Boolean>> allDamageNumbers;

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            //if (chr.getMap().isTown()) {
            //    final MapleMonster od = chr.getMap().getMonsterByOid(oid);
            //    if (od != null && od.getLinkCID() > 0) {
            //	    return null;
            //    }
            //}
            lea.skip(12);
            bullets = lea.readByte();
            allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
            for (int j = 0; j < bullets; j++) {
                allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf(lea.readInt()), false)); //m.e. never crits
            }
            ret.allDamage.add(new AttackPair(Integer.valueOf(oid), allDamageNumbers));
            lea.skip(4); // C3 8F 41 94, 51 04 5B 01
        }
        lea.skip(4);
        bullets = lea.readByte();

        for (int j = 0; j < bullets; j++) {
            ret.allDamage.add(new AttackPair(Integer.valueOf(lea.readInt()), null));
            lea.skip(2);
        }
        // 8F 02/ 63 02

        return ret;
    }
    

}