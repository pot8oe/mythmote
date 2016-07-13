package tkj.android.homecontrol.mythmote;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AbstractMythmotePreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//this.setApplicationTheme();
	}
	
	private void setApplicationTheme() {

		/* *
		 * Locking to Dark theme as light theme is broken.
		 * Uncomment the following to re-enable theme selection
		 * */
//		int theme = this.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
//				.getInt(MythMotePreferences.PREF_APP_THEME, 0);

		int theme = 1;

		this.setTheme(AbstractMythmoteFragmentActivity.getThemeStyle(theme));
	}
	
}
