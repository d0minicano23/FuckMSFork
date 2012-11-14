/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Calander;

import java.util.Calendar;

/**
 *
 * @author Steven
 */
public class Calander {
    
       public static boolean getEventTime() {
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        switch (Calendar.DAY_OF_WEEK) {
            case 1:
                return time >= 1 && time <= 5;
            case 2:
                return time >= 4 && time <= 9;
            case 3:
                return time >= 7 && time <= 12;
            case 4:
                return time >= 10 && time <= 15;
            case 5:
                return time >= 13 && time <= 18;
            case 6:
                return time >= 16 && time <= 21;
         }
        return time >= 19 && time <= 24;
    }

    
    
    
}
