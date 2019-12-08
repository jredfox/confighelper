package net.minecraft.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.ObjectUtils;

import com.jredfox.confighelper.Registry;
import com.jredfox.confighelper.RegistryConfig;
import com.jredfox.confighelper.RegistryDataWatcher;
import com.jredfox.confighelper.RegistryTracker;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ReportedException;

public class DataWatcher
{
    private final Entity field_151511_a;
    /** When isBlank is true the DataWatcher is not watching any objects */
    private boolean isBlank = true;
    private static final HashMap dataTypes = new HashMap();
    private final Map watchedObjects = new HashMap();
    /** true if one or more object was changed */
    private boolean objectChanged;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final String __OBFID = "CL_00001559";
    public Registry reg;
    
    public DataWatcher(Entity e)
    {
        this.field_151511_a = e;
        if(e instanceof EntityPlayer)
        {
        	this.reg = new RegistryDataWatcher(this);
        }
    }

    /**
     * adds a new object to dataWatcher to watch, to update an already existing object see updateObject. Arguments: data
     * Value Id, Object to add
     */
    public void addObject(int id, Object obj)
    {
    	if(this.field_151511_a instanceof EntityPlayer)
    	{	
    		RegistryTracker.datawatchers = this.reg;
    		id = RegistryTracker.registerDataWatcher(this.field_151511_a.getClass(), id, reg);
    	}
        Integer integer = (Integer)dataTypes.get(obj.getClass());

        if (integer == null)
        {
            throw new IllegalArgumentException("Unknown data type: " + obj.getClass());
        }
        else if(id > 254 || id < 0)
        {
        	throw new IllegalArgumentException("Ids must be between (0-254)");
        }
        else if (this.containsId(Integer.valueOf(id)))
        {
            throw new IllegalArgumentException("Duplicate DataWatcher id:" + id + "!");
        }
        else
        {
            DataWatcher.WatchableObject watchableobject = new DataWatcher.WatchableObject(integer.intValue(), id, obj);
            this.lock.writeLock().lock();
            this.watchedObjects.put(Integer.valueOf(id), watchableobject);
            this.lock.writeLock().unlock();
            this.isBlank = false;
        }
    }

    /**
     * Add a new object for the DataWatcher to watch, using the specified data type.
     */
    public void addObjectByDataType(int id, int dataType)
    {
        DataWatcher.WatchableObject watchableobject = new DataWatcher.WatchableObject(dataType, id, (Object)null);
        this.lock.writeLock().lock();
        this.watchedObjects.put(Integer.valueOf(id), watchableobject);
        this.lock.writeLock().unlock();
        this.isBlank = false;
    }

    /**
     * gets the bytevalue of a watchable object
     */
    public byte getWatchableObjectByte(int id)
    {
        return ((Byte)this.getWatchedObject(id).getObject()).byteValue();
    }

    public short getWatchableObjectShort(int id)
    {
        return ((Short)this.getWatchedObject(id).getObject()).shortValue();
    }

    /**
     * gets a watchable object and returns it as a Integer
     */
    public int getWatchableObjectInt(int id)
    {
        return ((Integer)this.getWatchedObject(id).getObject()).intValue();
    }

    public float getWatchableObjectFloat(int id)
    {
        return ((Float)this.getWatchedObject(id).getObject()).floatValue();
    }

    /**
     * gets a watchable object and returns it as a String
     */
    public String getWatchableObjectString(int id)
    {
        return (String)this.getWatchedObject(id).getObject();
    }

    /**
     * Get a watchable object as an ItemStack.
     */
    public ItemStack getWatchableObjectItemStack(int id)
    {
        return (ItemStack)this.getWatchedObject(id).getObject();
    }

    /**
     * is threadsafe, unless it throws an exception, then
     */
    public DataWatcher.WatchableObject getWatchedObject(int id)
    {
        this.lock.readLock().lock();
        DataWatcher.WatchableObject watchableobject;

        try
        {
            watchableobject = (DataWatcher.WatchableObject)this.watchedObjects.get(Integer.valueOf(id));
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting synched entity data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Synched entity data");
            crashreportcategory.addCrashSection("Data ID", Integer.valueOf(id));
            throw new ReportedException(crashreport);
        }

        this.lock.readLock().unlock();
        return watchableobject;
    }
    
    public boolean containsId(int id)
    {
    	return this.watchedObjects.containsKey(id);
    }

    /**
     * updates an already existing object
     */
    public void updateObject(int id, Object obj)
    {
        DataWatcher.WatchableObject watchableobject = this.getWatchedObject(id);

        if (ObjectUtils.notEqual(obj, watchableobject.getObject()))
        {
            watchableobject.setObject(obj);
            this.field_151511_a.func_145781_i(id);
            watchableobject.setWatched(true);
            this.objectChanged = true;
        }
    }

    public void setObjectWatched(int id)
    {
        this.getWatchedObject(id).watched = true;
        this.objectChanged = true;
    }

    public boolean hasChanges()
    {
        return this.objectChanged;
    }

    /**
     * Writes the list of watched objects (entity attribute of type {byte, short, int, float, string, ItemStack,
     * ChunkCoordinates}) to the specified PacketBuffer
     */
    public static void writeWatchedListToPacketBuffer(List<DataWatcher.WatchableObject> list, PacketBuffer buf) throws IOException
    {
        if (list != null)
        {
            Iterator<DataWatcher.WatchableObject> iterator = list.iterator();

            while (iterator.hasNext())
            {
                DataWatcher.WatchableObject watchableobject = (DataWatcher.WatchableObject)iterator.next();
                writeWatchableObjectToPacketBuffer(buf, watchableobject);
            }
        }

        buf.writeByte(255);
    }

    public List<DataWatcher.WatchableObject> getChanged()
    {
        ArrayList arraylist = null;

        if (this.objectChanged)
        {
            this.lock.readLock().lock();
            Iterator iterator = this.watchedObjects.values().iterator();

            while (iterator.hasNext())
            {
                DataWatcher.WatchableObject watchableobject = (DataWatcher.WatchableObject)iterator.next();

                if (watchableobject.isWatched())
                {
                    watchableobject.setWatched(false);

                    if (arraylist == null)
                    {
                        arraylist = new ArrayList();
                    }

                    arraylist.add(watchableobject);
                }
            }

            this.lock.readLock().unlock();
        }

        this.objectChanged = false;
        return arraylist;
    }

    public void func_151509_a(PacketBuffer buf) throws IOException
    {
        this.lock.readLock().lock();
        Iterator iterator = this.watchedObjects.values().iterator();

        while (iterator.hasNext())
        {
            DataWatcher.WatchableObject watchableobject = (DataWatcher.WatchableObject)iterator.next();
            writeWatchableObjectToPacketBuffer(buf, watchableobject);
        }

        this.lock.readLock().unlock();
        buf.writeByte(255);
    }

    public List<DataWatcher.WatchableObject> getAllWatched()
    {
        ArrayList arraylist = null;
        this.lock.readLock().lock();
        DataWatcher.WatchableObject watchableobject;

        for (Iterator iterator = this.watchedObjects.values().iterator(); iterator.hasNext(); arraylist.add(watchableobject))
        {
            watchableobject = (DataWatcher.WatchableObject)iterator.next();

            if (arraylist == null)
            {
                arraylist = new ArrayList();
            }
        }

        this.lock.readLock().unlock();
        return arraylist;
    }

    /**
     * Writes a watchable object (entity attribute of type {byte, short, int, float, string, ItemStack,
     * ChunkCoordinates}) to the specified PacketBuffer
     */
    private static void writeWatchableObjectToPacketBuffer(PacketBuffer buf, DataWatcher.WatchableObject entry) throws IOException
    {
        buf.writeByte((byte)entry.getObjectType());
        buf.writeByte((byte)entry.getDataValueId());

        switch (entry.getObjectType())
        {
            case 0:
            	buf.writeByte(((Byte)entry.getObject()).byteValue());
                break;
            case 1:
            	buf.writeShort(((Short)entry.getObject()).shortValue());
                break;
            case 2:
            	buf.writeInt(((Integer)entry.getObject()).intValue());
                break;
            case 3:
            	buf.writeFloat(((Float)entry.getObject()).floatValue());
                break;
            case 4:
            	buf.writeStringToBuffer((String)entry.getObject());
                break;
            case 5:
                ItemStack itemstack = (ItemStack)entry.getObject();
                buf.writeItemStackToBuffer(itemstack);
                break;
            case 6:
                ChunkCoordinates chunkcoordinates = (ChunkCoordinates)entry.getObject();
                buf.writeInt(chunkcoordinates.posX);
                buf.writeInt(chunkcoordinates.posY);
                buf.writeInt(chunkcoordinates.posZ);
        }
    }

    /**
     * Reads a list of watched objects (entity attribute of type {byte, short, int, float, string, ItemStack,
     * ChunkCoordinates}) from the supplied PacketBuffer
     */
    public static List<DataWatcher.WatchableObject> readWatchedListFromPacketBuffer(PacketBuffer buf) throws IOException
    {
        ArrayList arraylist = null;
        int b;
        while ((b = buf.readUnsignedByte()) != 255)
        {
            if (arraylist == null)
            {
                arraylist = new ArrayList();
            }

            int dataType = b;
            int id = buf.readByte();
            DataWatcher.WatchableObject watchableobject = null;

            switch (dataType)
            {
                case 0:
                    watchableobject = new DataWatcher.WatchableObject(dataType, id, Byte.valueOf(buf.readByte()));
                    break;
                case 1:
                    watchableobject = new DataWatcher.WatchableObject(dataType, id, Short.valueOf(buf.readShort()));
                    break;
                case 2:
                    watchableobject = new DataWatcher.WatchableObject(dataType, id, Integer.valueOf(buf.readInt()));
                    break;
                case 3:
                    watchableobject = new DataWatcher.WatchableObject(dataType, id, Float.valueOf(buf.readFloat()));
                    break;
                case 4:
                    watchableobject = new DataWatcher.WatchableObject(dataType, id, buf.readStringFromBuffer(32767));
                    break;
                case 5:
                    watchableobject = new DataWatcher.WatchableObject(dataType, id, buf.readItemStackFromBuffer());
                    break;
                case 6:
                    int k = buf.readInt();
                    int l = buf.readInt();
                    int i1 = buf.readInt();
                    watchableobject = new DataWatcher.WatchableObject(dataType, id, new ChunkCoordinates(k, l, i1));
            }

            arraylist.add(watchableobject);
        }

        return arraylist;
    }

    @SideOnly(Side.CLIENT)
    public void updateWatchedObjectsFromList(List<DataWatcher.WatchableObject> list)
    {
        this.lock.writeLock().lock();
        Iterator iterator = list.iterator();

        while (iterator.hasNext())
        {
            DataWatcher.WatchableObject watchableobject = (DataWatcher.WatchableObject)iterator.next();
            DataWatcher.WatchableObject watchableobject1 = (DataWatcher.WatchableObject)this.watchedObjects.get(Integer.valueOf(watchableobject.getDataValueId()));

            if (watchableobject1 != null)
            {
                watchableobject1.setObject(watchableobject.getObject());
                this.field_151511_a.func_145781_i(watchableobject.getDataValueId());
            }
        }

        this.lock.writeLock().unlock();
        this.objectChanged = true;
    }

    public boolean getIsBlank()
    {
        return this.isBlank;
    }

    public void func_111144_e()
    {
        this.objectChanged = false;
    }

    static
    {
        dataTypes.put(Byte.class, Integer.valueOf(0));
        dataTypes.put(Short.class, Integer.valueOf(1));
        dataTypes.put(Integer.class, Integer.valueOf(2));
        dataTypes.put(Float.class, Integer.valueOf(3));
        dataTypes.put(String.class, Integer.valueOf(4));
        dataTypes.put(ItemStack.class, Integer.valueOf(5));
        dataTypes.put(ChunkCoordinates.class, Integer.valueOf(6));
    }

    public static class WatchableObject
        {
            private final int objectType;
            /** the Id of the data watcher object */
            private final int dataValueId;
            private Object watchedObject;
            private boolean watched;
            private static final String __OBFID = "CL_00001560";

            public WatchableObject(int dataType, int id, Object obj)
            {
                this.objectType = dataType;
                this.dataValueId = id;
                this.watchedObject = obj;
                this.watched = true;
            }

            public int getDataValueId()
            {
                return this.dataValueId;
            }

            public void setObject(Object p_75673_1_)
            {
                this.watchedObject = p_75673_1_;
            }

            public Object getObject()
            {
                return this.watchedObject;
            }

            public int getObjectType()
            {
                return this.objectType;
            }

            public boolean isWatched()
            {
                return this.watched;
            }

            public void setWatched(boolean p_75671_1_)
            {
                this.watched = p_75671_1_;
            }
        }
}