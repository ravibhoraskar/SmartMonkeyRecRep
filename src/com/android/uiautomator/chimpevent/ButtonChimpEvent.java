package com.android.uiautomator.chimpevent;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;

public class ButtonChimpEvent extends ChimpEvent{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8326882482878019614L;
	private PhysicalButton button;

	public ButtonChimpEvent(long wait, PhysicalButton button) {
		super();
		this.button=button;
		this.wait=wait;
		this.setType("Button");
		this.setMisc(button.toString());
	}
	
	public boolean invoke(IChimpDevice chimp)
	{
		super.invoke(chimp);
		return invokenow(chimp);
		
	}
	public boolean invokenow(IChimpDevice chimp)
	{
		chimp.press(button, TouchPressType.DOWN_AND_UP);
		return true;
	}

}
