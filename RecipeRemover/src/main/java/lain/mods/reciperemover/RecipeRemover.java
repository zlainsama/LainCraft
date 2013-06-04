package lain.mods.reciperemover;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.configuration.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "RecipeRemover", name = "RecipeRemover", version = "", dependencies = "required-after:LainCraftCore;after:*", useMetadata = true)
public class RecipeRemover
{

    private Config config;

    @Mod.PreInit
    public void init(FMLPreInitializationEvent event)
    {
        config = new Config(new File(SharedConstants.getLainCraftDirFile(), "RecipeRemover.cfg"), "RecipeRemover");
    }

    @Mod.PostInit
    public void modsLoaded(FMLPostInitializationEvent event)
    {
        System.out.println("Start removing recipes:");
        config.load();
        List toRemove = new ArrayList();
        List list = CraftingManager.getInstance().getRecipeList();
        for (Object obj : list)
        {
            try
            {
                if (obj instanceof IRecipe)
                {
                    ItemStack item = ((IRecipe) obj).getRecipeOutput();
                    if (item != null)
                    {
                        String n = Integer.toString(item.itemID);
                        if (item.getHasSubtypes())
                            n = n + ":" + Integer.toString(item.getItemDamage());
                        n = "DisableRecipe." + n;
                        if (!config.containsKey(n))
                            config.setProperty(n, "false");
                        try
                        {
                            config.get(n).comment = item.getDisplayName();
                        }
                        catch (Throwable t)
                        {
                            System.err.println(" - unable to obtain itemDisplayName for " + n + " (" + t.toString() + ")");
                            config.get(n).comment = null;
                        }
                        if (Boolean.parseBoolean(config.getProperty(n)))
                            toRemove.add(obj);
                    }
                }
            }
            catch (Throwable t)
            {
                System.err.println(" - error occured: " + t.toString());
                toRemove.remove(obj);
            }
        }
        config.save();
        System.out.println(" - found " + toRemove.size() + " recipes to remove");
        int size = list.size();
        list.removeAll(toRemove);
        System.out.println(" - removed " + (size - list.size()) + " recipes");
    }

}
