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
        LanguageManager manager = FMLClientHandler.instance().getClient().getLanguageManager();
        for (Language lang : (Set<Language>) manager.getLanguages())
            loadActualGameTable(t, lang.getLanguageCode());
    }

    public static void loadActualGameTable(TranslationTable t, String lang)
    {
        ResourceManager resources = FMLClientHandler.instance().getClient().getResourceManager();
        String n = String.format("lang/%s.lang", lang);
        for (String respack : (Set<String>) resources.getResourceDomains())
        {
            try
            {
                for (Resource resource : (List<Resource>) resources.getAllResources(new ResourceLocation(respack, n)))
                    t.importTranslationFile(resource.getInputStream(), lang);
            }
            catch (IOException ignored)
            {
            }
        }
        LanguageRegistry.instance().loadLanguageTable(t.getTranslationTableForLang(lang), lang);
    }

    public static void loadVanillaTable(TranslationTable t)
    {
        LanguageManager manager = FMLClientHandler.instance().getClient().getLanguageManager();
        for (Language lang : (Set<Language>) manager.getLanguages())
            loadVanillaTable(t, lang.getLanguageCode());
    }

    public static void loadVanillaTable(TranslationTable t, String lang)
    {
        ResourceManager resources = FMLClientHandler.instance().getClient().getResourceManager();
        String n = String.format("lang/%s.lang", lang);
        try
        {
            for (Resource resource : (List<Resource>) resources.getAllResources(new ResourceLocation("minecraft", n)))
                t.importTranslationFile(resource.getInputStream(), lang);
        }
        catch (IOException ignored)
        {
        }
    }

}
