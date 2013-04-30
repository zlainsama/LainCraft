package lain.mods.inputfix;

import java.io.File;
import lain.mods.laincraft.Plugin;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class InputFix extends Plugin
{

    @Override
    public String getName()
    {
        return "InputFix";
    }

    @Subscribe
    public void init(FMLPreInitializationEvent event)
    {
        InputFix_Config.setup(new File(event.getModConfigurationDirectory(), "InputFix.cfg"));
    }

}
