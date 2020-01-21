package jml.evilnotch.lib.simple;

import java.io.IOException;
import java.net.URI;

public class Directory extends java.io.File{

	public Directory(java.io.File parent, String child) 
	{
		super(parent, child);
	}
	
	public Directory(String parent, String child) 
	{
		super(parent, child);
	}
	
	public Directory(String str)
	{
		super(str);
	}
	
	public Directory(URI uri)
	{
		super(uri);
	}
	
	@Override
	public boolean mkdir()
	{
		if(this.exists())
		{
			return false;
		}
		return super.mkdir();
	}
	
	@Override
	public boolean mkdirs()
	{
		if(this.exists())
		{
			return false;
		}
		return this.getParentFile().exists() ? super.mkdir() : super.mkdirs();
	}
	
	public Directory create()
	{
		this.mkdirs();
		return this;
	}
	
	@Override
	public boolean isDirectory() throws SecurityException
	{
		if(!super.isDirectory())
			throw new SecurityException("file type must be a directory!");
		return true;
	}
	
	@Override
	public boolean createNewFile()
	{
		return this.mkdirs();
	}

}
