/*
   Copyright 2010 Shahriyar Amini

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package edu.cmu.ece.cache.framework.serialization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.content.Context;

public class Serializer {
	private Context mContext;
	
	public Serializer(Context c)
	{
		mContext = c;
	}
	
	// returns true if serialization successful
	public boolean serialize(String filename, Serializable s)
	{
		if (mContext == null) return false;
		
		// no need to share the output stream w/ other apps
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try {
			fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(s);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public Object deserialize(String filename)
	{
		if (mContext == null) return null;
		
		FileInputStream fis = null;
		ObjectInputStream oin = null;
		
		try {
			fis = mContext.openFileInput(filename);
			oin = new ObjectInputStream(fis);
			Object o = oin.readObject();
			return o;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
