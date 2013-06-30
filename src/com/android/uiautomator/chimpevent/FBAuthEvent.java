package com.android.uiautomator.chimpevent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.android.uiautomator.ProcUtils.ProcRunner;

public class FBAuthEvent extends ChimpEvent {

	/**
	 * 
	 */

	public FBAuthEvent(long wait) {
		super();
		this.wait = wait;
		this.setType("FBLogin");
	}

	public boolean invoke(IChimpDevice chimp) {
		super.invoke(chimp);
		return invokenow(chimp);

	}

	public boolean invokenow(IChimpDevice chimp) {
		IChimpImage screenshot = chimp.takeSnapshot();
		screenshot.writeToFile("/tmp/device.png", "png");
		List<String> cmd = new ArrayList<String>();
		cmd.add("getFBCoords2");
		cmd.add("/tmp/device.png");
		ProcRunner procrunner = new ProcRunner(cmd);
		try {
			procrunner.run(2000);
		} catch (IOException e) {
			System.err.println("Couldn't process screenshot");
			e.printStackTrace();
			return false;
		}
		String output = procrunner.getOutputBlob();
		output.replace('\n', ' ');
		String[] temp = output.split(" ");
		Double xx = 0., yy = 0.;
		try {
			xx = Double.parseDouble(temp[0].replaceAll("[^0-9.]+", ""));
			yy = Double.parseDouble(temp[1].replaceAll("[^0-9.]+", ""));
		} catch (NumberFormatException e) {
			System.err.println("FB Dialog not found");
			return false;
		}
		int x = xx.intValue();
		int y = yy.intValue();

		System.out.println(x + " " + y);
		if (x == 0 && y == 0) {
			System.err.println("FB Dialog not found");
			return false;
		}

		chimp.touch(x, y, TouchPressType.DOWN);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		chimp.touch(x, y, TouchPressType.UP);

		return true;

	}

}
