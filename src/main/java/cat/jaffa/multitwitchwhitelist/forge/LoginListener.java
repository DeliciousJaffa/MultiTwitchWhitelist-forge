package cat.jaffa.multitwitchwhitelist.forge;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.List;

/**
 * Created by Jaffa on 08/08/2017.
 */
public class LoginListener {

    public static String serverLogin(GameProfile p) {
        if (!MultiTwitchWhitelist.Enabled) return null;
        if (MultiTwitchWhitelist.validKey) {
            //Grab data from API
            try {
                WhitelistData data = WhitelistDataCreator.fromUser(p);
                //Check if data was returned, if it wasn't, check for cached data.
                if (data != null) {
                    return handleLogin(p, data);
                } else {
                    WhitelistData cachedata = TwitchData.get(p);
                    if (cachedata != null && MultiTwitchWhitelist.TryCacheOnFail) {
                        MultiTwitchWhitelist.log.warn("API returned invalid or no data, falling back to player cache");
                        return handleLogin(p, cachedata);
                    } else {
                        MultiTwitchWhitelist.log.warn("API returned invalid or no data, no player cache available");
                        if (MultiTwitchWhitelist.KickOnFail && !PermissionAPI.hasPermission(p, "mtwl.bypass.fail",null)) {
                            //Kick player on fail with no cache if option is enabled.
                            return MultiTwitchWhitelist.msgAPIFail;
                        } else {
                            return null;
                        }
                    }
                }
            } catch (Exception ex) {
                MultiTwitchWhitelist.log.fatal("Some kind of exception happened handling a login, please report this error with the stack trace.");
                ex.printStackTrace();
                if (!PermissionAPI.hasPermission(p, "mtwl.bypass.severe",null)) {
                    return MultiTwitchWhitelist.msgSevereError;
                }
                else {
                    return null;
                }
            }
        } else {
            MultiTwitchWhitelist.log.warn("Your API credentials are incorrect, change them and run /mtwl reload");
            MultiTwitchWhitelist.log.info(p.getName() + " Bypassing API fail");
            if (MultiTwitchWhitelist.KickOnFail) {
                return MultiTwitchWhitelist.msgFixCfg;
            } else {
                return null;
            }
        }
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        //Do stuff after successful login
    }

    private static String handleLogin(GameProfile profile, WhitelistData data) {
        if (data != null) {
            //Handle errors, user not linked or general error.
            int statusCode = data.getStatusCode();
            if (statusCode == 422) {
                if (PermissionAPI.hasPermission(profile, "mtwl.bypass.register",null)) {
                    MultiTwitchWhitelist.log.info(profile.getName() + " Bypassing register requirement");
                } else {
                    //profile.connection.disconnect(new TextComponentString(MultiTwitchWhitelist.msgLink));
                    return MultiTwitchWhitelist.msgLink;
                }
            } else if (statusCode == 401) {
                MultiTwitchWhitelist.validKey = false;
                MultiTwitchWhitelist.log.warn("Your API credentials are incorrect, change them and run /mtwl reload");
                if (PermissionAPI.hasPermission(profile, "mtwl.bypass.fail",null)) {
                    MultiTwitchWhitelist.log.info(profile.getName() + " Bypassing API fail");
                } else {
                    return MultiTwitchWhitelist.msgFixCfg;
                }
            } else if (statusCode != 200) {
                if (PermissionAPI.hasPermission(profile, "mtwl.bypass.fail",null)) {
                    MultiTwitchWhitelist.log.info(profile.getName() + " Bypassing API fail");
                } else {
                    return MultiTwitchWhitelist.msgAPIFail;
                }
            } else {
                //Cache data
                TwitchData.set(profile, data);
            }

            //Check if user has a ban
            List<WhitelistData.Ban> bans = data.getBans();
            if (bans.size() > 0) {
                //TODO: Check ban expiration
                // s = s + "\nYour ban will be removed on " + DATE_FORMAT.format(userlistipbansentry.getBanEndDate());
                WhitelistData.Ban ban = bans.get(0);
                if (ban.isGlobal()) {
                   return String.format(MultiTwitchWhitelist.msgBanGlobal, ban.getReason());
                } else if (PermissionAPI.hasPermission(profile, "mtwl.bypass.ban",null)) {
                    MultiTwitchWhitelist.log.info(String.format("%s (%s) Bypassing ban", profile.getName(), data.getUser().getUsername()));
                } else {
                    return String.format(MultiTwitchWhitelist.msgBan, ban.getInvokerDisplayname(), ban.getReason());
                }
            }

            //Check if manually whitelisted or subscribed.
            if (!data.isManual() && !data.isSubbed()) {
                if (PermissionAPI.hasPermission(profile, "mtwl.bypass.list",null)) {
                    MultiTwitchWhitelist.log.info(String.format("%s (%s) Bypassing whitelist", profile.getName(), data.getUser().getUsername()));
                } else {
                    return String.format(MultiTwitchWhitelist.msgNotSub,data.getUser().getDisplayname());
                }
            }

            //Bukkit Functions
            /*
            //Do things to the user if whitelisted.
            if (MultiTwitchWhitelist.ChangeDisplayname) {
                profile.setDisplayName(data.getUser().getDisplayname());
            }
            if (MultiTwitchWhitelist.ChangeListname) {
                profile.setPlayerListName(data.getUser().getDisplayname());
            }
            */
            MultiTwitchWhitelist.log.info(String.format("%s (%s) Passed all checks", profile.getName(), data.getUser().getUsername()));
            return null;

        } else {
            MultiTwitchWhitelist.log.warn("Null whitelist data passed to handleLogin, report this error.");
            if (MultiTwitchWhitelist.KickOnFail) {
                return MultiTwitchWhitelist.msgAPIFail;
            } else {
                return null;
            }
        }
    }
}
