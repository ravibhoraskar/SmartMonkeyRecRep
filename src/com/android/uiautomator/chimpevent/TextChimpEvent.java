package com.android.uiautomator.chimpevent;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import com.android.uiautomator.UiAutomatorModel;
import com.android.uiautomator.UiAutomatorViewer;

public class TextChimpEvent extends ChimpEvent {

	private String text;

	public TextChimpEvent(long wait, String text) {
		super();
		this.wait=wait;
		this.text = text;
		this.setType("Text");
		this.setMisc(text);
		
	}
	public boolean invoke(IChimpDevice chimp)
	{
		super.invoke(chimp);
		return invokenow(chimp);
		
	}
	public boolean invokenow(IChimpDevice chimp)
	{
		chimp.type(text);
		return true;
	}
	
}
