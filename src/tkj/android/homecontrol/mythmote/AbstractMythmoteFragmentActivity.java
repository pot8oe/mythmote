package tkj.android.homecontrol.mythmote;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class AbstractMythmoteFragmentActivity extends FragmentActivity {

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setApplicationTheme();
		
	}
	
	private void setApplicationTheme() {
		
		int theme = this.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
				.getInt(MythMotePreferences.PREF_APP_THEME, 0);
		
		if(theme == 1){
			this.setTheme(R.style.Theme_custom_dark);
		} else if(theme == 2){
			
		} else {
			this.setTheme(R.style.Theme_custom_light);
		}
	}
}
