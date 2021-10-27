package tkj.android.homecontrol.mythmote;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AbstractMythmoteFragmentActivity extends AppCompatActivity {
	
	private static int sCurrentTheme = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setApplicationTheme();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		int theme = this.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
				.getInt(MythMotePreferences.PREF_APP_THEME, 0);
		
		if(sCurrentTheme != -1 && theme != sCurrentTheme){
			Intent i = new Intent("android.intent.action.MAIN");
			i.setComponent(ComponentName.unflattenFromString("tkj.android.homecontrol.mythmote/tkj.android.homecontrol.mythmote.MythMote"));
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
	}
	
	
	
	
	private void setApplicationTheme() {
		sCurrentTheme = this.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
				.getInt(MythMotePreferences.PREF_APP_THEME, 0);
		
		this.setTheme(AbstractMythmoteFragmentActivity.getThemeStyle(sCurrentTheme));
	}
	
	public static int getThemeStyle(int preferenceValue){

		switch(preferenceValue)
		{
			default:
			case 0:
				return R.style.Theme_custom_light;
			case 1:
				return R.style.Theme_custom_dark;
			case 2:
				return R.style.Theme_custom_dark_glow;
			case 3:
				return R.style.Theme_custom_material_light;
			case 4:
				return R.style.Theme_custom_material_dark;
		}
		
	}
}
