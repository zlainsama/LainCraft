package lain.mods.notooltips;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import com.google.common.collect.Multimap;
import cpw.mods.fml.client.FMLClientHandler;

public class NoToolTipsHandler
{

    public static void handleAttributesForToolTip(Multimap<String, AttributeModifier> multimap)
    {
        if (!FMLClientHandler.instance().getClient().gameSettings.advancedItemTooltips)
            multimap.clear();
    }

}
