package tkj.android.homecontrol.mythmote;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import tkj.android.homecontrol.mythmote.keymanager.KeyBindingEntry;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyMapBinder;


public class AbstractMythmoteFragment extends Fragment implements KeyMapBinder {
	
	protected KeyBindingManager mKeyManager;
	protected MythCom mythCom;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mythCom = MythCom.GetMythCom(this.getActivity());
		mKeyManager = new KeyBindingManager(this.getActivity(), this, mythCom);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mKeyManager.loadKeys();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mythCom = null;
		mKeyManager = null;
	}

	/**
	 * Enable the long click and normal click actions where a long click will
	 * configure the button, and a normal tap will perform the command
	 * 
	 * This is the callback from the {@link KeyBindingManager}
	 */
	public View bind(KeyBindingEntry entry) {
		int buttonId = entry.getMythKey().getButtonId();
		View v = this.getActivity().findViewById(buttonId);
		if (null == v)
			return null;
		v.setOnLongClickListener(mKeyManager);
		v.setOnClickListener(mKeyManager);
		
		if(buttonId == R.id.ButtonJump1 || buttonId == R.id.ButtonJump2 ||
			buttonId == R.id.ButtonJump3 || buttonId == R.id.ButtonJump4 ||
			buttonId == R.id.ButtonJump5 || buttonId == R.id.ButtonJump6){
			((Button)v).setText(entry.getFriendlyName());
		}
			
		return v;
	}

	
}
