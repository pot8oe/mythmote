package tkj.android.homecontrol.mythmote;

import tkj.android.homecontrol.mythmote.R.id;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class MythmoteKeyboardInputFragment extends AbstractMythmoteDialogFragment implements TextWatcher, OnKeyListener, OnClickListener {
	
	//keeps track if this fragment is being displayed as a dialog or not
	boolean mIsDialog = false;
	String mStrInput = "";
	Button mButtonDelete = null;
	Button mButtonEnter = null;
	Button mButtonClear = null;
	EditText mEditTextInput = null;
	
	/**
	 * Called when the clear button is pressed
	 */
	private View.OnClickListener mClearButtonOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(null != mEditTextInput) {
				mStrInput = null;
				mEditTextInput.setText("");
			}
		}
	};
	
	/**
	 * Called when the enter button is clicked
	 */
	private View.OnClickListener mEnterButtonOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(null == mythCom || !mythCom.IsConnected()) return;
			mythCom.SendKey("enter");
		}
	};
	
	
	/**
	 * Called when the delete button is pressed
	 */
	private View.OnClickListener mDeleteButtonOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(null == mythCom || !mythCom.IsConnected()) return;
			mythCom.SendKey("backspace");
		}
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mIsDialog = true;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setMessage(R.string.keyboard_input_dialog_message);
		builder.setView(setupView(getActivity().getLayoutInflater().inflate(R.layout.fragment_mythmote_keyboardinput, null)));
		
		return builder.create();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//if this is a dialog the layout has already been inflated
		if(mIsDialog) return super.onCreateView(inflater, container, savedInstanceState);
		
		return this.setupView(inflater.inflate(R.layout.fragment_mythmote_keyboardinput, container, false));
	}

	@Override
	public void onResume() {
		
		//if this is a dialog do not play with the keyboard
		if(mIsDialog) {
			super.onResume();
			return;
		}
		
		//try to force the keyboard visible
		EditText input = (EditText) this.getView().findViewById(R.id.EditTextKeyboardInput);
		if(null != input){
			//input.setOnKeyListener(this);
			//input.addTextChangedListener(this);
			
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(input,InputMethodManager.SHOW_FORCED);
		}
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		
		//if this is a dialog do not play with the keyboard
		if(mIsDialog) {
			super.onPause();
			return;
		}
		
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
		

		super.onPause();
	}
	
	
	private void sendKey(char c){
		
		Log.d(MythMote.LOG_TAG, "SendKey("+c+")");
		
		if(null == this.mythCom || !this.mythCom.IsConnected()) return;
		
		// check if it's whitespace
		if (Character.isWhitespace(c)) {
			if (c == '\t')// tab
			{
				this.mythCom.SendKey("tab");
			} else if (c == ' ')// space
			{
				this.mythCom.SendKey("space");
			} else if (c == '\r')// enter/return
			{
				this.mythCom.SendKey("enter");
			}
		} else// not white space. Just send as is
		{
			this.mythCom.SendKey(c);
		}
	}
	
	private void sendString(CharSequence str){
		if(null == str) return;
		
		for(int i=0; i<str.length(); i++){
			sendKey(str.charAt(i));
		}
	}
	
	private View setupView(View view){
		
		mButtonClear = (Button)view.findViewById(R.id.ButtonClear);
		if(null != mButtonClear){
			mButtonClear.setOnClickListener(mClearButtonOnClick);
		}
		
		mButtonEnter = (Button)view.findViewById(R.id.ButtonEnter);
		if(null != mButtonEnter){
			mButtonEnter.setOnClickListener(mEnterButtonOnClick);
		}
		
		mButtonDelete = (Button)view.findViewById(R.id.ButtonKeyboardInputDelete);
		if(null != mButtonDelete){
			mButtonDelete.setOnClickListener(mDeleteButtonOnClick);
		}
		
		mEditTextInput = (EditText)view.findViewById(R.id.EditTextKeyboardInput);
		if(null != mEditTextInput){
			mEditTextInput.setOnKeyListener(this);
			mEditTextInput.addTextChangedListener(this);
		}
		
		return view;
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
		if(null != mStrInput){
			/* Handle backspace or additional characters */
			if(s.length() < mStrInput.length() || s.length() <= 0 ){
				if(this.mythCom != null && this.mythCom.IsConnected()) this.mythCom.SendKey("backspace");
			}else {
				if(s.toString().startsWith(mStrInput)){
					sendString(s.toString().substring(mStrInput.length()));
				}
			}
		}
		
		mStrInput = s.toString();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		/* we handle delete key events if we catch them and the edittext is empty.
		 * If edittext is empty the onTextChanged event will not be run and cannot
		 * send the backspace command. */
		if(event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP){
			if(this.mythCom != null && this.mythCom.IsConnected()){
			
				EditText editText = (EditText)v.findViewById(id.EditTextKeyboardInput);
				
				if(editText != null && editText.length() <= 0){
					this.mythCom.SendKey("backspace");
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		//don't have to do anything
	}
	
}
