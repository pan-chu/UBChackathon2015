package com.ubc.capstonegroup70;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import org.apache.commons.math3.complex.Complex;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;
import ceu.marten.ui.NewRecordingActivity;

//import com.example.bluetoothnew.R;

/** 
 * Reads the data stored in a target recordings text file and plots
 * the data onto a graph.
 * 
 * @author Caleb Ng
 */

public class DisplayStoredGraphActivity extends Activity {
    // Tag for logging errors
	private static final String DSGA_TAG = DisplayStoredGraphActivity.class.getName();
	// Progress Dialog Object
    private ProgressDialog prgDialog;
    // Progress Dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    
	private final Handler mHandler = new Handler();
	private Vector<Double> dataSetRAW = new Vector<Double>();
	private Vector<Double> dataSetFFT = new Vector<Double>();
	private Vector<Double> dataSetPWR = new Vector<Double>();

	public static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	public String recordingName = "EMG_DATA";
	public String recordingTimePattern = ".{168}(\\d+).(\\d+).(\\d\\d)(\\d+).(\\d+).(\\d\\d)";
	private String graphTitle = new String();
	private static final String TAG = NewRecordingActivity.class.getName();
	private GraphViewSeries rawSeries;
	private GraphViewSeries fftSeries;
	private GraphViewSeries pwrSeries;
	int setSize = 0;
	int max=0;
	int min=0;	
	public static double [] buffer;
	Context context;
	LinearLayout layout;
	// Data for determining the appropriate scale for the x-axis
	private int samplingFrequency = 1000;
	// Data for determining the appropriate time stamp
	private String startDate = "Jan 1, 2000";
	private int startHour = 00;
	private int startMinute = 00;
	private int startSecond = 00;
	private String endDate = "Jan 1, 2000";
	private int endHour = 00;
	private int endMinute = 00;
	private int endSecond = 00;
	private String PeridOfDay = "AM";
	private int sampleLength = 0;
	private String recordingDate;
	private String recordingTime;

  
  	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Initiating creation of DisplayStoredGraphActivity class");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			recordingName = extras.getString("FILE_NAME");
			graphTitle = "Recording for " + extras.getString("PATIENT_NAME");
		}
		else
			System.out.println("Unable to retrieve FILE_NAME");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_stored_graph_activity);
		prgDialog = new ProgressDialog(this);
				
		new ReadFileService().execute();
		
  }
  	
  	/*
  	 * Responds to changes in the radio button selection
  	 * Choice of radio button selection will determine which data set to plot:
  	 * 		1) dataSetRAW - the raw, unprocessed EMG signal
  	 * 		2) dataSetFFT_real - the EMG signal after FFT has been performed on it
  	 */
  	public void onRadioButtonClicked(View view) {
  		// Is the button checked?
  		boolean checked = ((RadioButton) view).isChecked();
  		final RadioButton rawData = (RadioButton) findViewById(R.id.rawGraphBtn);
  		final RadioButton fftData = (RadioButton) findViewById(R.id.fftGraphBtn);
  		final RadioButton pwrData = (RadioButton) findViewById(R.id.pwrGraphBtn);
		final RadioButton showVideoData = (RadioButton) findViewById(R.id.showVideoBtn);
  		
  		// Check which button was clicked
  		switch(view.getId()) {
	  		case R.id.rawGraphBtn:
	  			if (checked) {
	  				// Original signal option selected
	  				rawData.setClickable(false);
	  				fftData.setClickable(true);
	  				pwrData.setClickable(true);
					showVideoData.setClickable(true);

					graphData(dataSetRAW,100);
	  			}
	  			break;
			case R.id.showVideoBtn:
				if (checked) {
					// With Video option selected
					showVideoData.setClickable(false);
					rawData.setClickable(true);
					fftData.setClickable(true);
					pwrData.setClickable(true);

					graphData(dataSetRAW,100, true);
				}
				break;
	  		case R.id.fftGraphBtn:
	  			if (checked) { 
	  				// Frequency spectrum option selected
	  				rawData.setClickable(true);
	  				fftData.setClickable(false);
	  				pwrData.setClickable(true);
					showVideoData.setClickable(true);

					//If FFT has not already been calculated, do so now
	  				if(dataSetFFT.size() == 0 || dataSetPWR.size() == 0) {
	  					// Perform FFT to compute graph series
	  					prgDialog = new ProgressDialog(this);		
	  					new CalculateFFT().execute(true);
	  				}
	  				else
	  					graphData(dataSetFFT,dataSetFFT.size());
	  			}
	  			break;
	  		case R.id.pwrGraphBtn:
	  			if (checked) {
	  				// Power spectrum option selected
	  				rawData.setClickable(true);
	  				fftData.setClickable(true);
	  				pwrData.setClickable(false);
					showVideoData.setClickable(true);

					//If FFT has not already been calculated, do so now
	  				if(dataSetFFT.size() == 0 || dataSetPWR.size() == 0) {
	  					// Perform FFT to compute graph series
	  					prgDialog = new ProgressDialog(this);		
	  					new CalculateFFT().execute(false);
	  				}
	  				else
	  					graphData(dataSetPWR,dataSetPWR.size());
	  			}
	  			break;
  		}
  	}
  

	  /*
	   * Calculates the range (min and max) of values of the dataSet vector
	   * Parameters:	none
	   * Outputs:	Double[2]; Double[0] = min, Double[1] = max
	   */
	private double[] calculateRange(Vector<Double> dataSet) {
		double dataRange[] = {0, 0}; //{y-min, y-max}
		Object dataMin = Collections.min(dataSet);
		Object dataMax = Collections.max(dataSet);
		dataRange[0] = (double) dataMin;
		dataRange[1] = (double) dataMax;    	
		return dataRange;
	}

	private void graphData(final Vector<Double> dataSet, int viewPort) {
		graphData(dataSet, viewPort, false);
	}

	private void graphData(final Vector<Double> dataSet, int viewPort, boolean withVideo) {
		System.out.println("Defining data set.");

		// Determine the appropriate graphSeries to add depending on dataSet that was passed
		GraphViewSeries graphSeries;
		TextView yAxisString = (TextView) findViewById(R.id.graph_yAxis);
		TextView xAxisString = (TextView) findViewById(R.id.graph_xAxis);
		if( dataSet == dataSetFFT ) {
			graphSeries = fftSeries;
			yAxisString.setText("Voltage\n(mV)");
			xAxisString.setText("Frequency (Hz)");
		}
		else if (dataSet == dataSetPWR ) {
			graphSeries = pwrSeries;
			yAxisString.setText("Power  \n(dB/Hz)");
			xAxisString.setText("Frequency (Hz)");
		}
		else {
			graphSeries = rawSeries;
			yAxisString.setText("Voltage\n(mV)");
			xAxisString.setText("Time (Hour:Minute:Second)");
		}
		// Format graph labels to show the appropriate domain on x-axis
		GraphView graphView = new LineGraphView(this, graphTitle) {
			protected String formatLabel(double value, boolean isValueX) {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
				String time = df.format(c.getTime());
				if (isValueX) {
					long xValue;
					// unecessary because we are using Calendar now
					/*
					if (value < 0.000){
						xValue = 0;
						return "00:00:00";
					} */
					xValue = (long) value;
					if(dataSet == dataSetFFT || dataSet == dataSetPWR) {
						// Set x-axis to use the frequency domain
						return String.format("%d",(int) (xValue * samplingFrequency /dataSetRAW.size()));
					}
					else {
						// Set the x-axis to use the time domain
						// return String.format("%02d:%02d:%02d",(int) ((xValue / (samplingFrequency*60*60)) % 24), (int) ((xValue / (samplingFrequency*60)) % 60), (int) ((xValue / samplingFrequency)) % 60);
		return time;
								}

				} else {
					if(dataSet == dataSetFFT || dataSet == dataSetPWR) {
						return String.format("%d", (int) value);
					}
					else
						return String.format("%.2f", (double) value);
				}
			}
		};

		int yInterval = calculateYScale(dataSet);
		int yLabel = max;
		while ((yLabel-min) % yInterval != 0) {
			yLabel++;
		}

		// Calculate appropriate interval value in x-direction
		int xInterval = calculateXScale(dataSet);
		int xLabel = dataSet.size();
		while (xLabel % xInterval != 0) {
			xLabel++;
		}

		graphView.addSeries(graphSeries);
		((LineGraphView) graphView).setDrawBackground(false);

		// Settings for the graph to be scrollable and scalable
		graphView.setScalable(!withVideo);
		graphView.setScrollable(!withVideo);
		// Settings for graph view port size
		if (dataSet.size() < viewPort || withVideo)
			graphView.setViewPort(0,dataSet.size());
		else
			graphView.setViewPort(0, viewPort);
//	  graphView
		// Settings for the graph styling
		graphView.setManualYAxisBounds(yLabel, min);
		graphView.getGraphViewStyle().setGridColor(Color.BLACK);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(80);

		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, withVideo ? 0.5f : 1.0f);

		if (withVideo)
			param.bottomMargin = 20;

		graphView.setLayoutParams(param);

		LinearLayout layout = (LinearLayout) findViewById(R.id.dataGraph);

		layout.removeAllViews();


		final SeekBar seekBar = new SeekBar(this, null, android.R.attr.progressBarStyleHorizontal);
		LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		seekBarParams.leftMargin = graphView.getGraphViewStyle().getVerticalLabelsWidth();

		seekBar.setLayoutParams(seekBarParams);

		File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		String videoPath = "";

		// TODO currently we're just grabbing the first video that we find - should be able to just use recordingName.substring(0, recordingName.lastIndexOf('.')
		for (File file : dcimDir.listFiles())
		{
			final String videoFilePath = file.getPath();
			final String videoFileName = file.getName();
			if (videoFilePath.endsWith(".mp4") && ((videoFileName.substring(0, videoFileName.lastIndexOf('.'))).equalsIgnoreCase(recordingName.substring(0, recordingName.lastIndexOf('.'))))) {
				videoPath = file.getPath();
				break;
			}
		}

		// if withVideo then add videoView
		if (withVideo && !videoPath.isEmpty()) {

			final RelativeLayout relativeLayout = new RelativeLayout(this);
			LinearLayout.LayoutParams rlayoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, 0.5f);
			relativeLayout.setLayoutParams(rlayoutParams);

			final VideoView videoView = new VideoView(this);

			RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			videoParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			videoParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			videoParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			videoView.setLayoutParams(videoParams);


			videoView.setVideoPath(videoPath);

			MediaPlayer mp = MediaPlayer.create(this, Uri.parse(videoPath));
			final int totalDuration = mp.getDuration();
			mp.release();

			seekBar.setMax(totalDuration);


			final List<Boolean> videoFinished = new ArrayList<>();
			videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					videoFinished.add(true);
					seekBar.setProgress(seekBar.getMax());
				}
			});

			videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(final MediaPlayer mp) {
					if (seekBar.getMax() != mp.getDuration())
					{
						seekBar.setMax(mp.getDuration());
					}
				}
			});

			final Runnable videoProgressChecker = new Runnable() {
				@Override
				public void run() {
					while (videoFinished.size() < 1)
					{
						seekBar.setProgress(videoView.getCurrentPosition());
					}
				}
			};

			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (fromUser)
					{
						videoView.seekTo(progress);
						if (!videoView.isPlaying())
						{
							videoView.start();
							videoFinished.clear();
							new Thread(videoProgressChecker).start();
						}
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// auto generated
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// auto generated
				}
			});

			new Thread(videoProgressChecker).start();

			layout.addView(videoView);
			layout.addView(graphView);
			layout.addView(seekBar);

			videoView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (videoView.isPlaying())
					{
						videoView.pause();
					}
					else
					{
						videoView.start();
					}
					return true;
				}
			});

		}
		else
		{
			layout.addView(graphView);
		}

	}



	private int calculateYScale(Vector<Double> dataSet) {
		// Calculate range of y values
		double yBounds[] = {0,0};
		yBounds = calculateRange(dataSet);
		// Calculate the appropriate max value and min values in y-direction 
		max = (int) yBounds[1] + 1;
		if (yBounds[0] >= 0) {    
		min = (int) yBounds[0];
		}
		else {
			min = (int) yBounds[0] - 1;
		}		
		// Calculate interval level of labels in y-direction
		int yInterval;
//		if ((max-min) <= 5) {
//			yInterval = 0.5;
//		}
//		else 
			if ((max-min) <= 10) {
			yInterval = 1;
		}
		else if ((max-min) <= 50) {
			yInterval = 2;
		}
		else if ((max-min) <=100) {
			yInterval = 5;
		}
		else if ((max-min) <= 200){
			yInterval = 10;
		}
		else {
			yInterval = 25;
		}
		return yInterval;
	}
	  
	private int calculateXScale(Vector<Double> dataSet) {
		int xInterval;
		int xMax = dataSet.size();
		if (xMax <= 10) {
			xInterval = 1;
		}
		else if (xMax <= 50) {
			xInterval = 5;
		}
		else if (xMax <=100) {
			xInterval = 10;
		}
		else {
			xInterval = 20;
		}
		return xInterval;
	}
	
	/*
	 * Calculates the mean value (average) of a given dataSet vector and subtracts the mean from the original dataset
	 * Parameters:		Vector<Double> - Vector of type double containing the values from which the mean will be calculated
	 * Outputs:			Vector<Double> - Value of the original vector with the mean value subtracted
	 */
	private Vector<Double> removeMean(Vector<Double> dataSet) {
		double mean = 0;
		for(int i=0; i<dataSet.size(); i++) {
			mean += dataSet.elementAt(i);
		}
		mean = mean/dataSet.size();
		
		for(int i=0; i<dataSet.size(); i++) {
			dataSet.set(i, dataSet.elementAt(i)-mean);
		}
		return dataSet;
	}
	
	
	/**
	 * Destroys activity
	 */
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	/**
	 * When user presses on the UP button in Action Bar, simulate a back button press to return to parent activity
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Asynchronous Task for reading the data points within the data file
	 * @author Caleb Ng (2015)
	 * //<Params, Progress, Result>
	 */
	class ReadFileService extends AsyncTask<Void, String, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... args) {		
			Scanner strings = null;
			InputStream stream = null;
			BufferedInputStream bstream = null; 
			ZipFile zipFile = null;
			try {
				System.out.println(externalStorageDirectory + Constants.APP_DIRECTORY + recordingName + Constants.ZIP_FILE_EXTENTION);
		  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, recordingName);
		  		zipFile = new ZipFile(file);
		  		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		  				  		
		  		while (entries.hasMoreElements()) {
		  			ZipEntry zipEntry = entries.nextElement();
		  			stream = zipFile.getInputStream(zipEntry);
		  			strings = new Scanner(stream);			  			
		  			
		  			// Determine the start date and time from the header text
		  			System.out.println("Extracting recording start date and time.");
		  			Pattern pattern = Pattern.compile(".{184}");
		  			String extracted = strings.findInLine(pattern);
		  			pattern = Pattern.compile(recordingTimePattern);
		  			Matcher matcher = pattern.matcher(extracted);
		  			if (matcher.find()) {
		  				if (matcher.groupCount() == 6) {
			  				String Day = matcher.group(1);
			  				String Month = matcher.group(2);
			  				String Year = matcher.group(3);
			  				String Hour = matcher.group(4);
			  				String Minute = matcher.group(5);
			  				String Second = matcher.group(6);
			  				recordingDate = Month + "/" + Day + "/20" + Year;
			  				recordingTime = Hour + ":" + Minute + ":" + Second;
			  				graphTitle += " on " + recordingDate + " at " + recordingTime;

		  				}			  				
		  				else
		  					Log.e(DSGA_TAG, "ERROR: Insufficient number of matches found: " + matcher.groupCount());
		  			}
		  			
		  			// Use new line character as a delimiter for file data
		  			strings.nextLine();
		  			strings.useDelimiter("\n *");
		  		}
			}
			catch (FileNotFoundException error) {
				System.out.println("@IOERROR: " + error);
				return false;
			}
			catch (IOException error) {
				System.out.println("@IOERROR: " + error);
				return false;
			}
			// Loops for as long as there are more data points to be read from the text file
			while (strings.hasNext())
			{
				double dataPoint = Double.parseDouble(strings.next());
				dataSetRAW.add(dataPoint);
			}
			System.out.println("Closing strings.");
			try {
//					bstream.close();
				stream.close();
				zipFile.close();
//					zipInput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	strings.close();			
			return true;
		}
		
		protected void onProgressUpdate(String...progress) {
			//called when the background task makes any progress
		}

		protected void onPreExecute() {
			//called before doInBackground() is started
			super.onPreExecute();
			// Show Progress Bar Dialog before calling doInBackground method
//				showDialog(progress_bar_type);
			prgDialog.setTitle("Opening File");
			prgDialog.setMessage("Opening " + recordingName + "\nPlease wait...");
			prgDialog.setCancelable(false);
			prgDialog.show();
			return;
		}
		
		protected void onPostExecute(Boolean readFileSuccess) {
			//called after doInBackground() has finished 
			// Check if the file was read successfully. If not, output error message and generate sample set of data
			if(!readFileSuccess) {
				
				Random randomGenerator = new Random();			
				System.out.println("@IOERROR: Unable to read from file. Creating random dataset");
				for(int i=0; i<100; i++)
			    {
					dataSetRAW.add(randomGenerator.nextDouble());
			    }
			}

			// Prepare data set for graphing
			dataSetRAW = removeMean(dataSetRAW);
			rawSeries = new GraphViewSeries(new GraphViewData[] {
	        });
			System.out.println("DSGA-TAG: Number of samples read is " + dataSetRAW.size());
			for (int i=0; i<dataSetRAW.size(); i++) {
			  	double pointX = i;
			  	double pointY = dataSetRAW.get(i);
			  	rawSeries.appendData(new GraphViewData(pointX, pointY), true, dataSetRAW.size());
			}
			graphData(dataSetRAW,100);
			prgDialog.dismiss();
			prgDialog = null;
			return;
		}
	}
		
	/**
	 * Asynchronous Task for calculating the Fourier Transform of the data set
	 * Will compute the Frequency spectrum as well as the Power spectrum 
	 * @author Caleb Ng (2015)
	 * //<Params, Progress, Result>
	 */
	class CalculateFFT extends AsyncTask<Boolean, String, postProcessParams> {
		@Override
		protected postProcessParams doInBackground(Boolean... args) {	
			// If Boolean = true, plot the Frequency spectrum
			// If Boolean = false, plot the Power spectrum
			Boolean freqSpectrum = args[0];
			int numSamples = dataSetRAW.size();
	  		double[] datapoints = new double[numSamples*2];
	  		int[] xIndex = new int[numSamples];
	  		for(int i=0; i<numSamples; i++) {
	  			datapoints[i] = (double) dataSetRAW.get(i);
	  			xIndex[i] = i;
	  		}
	  		DoubleFFT_1D fft = new DoubleFFT_1D(numSamples);
	  		fft.realForwardFull(datapoints);

	  		fftSeries = new GraphViewSeries(new GraphViewData[] {});
	  		pwrSeries = new GraphViewSeries(new GraphViewData[] {});
	  		for(int i=0; i<numSamples/2+1; i++) {
	  			Complex c = new Complex(datapoints[2*i], datapoints[(2*i)+1]);
	  			// Take magnitude of FFT
	  			double pointY = (double) c.abs();
	  			dataSetFFT.add(pointY);
	  			fftSeries.appendData(new GraphViewData(i,pointY), true, datapoints.length/2);
	  			// Square the magnitude of FFT and normalize values by 2/(sampling Frequency * number of samples in original signal)
	  			double pointY_pwr = (double) 2*Math.pow(pointY, 2)/(samplingFrequency * numSamples);
	  			// Convert to decibel scale
				pointY_pwr = (double) 10*Math.log10(pointY_pwr);
	  			dataSetPWR.add(pointY_pwr);
	  			pwrSeries.appendData(new GraphViewData(i,pointY_pwr), true, datapoints.length/2);
	  			
	  		}
	  		
			postProcessParams returnParams = new postProcessParams();
			returnParams.readFilesSuccess = true;
			returnParams.freqSpectrum = freqSpectrum;
			return returnParams;
		}
		
		protected void onProgressUpdate(String...progress) {
			//called when the background task makes any progress
		}

		protected void onPreExecute() {
			//called before doInBackground() is started
			super.onPreExecute();
			// Show Progress Bar Dialog before calling doInBackground method
			prgDialog.setTitle("Processing Data");
			prgDialog.setMessage("Calculating the Fourier Transform\nPlease wait...");
			prgDialog.show();
			return;
		}
		
		protected void onPostExecute(postProcessParams returnParams) {
			//called after doInBackground() has finished 
			if(!returnParams.readFilesSuccess) {
				Log.e(TAG,"Unable to process data from file.");
				return;
			}
			else {
				if(returnParams.freqSpectrum)
					graphData(dataSetFFT, dataSetFFT.size());
				else
					graphData(dataSetPWR, dataSetPWR.size());
				prgDialog.dismiss();
				prgDialog = null;
				return;
			}				
		}
	}
	
	private class postProcessParams {
		public boolean readFilesSuccess;
		public boolean freqSpectrum;
	}
}

