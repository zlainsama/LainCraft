package lain.mods.inputfix;

import java.io.File;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.configuration.Config;
import net.minecraft.client.gui.GuiScreenFix;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "InputFix", name = "InputFix", version = "", dependencies = "required-after:LainCraftCore", useMetadata = true)
public class InputFix
{

    @Config.Property(defaultValue = "auto")
    public static String encoding;

    @Mod.PreInit
    public void init(FMLPreInitializationEvent event)
    {
        Config config = new Config(new File(SharedConstants.getLainCraftDirFile(), "InputFix.cfg"), "InputFix");
        config.register(InputFix.class, null);
        config.load();
        if ("auto".equals(encoding))
            encoding = System.getProperty("file.encoding");
        config.save();
        GuiScreenFix.encoding = encoding;
    }

}
