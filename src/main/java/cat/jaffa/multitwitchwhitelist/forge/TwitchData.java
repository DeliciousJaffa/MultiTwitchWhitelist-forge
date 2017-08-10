package cat.jaffa.multitwitchwhitelist.forge;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Jaffa on 04/08/2017.
 */




public class TwitchData {
    private static HashMap<UUID,WhitelistData> data = new HashMap<UUID,WhitelistData>();
    public static WhitelistData get (EntityPlayer player)
    {
        return data.get(player.getUniqueID());
    }
    static void set (EntityPlayer player, WhitelistData obj){
        data.put(player.getUniqueID(),obj);
    }

    public static int getID (EntityPlayer player)
    {
        return data.get(player.getUniqueID()).getUser().getId();
    }

    public static String getUsername (EntityPlayer player)
    {
        return data.get(player.getUniqueID()).getUser().getUsername();
    }

    public static String getDisplayname (EntityPlayer player)
    {
        return data.get(player.getUniqueID()).getUser().getDisplayname();
    }

    public static Date getCreated (EntityPlayer player)
    {
        return data.get(player.getUniqueID()).getUser().getAccountcreation();
    }
}