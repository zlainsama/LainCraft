package lain.mods.laincraft.core;

import java.util.Arrays;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class LainCraftCore extends DummyModContainer
{

    public LainCraftCore()
    {
        super(new ModMetadata());
        ModMetadata md = getMetadata();
        md.modId = "LainCraftCore";
        md.name = "LainCraftCore";
        md.version = "";
        md.authorList = Arrays.asList("Lain");
        md.description = "";
        md.autogenerated = false;
    }

    @Override
    public boolean registerBus(EventBus paramEventBus, LoadController paramLoadController)
    {
        return true;
    }

}