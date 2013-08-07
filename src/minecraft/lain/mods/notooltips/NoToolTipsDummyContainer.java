package lain.mods.notooltips;

import java.util.Arrays;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class NoToolTipsDummyContainer extends DummyModContainer
{

    public NoToolTipsDummyContainer()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "NoToolTips";
        meta.name = "NoToolTips";
        meta.version = "1.6.x-v2";
        meta.authorList = Arrays.asList("Lain");
        meta.description = "NoToolTips makes that annoying tooltip (in tools & swords) introduced in MC 1.6 toggleable";
        meta.credits = "";
        meta.url = "https://github.com/zlainsama/laincraft";
        meta.updateUrl = "https://github.com/zlainsama/LainCraft/releases";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        return true;
    }

}
