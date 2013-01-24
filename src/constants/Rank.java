/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;


public class Rank {
       public static enum PlayerGMRank {

        NORMAL('@', 0),
        DONATOR('#', 1),
        SUPERDONATOR('$', 2),
        INTERN('%', 3),
        GM('!', 4),
        SUPERGM('!', 5),
        ADMIN('!', 6);
        private char commandPrefix;
        private int level;

        PlayerGMRank(char ch, int level){
            commandPrefix = ch;
            this.level = level;
        }
        
        public char getCommandPrefix(){
            return commandPrefix;
        }

        public int getLevel(){
            return level;
        }
    }
    
     public static enum CommandType {

        NORMAL(0),
        TRADE(1),
        POKEMON(2);
        private int level;

        CommandType(int level){
            this.level = level;
        }

        public int getType(){
            return level;
        }
    }
}
