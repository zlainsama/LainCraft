package lain.mods.molang;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Resource;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class ModCompatibilities
{

    public static void importTable(TranslationTable t)
    {
        for (String key : t.getTranslationTable().rowKeySet())
        {
            for (String lang : t.getTranslationTable().columnKeySet())
            {
                if (t.getTranslationTable().contains(key, lang))
                {
                    String value = t.translateKey(key, lang);
                    LanguageRegistry.instance().addStringLocalization(key, lang, value);
                }
            }
        }
    }

    public static void loadActualGameTable(TranslationTable t)
    {
        LanguageManager manager = FMLClientHandler.instance().getClient().func_135016_M();
        for (Language lang : (Set<Language>) manager.func_135040_d())
            loadActualGameTable(t, lang.func_135034_a());
    }

    public static void loadActualGameTable(TranslationTable t, String lang)
    {
        ResourceManager resources = FMLClientHandler.instance().getClient().func_110442_L();
        String n = String.format("lang/%s.lang", lang);
        for (String respack : (Set<String>) resources.func_135055_a())
        {
            try
            {
                for (Resource resource : (List<Resource>) resources.func_135056_b(new ResourceLocation(respack, n)))
                    t.importTranslationFile(resource.func_110527_b(), lang);
            }
            catch (IOException ignored)
            {
            }
        }
        LanguageRegistry.instance().loadLanguageTable(t.getTranslationTableForLang(lang), lang);
    }

    public static void loadVanillaTable(TranslationTable t)
    {
        LanguageManager manager = FMLClientHandler.instance().getClient().func_135016_M();
        for (Language lang : (Set<Language>) manager.func_135040_d())
            loadVanillaTable(t, lang.func_135034_a());
    }

    public static void loadVanillaTable(TranslationTable t, String lang)
    {
        ResourceManager resources = FMLClientHandler.instance().getClient().func_110442_L();
        String n = String.format("lang/%s.lang", lang);
        try
        {
            for (Resource resource : (List<Resource>) resources.func_135056_b(new ResourceLocation("minecraft", n)))
                t.importTranslationFile(resource.func_110527_b(), lang);
        }
        catch (IOException ignored)
        {
        }
    }

}
