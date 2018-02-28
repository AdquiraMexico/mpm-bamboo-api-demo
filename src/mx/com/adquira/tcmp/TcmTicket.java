package mx.com.adquira.tcmp;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import mx.com.adquira.cv.dto.TicketRequestDto;
import mx.com.adquira.cv.helperobjects.Constants;
import mx.com.adquira.cv.helperobjects.TCMTicketResponse;
import mx.com.adquira.cv.helperobjects.TCMResponse;

public class TcmTicket extends Activity{
	
	public static final String CANCEL_REQUEST_DTO = "mx.com.adquira.cv.dto.TicketRequestDto";
	
	private EditText etCodigoAprobacion;
	private EditText etNroOrden;
	private TextView ticketResult;
	
	//private String user = "admin@iberoservice.com";
	//private String password = "7qk3Q3YH";
	//private String codigoAprobacion = "575418";
	//private String nroOrden = "885_988500010_2042";
	
	private String user = "mpm";
	private String password = "1234";
	private String codigoAprobacion = "Adq1609";
	private String nroOrden = "MK1";
	
	
	private Intent fromIntent;
	private IntentFilter filter = new IntentFilter("mx.com.adquira.cv.tcmpintents.TICKET_ACTION");
	private ResponseReceiver receiver = new ResponseReceiver();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		fromIntent = getIntent();
		setContentView(R.layout.activity_tcm_ticket);
		
		etCodigoAprobacion = (EditText)findViewById(R.id.editCodigoAprobacion);
		etNroOrden = (EditText)findViewById(R.id.editNroOrden);
		
		ticketResult = (TextView)findViewById(R.id.textPayResult);

		if(fromIntent.hasExtra("user"))
			user = fromIntent.getStringExtra("user");
		
		if(fromIntent.hasExtra("password"))
			password = fromIntent.getStringExtra("password");
		
		final Button btnTicket = (Button) findViewById(R.id.btnPay);
		
		btnTicket.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d("RVC", "Click!");
				//codigoAprobacion = etCodigoAprobacion.getText().toString();
				//nroOrden = etNroOrden.getText().toString();
				ticket(user, password, codigoAprobacion, nroOrden);
				
			}
		});
		
	}
	
	private void ticket(String user, String password, String codigoAprobacion, String nroOrden){
		Intent myIntent = new Intent();		
	    myIntent.setComponent(new ComponentName(TcmTicket.this,mx.com.adquira.cv.tcmpintents.TCMTicketService.class)); 
		myIntent.putExtra(Constants.TICKET_REQUEST_DTO, new TicketRequestDto(user, password, codigoAprobacion, nroOrden));
		TcmTicket.this.startService(myIntent);		
	}
	
	// Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver
	{
	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
	    public void onReceive(Context context, Intent intent) {    	
	    	// mProgress.setVisibility(8); //8=GONE - 4=INVISIBLE
	    	String message = "";
    		Integer code = 0;
		
	    	if(intent.hasExtra(Constants.TICKET_RESPONSE)){
	    		TCMTicketResponse resp = (TCMTicketResponse)intent.getSerializableExtra(Constants.TICKET_RESPONSE);
	    		message = resp.getStatus().getMessage();
	    		code = resp.getStatus().getCode();
	    		ticketResult.setText("Mensaje: " + message + ", c�digo: "+ code);
	    	} else {
	    		if(intent.hasExtra(Constants.ERROR_RESPONSE)) {
	    			TCMResponse errorResponse = (TCMResponse)intent.getSerializableExtra(Constants.ERROR_RESPONSE);
	    			ticketResult.setText("ERROR: "+errorResponse.getResponse());
	    		} else {
	    			ticketResult.setText(intent.getStringExtra("BT_STATUS_RESPONSE"));
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
		registerReceiver(receiver,new IntentFilter("mx.com.adquira.cv.tcmpintents.TICKET_ACTION"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}
}
