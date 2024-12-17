package com.telpo.tps550.api.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.other.BeepManager;
import com.telpo.tps550.api.demo.decode.DecodeActivity;
import com.telpo.tps550.api.demo.iccard.IccActivityNew;
import com.telpo.tps550.api.demo.iccard.PsamActivity;
import com.telpo.tps550.api.demo.idcard.IdCardActivity;
import com.telpo.tps550.api.demo.ir.IrActivity;
import com.telpo.tps550.api.demo.led.LedActivity;
import com.telpo.tps550.api.demo.led.LedActivity900;
import com.telpo.tps550.api.demo.megnetic.MegneticActivity;
import com.telpo.tps550.api.demo.moneybox.MoneyBoxActivity;
import com.telpo.tps550.api.demo.nfc.NfcActivity;
import com.telpo.tps550.api.demo.nfc.NfcActivity_tps900;
import com.telpo.tps550.api.demo.ocr.OcrIdCardActivity;
import com.telpo.tps550.api.demo.printer.PrinterActivity;
import com.telpo.tps550.api.demo.printer.UsbPrinterActivity;
import com.telpo.tps550.api.demo.rfid.RfidActivity;
import com.telpo.tps550.api.demo.util.XuanXiang;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;

import java.util.List;

// Dependencias para el serial Port
import java.io.File; // Para manejar archivos
import java.io.OutputStream; // Para enviar datos al puerto serial
import java.io.IOException; // Para manejar excepciones IO
import android_serialport_api.SerialPort; // Clase SerialPort para la comunicación serial

//Dependencias para solicitudes HTTP

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

	private int Oriental = -1;
	private Button BnPrint, BnQRCode, psambtn, magneticCardBtn, rfidBtn, pcscBtn, identifyBtn, 
	               moneybox, irbtn, ledbtn, decodebtn, nfcbtn;
	private BeepManager mBeepManager;
	private ImageView logo;
	private long exitTime = 0;//点击时间控制
	private int pressTimes = 0;//连续点击次数
	private SharedPreferences sharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (-1 == Oriental) {
			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				Oriental = 0;
			} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				Oriental = 1;
			}
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		sharedPreferences = getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
		logo = (ImageView) findViewById(R.id.logo);
		logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() - exitTime > 2000) {
					exitTime = System.currentTimeMillis();
					pressTimes = 0;
				} else {
					pressTimes++;
					if (pressTimes >= 5) {
						pressTimes = 0;
						Intent login = new Intent(MainActivity.this, XuanXiang.class);
						startActivity(login);
						finish();
					}
				}
			}
		});

		BnPrint = (Button) findViewById(R.id.print_test);
		BnQRCode = (Button) findViewById(R.id.qrcode_verify);
		magneticCardBtn = (Button) findViewById(R.id.magnetic_card_btn);
		rfidBtn = (Button) findViewById(R.id.rfid_btn);
		pcscBtn = (Button) findViewById(R.id.pcsc_btn);
		identifyBtn = (Button) findViewById(R.id.identity_btn);
		moneybox = (Button) findViewById(R.id.moneybox_btn);
		irbtn = (Button) findViewById(R.id.ir_btn);
		ledbtn = (Button) findViewById(R.id.led_btn);
		psambtn = (Button) findViewById(R.id.psam);
		decodebtn = (Button) findViewById(R.id.decode_btn);
		nfcbtn = (Button) findViewById(R.id.nfc_btn);
		mBeepManager = new BeepManager(this, R.raw.beep);

		//MoneyBox
		moneybox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, MoneyBoxActivity.class));
			}
		});
		
		//Barcode And Qrcode
		BnQRCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				/*if (checkPackage("com.codecorp.cortex_scan")) {
					Intent intent = new Intent();
					intent.setClassName("com.codecorp.cortex_scan", "com.codecorp.cortex_scan.CortexScan");
					try {
						startActivityForResult(intent, 0x124);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
				}*/


				if (checkPackage("com.telpo.tps550.api")) {
					Intent intent = new Intent();
					intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
					try {
						startActivityForResult(intent, 0x124);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		//Print
		BnPrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle(getString(R.string.printer_type_select));
				dialog.setNegativeButton(getString(R.string.printer_type_common), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						startActivity(new Intent(MainActivity.this, PrinterActivity.class));
					}
				});
				dialog.setPositiveButton(getString(R.string.printer_type_usb), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						startActivity(new Intent(MainActivity.this, UsbPrinterActivity.class));
					}
				});
				dialog.show();
			}
		});
		
		//Magnetic Card
		magneticCardBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(MainActivity.this, MegneticActivity.class));
			}
		});
		
		//RFID
		rfidBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(MainActivity.this, RfidActivity.class));
			}
		});

		//IC Card
		pcscBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, IccActivityNew.class));
			}
		});

		//IR
		irbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, IrActivity.class));
			}
		});
		
		//Led
		ledbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal()) {
					startActivity(new Intent(MainActivity.this, LedActivity900.class));
				} else {
					startActivity(new Intent(MainActivity.this, LedActivity.class));
				}
			}
		});

		//ID Card
		identifyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle(getString(R.string.idcard_xzgn));
				dialog.setMessage(getString(R.string.idcard_xzsfsbfs));

				dialog.setNegativeButton(getString(R.string.idcard_sxtsb), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//use camera
						startActivity(new Intent(MainActivity.this, OcrIdCardActivity.class));
					}
				});
				dialog.setPositiveButton(getString(R.string.idcard_dkqsb), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//use ID Card reader
						startActivity(new Intent(MainActivity.this, IdCardActivity.class));
					}
				});
				dialog.show();

			}

		});
		
		//PSAM Card
		psambtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, PsamActivity.class));
			}
		});		

		//laser qrcode
		decodebtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, DecodeActivity.class));
			}
		});
		
		//NFC
		nfcbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e("yw",""+SystemUtil.getDeviceType());
				Log.e("yw",""+StringUtil.DeviceModelEnum.TPS900.ordinal());
				if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal()){
			        startActivity(new Intent(MainActivity.this, NfcActivity_tps900.class));
			    }else{
			        startActivity(new Intent(MainActivity.this, NfcActivity.class));
			    }
			}
		});
	}

	private boolean checkPackage(String packageName) {
		PackageManager manager = this.getPackageManager();
		Intent intent = new Intent().setPackage(packageName);
		List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);//PackageManager.GET_INTENT_FILTERS
		if (infos == null || infos.size() < 1) {
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0x124) {
			if (resultCode == 0) {
				if (data != null) {
					mBeepManager.playBeepSoundAndVibrate();
					String qrcode = data.getStringExtra("qrCode");
					Toast.makeText(MainActivity.this, "Scan result:" + qrcode, Toast.LENGTH_LONG).show();



					// -----------Solicitud HTTP-----------
					new Thread(new Runnable() {
						@Override
						public void run() {
							makeAsyncData();
						}
					}).start();

					//----- Enviar señal al puerto serial-------
					//String dataSerial="OPEN\n";
					//sendSignalToSerialPort(dataSerial);
					//Toast.makeText(MainActivity.this, "Señal enviada:" + dataSerial, Toast.LENGTH_SHORT).show();

					return;
				}
			} else {
				Toast.makeText(MainActivity.this, "Scan Failed", Toast.LENGTH_LONG).show();
			}
		}

	}

	private void makeAsyncData() {
		HttpURLConnection connection = null;
		BufferedReader reader = null;

		try {
			// Establecer conexión
			URL url = new URL("https://jsonplaceholder.typicode.com/todos/4");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			// Leer la respuesta
			InputStream inputStream = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder result = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				result.append(line);
			}

			// Convertir la respuesta a texto
			final String response = result.toString();

			// Mostrar el resultado en un Toast en el hilo principal
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "Response: " + response, Toast.LENGTH_LONG).show();
					Toast.makeText(MainActivity.this, "SOLICITUD EXITOSA", Toast.LENGTH_LONG).show();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			// Mostrar errores en el hilo principal
			final String errorMessage=e.getMessage();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
				}
			});

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}



	/*
	private void sendSignalToSerialPort(String signal) {
		SerialPort serialPort = null;
		OutputStream outputStream = null;

		try {
			//dev/ttyS0" al dispositivo serial adecuado
			File device = new File("/dev/ttyS0");
			int baudrate = 9600; // baudrate según las especificaciones del molinete
			int flags = 0;

			// Abrir el puerto serial
			serialPort = new SerialPort(device, baudrate, flags);
			outputStream = serialPort.getOutputStream();

			Log.d("SerialTest", "Simulando envío: " + signal);
			// Enviar la señal
			outputStream.write(signal.getBytes());
			outputStream.flush();

			Toast.makeText(MainActivity.this, "Señal enviada al molinete", Toast.LENGTH_SHORT).show();

		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, "Error al enviar señal al molinete", Toast.LENGTH_LONG).show();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (serialPort != null) {
					serialPort.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/


	@Override
	protected void onResume() {
		super.onResume();
		//setRequestedOrientation(Oriental);
		setfuncview();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBeepManager.close();
		mBeepManager = null;
	}

	private void setfuncview() {
		if (sharedPreferences.getBoolean("first",true)) {
			sharedPreferences.edit().putBoolean("first",false).commit();
		}else {
			if (sharedPreferences.getBoolean("BNPRINT",true)){
				BnPrint.setEnabled(true);
			}else {
				BnPrint.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("BNQRCODE",true)){
				BnQRCode.setEnabled(true);
			}else {
				BnQRCode.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("MAGNETICCARDBTN",true)){
				magneticCardBtn.setEnabled(true);
			}else {
				magneticCardBtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("RFIDBTN",true)){
				rfidBtn.setEnabled(true);
			}else {
				rfidBtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("PCSCBTN",true)){
				pcscBtn.setEnabled(true);
			}else {
				pcscBtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("NFCBTN",true)){
				nfcbtn.setEnabled(true);
			}else {
				nfcbtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("IDENTIFYBTN",true)){
				identifyBtn.setEnabled(true);
			}else {
				identifyBtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("MONEYBOX",true)){
				moneybox.setEnabled(true);
			}else {
				moneybox.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("IRBTN",true)){
				irbtn.setEnabled(true);
			}else {
				irbtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("LEDBTN",true)){
				ledbtn.setEnabled(true);
			}else {
				ledbtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("PSAMBTN",true)){
				psambtn.setEnabled(true);
			}else {
				psambtn.setEnabled(false);
			}
			if (sharedPreferences.getBoolean("DECODEBTN",true)){
				decodebtn.setEnabled(true);
			}else {
				decodebtn.setEnabled(false);
			}
		}

	}

}
