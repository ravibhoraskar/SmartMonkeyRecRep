package com.smartmonkey.recrep.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import com.android.chimpchat.core.IChimpDevice;
import com.smartmonkey.recrep.SMonkeyModel;
import com.smartmonkey.recrep.chimpevent.ChimpEvent;

public class ClearRecordingAction extends Action {

	public ClearRecordingAction() {
		super("&Clear", IAction.AS_PUSH_BUTTON);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageHelper.loadImageDescriptorFromResource("images/delete.png");
	}

	@Override
	public void run() {
		SMonkeyModel.getModel().clearLog();
		System.out.println("printing");
	}

}
