package server;

import constants.ServerConstants;
import java.sql.SQLException;

import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.World;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import server.Timer.*;
import tools.packet.CWvsContext;

 public class ShutdownServer implements ShutdownServerMBean {

    public static ShutdownServer instance;

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            instance = new ShutdownServer();
            mBeanServer.registerMBean(instance, new ObjectName("server:type=ShutdownServer"));
        } catch (Exception e) {
            System.out.println("Error registering Shutdown MBean");
            e.printStackTrace();
        }
    }

    public static ShutdownServer getInstance() {
	return instance;
    }

    public int mode = 0;

    public void shutdown() {//can execute twice
	run();
    }

    @Override
    public void run() {
        int exit= 0;
	if (mode == 0) {
	    int ret = 0;
	    World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "The world is going to shutdown soon. Please log off safely."));
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.setShutdown();
		cs.setServerMessage("The world is going to shutdown soon. Please log off safely.");
                ret += cs.closeAllMerchant();
                exit++;
                if(exit==ServerConstants.NUM_CHANNELS){
                     break;
                }
            }
            World.Guild.save();
            World.Alliance.save();
	    World.Family.save();
	    System.out.println("Shutdown 1 has completed. Hired merchants saved: " + ret);
	    mode++;
	} else if (mode == 1) {
	    mode++;
			System.out.println("Shutdown 2 commencing...");
            try {
	        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "The world is going to shutdown now. Please log off safely."));
                Integer[] chs =  ChannelServer.getAllInstance().toArray(new Integer[0]);
        
                for (int i : chs) {
                    try {
                        ChannelServer cs = ChannelServer.getInstance(i);
                        synchronized (this) {
                            cs.shutdown();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
	        LoginServer.shutdown();
                CashShopServer.shutdown();
                DatabaseConnection.closeAll();
            } catch (SQLException e) {
                System.err.println("THROW" + e);
            }
            WorldTimer.getInstance().stop();
            MapTimer.getInstance().stop();
            BuffTimer.getInstance().stop();
            CloneTimer.getInstance().stop();
            EventTimer.getInstance().stop();
	    EtcTimer.getInstance().stop();
	    PingTimer.getInstance().stop();
		System.out.println("Shutdown 2 has finished.");
		try{
                Thread.sleep(5000);
            }catch(Exception e) {
                //shutdown
            }
            System.exit(0); //not sure if this is really needed for ChannelServer
	}
    }
}
