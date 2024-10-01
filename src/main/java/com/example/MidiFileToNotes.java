
/*
 * c2017 Courtney Brown 
 * 
 * Class: MidiFileToNotes
 * Description: Uses JMusic to get notes & info from a midi file, revised 2024 to include harmony.
 * 
 * 
 */

 package com.example;

 import java.util.*;
 import jm.music.data.*;
 import jm.util.*;
 
 public class MidiFileToNotes {
	 String filename;
	 ArrayList<Integer> pitches; //the midi values of the notes
	 ArrayList<Double> rhythms; //the rhythmic values (in jm notation)
	 ArrayList<Double> startTimes; //start times 
 
	 int whichLine = 0; //which instrument, basically -- but "line of music" or midi channel of music to be the most precise
	 boolean setSingleLine = false;
 
	 ArrayList<jm.music.data.Note> melody; //list of notes in jm form --> melody + rhythm
 
	 //input for the constructor is the midi file that we are reading
	 //f - is the filename of the midi file
	 MidiFileToNotes(String f) {
		 filename = f;
		 processPitchesAsTokens();
	 }
 
	 //set which line or instrument of the midi file
	 //line - which line  - 0 is the first line
	 void setWhichLine(int line) {
		 whichLine = line;
	 }

	 //only read in one part of the score, the one indicated
	 void setSingleLine(int index)
	 {
		whichLine = index;
		setSingleLine = true;
	 }

	 //read in all parts of the score
	 void setToAllParts()
	 {
		setSingleLine = false;
	 }

 
	 //extract the midi data from jm.music format into separate arrays of pitch and melody and startTimes
	 void processPitchesAsTokens() {

		//the pitches, melody, rhythms, and start times of all the notes
		 pitches = new ArrayList<Integer>();
		 melody = new ArrayList<jm.music.data.Note>();
		 rhythms = new ArrayList<Double>();
		 startTimes = new ArrayList<Double>();
 
		 //score names for JMusic
		 String scoreName = "score_" + filename;
		 Score theScore = new Score(scoreName);
 
		 // read the midi file into a score
		 Read.midi(theScore, filename);
 
		 // extract the melody and all its parts, unless requested a single line
		 int partStart = 0;
		 int partEnd = theScore.getPartArray().length;
		 if(setSingleLine)
		 {
			partStart = whichLine-1; 
			partEnd = whichLine;
		 }

		 //process the score here
		 for(int whichPart=partStart; whichPart<partEnd; whichPart++)
		 {
			Part part = theScore.getPart(whichPart);
			Phrase[] phrases = part.getPhraseArray();
	
			// extract all the pitches and notes from the melody & put them in MIDI style-note-ish ordering using an insertion-style sort
			for (int i = 0; i < phrases.length; i++) {
				jm.music.data.Note[] notes = phrases[i].getNoteArray();
				for (int j = 0; j < notes.length; j++) {
					
					int placeToAdd =0;
					boolean added = false;
					while ( placeToAdd < startTimes.size() && !added )
					{
						added = phrases[i].getNoteStartTime(j) < startTimes.get(placeToAdd);
						placeToAdd++;
					}

					if( added ) placeToAdd--;

					pitches.add(placeToAdd, notes[j].getPitch());
					rhythms.add(placeToAdd, notes[j].getDuration() );
					startTimes.add(placeToAdd, phrases[i].getNoteStartTime(j));
					melody.add(placeToAdd, notes[j]);
				}
			}
		}
	 }
 
	 public Integer[] getPitches() {
		 return pitches.toArray(new Integer[pitches.size()]);
	 }
 
	 public ArrayList<Integer> getPitchArray() {
		 return pitches;
	 }
 
	 public ArrayList<Double> getRhythmArray() {
		 return rhythms;
	 }

	 public ArrayList<Double> getStartTimeArray() {
		return startTimes;
	}
 
	 public ArrayList<jm.music.data.Note> getMelody() {
		 return melody;
	 }
 
	 public Double[] getRhythms() {
		 return rhythms.toArray(new Double[rhythms.size()]);
	 }
 
 }