package com.android.uiautomator.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import com.android.chimpchat.core.IChimpDevice;
import com.android.uiautomator.ProcUtils.ProcRunner;
import com.android.uiautomator.UiAutomatorModel;
import com.android.uiautomator.UiAutomatorViewer;
import com.android.uiautomator.chimpevent.ChimpEvent;

public class FBLoginAction extends Action {

    public FBLoginAction() {
        super("&FBLogin", IAction.AS_PUSH_BUTTON);
    }
    
    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageHelper.loadImageDescriptorFromResource("images/facebook.png");
    }
    
    @Override
    public void run() {
    	Thread t = new Thread (){
    		@Override
    		public void run()
    		{
    			UiAutomatorModel.getModel().FBLogin();
    		}
    	};
        t.run();
    }
}
