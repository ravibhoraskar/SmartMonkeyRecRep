/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartmonkey.recrep.actions;

import com.android.chimpchat.ChimpChat;

import com.android.chimpchat.core.IChimpDevice;
import com.smartmonkey.recrep.ProcUtils;
import com.smartmonkey.recrep.SMonkeyModel;
import com.smartmonkey.recrep.SMonkeyViewer;
import com.smartmonkey.recrep.ProcUtils.ProcRunner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotAction extends Action {

	SMonkeyViewer mViewer;

	public ScreenshotAction(SMonkeyViewer viewer) {
		super("&Device Screenshot");
		mViewer = viewer;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageHelper
				.loadImageDescriptorFromResource("images/screenshot.png");
	}

	@Override
	public void run() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(
				mViewer.getShell());
		try {
			dialog.run(true, false, new IRunnableWithProgress() {
				private void showError(final String msg, final Throwable t,
						IProgressMonitor monitor) {
					monitor.done();
					mViewer.getShell().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							Status s = new Status(IStatus.ERROR, "Screenshot",
									msg, t);
							ErrorDialog.openError(mViewer.getShell(), "Error",
									"Cannot take screenshot", s);
						}
					});
				}

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					ProcRunner procRunner = null;
					String serial = System.getenv("ANDROID_SERIAL");
					File tmpDir = null;
					File xmlDumpFile = null;
					File screenshotFile = null;
					int retCode = -1;
					try {
						tmpDir = File.createTempFile("uiautomatorviewer_", "");
						tmpDir.delete();
						if (!tmpDir.mkdirs())
							throw new IOException("Failed to mkdir");
						xmlDumpFile = File.createTempFile("dump_", ".xml",
								tmpDir);
						screenshotFile = File.createTempFile("screenshot_",
								".png", tmpDir);
					} catch (IOException e) {
						e.printStackTrace();
						showError("Cannot get temp directory", e, monitor);
						return;
					}

					// boiler plates to do a bunch of adb stuff to take XML
					// snapshot and screenshot
					monitor.beginTask("Getting UI status dump from device...",
							IProgressMonitor.UNKNOWN);
					monitor.subTask("Detecting device...");
					procRunner = ProcUtils.getAdbRunner(serial, "shell", "ls",
							"/system/bin/uiautomator");
					try {
						retCode = procRunner.run(30000);
					} catch (IOException e) {
						e.printStackTrace();
						showError("Failed to detect device", e, monitor);
						return;
					}
					if (retCode != 0) {
						showError("No device or multiple devices connected. "
								+ "Use ANDROID_SERIAL environment variable "
								+ "if you have multiple devices", null, monitor);
						return;
					}
					if (procRunner.getOutputBlob().indexOf(
							"No such file or directory") != -1) {
						showError(
								"/system/bin/uiautomator not found on device",
								null, monitor);
						return;
					}
					monitor.subTask("Deleting old UI XML snapshot ...");
					procRunner = ProcUtils.getAdbRunner(serial, "shell", "rm",
							"/sdcard/uidump.xml");
					try {
						retCode = procRunner.run(30000);
						if (retCode != 0) {
							throw new IOException(
									"Non-zero return code from \"rm\" xml dump command:\n"
											+ procRunner.getOutputBlob());
						}
					} catch (IOException e) {
						e.printStackTrace();
						showError("Failed to execute \"rm\" xml dump command.",
								e, monitor);
						return;
					}

					monitor.subTask("Taking UI XML snapshot...");
					procRunner = ProcUtils.getAdbRunner(serial, "shell",
							"/system/bin/uiautomator", "dump",
							"/sdcard/uidump.xml");
					try {
						retCode = procRunner.run(30000);
						if (retCode != 0) {
							throw new IOException(
									"Non-zero return code from dump command:\n"
											+ procRunner.getOutputBlob());
						}
					} catch (IOException e) {
						e.printStackTrace();
						showError("Failed to execute dump command.", e, monitor);
						return;
					}
					procRunner = ProcUtils.getAdbRunner(serial, "pull",
							"/sdcard/uidump.xml", xmlDumpFile.getAbsolutePath());
					try {
						retCode = procRunner.run(30000);
						if (retCode != 0) {
							throw new IOException(
									"Non-zero return code from pull command:\n"
											+ procRunner.getOutputBlob());
						}
					} catch (IOException e) {
						e.printStackTrace();
						showError("Failed to pull dump file.", e, monitor);
						return;
					}

					monitor.subTask("Deleting old device screenshot...");
					procRunner = ProcUtils.getAdbRunner(serial, "shell", "rm",
							"/sdcard/screenshot.png");
					try {
						retCode = procRunner.run(30000);
						if (retCode != 0) {
							throw new IOException(
									"Non-zero return code from \"rm\" screenshot command:\n"
											+ procRunner.getOutputBlob());
						}
					} catch (IOException e) {
						e.printStackTrace();
						showError(
								"Failed to execute \"rm\" screenshot command.",
								e, monitor);
						return;
					}

					monitor.subTask("Taking device screenshot...");
					procRunner = ProcUtils.getAdbRunner(serial, "shell", "screencap",
							"-p", "/sdcard/screenshot.png");
					try {
						retCode = procRunner.run(30000);
						if (retCode != 0) {
							throw new IOException(
									"Non-zero return code from screenshot command:\n"
											+ procRunner.getOutputBlob());
						}
					} catch (IOException e) {
						e.printStackTrace();
						showError("Failed to execute screenshot command.", e,
								monitor);
						return;
					}
					procRunner = ProcUtils.getAdbRunner(serial, "pull",
							"/sdcard/screenshot.png",
							screenshotFile.getAbsolutePath());
					try {
						retCode = procRunner.run(30000);
						if (retCode != 0) {
							throw new IOException(
									"Non-zero return code from pull command:\n"
											+ procRunner.getOutputBlob());
						}
					} catch (IOException e) {
						e.printStackTrace();
						showError("Failed to pull dump file.", e, monitor);
						return;
					}
					
					monitor.subTask("Getting Chimp...");
					IChimpDevice device= SMonkeyModel.getModel().getChimp();
					if(device==null)
					{
						// get the Chimpchat device
						try {
							long TIMEOUT = 5000; 
							ChimpChat mChimpChat = ChimpChat.getInstance();
							 device = mChimpChat.waitForConnection(TIMEOUT, ".*");
							 if (device==null)
							 {
								 throw new RuntimeException("Chimp Timeout");
							 }
							 device.wake();
							
						} catch (Exception e) {
							e.printStackTrace();
							showError("Failed to get MonkeyDevice", e, monitor);
						}

					}
					else
					{
						monitor.subTask("Already have chimp");
					}
					
					final File png = screenshotFile, xml = xmlDumpFile;
					final IChimpDevice finaldevice = device;
					if (png.length() == 0) {
						showError("Screenshot file size is 0", null, monitor);
						return;
					} else {
						mViewer.getShell().getDisplay()
								.syncExec(new Runnable() {
									@Override
									public void run() {
										SMonkeyModel.getModel()
												.loadScreenshotAndXmlDump(png,
														xml);
										SMonkeyModel.getModel().setChimp(finaldevice);
									}
								});
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
