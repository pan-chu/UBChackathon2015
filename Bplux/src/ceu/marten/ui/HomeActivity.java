//Modified by Brittaney Geisler March 2015

package ceu.marten.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ceu.marten.bitadroid.R;
import ceu.marten.model.DeviceConfiguration;

import com.ubc.capstonegroup70.PatientSessionActivity;


public class HomeActivity extends Activity {
	
	public static boolean configset = false;
	public static boolean nameset = false;
	public static String PatientName;
	public static String PatientFName;
	public static String PatientLName;
	public int i=1;
	public static String btName;
	private String SettingsDirectory = "/storage/emulated/0/Bioplux/Settings/";
	private String PInfoDirectory = "/storage/emulated/0/Bioplux/Patients/";
	private String SettingsFile = "Bplux_BluetoothSelection.txt";
	private String MasterPatientList = "patientNames.txt";
	private String PatientInfoExtension = "INFO.txt";
	private DeviceConfiguration newConfiguration;
	private String[]  activeChannels = {"EMG"};
	private String[] spinner_array = new String[20];
	private int spinner_array_count;
	Context context = this;
	Button mButton, mButton1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ly_home);
		setConfigurationDefaults();
		spinner_array_count = 0;
		for (int j=0; j<20; j++){
			spinner_array[j] = " ";
		}
		try {
			readNamesFromFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File(SettingsDirectory + SettingsFile);
		if(file.exists()) {
			try {
				readwriteBT(false, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			btListGenerator();
		}

		mButton = (Button)findViewById(R.id.button1);
		mButton.setText("BLUETOOTH: "+newConfiguration.getMacAddress());
		mButton.setTextSize(15);
		mButton1 = (Button)findViewById(R.id.button3);
		mButton1.setTextSize(15);
	
	}
	
	public void onClickedPatientName(View view) {
		patientListGenerator();
	}

	private void patientListGenerator(){
		
		final String[] string= new String[spinner_array_count];
		final boolean[] states = new boolean[spinner_array_count];
		for (int j=0; j<spinner_array_count; j++){
			string[j] = spinner_array[j];
			states[j] = false;
		}
		
		AlertDialog dialog ;
		
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("Patients");

		builder.setMultiChoiceItems(string, states, new DialogInterface.OnMultiChoiceClickListener(){
			public void onClick(DialogInterface dialogInterface, int item, boolean state) {

			}
		});
		
		builder.setPositiveButton("NEW", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		    	AddNewPatientDialog();
		    }
		});
		
		final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		
		builder.setNeutralButton("SELECT", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		    	int j=0;
		    	int count = 0;
		    	boolean choice = true;
		    	SparseBooleanArray Checked = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
		    	
		    	if (((AlertDialog)dialog).getListView().getCheckedItemCount()>1){
                    dlgAlert.setMessage("PLEASE SELECT ONE PATIENT");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                    
                    dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                    });
		    	}
		    	else {
			    	while (Checked.get(j) == false){
			    		j++;
			    		if (j == spinner_array_count) {
			    			choice = false;
			    			break;
			    		}
			    	}
			    	if (choice){
						PatientName = spinner_array[j];
		    			Pattern pattern = Pattern.compile("(.+) (.+)");
		      			Matcher matcher = pattern.matcher(PatientName);
		    			if (matcher.find()) {
		      				if (matcher.groupCount() == 2) {	  				
		    	  				PatientFName = matcher.group(1);
		    	  				PatientLName = matcher.group(2);
		      				}
		      				else {
		      					System.out.print("ERROR: Insufficient number of matches found: " + matcher.groupCount());
		      				}
		    			}
		    			nameset = true;
				    	mButton1.setText(spinner_array[j]);
			    	}
			    	else {
			    		mButton1.setText("Patient Name");
			    		nameset = false;
			    	}
		    	}
		    }
		});
		
		builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		    	
	            SparseBooleanArray Checked = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
	            for (int j=0; j<spinner_array_count; j++){
	            
	            	if(Checked.get(j) == true){
	            		File file = new File(PInfoDirectory + spinner_array[j] + PatientInfoExtension);
	            		//Toast.makeText(getApplicationContext(), PInfoDirectory + spinner_array[j] + PatientInfoExtension,Toast.LENGTH_SHORT).show();
	          			if(file.exists()) {
	          				file.delete();
	          			}
	          			spinner_array[j] = " ";
	          			
	          			for (int l=j; l<spinner_array_count; l++){
	          				spinner_array[j] = spinner_array[j+1];
	          			}
	          			removePatientFromFile(j);
	          			spinner_array_count--;
	          			mButton1.setText("Patient Name");
	    	    		nameset = false;
	            	}
	            }
		    }
		});
		
		dialog = builder.create();
		dialog.show();
	}
	
	public void onClickedStart(View view) {
		if (configset && nameset){
			Intent patientSessionIntent = new Intent(this, PatientSessionActivity.class);
			patientSessionIntent.putExtra("patientName", PatientName);
			patientSessionIntent.putExtra("patientFName", PatientFName);
			patientSessionIntent.putExtra("patientLName", PatientLName);
			patientSessionIntent.putExtra("configuration", newConfiguration);
			startActivity(patientSessionIntent);
			overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
		}
		else if (configset) Toast.makeText(getApplicationContext(), "NO PATIENT SELECTED",Toast.LENGTH_SHORT).show();
		else if (nameset) Toast.makeText(getApplicationContext(), "NO BLUETOOTH SELECTED",Toast.LENGTH_SHORT).show();
		else Toast.makeText(getApplicationContext(), "NO BLUETOOTH OR PATIENT SELECTED",Toast.LENGTH_SHORT).show();
	}
	
	public void onClickedConfiguration(View view) {
		btListGenerator();
	}
	
	private void setConfigurationDefaults(){
		newConfiguration = new DeviceConfiguration(this);
		newConfiguration.setNumberOfBits(12);
		newConfiguration.setActiveChannels(activeChannels);
		newConfiguration.setDisplayChannels(activeChannels);
		newConfiguration.setName("MYconfig");
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date();
		newConfiguration.setCreateDate(dateFormat.format(date));
		newConfiguration.setVisualizationFrequency(1000);
		newConfiguration.setSamplingFrequency(100);
	}
	
	private void btListGenerator(){
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		
		final String[] string= new String[pairedDevices.size()];
		for (int j=0; j<pairedDevices.size(); j++){
			string[j] = " ";
		}
		int count=0;
		for(BluetoothDevice bt : pairedDevices){
			string[count] = bt.getName();
			count++;
		}
		
		AlertDialog dialog ;
		
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("PAIRED DEVICES");
		builder.setItems(string, new DialogInterface.OnClickListener() {

			@Override
		    public void onClick(DialogInterface dialog, int position) {
				btName = string[position];
				configset = true;
				newConfiguration.setMacAddress(btName);
				mButton.setText("BLUETOOTH: "+newConfiguration.getMacAddress());
				try {
					readwriteBT(true, btName);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }

		});
		
		dialog = builder.create();
		dialog.show();
	}

	private void AddNewPatientDialog(){	
		
		final Dialog dialog = new Dialog(context);
		dialog.setTitle("Patient Name");
		dialog.setContentView(R.xml.anp_button);
		
		final EditText name1 = (EditText)dialog.findViewById(R.id.first_name);	
		final EditText name2 = (EditText)dialog.findViewById(R.id.last_name);
		name1.setHint("First Name");
		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		        for (int i = start; i < end; i++) { 
		             if (Character.isSpaceChar(source.charAt(i))) { 
		            	 return "";     
		             }     
		        }
				return null;
			}  
		};
		name1.setFilters(new InputFilter[] { filter });
		name2.setFilters(new InputFilter[] { filter });
		
		Button negButton = (Button) dialog.findViewById(R.id.buttonCANCEL);
        	negButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	dialog.dismiss();
            }
        });
        	
        final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);	
        Button posButton = (Button) dialog.findViewById(R.id.buttonSUBMIT);
        	posButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (name1.getText().toString().equals("") || name2.getText().toString().equals("")){
	            	dlgAlert.setMessage("PLEASE ENTER A FIRST AND LAST NAME");
	                dlgAlert.setPositiveButton("OK", null);
	                dlgAlert.setCancelable(true);
	                dlgAlert.create().show();
	                
	                dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                        }
	                });
            	}
            	else{
            		Editable patientFName = name1.getText();
  				  	Editable patientLName = name2.getText();
  				  	spinner_array[spinner_array_count] = patientFName.toString() + " " + patientLName.toString();
  				  	spinner_array_count++;
  				  
  				  	try {				
  				  		saveNameToFile(patientFName.toString() + " " + patientLName.toString());
  				  	} catch (IOException e) {
  				  		e.printStackTrace();
  				  	}
  				  
	  				  PatientName = spinner_array[spinner_array_count -1];
	  				  Pattern pattern = Pattern.compile("(.+) (.+)");
	  	      		  Matcher matcher = pattern.matcher(PatientName);
	  	      		  if (matcher.find()) {
	  	      			  if (matcher.groupCount() == 2) {	  				
	  	      				  PatientFName = matcher.group(1);
	  	      				  PatientLName = matcher.group(2);
	  	      			  }
	  	      			  else {
	  	      				  System.out.print("ERROR: Insufficient number of matches found: " + matcher.groupCount());
	  	      			  }
	  	      		  }
	  	      		  nameset = true;
	  	      		  mButton1.setText(spinner_array[spinner_array_count-1]);
	  	      		  dialog.dismiss();
            	}
            	
            }
        });
        dialog.show();	
}
	
	private void saveNameToFile(String patientName) throws IOException{
		
		//WRITE 
		
		String write_string = patientName + '\n';
		
		try {
		    
		    // Check if the directory for the settings exists; create the folders if they don't exist
			File root = new File(SettingsDirectory);
			if (!root.exists()) {
				root.mkdirs();
			}
			// Check if the Master Patient List file exists
			File file = new File(root, MasterPatientList);
			if (!file.exists()) {
				file = new File(SettingsDirectory, MasterPatientList);
			}
			FileWriter writer = new FileWriter(file, true);
			writer.append(write_string);
			writer.flush();
			writer.close();
			        
		
		} catch (IOException e) {
			    e.printStackTrace();
			
		}

	}
	private void readNamesFromFile() throws IOException{
		
		//READ
		try {
			FileInputStream fIn = new FileInputStream(SettingsDirectory + MasterPatientList);
		    @SuppressWarnings("resource")
			Scanner scanner = new Scanner(fIn);
		    String readString = null;
		    boolean first_read = true;
		    while (scanner.hasNextLine())
		    {
		        String currentline = scanner.nextLine();
		        if (first_read == true) {
		        	readString = currentline;
		        	first_read = false;
		        }
		        else readString = readString + currentline;
		        spinner_array[spinner_array_count] = currentline;
		        spinner_array_count++;
		    }
		        
		} catch (IOException ioe) 
		    {ioe.printStackTrace();}
		
	}
	
	private void removePatientFromFile(int position){
		
		//READ
		boolean first_write = true;
	    String[] temp_array = new String[10];//temp array
	    int temp_count = 0;//keeps track of location in array
	    int total_count = 0;
	    for (int j=0; j<10; j++){
	    	temp_array[j] = null;
	    }
		try {
			FileInputStream fIn = new FileInputStream(SettingsDirectory + MasterPatientList);
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(fIn);
			while (scanner.hasNextLine())
			{
		    	String currentline = scanner.nextLine();
			  	if (temp_count < position){
			  		temp_array[temp_count] = currentline;
					temp_count++;
					total_count++;
		    	}
			  	else if (temp_count == position){
			  		temp_count++;
			  	}
			  	else if (temp_count > position){
			  		temp_array[temp_count-1] = currentline;
			  		temp_count++;
			  		total_count++;
			  	}
		    }  	

		} catch (IOException ioe) 
			 {ioe.printStackTrace();}
				
		//WRITE 
			
		String write_string=null;
				
		for (int j=0; j<total_count; j++){
				write_string = temp_array[j]+'\n';
				try {
					// Check if the directory for the settings exists; create the folders if they don't exist
					File root = new File(SettingsDirectory);
					if (!root.exists()) {
						root.mkdirs();
					}
					// Check if the Master Patient List file exists
					File file = new File(root, MasterPatientList);
					if (!file.exists()) {
						file = new File(SettingsDirectory, MasterPatientList);
					}
					FileWriter writer;
					if (first_write) {
						writer = new FileWriter(file, false);
						first_write = false;
					}
					else writer = new FileWriter(file, true);
					writer.append(write_string);
					writer.flush();
					writer.close();
				} catch (IOException e) {
					    e.printStackTrace();
				}
		}
	}

private void readwriteBT(boolean task, String btName) throws IOException{
		
		if (task){
			//WRITE
			try {
				// Check if the directory for the settings exists; create the folders if they don't exist
				File root = new File(SettingsDirectory);
				if (!root.exists()) {
					root.mkdirs();
				}
				// Check if the settings file exists
				File file = new File(root, SettingsFile);
				if (!file.exists()) {
					file = new File(SettingsDirectory, SettingsFile);
				}
				FileWriter writer = new FileWriter(file);
				writer.write(HomeActivity.btName);
				writer.flush();
				writer.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else {
			//READ
			try {
				FileInputStream fIn = new FileInputStream(SettingsDirectory + SettingsFile);
			    @SuppressWarnings("resource")
				Scanner scanner = new Scanner(fIn);
			    while (scanner.hasNextLine())
			    {
			        String currentline = scanner.nextLine();
					newConfiguration.setMacAddress(currentline);
					HomeActivity.btName = currentline;
					configset = true;   
			    }  
			    fIn.close();
			} catch (IOException ioe) 
			    {ioe.printStackTrace();}
			
		}
	}

}