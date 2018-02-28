package mx.com.adquira.tcmp;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import mx.com.adquira.blueadquira.global.Global;
import mx.com.adquira.cv.dto.PaymentRequestDto;
import mx.com.adquira.cv.helperobjects.Constants;
import mx.com.adquira.cv.helperobjects.TCMPaymentEMVResponse;
import mx.com.adquira.cv.helperobjects.TCMResponse;

public class TcmPay extends Activity implements AdapterView.OnItemSelectedListener {
	
	public static final String PAYMENT_REQUEST_DTO = "mx.com.adquira.cv.dto.PaymentRequestDto";
	
	private EditText txtOrderId;
	private EditText txtConcepto;
	private EditText txtMonto;
	private EditText editTextPeriodo;
	private EditText editTextServicio;
	private Spinner payCategory;
	private TextView payResult;
	private CheckBox checkEMV;
	private Intent myIntentbt;
	
	private String token = "";
	private Intent fromIntent;
	
	private int[] payCategories = {773,120};
	
	private String monto;
	private String concepto;
	private String orderId;
	private String periodo;
	private String servicio;
	private int payCat;
	private int currency;
	private boolean forceReconnect = false;
	
	private IntentFilter filter = new IntentFilter("mx.com.adquira.cv.tcmpintents.EMV_PAYMENT_ACTION");
	private ResponseReceiver receiver = new ResponseReceiver();
	private BlueVerify BlueVerifyReceiver = new BlueVerify();
	private IntentFilter BlueVerifyFilter = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		conexionBamboo();
		super.onCreate(savedInstanceState);
			
		fromIntent = getIntent();
		setContentView(R.layout.activity_tcm_pay);
		txtOrderId = (EditText)findViewById(R.id.editPayReference);
		txtConcepto = (EditText)findViewById(R.id.editPayConcept);
		txtMonto = (EditText)findViewById(R.id.editPayAmount);
		editTextPeriodo = (EditText)findViewById(R.id.editTextPeriodo);
		editTextServicio = (EditText)findViewById(R.id.editTextServicio);
		//payCategory = (Spinner) findViewById(R.id.payCategory);
		checkEMV = (CheckBox) findViewById(R.id.checkEMV);
		payResult = (TextView)findViewById(R.id.textPayResult);
		
		txtOrderId.setText("PB"+Math.rint(Math.random()*10000));
		txtConcepto.setText("TEST");
		txtMonto.setText("0.00");
		checkEMV.setChecked(true);
		
		if(fromIntent.hasExtra("user"))
			payResult.setText("Bienvenido "+fromIntent.getStringExtra("user"));
		
		if(fromIntent.hasExtra("token"))
			token = fromIntent.getStringExtra("token");
		
		/*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.paymentCategory, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		payCategory.setAdapter(adapter);
		payCategory.setOnItemSelectedListener(this);*/
		
		final Button btnPagar = (Button) findViewById(R.id.btnPay);
		
		//registerReceiver(receiver,filter);
		//registerReceiver(BlueVerifyReceiver,BlueVerifyFilter);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,new IntentFilter("TCMDialogAction-Canceled"));
		
		
		forceReconnect = true;
		
		
		
		btnPagar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				monto = txtMonto.getText().toString();
				concepto = txtConcepto.getText().toString();
				orderId = txtOrderId.getText().toString();
				servicio = editTextServicio.getText().toString();
				periodo = editTextPeriodo.getText().toString();
				currency=0; // MXP=0  USD=1
				//payCat=141;
				
				payResult.setText("Vas a pagar "+monto+", ref. "+orderId+
						", un "+concepto+" de cat."+payCat);
				
				//pago(Float.parseFloat(monto),orderId, concepto,""+payCat, ""+currency);
				pago(Float.parseFloat(monto),orderId, concepto, servicio, ""+currency, periodo);
			}
		});
		
	}
	
	
	private void print(){
		
		
		
	}
	
	private void pago(float monto, String orderId, String concepto, String category, String currency, String period){
		Intent myIntent = new Intent();		
		if(checkEMV.isChecked()) {
			myIntent.setComponent(new ComponentName(TcmPay.this,mx.com.adquira.cv.tcmpintents.TCMEMVPaymentService.class)); 
		} else {
			myIntent.setComponent(new ComponentName(TcmPay.this,mx.com.adquira.cv.tcmpintents.TCMSwipePaymentService.class)); 
		}
		
		myIntent.putExtra(Constants.PAYMENT_REQUEST_DTO, new PaymentRequestDto(token, orderId, concepto, monto, category,currency,period));
		myIntent.putExtra("FORCE_RECONNECT",forceReconnect);
		forceReconnect = false;
		TcmPay.this.startService(myIntent);		
	}
	
	// Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver
	{
	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
	    public void onReceive(Context context, Intent intent) {
	    	Log.d("TCMDialogActivity","en API: Cancel REcibido");
	    	
	    	// mProgress.setVisibility(8); //8=GONE - 4=INVISIBLE
	    	String approval = "";
    		String trxID = "";
		
	    	if(intent.hasExtra(Constants.EMV_RESPONSE)){
	    		TCMPaymentEMVResponse resp = (TCMPaymentEMVResponse)intent.getSerializableExtra(Constants.EMV_RESPONSE);
	    		approval = resp.getAuthCode();
	    		trxID = resp.getTransactionId();
	    		payResult.setText("Aprobaci�n: "+approval+", ref. "+trxID);
	    		Toast.makeText(getApplicationContext(),"Aprobaci�n: "+approval+", ref. "+trxID,
 					   Toast.LENGTH_LONG).show();
	    	} else {
	    		if(intent.hasExtra(Constants.ERROR_RESPONSE)) {
	    			TCMResponse errorResponse = (TCMResponse)intent.getSerializableExtra(Constants.ERROR_RESPONSE);
	    			payResult.setText("ERROR: "+errorResponse.getResponse());
	    			Toast.makeText(getApplicationContext(), "ERROR: "+errorResponse.getResponse(),
	    					   Toast.LENGTH_LONG).show();
	    		} else {
	    			payResult.setText(intent.getStringExtra("BT_STATUS_RESPONSE"));
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
	
	public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.tcmcancel:
            Intent intent = new Intent(this, TcmCancel.class);
            intent.putExtra("token", token);
            startActivity(intent);
            return true;
        case R.id.tcmticket:
            Intent intent2 = new Intent(this, TcmTicket.class);
            intent2.putExtra("token", token);
            startActivity(intent2);
            return true;    
        case R.id.tcmprint:
            Intent intent3 = new Intent(this, TcmPrint.class);
            startActivity(intent3);
            return true;        
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		payCat = payCategories[position];
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		payCat = 0;
	}
	
	@Override
	protected void onPause() {
		super.onPause(); 
		unregisterReceiver(receiver);
		registerReceiver(BlueVerifyReceiver,BlueVerifyFilter);
	}
	
	@Override
	protected void onResume() {
		super.onResume(); 
		registerReceiver(receiver,new IntentFilter("mx.com.adquira.cv.tcmpintents.EMV_PAYMENT_ACTION"));
		// Evento de Conexi�n con BT
		registerReceiver(BlueVerifyReceiver,BlueVerifyFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(Global.btConnected){
			// Eliminar conexi�n a BlueTooth
			myIntentbt = new Intent();	
			myIntentbt.setComponent(new ComponentName(TcmPay.this,mx.com.adquira.cv.tcmpintents.TCMKill.class));
			this.startService(myIntentbt);
		}
		unregisterReceiver(BlueVerifyReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}
	
	//Estado de la conexi�n a BT
	public class BlueVerify extends BroadcastReceiver
	{	
		public void onReceive(Context context, Intent intent) {
			Log.d("EVENTO","ENTRE");
			if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				Log.d("EVENTO","DESCONECTADO");
				Global.btConnected = false;
		    }
		}
    }
	
	public void conexionBamboo(){
		// Conexion a Bamboo...
		myIntentbt = new Intent();	
		myIntentbt.setComponent(new ComponentName(TcmPay.this,mx.com.adquira.cv.tcmpintents.TCMBtconnect.class));
		TcmPay.this.startService(myIntentbt);
		// Termina Conexi�n a Bamboo
	}
	
}
