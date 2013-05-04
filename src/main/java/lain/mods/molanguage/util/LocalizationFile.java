package lain.mods.molanguage.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import lain.mods.laincraft.util.StreamUtils;

public class LocalizationFile
{

    public Properties env = new Properties();
    public Set<String> authors = new HashSet<String>();

    public boolean load(InputStream stream, Localization container)
    {
        byte[] data = null;
        try
        {
            data = StreamUtils.readFully(stream);
        }
        catch (IOException e)
        {
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
        for (LocalizationFileLoader loader : LocalizationFileLoader.loaders)
        {
            Localization tmpContainer = new Localization();
            Properties tmpEnv = new Properties(env);
            Set<String> tmpList = new HashSet<String>();
            if (loader.load(new ByteArrayInputStream(data), tmpContainer, tmpEnv, tmpList))
            {
                for (String lang : tmpContainer.getTableNames())
                    container.putAll(tmpContainer.getTable(lang), lang);
                authors.addAll(tmpList);
                return true;
            }
        }
        Localization tmpContainer = new Localization();
        Properties tmpEnv = new Properties(env);
        Set<String> tmpList = new HashSet<String>();
        if (LocalizationFileLoader.loadDefault(new ByteArrayInputStream(data), tmpContainer, tmpEnv, tmpList))
        {
            for (String lang : tmpContainer.getTableNames())
                container.putAll(tmpContainer.getTable(lang), lang);
            authors.addAll(tmpList);
            return true;
        }
        return false;
    }

}
