package net.minecraft.network.play.server;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jml.evilnotch.lib.JavaUtil;

import java.io.IOException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.PotionEffect;

public class S1DPacketEntityEffect extends Packet
{
    private int entityId;
    private int potionId;
    private int amp;
    private int duration;

    public S1DPacketEntityEffect() {}

    public S1DPacketEntityEffect(int entityId, PotionEffect effect)
    {
        this.entityId = entityId;
        this.potionId = (byte)(effect.getPotionID() & 255);
        this.amp = JavaUtil.castShort(effect.getAmplifier());
        this.duration = JavaUtil.castShort(effect.getDuration());
    }
    
    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
    	buf.writeInt(this.entityId);
    	buf.writeShort(this.potionId);
    	buf.writeShort(this.amp);
    	buf.writeShort(this.duration);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityId = buf.readInt();
        this.potionId = buf.readShort();
        this.amp      = buf.readShort();
        this.duration = buf.readShort();
    }
    
    @SideOnly(Side.CLIENT)
    public boolean func_149429_c()
    {
        return this.duration == 32767;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient pc)
    {
        pc.handleEntityEffect(this);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandler nh)
    {
        this.processPacket((INetHandlerPlayClient)nh);
    }

    @SideOnly(Side.CLIENT)
    public int func_149426_d()
    {
        return this.entityId;
    }

    @SideOnly(Side.CLIENT)
    public int func_149427_e()
    {
        return this.potionId;
    }

    @SideOnly(Side.CLIENT)
    public int func_149428_f()
    {
        return this.amp;
    }

    @SideOnly(Side.CLIENT)
    public int func_149425_g()
    {
        return this.duration;
    }
}