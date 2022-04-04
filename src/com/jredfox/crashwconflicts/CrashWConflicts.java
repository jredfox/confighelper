package com.jredfox.crashwconflicts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import com.jredfox.crashwconflicts.proxy.Proxy;
import com.jredfox.crashwconflicts.reg.Registry;
import com.jredfox.crashwconflicts.reg.Registry.RegEntry;
import com.jredfox.crashwconflicts.tst.D;
import com.jredfox.crashwconflicts.tst.E;
import com.jredfox.util.IdChunk;
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = "crash-w-conflicts", name = "Crash With Conflicts", version = "b52")
public class CrashWConflicts implements ITickHandler{
	
	public static boolean hasConflicts;
	public static int entId = 127;
	public static boolean writeFreeIds;
	public static boolean autocfg;
	public static boolean showPassables;
	public static boolean passableNullables;
	public static File cwcMain = new File("config/cwc").getAbsoluteFile();
	public static File cwcDir = new File(cwcMain, "dumpIds");
	@SidedProxy(clientSide="com.jredfox.crashwconflicts.proxy.ClientProxy", serverSide="com.jredfox.crashwconflicts.proxy.Proxy")
	public static Proxy proxy;
	
	static
	{
		initCfg();//called in mod's conctructor to prevent other conflicts from happening in pre-init before this mod is loaded. <clinit> can cause class initialization errors if the RegTypes's class isn't initialized yet
	}
	
	public CrashWConflicts()
	{
		RegUtils.init();
	}
	
	@PreInit
	public static void preInit(FMLPreInitializationEvent pi)
	{
//		//DimensionTest ItemStack ItemBlock GameRegistry ItemArmor
//		DimensionManager.registerDimension(0, 0);
//		DimensionManager.registerDimension(-1, -1);
//		DimensionManager.registerProviderType(-1, WorldProviderSurface.class, true);
//		DimensionManager.registerProviderType(0, WorldProviderEnd.class, true);
//		
//		//conflict test
//		Item item1 = new Item(69).setUnlocalizedName("item.tst");
//		new Block(1, Material.anvil).setUnlocalizedName("tile.tst");
//		new BiomeGenOcean(3).setBiomeName("cwc-ocean");
//		new EnchantmentProtection(1, 5, 1);
//		new Potion(3, false, 400).setPotionName("cwcPotion1");
//		EntityRegistry.registerGlobalEntityID(D.class, "a", 50);
//		EntityList.addMapping(E.class, "a", 50);
//		new Item(69).setUnlocalizedName("item.tst2");
//		new Block(1, Material.anvil).setUnlocalizedName("tile.tst2");
//		new BiomeGenOcean(3).setBiomeName("cwc-ocean2");
//		new EnchantmentProtection(1, 5, 1);
//		new Potion(3, false, 400).setPotionName("Crash W Conflicts Potion Name");
		
		TickRegistry.registerTickHandler(new CrashWConflicts(), Side.CLIENT);
	}

	public static void initCfg()
	{
		Configuration cfg = new Configuration(new File(cwcMain, "cwc.cfg"));
		cfg.load();
		entId = cfg.get("global", "entityIdLimit", entId).getInt();
		writeFreeIds = cfg.get("global", "writeFreeIds", true).getBoolean(true);
		autocfg = cfg.get("global", "autoConfig", false).getBoolean(false);
		showPassables = cfg.get("global", "showPassableConflicts", true).getBoolean(true);
		passableNullables = cfg.get("global", "passableNullables", true, "allow mods to register to null indices which were previously registered as non null. Disable this if you have an unintended conflict from this and manually add each one to passables").getBoolean(true);
		String[] arr = cfg.get("global", "passable", new String[0], "for dimensions use null as the class. Format=num:class:modid").getStringList();
		for(String s : arr)
		{
			if(s.isEmpty())
				continue;
			try
			{
				String[] parts = s.split(":");
				String c = parts[1].trim();
				Passable p = new Passable(Integer.parseInt(parts[0]), c.equals("null") ? Passable.class.getName() : parts[1], parts[2]);
				System.out.println("adding passable:" + p);
				Registry.passables.add(p);
			}
			catch (Throwable t)
			{
				System.err.println("skipping passable:" + s);
				t.printStackTrace();
			}
		}
		if(cfg.hasChanged())
			cfg.save();
	}

	/**
	 * dump conflicts and free ids then crash the game
	 */
	public static void dumpIds()
	{
		try 
		{
			long ms = System.currentTimeMillis();
			cwcDir.mkdirs();
			if(writeFreeIds)
				writeFreeIds();
			writeConflicts();
			System.out.println("done dumpingIds:" + (System.currentTimeMillis() - ms) + "ms");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if(hasConflicts)
		{
			CrashReport c = CrashReport.makeCrashReport(new RuntimeException("id conflict"), "minecraft cannot continue with id conflicts shutting down! reconfigure your modpack ;)");
			proxy.displayCrash(c);
		}
		else
			System.out.println("\n\n----> conflict free UwU\n");
	}
	
	public static void writeFreeIds() throws IOException
	{
		for(RegTypes type : RegTypes.values())
		{
			int min = RegUtils.getMin(type, true);
			int max = RegUtils.getMax(type, true);
//			min = 0;
			Set<Integer> org = RegUtils.getOccupiedIds(type);
			Set<IdChunk> chunky = IdChunk.fromAround(min, max, org);
			BufferedWriter fw = RegUtils.getWriter(new File(cwcDir, "freeids-" + type.name().substring(0, 1) + type.name().toLowerCase().substring(1) + ".txt"));
			int count = 0;
			for(IdChunk chunk : chunky)
			{
				fw.write(RegUtils.unshiftIdChunk(type, chunk) + System.lineSeparator());
				if(count++ % 300 == 0)
					fw.flush();
			}
			fw.close();
		}
	}
	
	public static void writeConflicts() throws IOException
	{
		for(Registry reg : Registry.regs)
		{
			RegTypes type = reg.type;
			BufferedWriter fw = RegUtils.getWriter(new File(cwcDir, "conflicts-" + type.name().substring(0, 1) + type.name().toLowerCase().substring(1) + ".txt"));
			int count = 0;
			for(Set<RegEntry> entries : reg.registered.values())
			{
				RegEntry first = RegUtils.getFirst(entries);
				if(first == null || !reg.isConflicted(first.newId) || !showPassables && reg.isPassableConflict(first.newId))
					continue;
				StringBuilder b = new StringBuilder();
				b.append("id:" + RegUtils.unshiftId(type, first.newId) + (reg.hasItemBlock(entries) ? " blockId:" + first.newId : "") + System.lineSeparator());
				for(RegEntry e : entries)
					b.append("class:" + e.oClass.getName() + ", name:\"" + e.getName() + "\", modid:" + e.modid + ", modname:\"" + e.modname + "\"" + (e.passable  ? ", passable:" + true : "") + (e.newId != e.orgId ? ", newId:" + RegUtils.unshiftId(type, e.newId) : "") + System.lineSeparator());
				fw.write(b.toString());
				if(count++ % 100 == 0)
					fw.flush();
			}
			fw.close();
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData){}

	public boolean firstTick = true;
	public static boolean isCrashing;
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if(hasConflicts || firstTick)
			postLoad();
	}

	public void postLoad()
	{
		firstTick = false;
		if(autocfg)
			AutoConfig.INSTANCE.load().run();
		dumpIds();
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() 
	{
		return this.getClass().getSimpleName();
	}
}
