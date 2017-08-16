package cat.jaffa.multitwitchwhitelist.forge;

import com.mojang.authlib.GameProfile;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Jaffa on 04/08/2017.
 */




public class TwitchData {
    private static HashMap<UUID,WhitelistData> data = new HashMap<UUID,WhitelistData>();
    public static WhitelistData get (GameProfile player)
    {
        return data.get(player.getId());
    }
    static void set (GameProfile player, WhitelistData obj){
        data.put(player.getId(),obj);
    }

    public static int getID (GameProfile player)
    {
        return data.get(player.getId()).getUser().getId();
    }

    public static String getUsername (GameProfile player)
    {
        return data.get(player.getId()).getUser().getUsername();
    }

    public static String getDisplayname (GameProfile player)
    {
        return data.get(player.getId()).getUser().getDisplayname();
    }

    public static Date getCreated (GameProfile player)
    {
        return data.get(player.getId()).getUser().getAccountcreation();
    }
}