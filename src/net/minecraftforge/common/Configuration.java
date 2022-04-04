/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package net.minecraftforge.common;

import static net.minecraftforge.common.Property.Type.BOOLEAN;
import static net.minecraftforge.common.Property.Type.DOUBLE;
import static net.minecraftforge.common.Property.Type.INTEGER;
import static net.minecraftforge.common.Property.Type.STRING;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.jredfox.crashwconflicts.AutoConfig;
import com.jredfox.crashwconflicts.cfg.CfgMarker;
import com.jredfox.crashwconflicts.cfg.CfgVarBlock;
import com.jredfox.crashwconflicts.cfg.CfgVarItem;
import com.jredfox.util.RegTypes;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * This class offers advanced configurations capabilities, allowing to provide
 * various categories for configuration variables.
 */
public class Configuration
{
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_BLOCK   = "block";
    public static final String CATEGORY_ITEM    = "item";
    public static final String ALLOWED_CHARS = "._-";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String CATEGORY_SPLITTER = ".";
    public static String newLine = System.getProperty("line.separator");
    private static final Pattern CONFIG_START = Pattern.compile("START: \"([^\\\"]+)\"");
    private static final Pattern CONFIG_END = Pattern.compile("END: \"([^\\\"]+)\"");
    public static final CharMatcher allowedProperties = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf(ALLOWED_CHARS));
    private static Configuration PARENT = null;

    public File file;

    private Map<String, ConfigCategory> categories = new TreeMap<String, ConfigCategory>();
    private Map<String, Configuration> children = new TreeMap<String, Configuration>();

    private boolean caseSensitiveCustomCategories;
    public String defaultEncoding = DEFAULT_ENCODING;
    private String fileName = null;
    public boolean isChild = false;
    private boolean changed = false;
    public String modid = CfgMarker.getModId();
    
    public Configuration()
    {
    	
    }

    /**
     * Create a configuration file for the file given in parameter.
     */
    public Configuration(File file)
    {
    	this();
        this.file = file;
        String basePath = ((File)(FMLInjectionData.data()[6])).getAbsolutePath().replace(File.separatorChar, '/').replace("/.", "");
        String path = file.getAbsolutePath().replace(File.separatorChar, '/').replace("/./", "/").replace(basePath, "");
        if (PARENT != null)
        {
            PARENT.setChild(path, this);
            isChild = true;
        }
        else
        {
            fileName = path;
            load();
        }
    }

    public Configuration(File file, boolean caseSensitiveCustomCategories)
    {
        this(file);
        this.caseSensitiveCustomCategories = caseSensitiveCustomCategories;
    }

    /**
     * Gets or create a block id property. If the block id property key is
     * already in the configuration, then it will be used. Otherwise,
     * defaultId will be used, except if already taken, in which case this
     * will try to determine a free default id.
     */
    public Property getBlock(String key, int defaultID) { return getBlock(CATEGORY_BLOCK, key, defaultID, null); }
    public Property getBlock(String key, int defaultID, String comment) { return getBlock(CATEGORY_BLOCK, key, defaultID, comment); }
    public Property getBlock(String category, String key, int defaultID) { return getBlockInternal(category, key, defaultID, null, 256, Block.blocksList.length); }
    public Property getBlock(String category, String key, int defaultID, String comment) { return getBlockInternal(category, key, defaultID, comment, 256, Block.blocksList.length); }

    /**
     * Special version of getBlock to be used when you want to garentee the ID you get is below 256
     * This should ONLY be used by mods who do low level terrain generation, or ones that add new
     * biomes.
     * EXA: ExtraBiomesXL
     * 
     * Specifically, if your block is used BEFORE the Chunk is created, and placed in the terrain byte array directly.
     * If you add a new biome and you set the top/filler block, they need to be <256, nothing else.
     * 
     * If you're adding a new ore, DON'T call this function.
     * 
     * Normal mods such as '50 new ores' do not need to be below 256 so should use the normal getBlock
     */
    public Property getTerrainBlock(String category, String key, int defaultID, String comment)
    {
        return getBlockInternal(category, key, defaultID, comment, 0, 255);
    }

    public Property getBlockInternal(String category, String key, int d, String comment, int lower, int upper)
    {
    	Property p = this.get(category, key, d, comment);
    	AutoConfig.INSTANCE.addProperty(this.file, category, key, RegTypes.BLOCK);
    	int id = p.getInt();
    	if(id < lower || id >= upper)
    	{
    		for(int i=upper;i>=lower;i--)//have to do -1 because they used size for the upper but index for the lower
    		{
    			if(!CfgVarBlock.markBlocks.isMarked(i) && Block.blocksList[i] == null && (upper > 255 ? Item.itemsList[i] == null : true))//TODO: change per mc version
    			{
    				CfgVarBlock.markBlocks.mark(i, this.modid);//keep track of what blocks we have used
    				p.set(i);
    				return p;
    			}
    		}
    		throw new RuntimeException("out of free block ids for:" + this.file + " in cat:" + category + " min:" + lower + " max:" + upper);
    	}
    	CfgVarBlock.markBlocks.mark(id, this.modid);
    	return p;
    }

    public Property getItem(String key, int defaultID) { return getItem(CATEGORY_ITEM, key, defaultID, null); }
    public Property getItem(String key, int defaultID, String comment) { return getItem(CATEGORY_ITEM, key, defaultID, comment); }
    public Property getItem(String category, String key, int defaultID) { return getItem(category, key, defaultID, null); }

    public Property getItem(String category, String key, int defaultID, String comment)
    {
    	Property p = this.get(category, key, defaultID);
    	AutoConfig.INSTANCE.addProperty(this.file, category, key, RegTypes.ITEM);
    	int id = p.getInt();
    	int shifted = id + CfgVarItem.ITEM_SHIFT;
    	if(shifted < CfgVarItem.ITEM_MIN || shifted > CfgVarItem.ITEM_MAX)
    		p.set(this.nextItemID() - CfgVarItem.ITEM_SHIFT);//set out of bounds min item ids to max value
    	CfgVarItem.markItems.mark(p.getInt() + CfgVarItem.ITEM_SHIFT, this.modid);
    	return p;
    }

    public int nextItemID() 
    {
		for(int i=CfgVarItem.item_index;i>=0;i--)
		{
			CfgVarItem.item_index--;
			if(!CfgVarItem.markItems.isMarked(i) && Item.itemsList[i] == null)
				return i;
		}
		throw new RuntimeException("Configuration is out of free ids!");
	}

	public Property get(String category, String key, int defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, int defaultValue, String comment)
    {
        Property prop = get(category, key, Integer.toString(defaultValue), comment, INTEGER);
        if (!prop.isIntValue())
        {
            prop.set(defaultValue);
        }
        return prop;
    }

    public Property get(String category, String key, boolean defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, boolean defaultValue, String comment)
    {
        Property prop = get(category, key, Boolean.toString(defaultValue), comment, BOOLEAN);
        if (!prop.isBooleanValue())
        {
            prop.set(defaultValue);
        }
        return prop;
    }

    public Property get(String category, String key, double defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, double defaultValue, String comment)
    {
        Property prop = get(category, key, Double.toString(defaultValue), comment, DOUBLE);
        if (!prop.isDoubleValue())
        {
            prop.set(defaultValue);
        }
        return prop;
    }

    public Property get(String category, String key, String defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, String defaultValue, String comment)
    {
        return get(category, key, defaultValue, comment, STRING);
    }

    public Property get(String category, String key, String[] defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, String[] defaultValue, String comment)
    {
        return get(category, key, defaultValue, comment, STRING);
    }

    public Property get(String category, String key, int[] defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, int[] defaultValue, String comment)
    {
        String[] values = new String[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++)
        {
            values[i] = Integer.toString(defaultValue[i]);
        }

        Property prop =  get(category, key, values, comment, INTEGER);
        if (!prop.isIntList())
        {
            prop.set(values);
        }

        return prop;
    }

    public Property get(String category, String key, double[] defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, double[] defaultValue, String comment)
    {
        String[] values = new String[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++)
        {
            values[i] = Double.toString(defaultValue[i]);
        }

        Property prop =  get(category, key, values, comment, DOUBLE);
        
        if (!prop.isDoubleList())
        {
            prop.set(values);
        }

        return prop;
    }

    public Property get(String category, String key, boolean[] defaultValue)
    {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, boolean[] defaultValue, String comment)
    {
        String[] values = new String[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++)
        {
            values[i] = Boolean.toString(defaultValue[i]);
        }

        Property prop =  get(category, key, values, comment, BOOLEAN);
        
        if (!prop.isBooleanList())
        {
            prop.set(values);
        }

        return prop;
    }

    public Property get(String category, String key, String defaultValue, String comment, Property.Type type)
    {
        if (!caseSensitiveCustomCategories)
        {
            category = category.toLowerCase(Locale.ENGLISH);
        }

        ConfigCategory cat = getCategory(category);

        if (cat.containsKey(key))
        {
            Property prop = cat.get(key);

            if (prop.getType() == null)
            {
                prop = new Property(prop.getName(), prop.getString(), type);
                cat.put(key, prop);
            }

            prop.comment = comment;
            return prop;
        }
        else if (defaultValue != null)
        {
            Property prop = new Property(key, defaultValue, type);
            prop.set(defaultValue); //Set and mark as dirty to signify it should save 
            cat.put(key, prop);
            prop.comment = comment;
            return prop;
        }
        else
        {
            return null;
        }
    }

    public Property get(String category, String key, String[] defaultValue, String comment, Property.Type type)
    {
        if (!caseSensitiveCustomCategories)
        {
            category = category.toLowerCase(Locale.ENGLISH);
        }

        ConfigCategory cat = getCategory(category);

        if (cat.containsKey(key))
        {
            Property prop = cat.get(key);

            if (prop.getType() == null)
            {
                prop = new Property(prop.getName(), prop.getString(), type);
                cat.put(key, prop);
            }

            prop.comment = comment;

            return prop;
        }
        else if (defaultValue != null)
        {
            Property prop = new Property(key, defaultValue, type);
            prop.comment = comment;
            cat.put(key, prop);
            return prop;
        }
        else
        {
            return null;
        }
    }

    public boolean hasCategory(String category)
    {
        return categories.get(category) != null;
    }

    public boolean hasKey(String category, String key)
    {
        ConfigCategory cat = categories.get(category);
        return cat != null && cat.containsKey(key);
    }

    public void load()
    {
        if (PARENT != null && PARENT != this)
        {
            return;
        }

        BufferedReader buffer = null;
        UnicodeInputStreamReader input = null;
        try
        {
            if (file.getParentFile() != null)
            {
                file.getParentFile().mkdirs();
            }

            if (!file.exists() && !file.createNewFile())
            {
                return;
            }

            if (file.canRead())
            {
                input = new UnicodeInputStreamReader(new FileInputStream(file), defaultEncoding);
                defaultEncoding = input.getEncoding();
                buffer = new BufferedReader(input);

                String line;
                ConfigCategory currentCat = null;
                Property.Type type = null;
                ArrayList<String> tmpList = null;
                int lineNum = 0;
                String name = null;

                while (true)
                {
                    lineNum++;
                    line = buffer.readLine();

                    if (line == null)
                    {
                        break;
                    }

                    Matcher start = CONFIG_START.matcher(line);
                    Matcher end = CONFIG_END.matcher(line);

                    if (start.matches())
                    {
                        fileName = start.group(1);
                        categories = new TreeMap<String, ConfigCategory>();
                        continue;
                    }
                    else if (end.matches())
                    {
                        fileName = end.group(1);
                        Configuration child = new Configuration();
                        child.categories = categories;
                        this.children.put(fileName, child);
                        continue;
                    }

                    int nameStart = -1, nameEnd = -1;
                    boolean skip = false;
                    boolean quoted = false;

                    for (int i = 0; i < line.length() && !skip; ++i)
                    {
                        if (Character.isLetterOrDigit(line.charAt(i)) || ALLOWED_CHARS.indexOf(line.charAt(i)) != -1 || (quoted && line.charAt(i) != '"'))
                        {
                            if (nameStart == -1)
                            {
                                nameStart = i;
                            }

                            nameEnd = i;
                        }
                        else if (Character.isWhitespace(line.charAt(i)))
                        {
                            // ignore space charaters
                        }
                        else
                        {
                            switch (line.charAt(i))
                            {
                                case '#':
                                    skip = true;
                                    continue;

                                case '"':
                                    if (quoted)
                                    {
                                        quoted = false;
                                    }
                                    if (!quoted && nameStart == -1)
                                    {
                                        quoted = true;
                                    }
                                    break;

                                case '{':
                                    name = line.substring(nameStart, nameEnd + 1);
                                    String qualifiedName = ConfigCategory.getQualifiedName(name, currentCat);

                                    ConfigCategory cat = categories.get(qualifiedName);
                                    if (cat == null)
                                    {
                                        currentCat = new ConfigCategory(name, currentCat);
                                        categories.put(qualifiedName, currentCat);
                                    }
                                    else
                                    {
                                        currentCat = cat;
                                    }
                                    name = null;

                                    break;

                                case '}':
                                    if (currentCat == null)
                                    {
                                        throw new RuntimeException(String.format("Config file corrupt, attepted to close to many categories '%s:%d'", fileName, lineNum));
                                    }
                                    currentCat = currentCat.parent;
                                    break;

                                case '=':
                                    name = line.substring(nameStart, nameEnd + 1);

                                    if (currentCat == null)
                                    {
                                        throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, fileName, lineNum));
                                    }

                                    Property prop = new Property(name, line.substring(i + 1), type, true);
                                    i = line.length();

                                    currentCat.put(name, prop);

                                    break;

                                case ':':
                                    type = Property.Type.tryParse(line.substring(nameStart, nameEnd + 1).charAt(0));
                                    nameStart = nameEnd = -1;
                                    break;

                                case '<':
                                    if (tmpList != null)
                                    {
                                        throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                                    }

                                    name = line.substring(nameStart, nameEnd + 1);

                                    if (currentCat == null)
                                    {
                                        throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, fileName, lineNum));
                                    }

                                    tmpList = new ArrayList<String>();
                                    
                                    skip = true;
                                    
                                    break;

                                case '>':
                                    if (tmpList == null)
                                    {
                                        throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                                    }

                                    currentCat.put(name, new Property(name, tmpList.toArray(new String[tmpList.size()]), type));
                                    name = null;
                                    tmpList = null;
                                    type = null;
                                    break;

                                default:
                                    throw new RuntimeException(String.format("Unknown character '%s' in '%s:%d'", line.charAt(i), fileName, lineNum));
                            }
                        }
                    }

                    if (quoted)
                    {
                        throw new RuntimeException(String.format("Unmatched quote in '%s:%d'", fileName, lineNum));
                    }
                    else if (tmpList != null && !skip)
                    {
                        tmpList.add(line.trim());
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (buffer != null)
            {
                try
                {
                    buffer.close();
                } catch (IOException e){}
            }
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException e){}
            }
        }

        resetChangedState();
    }

    public void save()
    {
        if (PARENT != null && PARENT != this)
        {
            PARENT.save();
            return;
        }

        try
        {
            if (file.getParentFile() != null)
            {
                file.getParentFile().mkdirs();
            }

            if (!file.exists() && !file.createNewFile())
            {
                return;
            }

            if (file.canWrite())
            {
                FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, defaultEncoding));

                buffer.write("# Configuration file" + newLine + newLine);

                if (children.isEmpty())
                {
                    save(buffer);
                }
                else
                {
                    for (Map.Entry<String, Configuration> entry : children.entrySet())
                    {
                        buffer.write("START: \"" + entry.getKey() + "\"" + newLine);
                        entry.getValue().save(buffer);
                        buffer.write("END: \"" + entry.getKey() + "\"" + newLine + newLine);
                    }
                }

                buffer.close();
                fos.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void save(BufferedWriter out) throws IOException
    {        
        for (ConfigCategory cat : categories.values())
        {
            if (!cat.isChild())
            {
                cat.write(out, 0);
                out.newLine();
            }
        }
    }

    public ConfigCategory getCategory(String category)
    {
        ConfigCategory ret = categories.get(category);

        if (ret == null)
        {
            if (category.contains(CATEGORY_SPLITTER))
            {
                String[] hierarchy = category.split("\\"+CATEGORY_SPLITTER);
                ConfigCategory parent = categories.get(hierarchy[0]);

                if (parent == null)
                {
                    parent = new ConfigCategory(hierarchy[0]);
                    categories.put(parent.getQualifiedName(), parent);
                    changed = true;
                }

                for (int i = 1; i < hierarchy.length; i++)
                {
                    String name = ConfigCategory.getQualifiedName(hierarchy[i], parent);
                    ConfigCategory child = categories.get(name);

                    if (child == null)
                    {
                        child = new ConfigCategory(hierarchy[i], parent);
                        categories.put(name, child);
                        changed = true;
                    }

                    ret = child;
                    parent = child;
                }
            }
            else
            {
                ret = new ConfigCategory(category);
                categories.put(category, ret);
                changed = true;
            }
        }

        return ret;
    }

    public void removeCategory(ConfigCategory category)
    {
        for (ConfigCategory child : category.getChildren())
        {
            removeCategory(child);
        }

        if (categories.containsKey(category.getQualifiedName()))
        {
            categories.remove(category.getQualifiedName());
            if (category.parent != null)
            {
                category.parent.removeChild(category);
            }
            changed = true;
        }
    }

    public void addCustomCategoryComment(String category, String comment)
    {
        if (!caseSensitiveCustomCategories)
            category = category.toLowerCase(Locale.ENGLISH);
        getCategory(category).setComment(comment);
    }

    public void setChild(String name, Configuration child)
    {
        if (!children.containsKey(name))
        {
            children.put(name, child);
            changed = true;
        }
        else
        {
            Configuration old = children.get(name);
            child.categories = old.categories;
            child.fileName = old.fileName;
            old.changed = true;
        }
    }

    public static void enableGlobalConfig()
    {
        PARENT = new Configuration(new File(Loader.instance().getConfigDir(), "global.cfg"));
        PARENT.load();
    }

    public static class UnicodeInputStreamReader extends Reader
    {
        private final InputStreamReader input;
        private final String defaultEnc;

        public UnicodeInputStreamReader(InputStream source, String encoding) throws IOException
        {
            defaultEnc = encoding;
            String enc = encoding;
            byte[] data = new byte[4];

            PushbackInputStream pbStream = new PushbackInputStream(source, data.length);
            int read = pbStream.read(data, 0, data.length);
            int size = 0;

            int bom16 = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
            int bom24 = bom16 << 8 | (data[2] & 0xFF);
            int bom32 = bom24 << 8 | (data[3] & 0xFF);

            if (bom24 == 0xEFBBBF)
            {
                enc = "UTF-8";
                size = 3;
            }
            else if (bom16 == 0xFEFF)
            {
                enc = "UTF-16BE";
                size = 2;
            }
            else if (bom16 == 0xFFFE)
            {
                enc = "UTF-16LE";
                size = 2;
            }
            else if (bom32 == 0x0000FEFF)
            {
                enc = "UTF-32BE";
                size = 4;
            }
            else if (bom32 == 0xFFFE0000) //This will never happen as it'll be caught by UTF-16LE,
            {                             //but if anyone ever runs across a 32LE file, i'd like to disect it.
                enc = "UTF-32LE";
                size = 4;
            }

            if (size < read)
            {
                pbStream.unread(data, size, read - size);
            }

            this.input = new InputStreamReader(pbStream, enc);
        }

        public String getEncoding()
        {
            return input.getEncoding();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
            return input.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException
        {
            input.close();
        }
    }

    public boolean hasChanged()
    {
        if (changed) return true;
        
        for (ConfigCategory cat : categories.values())
        {
            if (cat.hasChanged()) return true;
        }

        for (Configuration child : children.values())
        {
            if (child.hasChanged()) return true;
        }

        return false;
    }

    public void resetChangedState()
    {
        changed = false;
        for (ConfigCategory cat : categories.values())
        {
            cat.resetChangedState();
        }

        for (Configuration child : children.values())
        {
            child.resetChangedState();
        }
    }

    public Set<String> getCategoryNames()
    {
        return ImmutableSet.copyOf(categories.keySet());
    }
}