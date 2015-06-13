#EMG SENSOR
##INSTRUCTIONS FOR USING THE APP

Minimum requirements: Android 4.0 and newer

### Developer Documentation
Please see the developer documentation found here: http://bit.ly/1GjLub3

###Install Application
Please Note: Install instructions might vary slightly depending on your device
	1.	Go to Settings > Security on your Android device
	2.	Ensure that the option to install from unknown sources is checked (Title of field is "Unknown Sources")
	3.	Connect Android device to computer and transfer APK file to device storage
	4.	Using a file manager app on the device, navigate to the directory where you placed the APK file
	5.	Click on the APK file to install

###Set Up a Bluetooth Connection
	1.	Upon first use, you will need to pair the hardware and software 
	2.	Ensure the hardware is powered on and the STATUS light is pulsing slowly
	3.	Go to Bluetooth settings menu of the Android smartphone or tablet (typically found under general settings menu).
	4.	Enable Bluetooth 
	5.	Select “bitalino” device from the list of devices available for pairing. If a list does not appear, try refreshing or searching for devices. 
	6.	When prompted to enter a PIN or password, enter 1234
	7.	This should successfully complete the pairing 

###Start Recording w/ Patient:
	1.	First ensure Bluetooth is enabled on your device, and that it is paired with the Bitalino device. 
	2.	Open the application and select the ‘Bluetooth’ button. From here, select the Bitalino device. If the device has not been already paired, the application will re-direct you to set up a Bluetooth connection through the smartphone or tablet’s Bluetooth settings menu. 
	3.	Select Patient. If there are no patients, the application will prompt you to enter a new patient as follows:
		*	Enter Patient Name, Provincial Health # (Ensure these first two are correct as you cannot change these two later), Gender, & Birthday
	4.	If there are patients, scroll through and select the patient you want. You can also delete and create new patients via their respective buttons
	5.	Once a patient has been selected or created, you may now select ‘Start Session’
	6.	From this page, you can select ‘New Recordings’, ‘Saved Recordings’, ‘Edit Patient Information’. In this case, to start a Recording, we will select ‘New Recording’	
	7.	Once this has been selected a Recording Session will open for the selected patient. Press ‘Start Recording’ to begin the recording process
	8.	Pending the Bitalino being properly connected and turned on, the recording process will begin and the white STATUS light will pulse fast. To end and save the recording press stop recording. The recording will the be saved to Saved Recordings

###View Saved Recording:
	1.	Select Patient.
	2.	Once a patient has been selected or created, you may now select ‘Start Session’
	3.	From this page, you can select ‘New Recordings’, ‘Saved Recordings’, ‘Edit Patient Information’. In this case, we will select ‘Saved Recordings’
	4.	A list of all the saved recordings under this patient will show up. 
		The file naming convention is as follows:
			Patient Initials – Provincial Health Number – Birthday (YYYY-MM-DD)__Start date of recording (YYYY-MM-DD).Start time of recording 24h convention (hh.mm.ss.)
			For example: “JS-0123456789-1999-01-01__2015-03-29.19.43.26.”
			You can scroll through and select individual recordings. Select a recording to open it
	5.	Once a recording has been opened you can scroll, zoom, and navigate through the recording
	6.	Additionally, you have the option to view the recording in it’s original form, FFT, or Power Spectrum. The FFT and Power Spectrum show the frequency spectrum 
	7.	To return, select the back button

###Export/Delete Saved Recording:
	1.	Select Patient
	2.	Once a patient has been selected or created, you may now select ‘Start Session’
	3.	From this page, you can select ‘New Recordings’, ‘Saved Recordings’, ‘Edit Patient Information’. In this case, we will select ‘Saved Recordings’
	4.	A list of all the saved recordings under this patient will show up
	5.	To Delete a recording, first select ‘Delete’ and then select the recording you would like to delete
	6.	To Export a recording, first select ‘Export’ and then select the recording you would like to Export. A prompt will pop up asking how you would like to Export the recording
	7.	To return, select the back button
	

##LICENSES
**Bitadroid**
>	Copyright (C) 2014 David G Marquez, Carlos Marten, Abraham Otero

>	Licensed under the GNU General Public License, version 2

>	This program is free software; you can redistribute it and/or
>	modify it under the terms of the GNU General Public License
>	as published by the Free Software Foundation; either version 2
>	of the License, or (at your option) any later version.

>	This program is distributed in the hope that it will be useful,
>	but WITHOUT ANY WARRANTY; without even the implied warranty of
>	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
>	GNU General Public License for more details.

>	You should have received a copy of the GNU General Public License
>	along with this program; if not, write to the Free Software
>	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

* Modifications:
** Changed user workflow
** Added offline graphing and signal processing capability
** Added ability to save and store data for specific patients
** See code for full details
* Source: https://github.com/DavidGMarquez/Bitadroid


**Android File Explore**
>   Copyright 2011 Manish Burman

>  Licensed under the Apache License, Version 2.0 (the "License");
>  you may not use this file except in compliance with the License.
>  You may obtain a copy of the License at

>      http://www.apache.org/licenses/LICENSE-2.0

>   Unless required by applicable law or agreed to in writing, software
>   distributed under the License is distributed on an "AS IS" BASIS,
>   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>   See the License for the specific language governing permissions and
>   limitations under the License.

* Modifications:
** Added functions for Exporting and Deleting file items
** See code in com.ubc.capstonegroup70.PatientSessionActivity for full details
* Source code: https://github.com/mburman/Android-File-Explore


##LIBRARIES
**GraphView**
>	Licensed under the GNU Lesser General Public License
* Source: https://github.com/jjoe64/GraphView


**JTransforms** 
> Licensed under the BSD 2-Clause License
* Source: https://sites.google.com/site/piotrwendykier/software/jtransforms

**Apache Commons Mathematics Library**
> Licensed under the Apache License, Version 2.0
* Source: http://commons.apache.org/proper/commons-math/index.html

