//Author: Brittaney Geisler - March 2015

package com.ubc.capstonegroup70;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;
import ceu.marten.model.DeviceConfiguration;
import ceu.marten.ui.NewRecordingActivity;


public class PatientSessionActivity extends Activity {//implements android.widget.PopupMenu.OnMenuItemClickListener {

	public static boolean configset = false;
	public static boolean nameset = false;
	public static String recname;
	public int i=1;
	public static String recname1;
	private DeviceConfiguration newConfiguration;
	private PatientClass newPatient;
	private Bundle extras;
	private String[] Month_Array = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	private String[] Day_31_Array = {"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
	private String[] Day_30_Array = {"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30"};
	private String[] Day_29_Array = {"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29"};
	private String SettingsDirectory = "/storage/emulated/0/Bioplux/Settings/";
	private String PInfoDirectory = "/storage/emulated/0/Bioplux/Patients/";
	private String SettingsFile = "Bplux_BluetoothSelection.txt";
	private String MasterPatientList = "patientNames.txt";
	private String PatientInfoExtension = "INFO.txt";
	Context context = this;
	boolean entry_error = false;
	ArrayAdapter SpinnerAdapter1;
	ArrayAdapter SpinnerAdapter2; 
	ArrayAdapter SpinnerAdapter3;
	boolean first_time_editing_patient = true;
	
	
	// Variables for file explorer
	private final int REQUEST_CODE_PICK_DIR = 1;
	private final int REQUEST_CODE_PICK_FILE = 2;
	
	// Stores names of traversed directories
	  ArrayList<String> str = new ArrayList<String>();

	// Check if the first level of the directory structure is the one showing
	private Boolean firstLvl = true;
	
	private static final String TAG = "F_PATH";
	
	private Item[] fileList;
	private final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	private File path = new File(Environment.getExternalStorageDirectory() + "/Bioplux");
	private String chosenFile;
	private static final int DIALOG_LOAD_FILE = 1000;
	private static final int DIALOG_EXPORT = 1001;
	private static final int DIALOG_DELETE = 1002;
	private static final int DIALOG_EMPTY = 1003;
	
	ListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ly_patientsession);
		newPatient = new PatientClass(this);
		extras = getIntent().getExtras();
		newConfiguration = (DeviceConfiguration) extras.getSerializable("configuration");
		
		SpinnerAdapter1 =  new ArrayAdapter(this, android.R.layout.simple_spinner_item,  Day_31_Array);
		SpinnerAdapter2 =  new ArrayAdapter(this, android.R.layout.simple_spinner_item,  Day_30_Array);
		SpinnerAdapter3 =  new ArrayAdapter(this, android.R.layout.simple_spinner_item,  Day_29_Array);
		
		
//		File file = new File("/storage/emulated/0/"+extras.getString("patientName")+"INFO"+".txt");
		File file = new File(PInfoDirectory +extras.getString("patientName")+PatientInfoExtension);
		if(file.exists()) {
			try {
				String patientName = extras.getString("patientFName") + " " + extras.getString("patientLName");
				readInfoFromFile(patientName);
				first_time_editing_patient = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
//			newPatient.setPatientName(extras.getString("patientName"));
			newPatient.setPatientFName(extras.getString("patientFName"));
			newPatient.setPatientLName(extras.getString("patientLName"));
			newPatient.setGender(true);
			newPatient.setHealthNumber("0123456789");
			newPatient.setBirthYear("1999");
			newPatient.setBirthMonth("01");
			newPatient.setBirthDay("01");
			first_time_editing_patient = true;
			setPatientInfoDialog();
		}
		nameset = true;
		configset = true;
		TextView myAwesomeTextView = (TextView)findViewById(R.id.lyHeader);
		myAwesomeTextView.setText(newPatient.getPatientName());
	}


	/************************ BUTTON EVENTS *******************/
	
	public void onClickedSetPatientInfo(View view) {
		setPatientInfoDialog();
	}
	
	private void setPatientInfoDialog() {
		
		entry_error = false;
		final Dialog dialog = new Dialog(context);
		dialog.setTitle("Patient Information");
		dialog.setContentView(R.xml.adb_button);
		
		final EditText name2 = (EditText)dialog.findViewById(R.id.last_name);
		name2.setText(newPatient.getPatientName());
		name2.setEnabled(false);
		
		final EditText phn2 = (EditText)dialog.findViewById(R.id.phn2);
		phn2.setText(newPatient.getHealthNumber());
		phn2.setInputType(InputType.TYPE_CLASS_NUMBER);
		phn2.setFilters( new InputFilter[] {new InputFilter.LengthFilter(10)});
		if (!first_time_editing_patient) phn2.setEnabled(false);
		
		final CheckBox gender2 = (CheckBox)dialog.findViewById(R.id.gender2);
		gender2.setChecked(newPatient.getGender());
		
		final CheckBox gender3 = (CheckBox)dialog.findViewById(R.id.gender3);
		gender3.setChecked(!newPatient.getGender());
		
		gender2.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (gender2.isChecked()) {
					gender3.setChecked(false);
				}
				else{
					gender3.setChecked(true);
				}
			}	
		});
		
		gender3.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (gender3.isChecked()) {
					gender2.setChecked(false);
				}
				else{
					gender2.setChecked(true);
				}
			}	
		});
		
		final EditText year2 = (EditText)dialog.findViewById(R.id.year2);
		year2.setText(newPatient.getBirthYear());	
		year2.setInputType(InputType.TYPE_CLASS_NUMBER);
		year2.setFilters( new InputFilter[] {new InputFilter.LengthFilter(4)});
		
		
		final Spinner month2 = (Spinner)dialog.findViewById(R.id.month2);
		ArrayAdapter SpinnerAdapter =  new ArrayAdapter(this, android.R.layout.simple_spinner_item,  Month_Array);
		month2.setAdapter(SpinnerAdapter);
		month2.setSelection(Integer.parseInt(newPatient.getBirthMonth())-1);
		
		final Spinner day2 = (Spinner)dialog.findViewById(R.id.day2);
		day2.setAdapter(SpinnerAdapter1);
		
		if (((Integer.parseInt(newPatient.getBirthMonth())-1)==0)||((Integer.parseInt(newPatient.getBirthMonth())-1)==2)||((Integer.parseInt(newPatient.getBirthMonth())-1)==4)||((Integer.parseInt(newPatient.getBirthMonth())-1)==6)||((Integer.parseInt(newPatient.getBirthMonth())-1)==7)||((Integer.parseInt(newPatient.getBirthMonth())-1)==9)||((Integer.parseInt(newPatient.getBirthMonth())-1)==11)){
			day2.setAdapter(SpinnerAdapter1);
		}
		else if (((Integer.parseInt(newPatient.getBirthMonth())-1)==3)||((Integer.parseInt(newPatient.getBirthMonth())-1)==5)||((Integer.parseInt(newPatient.getBirthMonth())-1)==8)||((Integer.parseInt(newPatient.getBirthMonth())-1)==10)){
			day2.setAdapter(SpinnerAdapter2);
		}
		else {
			day2.setAdapter(SpinnerAdapter3);
		}
		
		month2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	        @Override
	        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {                

	            int item = position;
	            if ((item == 0) || (item==2) || (item== 4) ||(item== 6)||(item== 7)||(item== 9)||(item== 11)){
	            	int position1 = day2.getSelectedItemPosition();
	            	day2.setAdapter(SpinnerAdapter1);
	            	day2.setSelection(position1);
	            }
	            else if ((item==3) || (item==5) || (item==8) || (item==10)){
	            	int position1 = day2.getSelectedItemPosition();
	            	day2.setAdapter(SpinnerAdapter2);
	            	if (position1 > 30) day2.setSelection(0);
	            	else day2.setSelection(position1);
	            }
	            else {
	            	int position1 = day2.getSelectedItemPosition();
	            	day2.setAdapter(SpinnerAdapter3);
	            	if (position1 > 29) day2.setSelection(0);
	            	else day2.setSelection(position1);
	            }
	        }
	        @Override
	        public void onNothingSelected(AdapterView<?> parent) {
	        }
	    });
		
		day2.setSelection(Integer.parseInt(newPatient.getBirthDay())-1);
		
		dialog.setCanceledOnTouchOutside(false);
		
		final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		
		Button negButton = (Button) dialog.findViewById(R.id.buttonCANCEL);
        	negButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(!first_time_editing_patient) dialog.dismiss();
            	
            	else {
            		dlgAlert.setMessage("Please Submit Patient Information First");
            	
            		dlgAlert.setPositiveButton("OK", null);
            		dlgAlert.setCancelable(true);
            		dlgAlert.create().show();
                
            		dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
            		});
            	}
            }
        });
        	
        Button posButton = (Button) dialog.findViewById(R.id.buttonSUBMIT);
        	posButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            
              if (phn2.getText().toString().length() != 10) {
            	  phn2.requestFocus();
      			  phn2.setError("10 Digits Required");
      			  entry_error = true;
      		  }	  
              if (year2.getText().toString().length() != 4) {
            	  phn2.clearFocus();
            	  year2.requestFocus();
    			  year2.setError("4 Digits Required");
    			  entry_error = true;
              }
      		  if (!entry_error){
      			  newPatient.setHealthNumber(phn2.getText().toString());
	              newPatient.setPatientName(name2.getText().toString());
	              newPatient.setHealthNumber(phn2.getText().toString());
	      		  if (gender2.isChecked()) {
	      			  newPatient.setGender(true);
	      		  }
	      		  else newPatient.setGender(false);
	      		  newPatient.setBirthYear(year2.getText().toString());
	      		  newPatient.setBirthMonth(Integer.toString(month2.getSelectedItemPosition()+1));
	      		  newPatient.setBirthDay(Integer.toString(day2.getSelectedItemPosition()+1));
	      		  try {
	      			saveInfoToFile(newPatient.getPatientName(), newPatient.getHealthNumber(), newPatient.getGender(), newPatient.getBirthYear(), newPatient.getBirthMonth(), newPatient.getBirthDay());
	      		  } catch (IOException e) {
	      			// TODO Auto-generated catch block
	      			e.printStackTrace();
	      		  }
	      		  try {
	      			readInfoFromFile(newPatient.getPatientName());
	      		  } catch (IOException e) {
	      			// TODO Auto-generated catch block
	      			e.printStackTrace();
	      		  }
	      		  first_time_editing_patient = false;
	      		  dialog.dismiss();
      		  }
      		  else {
      			  entry_error = false;
      		  }
            }
        });
        dialog.show();	
	}


	public void onClickedStart(View view) {
		recname1 = String.valueOf(newPatient.getPatientFirstName().charAt(0)) + String.valueOf(newPatient.getPatientLastName().charAt(0)) + "-" + newPatient.getHealthNumber() + "-" + String.format(Locale.getDefault(), "%04d", Integer.parseInt(newPatient.getBirthYear())) 
					+ "-" + String.format(Locale.getDefault(), "%02d", Integer.parseInt(newPatient.getBirthMonth())) + "-" + String.format(Locale.getDefault(), "%02d", Integer.parseInt(newPatient.getBirthDay())) + "__";
		Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
		newRecordingIntent.putExtra("recordingName", recname1);
		newRecordingIntent.putExtra("configuration", newConfiguration);
		newRecordingIntent.putExtra("patientFName", newPatient.getPatientFirstName());
		newRecordingIntent.putExtra("patientLName", newPatient.getPatientLastName());
//		newRecordingIntent.putExtra("patientName", newPatient.getPatientName());
		startActivity(newRecordingIntent);
		overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
	}

	private void saveInfoToFile(String patientName, String healthNumber, boolean gender, String birthYear, String birthMonth, String birthDay ) throws IOException{
		
		//WRITE 
		
		String nameString = patientName + '\n';
		String healthNumString = healthNumber + '\n';
		String genderString;
		if (gender) genderString = "Male"+'\n';
		else genderString = "Female"+'\n';
		String birthYearString = birthYear + '\n';
		String birthMonthString = birthMonth + '\n';
		String birthDayString = birthDay + '\n';
		
		try {
		    
		    // Check if the directory for the settings exists; create the folders if they don't exist
		    File root = new File(PInfoDirectory);
 			if (!root.exists()) {
 				root.mkdirs();
 			}
 			// Check if the Master Patient List file exists
 			File file = new File(root, patientName + PatientInfoExtension);
 			if (!file.exists()) {
 				file = new File(root, patientName + PatientInfoExtension);
 			}
 			FileWriter writer = new FileWriter(file);
 			writer.write(nameString);
 			writer.write(healthNumString);
 			writer.write(genderString);
 			writer.write(birthYearString);
 			writer.write(birthMonthString);
 			writer.write(birthDayString);
 			writer.flush();
 			writer.close();
		
		} catch (IOException e) {
			    e.printStackTrace();
			
		}
	}
	private void readInfoFromFile(String patientName) throws IOException{
		
		int linecount = 0;
		//READ
		try {
//			FileInputStream fIn = new FileInputStream("/storage/emulated/0/"+patientName+"INFO"+".txt");
			FileInputStream fIn = new FileInputStream(PInfoDirectory+patientName+PatientInfoExtension);
		    @SuppressWarnings("resource")
			Scanner scanner = new Scanner(fIn);
		    while (scanner.hasNextLine())
		    {
		        String currentline = scanner.nextLine();
		        if (linecount == 0) newPatient.setPatientName(currentline);
		        else if (linecount == 1) newPatient.setHealthNumber(currentline);
		        else if (linecount == 2) {
		        	if (currentline.equals("Male")) newPatient.setGender(true);
		        	else newPatient.setGender(false);
		        }
		        else if (linecount == 3) newPatient.setBirthYear(currentline);
		        else if (linecount == 4) newPatient.setBirthMonth(currentline);
		        else if (linecount == 5) newPatient.setBirthDay(currentline);
		        linecount++;
		        //Toast.makeText(context, currentline, Toast.LENGTH_SHORT).show();
		    }
		    
		        
		} catch (IOException ioe) 
		    {ioe.printStackTrace();}
		
	}
	
		
	
	public void onClickedSavedData(View view) {
		// Start FileExplorer    
	    if(!loadFileList()) {
        	showDialog(DIALOG_LOAD_FILE);
        	Log.d(TAG, "No files were found for this patient.");
        }
        else {
            showDialog(DIALOG_EMPTY);
            Log.d(TAG, path.getAbsolutePath());
        }
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	
	
	/*
	 * Android File Explore
	 * 
	 * Copyright 2011 Manish Burman
	 * Modified by Caleb Ng 2015
	 * 
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
	 * compliance with the License. You may obtain a copy of the License at
	 * 		http://www.apache.org/licenses/LICENSE-2.0
	 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
	 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	 * See the License for the specific language governing permissions and limitations under the License.
	 */
	// Open FileExplore
	private Boolean loadFileList() {
	    Boolean isEmpty = true;
		try {
	      path.mkdirs();
	    } catch (SecurityException e) {
	      Log.e(TAG, "unable to write on the sd card ");
	    }

	    // Checks whether path exists
	    if (path.exists()) {
	      FilenameFilter filter = new FilenameFilter() {
	        @Override
	        public boolean accept(File dir, String filename) {
	          File sel = new File(dir, filename);
	          // Modified by Caleb (2015) - Create string for filtering files based on the current patient's personal health number
	          String fileFilter = String.valueOf(newPatient.getPatientFirstName().charAt(0)) + String.valueOf(newPatient.getPatientLastName().charAt(0)) + "-" + newPatient.getHealthNumber() + "\\S+";
	          // Modified by Caleb (2015) - Filters based on whether the file is hidden or not, as well as whether or not it belongs to the current patient
	          return (sel.isFile() || sel.isDirectory()) && !sel.isHidden() && filename.matches(fileFilter);
	          
	        }
	      };

	      String[] fList = path.list(filter);
	      // Check if list is empty
	      if(fList.length <= 0)
	    	  return isEmpty=true;
	      
	      // Modified - Sort the list of files in reverse alphabetical order with most recent items appearing first
	      for(int i=0; i<fList.length; i++) {
	    	  fList[i] = fList[i].toUpperCase(Locale.getDefault());
	      }
	      Arrays.sort(fList,Collections.reverseOrder());
	      fileList = new Item[fList.length];
	      for (int i = 0; i < fList.length; i++) {
	        fileList[i] = new Item(fList[i], R.drawable.file_icon);

	        // Convert into file path
	        File sel = new File(path, fList[i]);

	        // Set drawables
	        if (sel.isDirectory()) {
	          fileList[i].icon = R.drawable.directory_icon;
	          Log.d("DIRECTORY", fileList[i].file);
	        } else {
	          Log.d("FILE", fileList[i].file);
	        }
	      }

	      if (!firstLvl) {
	        Item temp[] = new Item[fileList.length + 1];
	        for (int i = 0; i < fileList.length; i++) {
	          temp[i + 1] = fileList[i];
	        }
	        temp[0] = new Item("Up", R.drawable.directory_up);
	        fileList = temp;
	      }
	    } else {
	      Log.e(TAG, "path does not exist");
	    }

	    adapter = new ArrayAdapter<Item>(this,
	        android.R.layout.select_dialog_item, android.R.id.text1,
	        fileList) {
	      @Override
	      public View getView(int position, View convertView, ViewGroup parent) {
	        // creates view
	        View view = super.getView(position, convertView, parent);
	        TextView textView = (TextView) view
	            .findViewById(android.R.id.text1);

	        // put the image on the text view
	        textView.setCompoundDrawablesWithIntrinsicBounds(
	            fileList[position].icon, 0, 0, 0);

	        // add margin between image and text (support various screen densities)
	        int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
	        textView.setCompoundDrawablePadding(dp5);

	        return view;
	      }
	    };
	    
	    return false;
	  }

	  private class Item {
	    public String file;
	    public int icon;

	    public Item(String file, Integer icon) {
	      this.file = file;
	      this.icon = icon;
	    }

	    @Override
	    public String toString() {
	      return file;
	    }
	  }
	  
	  /*
	   * Convert an Item array into a CharSequence array
	   */
	  private CharSequence[] itemToSequence(Item[] list) {
		  CharSequence[] chars = new CharSequence[list.length];
		  for(int i=0; i<list.length; i++) {
			  chars[i] = list[i].toString();
		  }
		  
		  return chars;
	  }

	
	@Override
	  protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
	    AlertDialog.Builder builder = new Builder(this);
	    
	    onPrepareDialog(id, dialog);

	    return dialog;
	  }
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		final File externalStorageDirectory = Environment.getExternalStorageDirectory();
		final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();
		final AlertDialog.Builder builder = new Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		
		if (fileList == null) {
	      Log.e(TAG, "No files loaded");
	      dialog = builder.create();
	    }
//		builder.setView(inflater.inflate(R.layout.dialog_stored_recordings, null));
				
	    switch (id) {
	    case DIALOG_LOAD_FILE:
	      builder.setTitle("Choose the recording you wish to open:");
	      builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	          chosenFile = fileList[which].file;
	          File sel = new File(path + "/" + chosenFile);
	          if (sel.isDirectory()) {
	            firstLvl = false;

	            // Adds chosen directory to list
	            str.add(chosenFile);
	            fileList = null;
	            path = new File(sel + "");

	            loadFileList();
	            removeDialog(DIALOG_LOAD_FILE);
	            showDialog(DIALOG_LOAD_FILE);
	          }

	          // Checks if 'up' was clicked
	          else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

	            // present directory removed from list
	            String s = str.remove(str.size() - 1);

	            // path modified to exclude present directory
	            path = new File(path.toString().substring(0,
	                path.toString().lastIndexOf(s)));
	            fileList = null;

	            // if there are no more directories in the list, then
	            // its the first level
	            if (str.isEmpty()) {
	              firstLvl = true;
	            }
	            loadFileList();

	            removeDialog(DIALOG_LOAD_FILE);
	            showDialog(DIALOG_LOAD_FILE);
	            
	            Log.d(TAG, path.getAbsolutePath());

	          }
	          // File picked
	          else {
	            // Perform action with file picked
	            Log.d(TAG,"FILE EXPLORE: Chosen file is: " + chosenFile);
	            Intent intent = new Intent(PatientSessionActivity.this, DisplayStoredGraphActivity.class);
	            intent.putExtra("FILE_NAME", chosenFile);
	            intent.putExtra("PATIENT_NAME", newPatient.getPatientFirstName() + " " + newPatient.getPatientLastName());
	            startActivity(intent);
	          }

	        }
	      });
	      
	      /**
	       * Added buttons for implementing exporting and deleting functionality
	       * @author Caleb Ng (2015)
	       */
	      builder.setPositiveButton(R.string.export_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// If user clicks on the Export button, allow users to select files that they wish to export
					removeDialog(DIALOG_LOAD_FILE);
		            showDialog(DIALOG_EXPORT);
					
				}
			});
			builder.setNegativeButton(R.string.delete_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// If user clicks on the Delete button, allow users to select files that they wish to delete
					removeDialog(DIALOG_LOAD_FILE);
		            showDialog(DIALOG_DELETE);					
				}
			});
	      break;
	    case DIALOG_EMPTY:
		      builder.setTitle("Choose the recording you wish to open:");
		      builder.setMessage("No recordings were found for this patient.");
		      break;
	      /**
	       * Added functionality for Exporting recordings
	       * @author Caleb Ng (2015)
	       */
	    case DIALOG_EXPORT:
	    	builder.setTitle("Choose the recording(s) you wish to export:");
	    	builder.setMultiChoiceItems(itemToSequence(fileList), null, 
					new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							loadFileList();
							if(isChecked) {
								// If user checked this item, add this to list of files to export
								mSelectedItems.add(which);
							} else if(mSelectedItems.contains(which)) {
								// If the user selects to remove a checked item, then remove from list of files to export
								mSelectedItems.remove(Integer.valueOf(which));
							}
						}
					});
			// Set text of positive button to "Confirm"
			builder.setPositiveButton(R.string.confirm_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// On click of positive button, allow user to export selected files
					sendRecordings(mSelectedItems);
					Toast toast = Toast.makeText(getApplicationContext(), "Item(s) exported", Toast.LENGTH_SHORT);
					toast.show();					
				}
			});			
			// Set text of negative button to "Cancel"
			builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// On click of negative button, change dialog back to a single-choice list.
					removeDialog(DIALOG_EXPORT);
		            showDialog(DIALOG_LOAD_FILE);						
				}
			});			
			break;
			/**
			 * Added functionality for deleting recordings
			 * @author Caleb Ng (2015)
			 */
	    case DIALOG_DELETE:
	    	builder.setTitle("Choose the recording(s) you wish to delete:");
	    	builder.setMultiChoiceItems(itemToSequence(fileList), null, 
					new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							loadFileList();
							if(isChecked) {
								// If user checked this item, add this to list of files to export
								mSelectedItems.add(which);
							} else if(mSelectedItems.contains(which)) {
								// If the user selects to remove a checked item, then remove from list of files to export
								mSelectedItems.remove(Integer.valueOf(which));
							}
						}
			});
	    	
			// Set text of positive button to "Confirm"
			builder.setPositiveButton(R.string.confirm_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Call function to delete the selected items
					try{
						deleteRecordings(mSelectedItems);
					} catch(IOException e) {
						Log.e(TAG, "Exception removing recording from database ", e);
						Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
						toast.show();	
					}
					Toast toast = Toast.makeText(getApplicationContext(), "Successfully deleted " + mSelectedItems.size() + " items", Toast.LENGTH_SHORT);
					toast.show();	
				}
			});			
			// Set text of negative button to "Cancel"
			builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// On click of negative button, change dialog back to a single-choice list.
					removeDialog(DIALOG_DELETE);
		            showDialog(DIALOG_LOAD_FILE);						
				}
			});			
			break;
	    	
	    }
	    
	    
	    dialog = builder.show();
	}
	// End of FileExplore
	
	// File Export functions adapted from ceu.marten.services.RecordingActivity
	// @author Caleb Ng (2015)
	/**
	 * True if the selection of files is larger than 20 MB. False otherwise.
	 * @author Caleb Ng (2015)
	 */
	private boolean selectionSizeBiggerThan20Mb(ArrayList<Integer> mSelectedItems) {
		double summedSize = 0;
		for(int i=0; i<mSelectedItems.size(); i++) {
			File recordingZipFile = new File(externalStorageDirectory + Constants.APP_DIRECTORY + fileList[mSelectedItems.get(i)]);
			summedSize += (recordingZipFile.length() / 1024d) / 1024d;
		}
		if(summedSize > 20.0d)
			return true;
		else
			return false;
	}
	/**
	 * Export selected files
	 * @author Caleb Ng (2015)
	 */
	private void sendRecordings(ArrayList<Integer> mSelectedItems) {
		
		if(mSelectedItems.size() <= 0) {
			// No items selected, display a toast notification to notify user
			removeDialog(DIALOG_EXPORT);
            showDialog(DIALOG_EXPORT);
			Toast toast = Toast.makeText(getApplicationContext(), "No items selected", Toast.LENGTH_SHORT);
			toast.show();			
		}
		else {
			// Check if size of selected items exceed 20MB
			if(selectionSizeBiggerThan20Mb(mSelectedItems)) {
				// Selection size is too large, display a toast notification to notify user
				Toast toast = Toast.makeText(getApplicationContext(), "Selection size exceeds 20MB limit", Toast.LENGTH_SHORT);
				toast.show();
			}
			else {
				Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				sendIntent.setType("application/zip");
				ArrayList<Uri> uris = new ArrayList<Uri>();
				for(int i=0; i<mSelectedItems.size(); i++) {
					File recordingZipFile = new File(externalStorageDirectory + Constants.APP_DIRECTORY + fileList[mSelectedItems.get(i)]);
					// gets recording file Uri
					Uri fileUri = Uri.fromFile(recordingZipFile);
					uris.add(fileUri);
				}
				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				startActivity(Intent.createChooser(sendIntent, getString(R.string.ra_send_dialog_title)));
			}
		}		
	}
	/**
	 * Delete selected files
	 * @author Caleb Ng (2015)
	 * @throws IOException 
	 */
	private void deleteRecordings(ArrayList<Integer> mSelectedItems) throws IOException {
		if(mSelectedItems.size() <= 0) {
			// No items selected, display a toast notification to notify user			
			removeDialog(DIALOG_DELETE);
            showDialog(DIALOG_DELETE);
			Toast toast = Toast.makeText(getApplicationContext(), "No items selected", Toast.LENGTH_SHORT);
			toast.show();	
		}
		else {
			for(int i=0; i<mSelectedItems.size(); i++) {
				File recordingZipFile = new File(externalStorageDirectory + Constants.APP_DIRECTORY + fileList[mSelectedItems.get(i)]);
				// Delete file; throw an IOException if delete is unsuccessful
				if(!recordingZipFile.delete())
					throw new IOException("Failed to delete " + recordingZipFile.getName());
			}		
		}
	}
	
}
