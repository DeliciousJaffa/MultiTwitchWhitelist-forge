package cat.jaffa.multitwitchwhitelist.forge;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.List;

/**
 * Created by Jaffa on 08/08/2017.
 */
public class LoginListener {

    static MinecraftServer server;



    @SubscribeEvent
    public void serverLogin(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        if (event.isLocal()) return;
        if (!MultiTwitchWhitelist.Enabled) return;
        EntityPlayerMP p = ((NetHandlerPlayServer) event.getHandler()).player;
        if (MultiTwitchWhitelist.validKey) {
            //Grab data from API
            try {
                WhitelistData data = WhitelistDataCreator.fromUser(p);
                //Check if data was returned, if it wasn't, check for cached data.
                if (data != null) {
                    handleLogin(event, p, data);
                } else {
                    WhitelistData cachedata = TwitchData.get(p);
                    if (cachedata != null && MultiTwitchWhitelist.TryCacheOnFail) {
                        MultiTwitchWhitelist.log.warn("API returned invalid or no data, falling back to player cache");
                        handleLogin(event, p, cachedata);
                    } else {
                        MultiTwitchWhitelist.log.warn("API returned invalid or no data, no player cache available");
                        if (MultiTwitchWhitelist.KickOnFail && !PermissionAPI.hasPermission(p, "mtwl.bypass.fail")) {
                            //Kick player on fail with no cache if option is enabled.
                            dcPlayer(event,MultiTwitchWhitelist.msgAPIFail);
                        }
                    }
                }
            } catch (Exception ex) {
                MultiTwitchWhitelist.log.fatal("Some kind of exception happened handling a login, please report this error with the stack trace.");
                ex.printStackTrace();
                if (!PermissionAPI.hasPermission(p, "mtwl.bypass.severe"))
                    dcPlayer(event,MultiTwitchWhitelist.msgSevereError);
            }
        } else {
            MultiTwitchWhitelist.log.warn("Your API credentials are incorrect, change them and run /mtwl reload");
            MultiTwitchWhitelist.log.info(p.getName() + " Bypassing API fail");
            if (MultiTwitchWhitelist.KickOnFail) {
                dcPlayer(event,MultiTwitchWhitelist.msgFixCfg);
            }
        }
    }

    private void dcPlayer(FMLNetworkEvent.ServerConnectionFromClientEvent event, String msg) {
        //GameProfile profile = player.getGameProfile();
        //EntityPlayerMP player2 = ((NetHandlerPlayServer) event.getHandler()).player;

        //e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unable to connect to whitelisting server\nPlease try again later.");
        //((EntityPlayerMP) event.player).connection.onDisconnect(new TextComponentString(MultiTwitchWhitelist.msgAPIFail));
        //p.connection.disconnect(new TextComponentString(MultiTwitchWhitelist.msgAPIFail));
        //((EntityPlayerMP) p).connection.disconnect(new TextComponentString(MultiTwitchWhitelist.msgAPIFail));
        //player.connection.onDisconnect(new TextComponentString(msg));
        //player.connection.disconnect(new TextComponentString(msg));
        //server.getPlayerList().getPlayerByUUID(player.getUniqueID());
        ((NetHandlerPlayServer) event.getHandler()).disconnect(new TextComponentString(msg));
        //((NetHandlerPlayServer) event.getHandler()).netManager.closeChannel(new TextComponentString(msg));

    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        //Do stuff after successful login
    }

    private void handleLogin(FMLNetworkEvent.ServerConnectionFromClientEvent event,EntityPlayerMP p , WhitelistData data) {
        process:
        if (data != null) {

            //Handle errors, user not linked or general error.
            int statusCode = data.getStatusCode();
            if (statusCode == 422) {
                if (PermissionAPI.hasPermission(p, "mtwl.bypass.register")) {
                    MultiTwitchWhitelist.log.info(p.getName() + " Bypassing register requirement");
                } else {
                    //p.connection.disconnect(new TextComponentString(MultiTwitchWhitelist.msgLink));
                    dcPlayer(event,MultiTwitchWhitelist.msgLink);
                    break process;
                }
            } else if (statusCode == 401) {
                MultiTwitchWhitelist.validKey = false;
                MultiTwitchWhitelist.log.warn("Your API credentials are incorrect, change them and run /mtwl reload");
                if (PermissionAPI.hasPermission(p, "mtwl.bypass.fail")) {
                    MultiTwitchWhitelist.log.info(p.getName() + " Bypassing API fail");
                } else {
                    dcPlayer(event,MultiTwitchWhitelist.msgFixCfg);
                    break process;
                }
                break process;
            } else if (statusCode != 200) {
                if (PermissionAPI.hasPermission(p, "mtwl.bypass.fail")) {
                    MultiTwitchWhitelist.log.info(p.getName() + " Bypassing API fail");
                } else {
                    dcPlayer(event,MultiTwitchWhitelist.msgAPIFail);
                    break process;
                }
            } else {
                //Cache data
                TwitchData.set(p, data);
            }

            //Check if user has a ban
            List<WhitelistData.Ban> bans = data.getBans();
            if (bans.size() > 0) {
                //TODO: Check ban expiration
                WhitelistData.Ban ban = bans.get(0);
                if (ban.isGlobal()) {
                    dcPlayer(event,String.format(MultiTwitchWhitelist.msgBanGlobal, ban.getReason()));
                    break process;
                } else if (PermissionAPI.hasPermission(p, "mtwl.bypass.ban")) {
                    MultiTwitchWhitelist.log.info(String.format("%s (%s) Bypassing ban", p.getName(), data.getUser().getUsername()));
                } else {
                    dcPlayer(event,String.format(MultiTwitchWhitelist.msgBan, ban.getInvokerDisplayname(), ban.getReason()));
                    break process;
                }
            }

            //Check if manually whitelisted or subscribed.
            if (!data.isManual() && !data.isSubbed()) {
                if (PermissionAPI.hasPermission(p, "mtwl.bypass.list")) {
                    MultiTwitchWhitelist.log.info(String.format("%s (%s) Bypassing whitelist", p.getName(), data.getUser().getUsername()));
                } else {
                    dcPlayer(event,String.format(MultiTwitchWhitelist.msgNotSub,data.getUser().getDisplayname()));
                    break process;
                }
            }

            //Bukkit Functions
            /*
            //Do things to the user if whitelisted.
            if (MultiTwitchWhitelist.ChangeDisplayname) {
                p.setDisplayName(data.getUser().getDisplayname());
            }
            if (MultiTwitchWhitelist.ChangeListname) {
                p.setPlayerListName(data.getUser().getDisplayname());
            }
            */
            MultiTwitchWhitelist.log.info(String.format("%s (%s) Passed all checks", p.getName(), data.getUser().getUsername()));

        } else {
            MultiTwitchWhitelist.log.warn("Null whitelist data passed to handleLogin, report this error.");
            if (MultiTwitchWhitelist.KickOnFail) {
                dcPlayer(event,MultiTwitchWhitelist.msgAPIFail);
            }
        }
    }
}
