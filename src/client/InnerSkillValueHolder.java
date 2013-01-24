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

public class InnerSkillValueHolder {
    
    private int skillId = 0;
    private byte skillLevel = 0;
    private byte maxLevel = 0;
    private byte rank = 0;
    
    public InnerSkillValueHolder(int skillId, byte skillLevel, byte maxLevel, byte rank){
        this.skillId = skillId;
        this.skillLevel = skillLevel;
        this.maxLevel = maxLevel;
        this.rank = rank;
    }
    
    public int getSkillId(){
        return skillId;
    }
    
    public byte getSkillLevel(){
        return skillLevel;
    }
    
    public byte getMaxLevel(){
        return maxLevel;
    }
    
    public byte getRank(){
        return rank;
    }
}
