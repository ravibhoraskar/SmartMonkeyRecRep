package com.smartmonkey.recrep.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import com.android.chimpchat.core.IChimpDevice;
import com.smartmonkey.recrep.SMonkeyModel;
import com.smartmonkey.recrep.SMonkeyViewer;
import com.smartmonkey.recrep.ProcUtils.ProcRunner;
import com.smartmonkey.recrep.chimpevent.ChimpEvent;

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
    			SMonkeyModel.getModel().FBLogin();
    		}
    	};
        t.run();
    }
}
