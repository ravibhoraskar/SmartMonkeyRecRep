package com.smartmonkey.recrep.chimpevent;

import com.android.chimpchat.core.IChimpDevice;

public class NoOpEvent extends ChimpEvent{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8219271496204378528L;
	/**
	 * 
	 */

	public NoOpEvent(long wait) {
		super();
		this.wait=wait;
		this.setType("NoOp");
	}
	
	public boolean invokenow(IChimpDevice chimp)
	{
		return true;
	}
	public boolean invoke(IChimpDevice chimp)
	{
		super.invoke(chimp);
		return invokenow(chimp);
	}
}
