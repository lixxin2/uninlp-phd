///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2007 University of Texas at Austin and (C) 2005
//University of Pennsylvania and Copyright (C) 2002, 2003 University
//of Massachusetts Amherst, Department of Computer Science.
//
//This software is licensed under the terms of the Common Public
//License, Version 1.0 or (at your option) any subsequent version.
//
//The license is approved by the Open Source Initiative, and is
//available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////

package edu.hitsz.nlp.mstjoint.io;

import java.io.*;

import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;
import edu.hitsz.nlp.mstjoint.*;

/**
* A class that defines common behavior and abstract methods for
* writers for different formats.
*
* <p>
* Created: Sat Nov 10 15:25:10 2001
* </p>
*
* @author Jason Baldridge
* @version $Id: DependencyWriter.java 94 2007-01-17 17:05:12Z jasonbaldridge $
*/
public abstract class DependencyWriter {

 protected BufferedWriter writer;
 protected boolean labeled = false;

 /**
  * 初始化依存写入器，包括文件类型和是否有label
  * @param format
  * @param labeled
  * @return
  * @throws IOException
  */
 public static DependencyWriter createDependencyWriter (String format, boolean labeled) throws IOException {
	if (format.equals("MST")) {
	    return new MSTWriter(labeled);
	} else {
	    return new CONLLWriter(labeled);
	}
 }

 public void startWriting (String file) throws IOException {
	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF8"));
 }

 public void finishWriting () throws IOException {
	writer.flush();
	writer.close();
 }

 public boolean isLabeled() {
	return labeled;
 }

 public abstract void write(DependencyInstanceJoint instance) throws IOException;

}
