package com.android.uiautomator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.uiautomator.ProcUtils.ProcRunner;
import com.android.uiautomator.actions.FBLoginAction;
import com.android.uiautomator.chimpevent.ChimpEvent;
import com.android.uiautomator.chimpevent.FBAuthEvent;
import com.android.uiautomator.chimpevent.FBLoginEvent;

public class ReplayTrace {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		if(args.length<1)
		{
			System.err.println("Expects argument: Trace File");
			return;
		}
		IChimpDevice chimp = null;
		try {
			long TIMEOUT = 5000; 
			ChimpChat mChimpChat = ChimpChat.getInstance();
			 chimp = mChimpChat.waitForConnection(TIMEOUT, ".*");
			 if (chimp==null)
			 {
				 throw new RuntimeException("Chimp Timeout");
			 }
			 chimp.wake();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to get MonkeyDevice");
		}

		System.out.println("Opening trace file "+args[0]);
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[0]));
		List<ChimpEvent> x =  (List<ChimpEvent>) in.readObject();
		in.close();
		
		for (ChimpEvent e : x)
		{
			e.invoke(chimp);
		}
		(new FBLoginEvent(3000)).invoke(chimp);
		
		FBAuthEvent e = new FBAuthEvent(3000);
		int counter=2;
		while(e.invoke(chimp)) // keep pressing the okay 
		{
			System.out.println("Counter: "+ counter);
			if (counter-- <= 0 )
			{
				break;
			}
		}
		
		
		IChimpImage screenshot = chimp.takeSnapshot();
		screenshot.writeToFile(args[0]+".png", "png");
		
		String serial = System.getenv("ANDROID_SERIAL");
		ProcRunner procRunner = ProcUtils.getAdbRunner(serial, "shell",
				"/system/bin/uiautomator", "dump",
				"/sdcard/uidump.xml");
		int retCode;
		try {
			retCode = procRunner.run(30000);
			if (retCode != 0) {
				throw new IOException(
						"Non-zero return code from dump command:\n"
								+ procRunner.getOutputBlob());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Failed to execute dump command.");
			return;
		}
		procRunner = ProcUtils.getAdbRunner(serial, "pull",
				"/sdcard/uidump.xml", args[0]+".xml");
		try {
			retCode = procRunner.run(30000);
			if (retCode != 0) {
				throw new IOException(
						"Non-zero return code from pull command:\n"
								+ procRunner.getOutputBlob());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Failed to pull dump file.");
			return;
		}



		
		System.out.println("Done");
		chimp.dispose();
		System.out.println("Exiting");
		System.exit(0);

	}

}
