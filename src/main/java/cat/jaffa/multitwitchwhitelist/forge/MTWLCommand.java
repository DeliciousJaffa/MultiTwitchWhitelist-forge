package cat.jaffa.multitwitchwhitelist.forge;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * Created by Jaffa on 16/08/2017.
 */
public class MTWLCommand extends CommandBase {

    @Override
    public String getName() {
        return "mtwl";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/mtwl enable | disable | reload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reload": {
                    MultiTwitchWhitelist.loadConfig();
                    MultiTwitchWhitelist.validKey = true;
                    for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                        if (PermissionAPI.hasPermission(player,"mtwl.admin")) {
                            player.sendMessage(new TextComponentString("MultiTwitchWhitelist reloaded by " + sender.getName()));
                        }
                    }
                    MultiTwitchWhitelist.log.info("Reloaded by command (" + sender.getName() + ")");
                }
                break;
                case "enable": {
                    if (!MultiTwitchWhitelist.Enabled) {
                        MultiTwitchWhitelist.softEnable(true);
                        MultiTwitchWhitelist.validKey = true;
                        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                            if (PermissionAPI.hasPermission(player,"mtwl.admin")) {
                                player.sendMessage(new TextComponentString("MultiTwitchWhitelist enabled by " + sender.getName()));
                            }
                        }
                        MultiTwitchWhitelist.log.info("Enabled by command (" + sender.getName() + ")");
                    } else sender.sendMessage(new TextComponentString("MultiTwitchWhitelist is already enabled."));
                }
                break;
                case "disable": {
                    if (MultiTwitchWhitelist.Enabled) {
                        MultiTwitchWhitelist.softEnable(false);
                        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                            if (PermissionAPI.hasPermission(player,"mtwl.admin")) {
                                player.sendMessage(new TextComponentString("MultiTwitchWhitelist disabled by " + sender.getName()));
                            }
                        }
                        MultiTwitchWhitelist.log.info("Disabled by command (" + sender.getName() + ")");
                    } else sender.sendMessage(new TextComponentString("MultiTwitchWhitelist is already disabled."));
                }
                break;
                //TODO: Implement add/remove static whitelisting commands
                default: {
                    sender.sendMessage(new TextComponentString("Unknown command, check your spelling."));
                }
            }
        } else {
            sender.sendMessage(new TextComponentString("Possible commands: enable, disable, reload"));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender.getName().equals("Server")) return true;
        try {
            return PermissionAPI.hasPermission(getCommandSenderAsPlayer(sender),"mtwl.admin");
        } catch (PlayerNotFoundException e) {
            return false;
        }
    }
}
