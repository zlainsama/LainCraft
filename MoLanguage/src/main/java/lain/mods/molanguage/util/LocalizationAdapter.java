package lain.mods.molanguage.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.util.StringTranslate;
import cpw.mods.fml.common.FMLCommonHandler;

public abstract class LocalizationAdapter
{

    private static final Localization copy = new Localization();

    public static final List<LocalizationAdapter> adapters = new ArrayList<LocalizationAdapter>();

    static
    {
        // Vanilla System (FML)
        new LocalizationAdapter()
        {
            @Override
            public void fillProperties(Properties properties)
            {
                try
                {
                    properties.putAll(StringTranslate.getInstance().translateTable);
                }
                catch (Throwable t)
                {
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                }
            }

            @Override
            public void notifyLanguageChanges(boolean force)
            {
            }

            @Override
            public boolean setup() throws Throwable
            {
                return true;
            }

            @Override
            public void update(boolean force)
            {
            }
        };
        // CoFH System
        new LocalizationAdapter()
        {
            Field loadedLanguage; // String
            Field mappings; // Properties
            Method get; // String get(String key);
            String prevLoadedLanguage;

            @Override
            public void fillProperties(Properties properties)
            {
                try
                {
                    Properties table = (Properties) mappings.get(null);
                    properties.putAll(table);
                }
                catch (Throwable t)
                {
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                }
            }

            @Override
            public void notifyLanguageChanges(boolean force)
            {
                try
                {
                    if (force)
                        loadedLanguage.set(null, null);
                    get.invoke(null, "");
                }
                catch (Throwable t)
                {
                    System.err.println("Error occured.");
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                    System.err.println("Good bye JVM, deleting self.");
                    adapters.remove(this);
                }
            }

            @Override
            public boolean setup() throws Throwable
            {
                Class cls = Class.forName("cofh.util.Localization");
                loadedLanguage = cls.getDeclaredField("loadedLanguage");
                loadedLanguage.setAccessible(true);
                mappings = cls.getDeclaredField("mappings");
                mappings.setAccessible(true);
                get = cls.getDeclaredMethod("get", String.class);
                get.setAccessible(true);
                return true;
            }

            @Override
            public void update(boolean force)
            {
                try
                {
                    String language = (String) loadedLanguage.get(null);
                    if (force || prevLoadedLanguage == null || !prevLoadedLanguage.equals(language))
                    {
                        if (force)
                            notifyLanguageChanges(true);
                        Properties table = (Properties) mappings.get(null);
                        if (copy.getTable(language) != null)
                            table.putAll(copy.getTable(language));
                        prevLoadedLanguage = language;
                    }
                }
                catch (Throwable t)
                {
                    System.err.println("Error occured.");
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                    System.err.println("Good bye JVM, deleting self.");
                    adapters.remove(this);
                }
            }
        };
        // Forestry System
        new LocalizationAdapter()
        {
            Field loadedLanguage; // String
            Field mappings; // Properties
            Method get; // String get(String key);
            Field instance; // Object
            String prevLoadedLanguage;

            @Override
            public void fillProperties(Properties properties)
            {
                try
                {
                    Object obj = instance.get(null);
                    Properties table = (Properties) mappings.get(obj);
                    properties.putAll(table);
                }
                catch (Throwable t)
                {
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                }
            }

            @Override
            public void notifyLanguageChanges(boolean force)
            {
                try
                {
                    Object obj = instance.get(null);
                    if (force)
                        loadedLanguage.set(obj, null);
                    get.invoke(obj, "");
                }
                catch (Throwable t)
                {
                    System.err.println("Error occured.");
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                    System.err.println("Good bye JVM, deleting self.");
                    adapters.remove(this);
                }
            }

            @Override
            public boolean setup() throws Throwable
            {
                Class cls = Class.forName("forestry.core.utils.Localization");
                loadedLanguage = cls.getDeclaredField("loadedLanguage");
                loadedLanguage.setAccessible(true);
                mappings = cls.getDeclaredField("mappings");
                mappings.setAccessible(true);
                get = cls.getDeclaredMethod("get", String.class);
                get.setAccessible(true);
                instance = cls.getDeclaredField("instance");
                instance.setAccessible(true);
                return true;
            }

            @Override
            public void update(boolean force)
            {
                try
                {
                    Object obj = instance.get(null);
                    String language = (String) loadedLanguage.get(obj);
                    if (force || prevLoadedLanguage == null || !prevLoadedLanguage.equals(language))
                    {
                        if (force)
                            notifyLanguageChanges(true);
                        Properties table = (Properties) mappings.get(obj);
                        if (copy.getTable(language) != null)
                            table.putAll(copy.getTable(language));
                        prevLoadedLanguage = language;
                    }
                }
                catch (Throwable t)
                {
                    System.err.println("Error occured.");
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                    System.err.println("Good bye JVM, deleting self.");
                    adapters.remove(this);
                }
            }
        };
        // CustomNPCs Server Localization Helper
        new LocalizationAdapter()
        {
            Constructor constructorLanguageLoaderServer;
            String loadedLanguage;

            @Override
            public void fillProperties(Properties properties)
            {
            }

            @Override
            public void notifyLanguageChanges(boolean force)
            {
                try
                {
                    String currentLanguage = StringTranslate.getInstance().getCurrentLanguage();
                    if (force || currentLanguage == null || !currentLanguage.equals(loadedLanguage))
                    {
                        constructorLanguageLoaderServer.newInstance(new Object[0]);
                        if (copy.getTable(currentLanguage) != null)
                            StringTranslate.getInstance().translateTable.putAll(copy.getTable(currentLanguage));
                        loadedLanguage = currentLanguage;
                    }
                }
                catch (Throwable t)
                {
                    System.err.println("Error occured.");
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                    System.err.println("Good bye JVM, deleting self.");
                    adapters.remove(this);
                }
            }

            @Override
            public boolean setup() throws Throwable
            {
                if (FMLCommonHandler.instance().getSide().isClient())
                    return false;
                Class cls = Class.forName("noppes.npcs.LanguageLoaderServer");
                constructorLanguageLoaderServer = cls.getDeclaredConstructor(new Class[0]);
                constructorLanguageLoaderServer.setAccessible(true);
                return true;
            }

            @Override
            public void update(boolean force)
            {
                notifyLanguageChanges(force);
            }
        };
        // CustomNPCs Client Localization Helper
        new LocalizationAdapter()
        {
            Method loadLanguage; // void loadLanguage()
            Field current; // ITexturePack
            String loadedLanguage;

            @Override
            public void fillProperties(Properties properties)
            {
            }

            @Override
            public void notifyLanguageChanges(boolean force)
            {
                try
                {
                    String currentLanguage = StringTranslate.getInstance().getCurrentLanguage();
                    if (force || currentLanguage == null || !currentLanguage.equals(loadedLanguage))
                    {
                        current.set(null, null);
                        loadLanguage.invoke(null, new Object[0]);
                        if (copy.getTable(currentLanguage) != null)
                            StringTranslate.getInstance().translateTable.putAll(copy.getTable(currentLanguage));
                        loadedLanguage = currentLanguage;
                    }
                }
                catch (Throwable t)
                {
                    System.err.println("Error occured.");
                    System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
                    System.err.println("Good bye JVM, deleting self.");
                    adapters.remove(this);
                }
            }

            @Override
            public boolean setup() throws Throwable
            {
                if (!FMLCommonHandler.instance().getSide().isClient())
                    return false;
                Class cls = Class.forName("noppes.npcs.LanguageLoaderClient");
                loadLanguage = cls.getDeclaredMethod("loadLanguage", new Class[0]);
                loadLanguage.setAccessible(true);
                current = cls.getDeclaredField("current");
                current.setAccessible(true);
                return true;
            }

            @Override
            public void update(boolean force)
            {
                notifyLanguageChanges(force);
            }
        };
    }

    public static void addLocalization(String key, String lang, String value)
    {
        copy.put(key, value, lang);
    }

    public LocalizationAdapter()
    {
        try
        {
            if (setup())
                adapters.add(this);
        }
        catch (Throwable t)
        {
            System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    public abstract void fillProperties(Properties properties);

    public abstract void notifyLanguageChanges(boolean force);

    public abstract boolean setup() throws Throwable;

    public abstract void update(boolean force);

}
