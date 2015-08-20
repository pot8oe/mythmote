package tkj.android.homecontrol.mythmote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MythmoteQuickJumpFragment extends AbstractMythmoteFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_mythmote_quickjump, container, false);
		
		return view;
	}
}
