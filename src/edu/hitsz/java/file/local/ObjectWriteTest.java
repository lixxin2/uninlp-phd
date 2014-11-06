package edu.hitsz.java.file.local;


import java.io.*;

/**
 * 比较序列化存取的速度
 * @author Xinxin Li
 * @since Oct 21, 2013
 * @author Jakub Kubrynski <jkubrynski@gmail.com>
 * @since 29.07.12
*/
public class ObjectWriteTest {

	Object test;  
	public static final int MAX_BUFFER_SIZE = 1024;
	public static final String FILE_MODE_RW = "rw";
	public static final String FILE_MODE_R = "r";

    public void standardSerializationWriteBuffered(String inFileName){
      ObjectOutputStream objectOutputStream = null;
      try {
        FileOutputStream fileOutputStream = new FileOutputStream(inFileName);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
        objectOutputStream.writeObject(test);
        objectOutputStream.close();
      } 
      catch (IOException e) {
    	  
      }
    }

    public Object standardSerializationReadBuffered(String outFileName) throws ClassNotFoundException{
      ObjectInputStream objectInputStream = null;
      try {
        FileInputStream fileInputStream = new FileInputStream(outFileName);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        objectInputStream = new ObjectInputStream(bufferedInputStream);
        objectInputStream.close();
        Object obj = objectInputStream.readObject();
        return obj;        
      } 
      catch (IOException e) {
    	  
      }
      return null;
    }

    public void StandardSerializationRafWriteBuffered(Object test, String fileName) throws IOException {
      ObjectOutputStream objectOutputStream = null;
      try {
        RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, FILE_MODE_RW);
        FileOutputStream fileOutputStream = new FileOutputStream(randomAccessFile.getFD());
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(test);
      } finally {
        if (objectOutputStream != null) {
          objectOutputStream.close();
        }
      }
    }

    public Object StandardSerializationRafReadBuffered(String fileName) throws IOException, ClassNotFoundException {
      ObjectInputStream objectInputStream = null;
      try {
        RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, FILE_MODE_R);
        FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
        objectInputStream = new ObjectInputStream(fileInputStream);
        return objectInputStream.readObject();
      } finally {
        if (objectInputStream != null) {
          objectInputStream.close();
        }
      }
    }
    
    /**
    public void Kryo2SerializationWriteBuffered(Object test, String fileName) throws IOException {
      Output output = null;
      try {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        output = new Output(new FileOutputStream(raf.getFD()), MAX_BUFFER_SIZE);
        kryo.writeObject(output, test);
      } finally {
        if (output != null) {
          output.close();
        }
      }
    }

    public Object Kryo2SerializationReadBuffered(String fileName) throws IOException {
      Input input = null;
      try {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        input = new Input(new FileInputStream(raf.getFD()), MAX_BUFFER_SIZE);
        return kryo.readObject(input, TestObject.class);
      } finally {
        if (input != null) {
          input.close();
        }
      }

    }
    **/
    

}
