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
		
		this.setTheme(AbstractMythmoteFragmentActivity.getThemeStyle(theme));
	}
	
	public static int getThemeStyle(int preferenceValue){
		
		if(preferenceValue == 1){
			return R.style.Theme_custom_dark;
		} else if(preferenceValue == 2){
			return R.style.Theme_custom_dark_glow;
		} else {
			return R.style.Theme_custom_light;
		}
		
	}
}
