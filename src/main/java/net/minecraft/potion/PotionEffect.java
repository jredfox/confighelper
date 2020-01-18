package net.minecraft.potion;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PotionEffect
{
    /** ID value of the potion this effect matches. */
    private int potionID;
    /** The duration of the potion effect */
    private int duration;
    /** The amplifier of the potion effect */
    private int amplifier;
    /** Whether the potion is a splash potion */
    private boolean isSplashPotion;
    /** Whether the potion effect came from a beacon */
    private boolean isAmbient;
    /** True if potion effect duration is at maximum, false otherwise. */
    @SideOnly(Side.CLIENT)
    private boolean isPotionDurationMax;
    private static final String __OBFID = "CL_00001529";
    /** List of ItemStack that can cure the potion effect **/
    private List<ItemStack> curativeItems;
    
    public PotionEffect(short b0, int i, short b1, boolean flag) 
    {
    	
	}
    
    private boolean getIsAmbient() {
		return false;
	}

	private int getDuration() {
		return 0;
	}

	private short getAmplifier() {
		return 0;
	}

	private short getPotionID() {
		return 0;
	}

	/**
     * Write a custom potion effect to a potion item's NBT data.
     */
    public NBTTagCompound writeCustomPotionEffectToNBT(NBTTagCompound p_82719_1_)
    {
        p_82719_1_.setShort("Id", (short)this.getPotionID());
        p_82719_1_.setShort("Amplifier", (short)this.getAmplifier());
        p_82719_1_.setInteger("Duration", this.getDuration());
        p_82719_1_.setBoolean("Ambient", this.getIsAmbient());
        return p_82719_1_;
    }

	/**
     * Read a custom potion effect from a potion item's NBT data.
     */
    public static PotionEffect readCustomPotionEffectFromNBT(NBTTagCompound p_82722_0_)
    {
        short b0 = p_82722_0_.getShort("Id");

        if (b0 >= 0 && b0 < Potion.potionTypes.length && Potion.potionTypes[b0] != null)
        {
            short b1 = p_82722_0_.getShort("Amplifier");
            int i = p_82722_0_.getInteger("Duration");
            boolean flag = p_82722_0_.getBoolean("Ambient");
            return new PotionEffect(b0, i, b1, flag);
        }
        else
        {
            return null;
        }
    }
}