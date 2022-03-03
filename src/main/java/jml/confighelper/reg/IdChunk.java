package jml.confighelper.reg;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IdChunk {
	
	public int minId;
	public int maxId;
	
	public IdChunk(int min, int max)
	{
		this.minId = min;
		this.maxId = max;
	}
	
	/**
	 * generates id chunks that are not found from inside the collection of numbers
	 */
	public static Set<IdChunk> fromAround(int min, int max, Collection<Integer> ids)
	{
		if(ids.isEmpty())
			return Collections.EMPTY_SET;
		if(!(ids instanceof TreeSet))
			ids = new TreeSet(ids);
		Set<IdChunk> chunks = new LinkedHashSet();
		int minId = min;
		int maxId = max;
		for(Integer taken : ids)
		{
			if(taken < min)
				continue;
			else if(taken > max)
				break;
			maxId = taken - 1;
			if(maxId >= minId)
				chunks.add(new IdChunk(minId, maxId));
			minId = taken + 1;//reset min id for the next use
		}
		//add the last chunk if applicable
		if(minId <= max)
		{
			maxId = max;
			chunks.add(new IdChunk(minId, maxId));
		}
		return chunks;
	}
	
	/**
	 * generates id chunks from ids inside the collection of numbers
	 */
	public static Set<IdChunk> from(Collection<Integer> ids)
	{
		return from(Integer.MIN_VALUE, Integer.MAX_VALUE, ids);
	}
	
	/**
	 * generates id chunks from ids inside the collection of numbers
	 */
	public static Set<IdChunk> from(int min, int max, Collection<Integer> ids)
	{
		if(ids.isEmpty())
			return Collections.EMPTY_SET;
		if(!(ids instanceof TreeSet))
			ids = new TreeSet(ids);
		
		LinkedHashSet<IdChunk> chunks = new LinkedHashSet();
		TreeSet s = (TreeSet)ids;
		IdChunk last = null;
		for(Integer id : ids)
		{
			if(id < min)
				continue;
			else if(id > max)
				break;//skip out of bounds ids
			if(last != null && last.canAppend(id))
			{
				last.maxId = id;
				continue;
			}
			IdChunk chunk = new IdChunk(id, id);
			chunks.add(chunk);
			last = chunk;
		}
		return chunks;
	}
	
	public boolean canAppend(Integer id) 
	{
		return this.maxId + 1 == id || this.maxId == id;
	}

	@Override
	public boolean equals(Object other)
	{
		if(!(other instanceof IdChunk))
			return false;
		IdChunk c = (IdChunk)other;
		return this.minId == c.minId;
	}
	
	@Override
	public int hashCode()
	{
		return this.minId;
	}
	
	@Override
	public String toString()
	{ 
		return "id:(" + (this.minId == this.maxId ? this.minId : this.minId + " - " + this.maxId) + ")";
	}

}