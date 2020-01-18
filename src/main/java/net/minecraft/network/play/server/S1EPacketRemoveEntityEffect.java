package net.minecraft.network.play.server;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.PotionEffect;

public class S1EPacketRemoveEntityEffect extends Packet
{
    private int entityId;
    private int potionId;

    public S1EPacketRemoveEntityEffect() {}

    public S1EPacketRemoveEntityEffect(int id, PotionEffect effect)
    {
        this.entityId = id;
        this.potionId = effect.getPotionID();
    }
    
    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(this.entityId);
        buf.writeShort(this.potionId);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityId = buf.readInt();
        this.potionId = buf.readShort();
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient pc)
    {
        pc.handleRemoveEntityEffect(this);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandler nh)
    {
        this.processPacket((INetHandlerPlayClient)nh);
    }

    @SideOnly(Side.CLIENT)
    public int func_149076_c()
    {
        return this.entityId;
    }

    @SideOnly(Side.CLIENT)
    public int func_149075_d()
    {
        return this.potionId;
    }
}