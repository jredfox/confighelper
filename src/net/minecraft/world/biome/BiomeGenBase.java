package net.minecraft.world.biome;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jredfox.crashwconflicts.CrashWConflicts;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;

import net.minecraftforge.common.*;
import net.minecraftforge.event.terraingen.*;

public abstract class BiomeGenBase
{
    /** An array of all the biomes, indexed by biome id. */
    public static final BiomeGenBase[] biomeList = new BiomeGenBase[256];
    public static final BiomeGenBase ocean = (new BiomeGenOcean(0)).setColor(112).setBiomeName("Ocean").setMinMaxHeight(-1.0F, 0.4F);
    public static final BiomeGenBase plains = (new BiomeGenPlains(1)).setColor(9286496).setBiomeName("Plains").setTemperatureRainfall(0.8F, 0.4F);
    public static final BiomeGenBase desert = (new BiomeGenDesert(2)).setColor(16421912).setBiomeName("Desert").setDisableRain().setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.1F, 0.2F);
    public static final BiomeGenBase extremeHills = (new BiomeGenHills(3)).setColor(6316128).setBiomeName("Extreme Hills").setMinMaxHeight(0.3F, 1.5F).setTemperatureRainfall(0.2F, 0.3F);
    public static final BiomeGenBase forest = (new BiomeGenForest(4)).setColor(353825).setBiomeName("Forest").func_76733_a(5159473).setTemperatureRainfall(0.7F, 0.8F);
    public static final BiomeGenBase taiga = (new BiomeGenTaiga(5)).setColor(747097).setBiomeName("Taiga").func_76733_a(5159473).setEnableSnow().setTemperatureRainfall(0.05F, 0.8F).setMinMaxHeight(0.1F, 0.4F);
    public static final BiomeGenBase swampland = (new BiomeGenSwamp(6)).setColor(522674).setBiomeName("Swampland").func_76733_a(9154376).setMinMaxHeight(-0.2F, 0.1F).setTemperatureRainfall(0.8F, 0.9F);
    public static final BiomeGenBase river = (new BiomeGenRiver(7)).setColor(255).setBiomeName("River").setMinMaxHeight(-0.5F, 0.0F);
    public static final BiomeGenBase hell = (new BiomeGenHell(8)).setColor(16711680).setBiomeName("Hell").setDisableRain().setTemperatureRainfall(2.0F, 0.0F);

    /** Is the biome used for sky world. */
    public static final BiomeGenBase sky = (new BiomeGenEnd(9)).setColor(8421631).setBiomeName("Sky").setDisableRain();
    public static final BiomeGenBase frozenOcean = (new BiomeGenOcean(10)).setColor(9474208).setBiomeName("FrozenOcean").setEnableSnow().setMinMaxHeight(-1.0F, 0.5F).setTemperatureRainfall(0.0F, 0.5F);
    public static final BiomeGenBase frozenRiver = (new BiomeGenRiver(11)).setColor(10526975).setBiomeName("FrozenRiver").setEnableSnow().setMinMaxHeight(-0.5F, 0.0F).setTemperatureRainfall(0.0F, 0.5F);
    public static final BiomeGenBase icePlains = (new BiomeGenSnow(12)).setColor(16777215).setBiomeName("Ice Plains").setEnableSnow().setTemperatureRainfall(0.0F, 0.5F);
    public static final BiomeGenBase iceMountains = (new BiomeGenSnow(13)).setColor(10526880).setBiomeName("Ice Mountains").setEnableSnow().setMinMaxHeight(0.3F, 1.3F).setTemperatureRainfall(0.0F, 0.5F);
    public static final BiomeGenBase mushroomIsland = (new BiomeGenMushroomIsland(14)).setColor(16711935).setBiomeName("MushroomIsland").setTemperatureRainfall(0.9F, 1.0F).setMinMaxHeight(0.2F, 1.0F);
    public static final BiomeGenBase mushroomIslandShore = (new BiomeGenMushroomIsland(15)).setColor(10486015).setBiomeName("MushroomIslandShore").setTemperatureRainfall(0.9F, 1.0F).setMinMaxHeight(-1.0F, 0.1F);

    /** Beach biome. */
    public static final BiomeGenBase beach = (new BiomeGenBeach(16)).setColor(16440917).setBiomeName("Beach").setTemperatureRainfall(0.8F, 0.4F).setMinMaxHeight(0.0F, 0.1F);

    /** Desert Hills biome. */
    public static final BiomeGenBase desertHills = (new BiomeGenDesert(17)).setColor(13786898).setBiomeName("DesertHills").setDisableRain().setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.3F, 0.8F);

    /** Forest Hills biome. */
    public static final BiomeGenBase forestHills = (new BiomeGenForest(18)).setColor(2250012).setBiomeName("ForestHills").func_76733_a(5159473).setTemperatureRainfall(0.7F, 0.8F).setMinMaxHeight(0.3F, 0.7F);

    /** Taiga Hills biome. */
    public static final BiomeGenBase taigaHills = (new BiomeGenTaiga(19)).setColor(1456435).setBiomeName("TaigaHills").setEnableSnow().func_76733_a(5159473).setTemperatureRainfall(0.05F, 0.8F).setMinMaxHeight(0.3F, 0.8F);

    /** Extreme Hills Edge biome. */
    public static final BiomeGenBase extremeHillsEdge = (new BiomeGenHills(20)).setColor(7501978).setBiomeName("Extreme Hills Edge").setMinMaxHeight(0.2F, 0.8F).setTemperatureRainfall(0.2F, 0.3F);

    /** Jungle biome identifier */
    public static final BiomeGenBase jungle = (new BiomeGenJungle(21)).setColor(5470985).setBiomeName("Jungle").func_76733_a(5470985).setTemperatureRainfall(1.2F, 0.9F).setMinMaxHeight(0.2F, 0.4F);
    public static final BiomeGenBase jungleHills = (new BiomeGenJungle(22)).setColor(2900485).setBiomeName("JungleHills").func_76733_a(5470985).setTemperatureRainfall(1.2F, 0.9F).setMinMaxHeight(1.8F, 0.5F);
    public String biomeName;
    public int color;

    /** The block expected to be on the top of this biome */
    public byte topBlock;

    /** The block to fill spots in when not on the top */
    public byte fillerBlock;
    public int field_76754_C;

    /** The minimum height of this biome. Default 0.1. */
    public float minHeight;

    /** The maximum height of this biome. Default 0.3. */
    public float maxHeight;

    /** The temperature of this biome. */
    public float temperature;

    /** The rainfall in this biome. */
    public float rainfall;

    /** Color tint applied to water depending on biome */
    public int waterColorMultiplier;

    /** The biome decorator. */
    public BiomeDecorator theBiomeDecorator;

    /**
     * Holds the classes of IMobs (hostile mobs) that can be spawned in the biome.
     */
    protected List spawnableMonsterList;

    /**
     * Holds the classes of any creature that can be spawned in the biome as friendly creature.
     */
    protected List spawnableCreatureList;

    /**
     * Holds the classes of any aquatic creature that can be spawned in the water of the biome.
     */
    protected List spawnableWaterCreatureList;
    protected List spawnableCaveCreatureList;

    /** Set to true if snow is enabled for this biome. */
    private boolean enableSnow;

    /**
     * Is true (default) if the biome support rain (desert and nether can't have rain)
     */
    private boolean enableRain;

    /** The id number to this biome, and its index in the biomeList array. */
    public final int biomeID;

    /** The tree generator. */
    protected WorldGenTrees worldGeneratorTrees;

    /** The big tree generator. */
    protected WorldGenBigTree worldGeneratorBigTree;

    /** The forest generator. */
    protected WorldGenForest worldGeneratorForest;

    /** The swamp tree generator. */
    protected WorldGenSwamp worldGeneratorSwamp;

    public BiomeGenBase(int par1)
    {
        this.topBlock = (byte)Block.grass.blockID;
        this.fillerBlock = (byte)Block.dirt.blockID;
        this.field_76754_C = 5169201;
        this.minHeight = 0.1F;
        this.maxHeight = 0.3F;
        this.temperature = 0.5F;
        this.rainfall = 0.5F;
        this.waterColorMultiplier = 16777215;
        this.spawnableMonsterList = new ArrayList();
        this.spawnableCreatureList = new ArrayList();
        this.spawnableWaterCreatureList = new ArrayList();
        this.spawnableCaveCreatureList = new ArrayList();
        this.enableRain = true;
        this.worldGeneratorTrees = new WorldGenTrees(false);
        this.worldGeneratorBigTree = new WorldGenBigTree(false);
        this.worldGeneratorForest = new WorldGenForest(false);
        this.worldGeneratorSwamp = new WorldGenSwamp();
        if( biomeList[par1] != null)
        	par1 = CrashWConflicts.getFreeId(CrashWConflicts.biomes, biomeList, par1, this.getClass(), biomeList[par1].getClass().getName());
        this.biomeID = par1;
        
        biomeList[par1] = this;
        this.theBiomeDecorator = this.createBiomeDecorator();
        this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityPig.class, 10, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityChicken.class, 10, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityCow.class, 8, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEnderman.class, 1, 1, 4));
        this.spawnableWaterCreatureList.add(new SpawnListEntry(EntitySquid.class, 10, 4, 4));
        this.spawnableCaveCreatureList.add(new SpawnListEntry(EntityBat.class, 10, 8, 8));
    }

    /**
     * Allocate a new BiomeDecorator for this BiomeGenBase
     */
    public BiomeDecorator createBiomeDecorator()
    {   
        return getModdedBiomeDecorator(new BiomeDecorator(this));
    }

    /**
     * Sets the temperature and rainfall of this biome.
     */
    public BiomeGenBase setTemperatureRainfall(float par1, float par2)
    {
        if (par1 > 0.1F && par1 < 0.2F)
        {
            throw new IllegalArgumentException("Please avoid temperatures in the range 0.1 - 0.2 because of snow");
        }
        else
        {
            this.temperature = par1;
            this.rainfall = par2;
            return this;
        }
    }

    /**
     * Sets the minimum and maximum height of this biome. Seems to go from -2.0 to 2.0.
     */
    public BiomeGenBase setMinMaxHeight(float par1, float par2)
    {
        this.minHeight = par1;
        this.maxHeight = par2;
        return this;
    }

    /**
     * Disable the rain for the biome.
     */
    public BiomeGenBase setDisableRain()
    {
        this.enableRain = false;
        return this;
    }

    /**
     * Gets a WorldGen appropriate for this biome.
     */
    public WorldGenerator getRandomWorldGenForTrees(Random par1Random)
    {
        return (WorldGenerator)(par1Random.nextInt(10) == 0 ? this.worldGeneratorBigTree : this.worldGeneratorTrees);
    }

    /**
     * Gets a WorldGen appropriate for this biome.
     */
    public WorldGenerator getRandomWorldGenForGrass(Random par1Random)
    {
        return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
    }

    /**
     * sets enableSnow to true during biome initialization. returns BiomeGenBase.
     */
    public BiomeGenBase setEnableSnow()
    {
        this.enableSnow = true;
        return this;
    }

    public BiomeGenBase setBiomeName(String par1Str)
    {
        this.biomeName = par1Str;
        return this;
    }

    public BiomeGenBase func_76733_a(int par1)
    {
        this.field_76754_C = par1;
        return this;
    }

    public BiomeGenBase setColor(int par1)
    {
        this.color = par1;
        return this;
    }

    @SideOnly(Side.CLIENT)

    /**
     * takes temperature, returns color
     */
    public int getSkyColorByTemp(float par1)
    {
        par1 /= 3.0F;

        if (par1 < -1.0F)
        {
            par1 = -1.0F;
        }

        if (par1 > 1.0F)
        {
            par1 = 1.0F;
        }

        return Color.getHSBColor(0.62222224F - par1 * 0.05F, 0.5F + par1 * 0.1F, 1.0F).getRGB();
    }

    /**
     * Returns the correspondent list of the EnumCreatureType informed.
     */
    public List getSpawnableList(EnumCreatureType par1EnumCreatureType)
    {
        return par1EnumCreatureType == EnumCreatureType.monster ? this.spawnableMonsterList : (par1EnumCreatureType == EnumCreatureType.creature ? this.spawnableCreatureList : (par1EnumCreatureType == EnumCreatureType.waterCreature ? this.spawnableWaterCreatureList : (par1EnumCreatureType == EnumCreatureType.ambient ? this.spawnableCaveCreatureList : null)));
    }

    /**
     * Returns true if the biome have snowfall instead a normal rain.
     */
    public boolean getEnableSnow()
    {
        return this.enableSnow;
    }

    /**
     * Return true if the biome supports lightning bolt spawn, either by have the bolts enabled and have rain enabled.
     */
    public boolean canSpawnLightningBolt()
    {
        return this.enableSnow ? false : this.enableRain;
    }

    /**
     * Checks to see if the rainfall level of the biome is extremely high
     */
    public boolean isHighHumidity()
    {
        return this.rainfall > 0.85F;
    }

    /**
     * returns the chance a creature has to spawn.
     */
    public float getSpawningChance()
    {
        return 0.1F;
    }

    /**
     * Gets an integer representation of this biome's rainfall
     */
    public final int getIntRainfall()
    {
        return (int)(this.rainfall * 65536.0F);
    }

    /**
     * Gets an integer representation of this biome's temperature
     */
    public final int getIntTemperature()
    {
        return (int)(this.temperature * 65536.0F);
    }

    @SideOnly(Side.CLIENT)

    /**
     * Gets a floating point representation of this biome's rainfall
     */
    public final float getFloatRainfall()
    {
        return this.rainfall;
    }

    /**
     * Gets a floating point representation of this biome's temperature
     */
    public final float getFloatTemperature()
    {
        return this.temperature;
    }

    public void decorate(World par1World, Random par2Random, int par3, int par4)
    {
        this.theBiomeDecorator.decorate(par1World, par2Random, par3, par4);
    }

    @SideOnly(Side.CLIENT)

    /**
     * Provides the basic grass color based on the biome temperature and rainfall
     */
    public int getBiomeGrassColor()
    {
        double d0 = (double)MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
        double d1 = (double)MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
        return getModdedBiomeGrassColor(ColorizerGrass.getGrassColor(d0, d1));
    }

    @SideOnly(Side.CLIENT)

    /**
     * Provides the basic foliage color based on the biome temperature and rainfall
     */
    public int getBiomeFoliageColor()
    {
        double d0 = (double)MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
        double d1 = (double)MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
        return getModdedBiomeFoliageColor(ColorizerFoliage.getFoliageColor(d0, d1));
    }

    public BiomeDecorator getModdedBiomeDecorator(BiomeDecorator original)
    {
        BiomeEvent.CreateDecorator event = new BiomeEvent.CreateDecorator(this, original);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.newBiomeDecorator;
    }

    @SideOnly(Side.CLIENT)
    public int getWaterColorMultiplier()
    {
        BiomeEvent.GetWaterColor event = new BiomeEvent.GetWaterColor(this, waterColorMultiplier);
        MinecraftForge.EVENT_BUS.post(event);
        return event.newColor;
    }
    
    @SideOnly(Side.CLIENT)
    public int getModdedBiomeGrassColor(int original)
    {
        BiomeEvent.GetGrassColor event = new BiomeEvent.GetGrassColor(this, original);
        MinecraftForge.EVENT_BUS.post(event);
        return event.newColor;
    }
    

    @SideOnly(Side.CLIENT)
    public int getModdedBiomeFoliageColor(int original)
    {
        BiomeEvent.GetFoliageColor event = new BiomeEvent.GetFoliageColor(this, original);
        MinecraftForge.EVENT_BUS.post(event);
        return event.newColor;
    }
}
