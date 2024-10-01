/*
 * c2017-2024 Courtney Brown 
 * Class: Main Class for Hello World for CC3 Class Projects streaming MIDI, etc.
 * Description: Demonstration of MIDI file manipulations, etc. & 'MelodyPlayer' sequencer, 2024 - add processing/interactivity
 * 
 */

 package com.example;

 //importing the JMusic stuff
 import jm.music.data.*;
 import jm.util.*;
 
 //import FileSystem for cross-platform file referencing
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 
 //Processing
 import processing.core.*;
 
 //make sure this class name matches your file name, if not fix.
 public class App extends PApplet {
 
	 static MelodyPlayer player; //play a midi sequence
	 static MidiFileToNotes midiNotes; //read a midi file
	 static int noteCount = 0; //index into the array of notes to send to the MIDI file.
	 
	 //make cross-platform
	 static FileSystem sys = FileSystems.getDefault();
 
	 //the getSeperator() creates the appropriate back or forward slash based on the OS in which it is running -- OS X & Windows use same code :) 
	 static String filePath = "mid"  + sys.getSeparator() +  "MaryHadALittleLamb.mid"; // path to the midi file -- you can change this to your file
																 // location/name
 
	 public static void main(String[] args) {
		 PApplet.main("com.example.App");		
	 }
 
	 public void settings()
	 {
		 size(500, 500);
		 midiSetup(filePath);
 
 
		 //uncomment to debug your midi file 
		 //this code MUST be commited when submitting the project -- it is only for testing MIDI file access.
		 //playMidiFileDebugTest(filePath);
	 }
 
	 //doing all the setup stuff for the midi and also make the background black
	 public void setup() {
 
		 background(0);
		 player.reset();
	 }
 
	 //play the melody in real-time
	 public void draw()
	 {
		 playMelody(); 
	 }
 
	 //plays the midi file using the player -- so sends the midi to an external synth such as Kontakt or a DAW like Ableton or Logic
	 static public void playMelody() {
 
		 //NOTE: for assert() to work, you need to change the Java extension settings to run with assertions enabled
		 assert(player != null); //this will throw an error if player is null -- eg. if you haven't called setup() first
		 player.play(); //play each note in the sequence -- the player will determine whether is time for a note onset
		 
	 }
 
	 //opens the midi file, extracts a voice, then initializes a melody player with that midi voice (e.g. the melody)
	  //filePath -- the name of the midi file to play
	 static void midiSetup(String filePath) {
 
		 //Change the bus to the relevant port -- if you have named it something different OR you are using Windows
		 player = new MelodyPlayer(100, "Bus 1"); //sets up the player with your bus. 
		 //player.listDevices(); //prints available midi devices to the console -- find your device
 
		 midiNotes = new MidiFileToNotes(filePath); // creates a new MidiFileToNotes -- reminder -- ALL objects in Java
													 // must
													 // be created with "new". Note how every object is a pointer or
													 // reference. Every. single. one.
 
		 // // which line to read in --> this object only reads one line (or ie, voice or
		 // ie, one instrument)'s worth of data from the file
		 midiNotes.setWhichLine(0); // this assumes the melody is midi channel 0 -- this is usually but not ALWAYS
									 // the case, so you can try other channels as well, if 0 is not working out for
									 // you.
 
		 noteCount = midiNotes.getPitchArray().size(); //get the number of notes in the midi file
 
		 //NOTE: for assert() to work, you need to change the Java extension settings to run with assertions enabled
		 assert(noteCount > 0); // make sure it got some notes (throw an error to alert you, the coder, if not)
 
 
		 //sets the player to the melody to play the voice grabbed from the midi file above
		player.setMelody(midiNotes.getPitchArray());
		player.setRhythm(midiNotes.getRhythmArray());
		player.setStartTimes(midiNotes.getStartTimeArray());
 
	 }
 
	 //start the melody at the beginning again when a key is pressed
	 public void keyPressed() {
		 player.reset();
 
	 }
 
	 //this function is not currently called. you may call this from setup() if you want to test
	 //this just plays the midi file -- all of it via your software synth. You will not use this function in upcoming projects
	 //but it could be a good debug tool.
	 //filename -- the name of the midi file to play
	 void playMidiFileDebugTest(String filename) {
		 Score theScore = new Score("Temporary score");
		 Read.midi(theScore, filename);
		 Play.midi(theScore);
	 }
 }