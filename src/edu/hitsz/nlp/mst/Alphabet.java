/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
*/

package edu.hitsz.nlp.mst;

import java.util.ArrayList;
import java.io.*;
import java.util.Iterator;

/**
 * 将字符串转换为数字，存储到map
 * @author tm
 *
 */
public class Alphabet implements Serializable
{
 gnu.trove.TObjectIntHashMap map;
 int numEntries;
 boolean growthStopped = false;

 public Alphabet (int capacity)
 {
	this.map = new gnu.trove.TObjectIntHashMap (capacity);
	//this.map.setDefaultValue(-1);

	numEntries = 0;
 }

 public Alphabet ()
 {
	this (10000);
 }


 /**
  * 查找对象，返回对应的数字;如果没有，并且没有停止增长，则添加并返回；如果停止了，则返回－1
  * Return -1 if entry isn't present.
  */
 public int lookupIndex (Object entry)
 {
	if (entry == null) {
	    throw new IllegalArgumentException ("Can't lookup \"null\" in an Alphabet.");
	}

	int ret = map.get(entry);

	if (ret == -1 && !growthStopped) {
	    ret = numEntries;
	    map.put (entry, ret);
	    numEntries++;
	}

	return ret;
 }

 public Object[] toArray () {
	return map.keys();
 }

 public boolean contains (Object entry)
 {
	return map.contains (entry);
 }

 public int size ()
 {
	return numEntries;
 }

 public void stopGrowth ()
 {
	growthStopped = true;
	map.compact();
 }

 public void allowGrowth ()
 {
	growthStopped = false;
 }

 public boolean growthStopped ()
 {
	return growthStopped;
 }


 // Serialization

 private static final long serialVersionUID = 1;
 private static final int CURRENT_SERIAL_VERSION = 0;

 private void writeObject (ObjectOutputStream out) throws IOException {
	out.writeInt (CURRENT_SERIAL_VERSION);
	out.writeInt (numEntries);
	out.writeObject(map);
	out.writeBoolean (growthStopped);
 }

 private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
	int version = in.readInt ();
	numEntries = in.readInt();
	map = (gnu.trove.TObjectIntHashMap)in.readObject();
	growthStopped = in.readBoolean();
 }

}

