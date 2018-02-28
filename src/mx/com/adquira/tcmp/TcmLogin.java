package mx.com.adquira.tcmp;

import mx.com.adquira.cv.helperobjects.Constants;
import mx.com.adquira.cv.helperobjects.TCMLoginResponse;
import mx.com.adquira.cv.dto.LoginRequestDto;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TcmLogin extends Activity {
	
	public static final int TCM_LOGIN_ACTIVITY = 1;
	public static final String LOGIN_REQUEST_DTO = "mx.com.adquira.cv.dto.LoginRequestDto";
	public static final String LOGIN_RESPONSE = "mx.com.adquira.cv.helperobjects.TCMLoginResponse";
	
	Button btnLogin;
	EditText txtUser;
	EditText txtPasswd;
	TextView txtMessage;
	private String username = "";
	private ProgressBar mProgress;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tcm_login);
		
		btnLogin = (Button) findViewById(R.id.login_BtnLogin);
		txtUser = (EditText) findViewById(R.id.login_Username);
		txtPasswd = (EditText) findViewById(R.id.login_Password);
		txtMessage = (TextView) findViewById(R.id.loginMessage);
		mProgress = (ProgressBar) findViewById(R.id.loginProgressBar);
		
		txtMessage.setText("");
		mProgress.setVisibility(4);
		registerReceiver(receiver,filter);
		
		btnLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mProgress.setVisibility(0); // Visible
				username = ""+txtUser.getText().toString();
				String password = ""+txtPasswd.getText().toString();
				Intent myIntent = new Intent();		
				myIntent.setComponent(new ComponentName(TcmLogin.this, mx.com.adquira.cv.tcmpintents.TCMLoginService.class));    
				myIntent.addCategory("android.intent.category.LAUNCHER");		
				myIntent.putExtra(Constants.LOGIN_REQUEST_DTO, new LoginRequestDto(username, password));
				TcmLogin.this.startService(myIntent);
			}
		});

	}
	
	private IntentFilter filter = new IntentFilter("mx.com.adquira.cv.tcmpintents.LOGIN_ACTION");
	private ResponseReceiver receiver = new ResponseReceiver();
	
	// Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver
	{
	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
	    public void onReceive(Context context, Intent intent) {
	    	mProgress.setVisibility(8); //8=GONE - 4=INVISIBLE
	    	TCMLoginResponse resp = (TCMLoginResponse)intent.getSerializableExtra(Constants.LOGIN_RESPONSE);
			String token = resp.getToken();
			if(token!=null){
				Intent myIntent = new Intent(TcmLogin.this, TcmPay.class);
				myIntent.putExtra("token", token);
				myIntent.putExtra("user", username);
				TcmLogin.this.startActivity(myIntent); 
			} else {
				txtMessage.setText("USUARIO/PWD INCORRECTOS");
			}
	    }
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tcm_login, menu);
		return true;
	}

	@Override
	protected void onDestroy()
	{
	    unregisterReceiver(receiver);
	    super.onDestroy();
	}
}
