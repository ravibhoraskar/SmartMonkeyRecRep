package com.android.uiautomator.chimpevent;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.TouchPressType;

public class TouchChimpEvent extends ChimpEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3260772755946861106L;
	int x, y;

	public TouchChimpEvent(long wait, int x, int y) {
		super();
		this.x = x;
		this.y = y;
		this.wait = wait;
		this.setType("Touch");
		this.setMisc(x + " " + y);
	}

	public boolean invoke(IChimpDevice chimp)
	{
		super.invoke(chimp);
		return invokenow(chimp);
		
	}
	
	public boolean invokenow(IChimpDevice chimp) {
		chimp.touch(x, y, TouchPressType.DOWN_AND_UP);
		return true;
	}
}
