package tkj.android.homecontrol.mythmote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MythmoteNavigationFragment extends AbstractMythmoteFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_mythmote_navigation, container, false);
		
		return view;
	}

}
