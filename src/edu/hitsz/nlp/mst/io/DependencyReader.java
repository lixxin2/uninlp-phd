///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
//
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////

package edu.hitsz.nlp.mst.io;


import java.io.*;


/**
 * A class that defines common behavior and abstract methods for
 * readers for different formats.
 * <p> 文件读取类的抽象类
 * <p>
 * Created: Sat Nov 10 15:25:10 2001
 * </p>
 *
 * @author Jason Baldridge
 * @version $Id: DependencyReader.java 112 2007-03-23 19:19:28Z jasonbaldridge $
 */
public abstract class DependencyReader {

    protected BufferedReader inputReader;
    protected boolean labeled = true; //是否有依存关系

    /**
     * 生成依存文件读取器，选择CoNLLReader还是MSTReader
     * @param format
     * @param discourseMode
     * @return
     * @throws IOException
     */
    public static DependencyReader createDependencyReader (String format,
							   boolean discourseMode)
	throws IOException {

		if (format.equals("MST")) {
		    return new MSTReader();
		} else if (format.equals("CONLL")) {
		    return new CONLLReader(discourseMode);
		} else {
		    System.out.println("!!!!!!!  Not a supported format: " + format);
		    System.out.println("********* Assuming CONLL format. **********");
		    return new CONLLReader(discourseMode);
		}
    }

    /**
     * 生成依存文件读取器
     * @param format
     * @return
     * @throws IOException
     */
    public static DependencyReader createDependencyReader (String format)
	throws IOException {

    	return createDependencyReader(format, false);
    }

    /**
     * 设置读取器,判断是否有label,以及文件读取器。返回是否有label
     * @param file
     * @return
     * @throws IOException
     */
    public boolean startReading (String file) throws IOException {
		labeled = fileContainsLabels(file);
		inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF8"));
		return labeled;
    }

    public boolean isLabeled() {
    	return labeled;
    }

    /**
     * 获取下一个实例
     * @since Dec 9, 2011
     * @return
     * @throws IOException
     */
    public abstract edu.hitsz.nlp.mst.DependencyInstance getNext() throws IOException;

    /**
     * 是否包含依存关系
     * @param filename
     * @return
     * @throws IOException
     */
    protected abstract boolean fileContainsLabels(String filename) throws IOException;


    /**
     * 如果是数字，则返回num；字符不变
     * @param s
     * @return
     */
    protected String normalize (String s) {
		if(s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+"))
		    return "<num>";

		return s;
    }

}

