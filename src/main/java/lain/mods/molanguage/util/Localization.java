package lain.mods.molanguage.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Localization
{

    private final Map<String, Map<String, String>> tables;

    public Localization()
    {
        tables = new ConcurrentHashMap<String, Map<String, String>>();
    }

    public String get(String key, String lang)
    {
        if (tables.containsKey(lang))
            return tables.get(lang).get(key);
        return null;
    }

    public Map<String, String> getTable(String lang)
    {
        return tables.get(lang);
    }

    public Set<String> getTableNames()
    {
        return tables.keySet();
    }

    public String put(String key, String value, String lang)
    {
        if (!tables.containsKey(lang))
            tables.put(lang, new ConcurrentHashMap<String, String>());
        return tables.get(lang).put(key, value);
    }

    public void putAll(Map<String, String> paramMap, String lang)
    {
        if (!tables.containsKey(lang))
            tables.put(lang, new ConcurrentHashMap<String, String>());
        tables.get(lang).putAll(paramMap);
    }

    public String remove(String key, String lang)
    {
        if (tables.containsKey(lang))
            return tables.get(lang).remove(key);
        return null;
    }

    public Map<String, String> removeTable(String lang)
    {
        if (tables.containsKey(lang))
            return tables.remove(lang);
        return null;
    }

}
