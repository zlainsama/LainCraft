package lain.mods.molang;

import java.io.IOException;
import java.util.Properties;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.LineProcessor;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class TranslationFileProcessor implements LineProcessor<Table<String, String, String>>
{

    private final Properties env;
    private final Table<String, String, String> t;

    public TranslationFileProcessor()
    {
        this.env = new Properties();
        this.t = HashBasedTable.create();
    }

    public TranslationFileProcessor(Properties defaults)
    {
        this.env = new Properties(defaults);
        this.t = HashBasedTable.create();
    }

    @Override
    public Table<String, String, String> getResult()
    {
        return t;
    }

    @Override
    public boolean processLine(String line) throws IOException
    {
        int lineNum = Integer.parseInt(env.getProperty("lineNum", "0")) + 1;
        String lang = env.getProperty("lang", "");
        String head = env.getProperty("head", "");
        String tail = env.getProperty("tail", "");
        String mod = env.getProperty("mod", "");
        String version = env.getProperty("version", "");
        line = line.trim();
        if (line.startsWith("#"))
        {
            if (lang.isEmpty() && lineNum == 1)
                lang = line.indexOf(" ") != -1 ? line.substring(1, line.indexOf(" ")) : line.substring(1);
            else if (line.equals("#langclear"))
                lang = "";
            else if (line.equals("#headclear"))
                head = "";
            else if (line.equals("#tailclear"))
                tail = "";
            else if (line.equals("#modclear"))
                mod = "";
            else if (line.equals("#versionclear"))
                version = "";
            else if (line.startsWith("#lang="))
                lang = line.substring(6);
            else if (line.startsWith("#head="))
                head = line.substring(6);
            else if (line.startsWith("#tail="))
                tail = line.substring(6);
            else if (line.startsWith("#mod="))
                mod = line.substring(5);
            else if (line.startsWith("#version="))
                version = line.substring(9);
        }
        else if (line.indexOf("=") != -1)
        {
            String k = line.substring(0, line.indexOf("=")).trim();
            String v = line.substring(line.indexOf("=") + 1).trim();
            if (!lang.isEmpty())
            {
                if (!head.isEmpty())
                    v = head + v;
                if (!tail.isEmpty())
                    v = v + tail;
                if (mod.isEmpty() || mod.equals("*") || Loader.isModLoaded(mod))
                {
                    ModContainer mc = Loader.instance().getIndexedModList().get(mod);
                    if (mc == null || version.isEmpty() || version.equals("*") || version.equals(mc.getVersion()))
                        t.put(k, lang, v);
                }
            }
        }
        env.setProperty("lineNum", Integer.toString(lineNum));
        env.setProperty("lang", lang);
        env.setProperty("head", head);
        env.setProperty("tail", tail);
        env.setProperty("mod", mod);
        env.setProperty("version", version);
        return true;
    }

}
