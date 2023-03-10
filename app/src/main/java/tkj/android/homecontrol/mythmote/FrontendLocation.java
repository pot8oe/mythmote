/*
 * Copyright (C) 2010 Thomas G. Kenny Jr
 *
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */


package tkj.android.homecontrol.mythmote;

/*
 * Contains all information for a mythtv frontend.
 * */
public class FrontendLocation {

	public static final int DEFAULT_ID = -1;
	public static final String DEFAULT_NAME = "";
	public static final String DEFAULT_ADDRESS = "";
	public static final int DEFAULT_PORT = 6456;
	public static final String DEFAULT_MAC = "";
	public static final int DEFAULT_WIFI_ONLY = 1;

	public static String STR_ID = "ID";
	public static String STR_NAME = "NAME";
	public static String STR_ADDRESS = "ADDRESS";
	public static String STR_PORT = "PORT";
	public static String STR_MAC = "MAC";
	public static String STR_WIFIONLY = "WIFIONLY";
	
	
	public int ID = DEFAULT_ID;
	public String Name = DEFAULT_NAME;
	public String Address = DEFAULT_ADDRESS;
	public int Port = DEFAULT_PORT;
	public String MAC = DEFAULT_MAC;
	public int WifiOnly = DEFAULT_WIFI_ONLY;
	
	
	
	public FrontendLocation()
	{
		
	}
}
