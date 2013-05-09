package lain.mods.inputfix;

import lain.mods.laincraft.Plugin;
import lain.mods.laincraft.util.configuration.Config;
import net.minecraft.client.gui.GuiScreenFix;

public class InputFix extends Plugin
{

    @Config.Property(defaultValue = "GBK")
    public static String encoding;

    @Override
    public String getName()
    {
        return "InputFix";
    }

    @Override
    public void onDisable()
    {
    }

    @Override
    public void onEnable()
    {
        Config config = getConfig();
        config.register(InputFix.class, null);
        config.load();
        config.save();
        GuiScreenFix.encoding = encoding;
    }

}
