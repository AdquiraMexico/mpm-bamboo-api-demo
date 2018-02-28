package mx.com.adquira.tcmp;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import mx.com.adquira.cv.dto.CancelRequestDto;
import mx.com.adquira.cv.helperobjects.Constants;
import mx.com.adquira.cv.helperobjects.TCMCancelResponse;
import mx.com.adquira.cv.helperobjects.TCMResponse;

public class TcmCancel extends Activity{
	
	public static final String CANCEL_REQUEST_DTO = "mx.com.adquira.cv.dto.CancelRequestDto";
	
	private EditText txtTransactionId;
	private EditText txtAmount;
	private TextView cancelResult;
	private String token = "";
	private String transactionId = "";
	private Float amount;
	private Intent fromIntent;
	private IntentFilter filter = new IntentFilter("mx.com.adquira.cv.tcmpintents.CANCEL_ACTION");
	private ResponseReceiver receiver = new ResponseReceiver();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		fromIntent = getIntent();
		setContentView(R.layout.activity_tcm_cancel);
		txtTransactionId = (EditText)findViewById(R.id.editPayReference);
		txtAmount = (EditText)findViewById(R.id.editPayAmount);
		cancelResult = (TextView)findViewById(R.id.textPayResult);
		
		if(fromIntent.hasExtra("token"))
			token = fromIntent.getStringExtra("token");
		
		final Button btnCancelar = (Button) findViewById(R.id.btnPay);
		
		btnCancelar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				transactionId = txtTransactionId.getText().toString();
				amount = (txtAmount.getText().toString().equals(""))?0:Float.parseFloat(txtAmount.getText().toString());
				cancelar(Long.parseLong(transactionId),token,amount);
				
			}
		});
		
	}
	
	private void cancelar(Long transactionId, String token, Float amount){
		Intent myIntent = new Intent();		
	    myIntent.setComponent(new ComponentName(TcmCancel.this,mx.com.adquira.cv.tcmpintents.TCMCancelService.class)); 
		myIntent.putExtra(Constants.CANCEL_REQUEST_DTO, new CancelRequestDto(token,transactionId, amount));
		TcmCancel.this.startService(myIntent);		
	}
	
	// Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver
	{
	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
	    public void onReceive(Context context, Intent intent) {    	
	    	// mProgress.setVisibility(8); //8=GONE - 4=INVISIBLE
	    	String message = "";
    		Integer code = 0;
		
	    	if(intent.hasExtra(Constants.CANCEL_RESPONSE)){
	    		TCMCancelResponse resp = (TCMCancelResponse)intent.getSerializableExtra(Constants.CANCEL_RESPONSE);
	    		message = resp.getStatus().getMessage();
	    		code = resp.getStatus().getCode();
	    		cancelResult.setText("Mensaje: " + message + ", c�digo: "+ code);
	    	} else {
	    		if(intent.hasExtra(Constants.ERROR_RESPONSE)) {
	    			TCMResponse errorResponse = (TCMResponse)intent.getSerializableExtra(Constants.ERROR_RESPONSE);
	    			cancelResult.setText("ERROR: "+errorResponse.getResponse());
	    		} else {
	    			cancelResult.setText(intent.getStringExtra("BT_STATUS_RESPONSE"));
	    		}
	    	}
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tcm_pay, menu);
		return true;
	}

	
	@Override
	protected void onPause() {
		super.onPause(); 
		unregisterReceiver(receiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume(); 
		registerReceiver(receiver,new IntentFilter("mx.com.adquira.cv.tcmpintents.CANCEL_ACTION"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}
}
