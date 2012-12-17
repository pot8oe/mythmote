package tkj.android.homecontrol.mythmote;

import tkj.android.homecontrol.mythmote.keymanager.KeyBindingEntry;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyMapBinder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;


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
		View v = this.getActivity().findViewById(entry.getMythKey().getButtonId());
		if (null == v)
			return null;
		v.setOnLongClickListener(mKeyManager);
		v.setOnClickListener(mKeyManager);
		return v;
	}

	
}
