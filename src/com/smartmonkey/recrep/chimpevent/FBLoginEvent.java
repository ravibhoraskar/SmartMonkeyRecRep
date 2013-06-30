package com.smartmonkey.recrep.chimpevent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.smartmonkey.recrep.ProcUtils.ProcRunner;

public class FBLoginEvent extends ChimpEvent {

	/**
	 * 
	 */

	public FBLoginEvent(long wait) {
		super();
		this.wait = wait;
		this.setType("FBLogin");
	}

	public boolean invoke(IChimpDevice chimp) {
		super.invoke(chimp);
		return invokenow(chimp);

	}

	public boolean invokenow(IChimpDevice chimp) {
		int counter = 3;
		int x = 0, y = 0;
		while (true) {
			IChimpImage screenshot = chimp.takeSnapshot();
			screenshot.writeToFile("/tmp/device.png", "png");
			List<String> cmd = new ArrayList<String>();
			cmd.add("./res/getFBCoords");
			cmd.add("/tmp/device.png");
			ProcRunner procrunner = new ProcRunner(cmd);
			try {
				procrunner.run(5000);
			} catch (IOException e) {
				System.err.println("Couldn't process screenshot");
				e.printStackTrace();
				return false;
			}
			String output = procrunner.getOutputBlob();
			output.replace('\n', ' ');
			String[] temp = output.split(" ");

			Double xx = Double.parseDouble(temp[0].replaceAll("[^0-9.]+", ""));
			Double yy = Double.parseDouble(temp[1].replaceAll("[^0-9.]+", ""));

			x = xx.intValue();
			y = yy.intValue();

			System.out.println(x + " " + y);
			if (x == 0 && y == 0) {
				System.err.println("FB Dialog not found");
				counter--;
				if (counter >= 0) {
					System.out.println("Trying again " + counter);
				} else {
					return false;
				}
			} else {
				break;
			}
		}

		chimp.touch(x, y, TouchPressType.DOWN);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		chimp.touch(x, y, TouchPressType.UP);
		chimp.type("iamdcoolest");
		chimp.press("KEYCODE_TAB", TouchPressType.DOWN_AND_UP);
		chimp.type("ravi20990");
		chimp.press(PhysicalButton.ENTER, TouchPressType.DOWN_AND_UP);
		return true;

	}

}
