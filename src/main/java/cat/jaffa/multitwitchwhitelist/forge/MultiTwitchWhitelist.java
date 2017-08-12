package cat.jaffa.multitwitchwhitelist.forge;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

@Mod(modid = MultiTwitchWhitelist.MODID, version = MultiTwitchWhitelist.VERSION, acceptableRemoteVersions = "*")
public class MultiTwitchWhitelist {
    public static final String MODID = "multitwitchwhitelist-forge";
    public static final String VERSION = "0.1";

    @Instance(MODID)
    public static MultiTwitchWhitelist instance;

    static String apiURL = "https://whitelist.jaffa.cat/api";
    static Boolean validKey = true;
    static Logger log;

    static Configuration cfg;

    //Config
    static Boolean Enabled = false;
    static String ClientID = "Client ID";
    static String ClientSecret = "Client Secret";
    static Boolean ChangeDisplayname = false;
    static Boolean ChangeListname = false;
    static Boolean TryCacheOnFail = false;
    static Boolean KickOnFail = true;
    static String DebugapiURL = "default";

    //Messages
    static final String msgAPIFail = "Unable to connect to whitelisting server\nPlease try again later.";
    static final String msgSevereError = "Multi Twitch Whitelist Severe Error\nPlease try again later or contact a server admin.";
    static final String msgFixCfg = "Whitelisting configured incorrectly\nPlease try again later or contact a server admin.";
    static final String msgLink = "Connect your Twitch account at:\nwhitelist.jaffa.cat";
    static final String msgBan = "Banned by: %s\nReason: %s";
    static final String msgBanGlobal = "Global Banned\nReason: %s";
    static final String msgNotSub = "Your Twitch account %s is not subscribed to the appropriate streamers.";


    static String getApiURL() {
        String url = apiURL;
        if (DebugapiURL.equalsIgnoreCase("default")) {
            return apiURL;
        } else {
            return DebugapiURL;
        }
    }

    void loadConfig() {
        cfg.load();
        Enabled = cfg.get(Configuration.CATEGORY_GENERAL, "Enabled", true).getBoolean(true);
        ClientID = cfg.get(Configuration.CATEGORY_GENERAL, "ClientID", "Client ID").getString();
        ClientSecret = cfg.get(Configuration.CATEGORY_GENERAL, "ClientSecret", "Client Secret").getString();
        //cfg.get(Configuration.CATEGORY_GENERAL, "ChangeDisplayname", true).getBoolean(true);
        //cfg.get(Configuration.CATEGORY_GENERAL, "ChangeListname", true).getBoolean(true);
        TryCacheOnFail = cfg.get(Configuration.CATEGORY_GENERAL, "TryCacheOnFail", true).getBoolean(true);
        KickOnFail = cfg.get(Configuration.CATEGORY_GENERAL, "KickOnFail", true).getBoolean(true);
        DebugapiURL = cfg.get("Debug", "apiURL", "default").getString();
        cfg.save();
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent e) {
        LoginListener.server = e.getServer();
    }

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        if (!event.getSide().isServer()) return;

        //Setup Config
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        loadConfig();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (!event.getSide().isServer()) return;

        PermissionAPI.registerNode("mtwl.bypass.admin", DefaultPermissionLevel.OP, "Grants access to administration commands of the plugin");
        PermissionAPI.registerNode("mtwl.bypass.list", DefaultPermissionLevel.OP, "Bypasses whitelist requirements");
        PermissionAPI.registerNode("mtwl.bypass.ban", DefaultPermissionLevel.OP, "Bypasses MultiTwitchWhitelist bans.");
        PermissionAPI.registerNode("mtwl.bypass.fail", DefaultPermissionLevel.OP, "Bypasses when connection failed (Dangerous, will allow uncached player who has ban to join)");
        PermissionAPI.registerNode("mtwl.bypass.register", DefaultPermissionLevel.NONE, "Allows the user to bypass connecting their account");
        PermissionAPI.registerNode("mtwl.bypass.severe", DefaultPermissionLevel.NONE, "This permission should only be used when instructed");
    }

    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        if (!event.getSide().isServer()) return;
        MinecraftForge.EVENT_BUS.register(new LoginListener());
        //FMLCommonHandler.instance().handleServerHandshake();
    }

}
