package com.smartmonkey.recrep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ProcUtils {
	

	/*
	 * Convenience function to construct an 'adb' command, e.g. use 'adb' or
	 * 'adb -s NNN'
	 */
	public static ProcRunner getAdbRunner(String serial, String... command) {
		List<String> cmd = new ArrayList<String>();
		cmd.add("adb");
		if (serial != null) {
			cmd.add("-s");
			cmd.add(serial);
		}
		for (String s : command) {
			cmd.add(s);
		}
		return new ProcRunner(cmd);
	}

	/**
	 * Convenience class to run external process.
	 * 
	 * Always redirects stderr into stdout, has timeout control
	 * 
	 */
	public static class ProcRunner {

		ProcessBuilder mProcessBuilder;

		List<String> mOutput = new ArrayList<String>();

		public ProcRunner(List<String> command) {
			mProcessBuilder = new ProcessBuilder(command)
					.redirectErrorStream(true);
		}

		public int run(long timeout) throws IOException {
			final Process p = mProcessBuilder.start();
			Thread t = new Thread() {
				@Override
				public void run() {
					String line;
					mOutput.clear();
					try {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(p.getInputStream()));
						while ((line = br.readLine()) != null) {
							mOutput.add(line);
						}
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				};
			};
			t.start();
			try {
				t.join(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (t.isAlive()) {
				throw new IOException("external process not terminating.");
			}
			try {
				return p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new IOException(e);
			}
		}

		public String getOutputBlob() {
			StringBuilder sb = new StringBuilder();
			for (String line : mOutput) {
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
			return sb.toString();
		}
	}

}
