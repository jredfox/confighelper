package com.jredfox.crashwconflicts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.jredfox.crashwconflicts.proxy.Proxy;
import com.jredfox.crashwconflicts.reg.Registry;
import com.jredfox.crashwconflicts.reg.Registry.RegEntry;
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
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = "crash-w-conflicts", name = "Crash With Conflicts", version = "build47")
public class CrashWConflicts implements ITickHandler{
	
	public static boolean hasConflicts;
	public static int entId = 127;
	public static boolean writeFreeIds;
	public static boolean autocfg;
	public static File cwcMain = new File("config/cwc").getAbsoluteFile();
	public static File cwcDir = new File(cwcMain, "dumpIds");
	@SidedProxy(clientSide="com.jredfox.crashwconflicts.proxy.ClientProxy", serverSide="com.jredfox.crashwconflicts.proxy.Proxy")
	public static Proxy proxy;
	
	static
	{
		initCfg();//called in mod's conctructor to prevent other conflicts from happening in pre-init before this mod is loaded. <clinit> can cause class initialization errors if the RegTypes's class isn't initialized yet
	}
	
	@PreInit
	public static void preInit(FMLPreInitializationEvent pi)
	{
//		//DimensionTest
//		DimensionManager.registerDimension(0, 0);
//		DimensionManager.registerDimension(-1, -1);
//		DimensionManager.registerProviderType(-1, WorldProvider.class, true);
//		DimensionManager.registerProviderType(0, WorldProvider.class, true);
		
		//conflict test
//		new Item(RegUtils.getMax(RegTypes.ITEM)).setUnlocalizedName("item.tst");
//		new Block(1, Material.anvil).setUnlocalizedName("tile.tst");
//		new BiomeGenOcean(3);
//		new EnchantmentProtection(1, 5, 1);
//		new Potion(3, false, 400);
//		EntityRegistry.registerGlobalEntityID(D.class, "a", 14);
//		EntityList.addMapping(E.class, "a", 14);
//		new Item(69).setUnlocalizedName("item.tst");
//		new Block(1, Material.anvil).setUnlocalizedName("tile.tst");
//		new BiomeGenOcean(3);
//		new EnchantmentProtection(1, 5, 1);
//		new Potion(3, false, 400);
//		ItemBlock
		RegUtils.init();
		TickRegistry.registerTickHandler(new CrashWConflicts(), Side.CLIENT);
	}

	public static void initCfg()
	{
		Configuration cfg = new Configuration(new File(cwcMain, "cwc.cfg"));
		cfg.load();
		entId = cfg.get("global", "entityIdLimit", entId).getInt();
		writeFreeIds = cfg.get("global", "writeFreeIds", true).getBoolean(true);
		autocfg = cfg.get("global", "autoConfig", false).getBoolean(false);
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
			Set<Integer> org = RegUtils.getOrgIds(type);
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
				if(first == null || !reg.isConflicted(first.id))
					continue;//skip the non conflicted registrations
				StringBuilder b = new StringBuilder();
				b.append("id:" + RegUtils.unshiftId(type, first.id) + (reg.hasItemBlock(entries) ? " blockId:" + first.id : ""));
				for(RegEntry e : entries)
					b.append(e.oClass + ", " + e.getName() + ", " + e.modid + ", " + e.modname + ", passable:" + e.passable + ", isGhost:" + e.isGhost + (e.id != e.orgId ? ", orgId:" + e.orgId : ""));
				fw.write(b.toString() + System.lineSeparator());
				if(count++ % 300 == 0)
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
			new AutoConfig().load().run();
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
