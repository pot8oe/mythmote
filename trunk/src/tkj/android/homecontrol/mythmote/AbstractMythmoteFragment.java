package tkj.android.homecontrol.mythmote;

import android.os.Bundle;
import android.support.v4.app.Fragment;


public class AbstractMythmoteFragment extends Fragment {
	
	protected MythCom mythCom;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mythCom = MythCom.GetMythCom();
		
		super.onCreate(savedInstanceState);
	}

	
}
