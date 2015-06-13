package com.ubc.capstonegroup70;

import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PatientClass implements Serializable{
	
	private static final long serialVersionUID = -4487071327586521666L;
	
	private static Context context;
	private String patient_name;
	private String patient_firstName = "NULL";
	private String patient_lastName = "NULL";
	private String health_number;
	private boolean gender;//true = male , false = female
	private String birth_year;
	private String birth_month;
	private String birth_day;
	
	public PatientClass() {
	}
	
	public PatientClass(Context _context) {
		PatientClass.context = _context;
	}
	
	public void setPatientName(String patient_name) {
//		this.patient_name = patient_name;
		Pattern pattern = Pattern.compile("(.+) (.+)");
		Matcher matcher = pattern.matcher(patient_name);
		if (matcher.find()) {
				if (matcher.groupCount() == 2) {	  				
					patient_firstName = matcher.group(1);
					patient_lastName = matcher.group(2);
				}
				else {
					System.out.print("ERROR: Insufficient number of matches found: " + matcher.groupCount());
					patient_firstName = "NULL";
					patient_lastName = "NULL";
				}
		}
	}
	
	public void setPatientFName(String patient_firstName) {
		this.patient_firstName = patient_firstName;
	}
	
	public void setPatientLName(String patient_lastName) {
		this.patient_lastName = patient_lastName;
	}
	
	public String getPatientName() {
//		return patient_name;
		return patient_firstName + " " + patient_lastName;
	}
	
	public String getPatientFirstName() {
		return patient_firstName;
	}
	
	public String getPatientLastName() {
		return patient_lastName;
	}
	
	public void setHealthNumber(String health_number) {
		this.health_number = health_number;
	}
	
	public String getHealthNumber(){
		return health_number;
	}
	
	public void setGender(boolean gender) {
		this.gender = gender;
	}
	
	public boolean getGender() {
		return gender;
	}
	
	public void setBirthYear(String birth_year) {
		this.birth_year = birth_year;
	}
	
	public String getBirthYear(){
		return birth_year;
	}
	
	public void setBirthMonth(String birth_month) {
		this.birth_month = birth_month;
	}
	
	public String getBirthMonth(){
		return birth_month;
	}
	
	public void setBirthDay(String birth_day) {
		this.birth_day = birth_day;
	}
	
	public String getBirthDay(){
		return birth_day;
	}
	
}
