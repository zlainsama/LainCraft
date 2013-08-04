package lain.mods.molang;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.CharStreams;

public class TranslationTable
{

    private final Table<String, String, String> t = HashBasedTable.create();

    public String addTranslation(String key, String value)
    {
        return t.put(key, "en_US", value);
    }

    public String addTranslation(String key, String value, String lang)
    {
        return t.put(key, lang, value);
    }

    public Table<String, String, String> getTranslationTable()
    {
        return t;
    }

    public Map<String, String> getTranslationTableForKey(String key)
    {
        return t.row(key);
    }

    public Map<String, String> getTranslationTableForLang(String lang)
    {
        return t.column(lang);
    }

    public void importTranslationFile(InputStream data) throws IOException
    {
        t.putAll(CharStreams.readLines(new UnicodeInputStreamReader(data, "UTF-8"), new TranslationFileProcessor()));
    }

    public void importTranslationFile(InputStream data, String lang) throws IOException
    {
        Properties defaults = new Properties();
        defaults.setProperty("lang", lang);
        t.putAll(CharStreams.readLines(new UnicodeInputStreamReader(data, "UTF-8"), new TranslationFileProcessor(defaults)));
    }

    public void importTranslations(Table<String, String, String> t)
    {
        t.putAll(t);
    }

    public String translateKey(String key)
    {
        return t.get(key, "en_US");
    }

    public String translateKey(String key, String lang)
    {
        return t.get(key, lang);
    }

}
