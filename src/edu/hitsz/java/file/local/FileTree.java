package edu.hitsz.java.file.local;
import java.io.File;
import java.util.*;

/**
 * operate all files under a directory, or a single file
 * <p> 用来遍历一个文件，或者一个目录下的所有文件
 * @author tm
 *
 */
public class FileTree {

    public class TreeNode {

        private File file;
        private int level;
        private boolean isEnd;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {

            this.file = file;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean isEnd) {
            this.isEnd = isEnd;
        }

        public TreeNode(File fileNode, int level, boolean isEnd) {
            this.file = fileNode;
            this.level = level;
            this.isEnd = isEnd;
        }

        @Override
        public String toString() {
            return "[" + file.getPath() + "," + level + "," + isEnd + "]";
        }
    }

    List<TreeNode> list;
    public FileTree(){
    	list = new ArrayList<TreeNode>();
    }

    public FileTree(String dir){
    	list = new ArrayList<TreeNode>();
    	this.generateFrom(dir);
    }


    /**
     * generate the list of files according to the path name, or a single file
     * @param pathName
     */
    public void generateFrom(String pathName){
    	File f = new File(pathName);
    	if(!f.exists()){
    		System.out.println("\""+pathName+"\" is not a directory or a file. Please input again: ");
    		System.exit(-1);
    	}
    	System.out.println("Generating files in "+pathName);
	    TreeNode root = new TreeNode(f, 0, true);
	    int cursor = 0;
	    list.add(root);
	    if(f.isDirectory()){
	    	System.out.println("Your input \""+pathName+"\" is a directory");
		    do {
		        tree(cursor);
		        cursor++;
		    } while (cursor < list.size());
	    }
	    else if(f.isFile()){
	    	System.out.println("Your input \""+pathName+"\" is a single file");
	    }
    }

    /**
     * display all, including directories and files
     */
    public void displayAll( ){

	    boolean[] haveLine = new boolean[20];
	    int level;
	    String line;
	    for (int i = 0; i < list.size(); i++) {
	        line = "";
	        level = list.get(i).getLevel();
	        for (int j = 0; j < level; j++) {
	            if (haveLine[j]) {
	                line = line + "|***";
	            }
	            else {
	                line = line + "****";
	            }
	        }
	        haveLine[level] = !list.get(i).isEnd();

	        if (level != 0) {
	            line = line + "|_";
	        }

	        line = line + list.get(i).getFile().getPath();
	        System.out.println(line);
	    }
    }

    /**
     * display only files
     */
    public void displayFiles( ){
 	    for (int i = 0; i < list.size(); i++) {
 	    	if(list.get(i).getFile().isFile())
 	    		System.out.println(list.get(i).getFile().getPath());
 	    }
    }


 

    /**
     * 返回所有文件名
     */
    public ArrayList<String> getFileNames(){
    	ArrayList<String> fileNames = new ArrayList<String>();
     	for (int i = 0; i < list.size(); i++)
 	    	if(list.get(i).getFile().isFile())
 	    		fileNames.add(list.get(i).getFile().getPath());
     	Collections.sort(fileNames);     	
     	return fileNames;
     	
    }



    /**
     * get all names of all files in the directory into a ArrayList, with suffix
     * 找到文件夹里所有有设定后缀的文件
     * @param fileNames
     * @param suffix
     */
 

    public ArrayList<String> getFileNamesWithSuffix(String suffix){
    	ArrayList<String> fileNames = new ArrayList<String>();
     	for (int i = 0; i < list.size(); i++) {
 	    	if(list.get(i).getFile().isFile()){
 	    		String oneFileName = list.get(i).getFile().getPath();
 	    		if(oneFileName.endsWith(suffix))
 	    			fileNames.add(oneFileName);
 	    	}
 	    }
     	Collections.sort(fileNames);     	
     	return fileNames;
    }
    
    public ArrayList<String> getFileNamesWithPrefix(String prefix){
    	ArrayList<String> fileNames = new ArrayList<String>();
     	for (int i = 0; i < list.size(); i++) {
 	    	if(list.get(i).getFile().isFile()){
 	    		String oneFileName = list.get(i).getFile().getPath();
 	    		if(oneFileName.startsWith(prefix))
 	    			fileNames.add(oneFileName);
 	    	}
 	    }
     	Collections.sort(fileNames);     	
     	return fileNames;
    }


    private void tree(int currentCursor) {
        File file = list.get(currentCursor).getFile();
        if (!file.isDirectory()) {
           return;
        }
       int newLevel = list.get(currentCursor).getLevel() + 1;

        List<TreeNode> newList = new ArrayList<TreeNode>();
        File[] childs = file.listFiles();

        for (int i = 0; i < childs.length; i++) {
            boolean isEnd = false;
            if (i == childs.length - 1) {
               isEnd = true;
            }
            TreeNode node = new TreeNode(childs[i], newLevel, isEnd);
            newList.add(node);
        }
       list.addAll(currentCursor + 1, newList);
    }


    private static void test(){
    	FileTree newTree = new FileTree();
    	newTree.generateFrom("/home/tm/conll/test/dev_auto_conll");
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix(".bib");
    	System.out.print("a");
    }


    public static void main(String[] args) {
    	test();

    }





}