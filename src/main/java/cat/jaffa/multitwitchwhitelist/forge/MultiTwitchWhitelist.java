package cat.jaffa.multitwitchwhitelist.forge;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

//@Mod(name = MultiTwitchWhitelist.MODNAME, modid = MultiTwitchWhitelist.MODID, version = MultiTwitchWhitelist.MODVERSION, acceptableRemoteVersions = "*", serverSideOnly = true)
public class MultiTwitchWhitelist extends DummyModContainer {
    public static final String MODNAME = "MultiTwitchWhitelist";
    public static final String MODID = "multitwitchwhitelist-forge";
    public static final String MODVERSION = "0.2.2";

    public MultiTwitchWhitelist() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = MODID;
        meta.name = MODNAME;
        meta.version = MODVERSION;
        meta.credits = "";
        meta.authorList = Arrays.asList("Jaffa");
        meta.description = "";
        meta.url = "https://github.com/DeliciousJaffa/MultiTwitchWhitelist-forge";
        meta.screenshots = new String[0];
        meta.logoFile = "";
        meta.parent = "multitwitchwhitelist-forge";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController loader) {
        bus.register(this);
        return true;
    }

    @Override
    public Disableable canBeDisabled() {
        return Disableable.NEVER;
    }

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
        if (DebugapiURL.equalsIgnoreCase("default")) {
            return apiURL;
        } else {
            return DebugapiURL;
        }
    }



    static void softEnable(Boolean state) {
        Enabled = state;
        cfg.load();
        cfg.get(Configuration.CATEGORY_GENERAL, "Enabled", true).set(state);
        cfg.save();
    }

    static void loadConfig() {
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

    @Subscribe
    public void preinit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        log.info("MultiTwitchWhitelist Starting");
        if (!event.getSide().isServer()) return;

        //Setup Config
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        loadConfig();
        instance = this;
    }

    @Subscribe
    public void init(FMLInitializationEvent event) {
        if (!event.getSide().isServer()) return;

        PermissionAPI.registerNode("mtwl.admin", DefaultPermissionLevel.OP, "Grants access to administration commands of the plugin");
        PermissionAPI.registerNode("mtwl.bypass.list", DefaultPermissionLevel.OP, "Bypasses whitelist requirements");
        PermissionAPI.registerNode("mtwl.bypass.ban", DefaultPermissionLevel.OP, "Bypasses MultiTwitchWhitelist bans.");
        PermissionAPI.registerNode("mtwl.bypass.fail", DefaultPermissionLevel.OP, "Bypasses when connection failed (Dangerous, will allow uncached player who has ban to join)");
        PermissionAPI.registerNode("mtwl.bypass.register", DefaultPermissionLevel.NONE, "Allows the user to bypass connecting their account");
        PermissionAPI.registerNode("mtwl.bypass.severe", DefaultPermissionLevel.NONE, "This permission should only be used when instructed");
    }

    @Subscribe
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new MTWLCommand());
    }
}
