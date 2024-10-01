/*
 * Courtney Brown (2023) slightly modified the code from the MidiBus class, from https://github.com/sparks/themidibus/ 
 * by Severin Smith c2009, LGPL 
 * 
 * The MidiBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Class: MidiBusCRCP
 * Sends midi notes to an external player/midi channel -- does not handle input, listening, etc.
 * 
 * 
 *  Description: Partial functionality of the MidiBus class, from https://github.com/sparks/themidibus/
 *  The fastest way to get the functionality given use of Maven + Git Education tech, as previously was using this processing library
 *  Only checks/uses midi outputs to make things simple -- only using the output for class projects
 * 
 */

package com.example;


import java.util.Vector;
import javax.sound.midi.*;

public class MidiBusCRCP {

	static MidiDevice.Info[] available_devices = null;
	boolean sendTimestamps = true; 
	Vector<OutputDeviceContainer> output_devices;

	/**
	 * Constructs a new MidiBus with the specified bus_name and registers the
	 * specified parent (PApplet or other) for callbacks. Opens the MIDI input and
	 * output devices specified by the names out_device_name and out_device_name. An
	 * empty String can be passed to in_device_name if no input MIDI device is to be
	 * opened, or to out_device_name if no output MIDI device is to be opened.
	 * <p>
	 * If two or more MIDI inputs have the same name, whichever appears first when
	 * {@link #list()} is called will be added, simlarly for two or more MIDI
	 * outputs with the same name. If this behavior is problematic use
	 * {@link #MidiBus(Object parent, int in_device_num, int out_device_num, String bus_name)}
	 * instead.
	 *
	 * @param in_device_name the name of the MIDI input device to be opened.
	 * @see #addInput(String device_name)
	 * @see #addOutput(int device_num)
	 */
	public MidiBusCRCP(String out_device_name) {
		init();
		addOutput(out_device_name);
	}

	public MidiBusCRCP(int out_device_num) {
		init();
		addOutput(out_device_num);
	}

	/**
	 * Perfoms the initialisation of new MidiBus objects, is private for a reason,
	 * and is only ever called within the constructors. This method exists only for
	 * the purpose of cleaner and easier to maintain code.
	 * Creates a new (hopefully/probably) unique bus_name value for new MidiBus
	 * objects that weren't given one.
	 * If two MidiBus object were to have the same name, this would be bad, but not
	 * fatal, so there's no point in spending too much time worrying about it.
	 */
	private void init() {
		output_devices = new Vector<OutputDeviceContainer>();
	}

	/**
	 * Returns the MidiDevice.Info of all the available output devices.
	 *
	 * @return the MidiDevice.Info of the available output.
	 */
	static MidiDevice.Info[] availableOutputsMidiDeviceInfo() {
		if (MidiBusCRCP.available_devices == null)
			MidiBusCRCP.available_devices = MidiSystem.getMidiDeviceInfo();
		MidiDevice device;

		Vector<MidiDevice.Info> devices_list = new Vector<MidiDevice.Info>();

		for (int i = 0; i < MidiBusCRCP.available_devices.length; i++) {
			try {
				device = MidiSystem.getMidiDevice(MidiBusCRCP.available_devices[i]);
				// This open close checks to make sure the announced device is truely available
				// There are many reports on Windows that some devices lie about their
				// availability
				// (For instance the Microsoft GS Wavetable Synth)
				// But in theory I guess this could happen on any OS, so I'll just do it all the
				// time.
				// if (!device.isOpen()) {
				// device.open();
				// device.close();
				// }
				if (device.getMaxReceivers() != 0)
					devices_list.add(MidiBusCRCP.available_devices[i]);
			} catch (MidiUnavailableException e) {
				// Device was unavailable which is fine, we only care about available output
			}
		}

		MidiDevice.Info[] devices = new MidiDevice.Info[devices_list.size()];

		devices_list.toArray(devices);

		return devices;
	}

	static public String[] availableOutputs() {
		MidiBusCRCP.available_devices = MidiSystem.getMidiDeviceInfo();
		MidiDevice.Info[] devices_info = availableOutputsMidiDeviceInfo();
		String[] devices = new String[devices_info.length];

		for (int i = 0; i < devices_info.length; i++) {
			devices[i] = devices_info[i].getName();
		}

		return devices;
	}

	static public void listDevices() {

		String[] available_outputs = availableOutputs();

		if (available_outputs.length == 0)
			return;

		if (available_outputs.length != 0) {
			System.out.println("----------Output----------");
			for (int i = 0; i < available_outputs.length; i++)
				System.out.println("[" + i + "] \"" + available_outputs[i] + "\"");
		}
	}

	/**
	 * Adds a new MIDI output device specified by the MidiDevice.Info device_info.
	 * If the MIDI output device has already been added, it will not be added again.
	 *
	 * @param device_info the MidiDevice.Info of the MIDI output device to be added.
	 * @return true if and only if the input device was successfully added.
	 */
	synchronized boolean addOutput(MidiDevice.Info device_info) {
		try {
			MidiDevice new_device = MidiSystem.getMidiDevice(device_info);

			if (new_device.getMaxReceivers() == 0) {
				System.err.println("\nThe MidiBus Warning: The chosen output device \"" + device_info.getName()
						+ "\" was not added because it is input only");
				return false;
			}

			for (OutputDeviceContainer container : output_devices) {
				if (device_info.getName().equals(container.info.getName()))
					return false;
			}

			if (!new_device.isOpen())
				new_device.open();

			OutputDeviceContainer new_container = new OutputDeviceContainer(new_device);
			new_container.receiver = new_device.getReceiver();

			output_devices.add(new_container);

			return true;
		} catch (MidiUnavailableException e) {
			System.err.println("\nThe MidiBus Warning: The chosen output device \"" + device_info.getName()
					+ "\" was not added because it is unavailable");
			return false;
		}
	}

	/**
	 * Adds a new MIDI output device specified by the name device_name. If the MIDI
	 * output device has already been added, it will not be added again.
	 * <p>
	 * If two or more MIDI outputs have the same name, whichever appears first when
	 * {@link #list()} is called will be added. If this behavior is problematic use
	 * {@link #addOutput(int device_num)} instead.
	 *
	 * @param device_name the name of the MIDI output device to be added.
	 * @return true if and only if the output device was successfully added.
	 * @see #addOutput(int device_num)
	 * @see #list()
	 */
	public boolean addOutput(String device_name) {
		if (device_name.equals(""))
			return false;

		MidiDevice.Info[] devices = availableOutputsMidiDeviceInfo();

		for (int i = 0; i < devices.length; i++) {
			if (devices[i].getName().equals(device_name))
				return addOutput(devices[i]);
		}

		System.err.println(
				"\nThe MidiBus Warning: No available input MIDI devices named: \"" + device_name + "\" were found");
		return false;
	}

	/**
	 * Adds a new MIDI output device specified by the index device_num. If the MIDI
	 * output device has already been added, it will not be added again.
	 *
	 * @param device_num the index of the MIDI output device to be added.
	 * @return true if and only if the output device was successfully added.
	 * @see #addOutput(String device_name)
	 * @see #list()
	 */
	public boolean addOutput(int device_num) {
		if (device_num == -1)
			return false;

		MidiDevice.Info[] devices = availableOutputsMidiDeviceInfo();

		if (device_num >= devices.length || device_num < 0) {
			System.err.println("\nThe MidiBus Warning: The chosen output device numbered [" + device_num
					+ "] was not added because it doesn't exist");
			return false;
		}

		return addOutput(devices[device_num]);
	}

		/* -- MIDI Out -- */
	
	/**
	 * Sends a MIDI message with an unspecified number of bytes. The first byte should be always be the status byte. If the message is a Meta message of a System Exclusive message it can have more than 3 byte, otherwise all extra bytes will be dropped.
	 *
	 * @param data the bytes of the MIDI message.
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	*/
	public void sendMessage(byte[] data) {
		if ((int)((byte)data[0] & 0xFF) == MetaMessage.META) {
				MetaMessage message = new MetaMessage();
				try {
					byte[] payload = new byte[data.length-2];
					System.arraycopy(data, 2, payload, 0, data.length-2);
					message.setMessage((int)((byte)data[1] & 0xFF), payload, data.length-2);
					sendMessage(message);
				} catch(InvalidMidiDataException e) {
					System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
				}
			} else if ((int)((byte)data[0] & 0xFF) == SysexMessage.SYSTEM_EXCLUSIVE || (int)((byte)data[0] & 0xFF) == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
				SysexMessage message = new SysexMessage();
				try {
					message.setMessage(data, data.length);
					sendMessage(message);
				} catch(InvalidMidiDataException e) {
					System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
				}
			} else {
				ShortMessage message = new ShortMessage();
				try {
					if (data.length > 2) message.setMessage((int)((byte)data[0] & 0xFF), (int)((byte)data[1] & 0xFF), (int)((byte)data[2] & 0xFF));
					else if (data.length > 1) message.setMessage((int)((byte)data[0] & 0xFF), (int)((byte)data[1] & 0xFF), 0);
					else message.setMessage((int)((byte)data[0] & 0xFF));
					sendMessage(message);
				} catch(InvalidMidiDataException e) {
					System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
				}
			}
	}
	
	/**
	 * Sends a MIDI message that takes no data bytes.
	 *
	 * @param status the status byte
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	*/
	public void sendMessage(int status) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(status);
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.out.println(e);
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
		}
	}
	
	/**
	 * Sends a MIDI message that takes only one data byte. If the message does not take data, the data byte is ignored.
	 *
	 * @param status the status byte
	 * @param data the data byte
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	*/
	public void sendMessage(int status, int data) {
		sendMessage(status, data, 0);
	}
	
	/**
	 * Sends a MIDI message that takes one or two data bytes. If the message takes only one data byte, the second data byte is ignored; if the message does not take any data bytes, both data bytes are ignored.
	 *
	 * @param status the status byte.
	 * @param data1 the first data byte.
	 * @param data2 the second data byte.
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	*/
	public void sendMessage(int status, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(status, data1, data2);
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
		}
	}
	
	/**
	 * Sends a channel message which takes up to two data bytes. If the message only takes one data byte, the second data byte is ignored; if the message does not take any data bytes, both data bytes are ignored.
	 *
	 * @param command the MIDI command represented by this message.
	 * @param channel the channel associated with the message.
	 * @param data1 the first data byte.
	 * @param data2 the second data byte.
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	*/
	public void sendMessage(int command, int channel, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(command, channel, data1, data2);
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
		}
	}
	
	/**
	 * Sends a MidiMessage object.
	 *
	 * @param message the MidiMessage.
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	*/
	public synchronized void sendMessage(MidiMessage message) {
		for (OutputDeviceContainer container : output_devices) {
			if (sendTimestamps) container.receiver.send(message, System.currentTimeMillis());
			else container.receiver.send(message, 0);
		}
	}

		/**
	 * Sends a NoteOn message to a channel with the specified pitch and velocity.
	 *
	 * @param channel the channel associated with the message.
	 * @param pitch the pitch associated with the message.
	 * @param velocity the velocity associated with the message.
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	 * 
	*/
	public void sendNoteOn(int channel, int pitch, int velocity) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(ShortMessage.NOTE_ON, constrain(channel,0,15), constrain(pitch,0,127), constrain(velocity,0,127));
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
		}
	}

		/**
	 * Sends a NoteOff message to a channel with the specified pitch and velocity.
	 *
	 * @param channel the channel associated with the message.
	 * @param pitch the pitch associated with the message.
	 * @param velocity the velocity associated with the message.
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOn(Note note)
	 * @see #sendNoteOff(Note note)
	 * @see #sendControllerChange(int channel, int number, int value)
	 * @see #sendControllerChange(ControlChange change)
	*/
	public void sendNoteOff(int channel, int pitch, int velocity) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(ShortMessage.NOTE_OFF, constrain(channel,0,15), constrain(pitch,0,127), constrain(velocity,0,127));
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
		}
	}

	/* -- Utilites -- */
	
	/**
	 * It's just convient ... move along...
	*/
	int constrain(int value, int min, int max) {
		if (value > max) value = max;
		if (value < min) value = min;
		return value;
	}

	/******** nested class as in original */
	private class OutputDeviceContainer {

		MidiDevice.Info info;

		Receiver receiver;

		OutputDeviceContainer(MidiDevice device) {
			this.info = device.getDeviceInfo();
		}

		public boolean equals(Object container) {
			if (container instanceof OutputDeviceContainer
					&& ((OutputDeviceContainer) container).info.getName().equals(this.info.getName()))
				return true;
			else
				return false;
		}

		public int hashCode() {
			return info.getName().hashCode();
		}

	}

}
