package cat.jaffa.multitwitchwhitelist.forge.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Created by Jaffa on 16/08/2017.
 */
@IFMLLoadingPlugin.TransformerExclusions({"cat.jaffa.multitwitchwhitelist.forge.asm"})
public class MTWLCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"cat.jaffa.multitwitchwhitelist.forge.asm.LoginTransformer"};
    }

    @Override
    public String getModContainerClass() {
        //return "cat.jaffa.multitwitchwhitelist.forge.MultiTwitchWhitelist";
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
