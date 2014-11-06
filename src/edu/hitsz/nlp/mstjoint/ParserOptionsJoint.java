package edu.hitsz.nlp.mstjoint;

import java.io.File;

/**
* Hold all the options for the parser so they can be passed around easily.
* <p> 依存句法分析器的参数配置详情
* <p>
* Created: Sat Nov 10 15:25:10 2001
* </p>
*
* @author Jason Baldridge
* @version $Id: CONLLReader.java 103 2007-01-21 20:26:39Z jasonbaldridge $
* @see mstparser.io.DependencyReader
*/
public class ParserOptionsJoint {
	

	 public boolean joint = true;
	 public boolean train = false;
	 public boolean test = false;
	 public boolean eval = true;	 

	 public String lossType = "punc";
	 public boolean createForest = true;
	 public String decodeType = "proj";
	 public String format = "MST";
	 public int numIters = 3;
	 public int trainK = 1;
	 public int testK = 1;
	 public int latticeK = 5;
	 public boolean secondOrder = false;
	 public boolean useRelationalFeatures = false;
	 public boolean discourseMode = false;
	 
	 public double wordProb = 0.0;
	 public double depProb = 1 - wordProb;

	 public String forwardCharModel = "/home/tm/disk/disk1/joint/segpos/ctb5-charModel-9";
	 public String backwardCharModel = "/home/tm/disk/disk1/joint/segpos/ctb5-charModel-r-9";
	 
	 public String trainfile = "/home/tm/disk/disk1/joint/data/test-row";
	 public String trainforest = "/home/tm/disk/disk1/joint/test.forest";
	 public String trainpipe = "/home/tm/disk/disk1/joint/test.pipe";
	 public String trainlattice = "/home/tm/disk/disk1/joint/test.lattice";
	 public String testfile = "/home/tm/disk/disk1/joint/data/test-row";
	 public String testlattice = "/home/tm/disk/disk1/joint/test.lattice.2";
	 public String modelName = "/home/tm/disk/disk1/joint/data/ctb5.dep.model.ap1-9";
	 public String outfile = "/home/tm/disk/disk1/joint/joint.test.result.k2";
	 public String goldfile = "/home/tm/disk/disk1/joint/data/test-row";

	/**
	 * 从命令行读取参数配置
	 * @since Dec 9, 2011
	 * @param args
	 */
	 public ParserOptionsJoint (String[] args) {

		for(int i = 0; i < args.length; i++) {
		    String[] pair = args[i].split(":");

		    if (pair[0].equals("joint")) {
			joint = true;
			}
		    if (pair[0].equals("train")) {
			train = true;
		    }
		    if (pair[0].equals("eval")) {
			eval = true;
		    }
		    if (pair[0].equals("test")) {
			test = true;
		    }
		    if (pair[0].equals("iters")) {
			numIters = Integer.parseInt(pair[1]);
		    }
		    if (pair[0].equals("output-file")) {
			outfile = pair[1];
		    }
		    if (pair[0].equals("gold-file")) {
			goldfile = pair[1];
		    }
		    if (pair[0].equals("train-file")) {
			trainfile = pair[1];
		    }
		    if (pair[0].equals("train-forest")) {
			trainforest = pair[1];
			}
		    if (pair[0].equals("train-pipe")) {
			trainpipe = pair[1];
			}
		    if (pair[0].equals("train-lattice")) {
		    trainlattice = pair[1];
			}
		    if (pair[0].equals("test-file")) {
			testfile = pair[1];
		    }
		    if (pair[0].equals("test-lattice")) {
			testlattice = pair[1];
			}
		    if (pair[0].equals("model-name")) {
			modelName = pair[1];
		    }
		    if (pair[0].equals("training-k")) {
			trainK = Integer.parseInt(pair[1]);
		    }
		    if (pair[0].equals("loss-type")) {
			lossType = pair[1];
		    }
		    if (pair[0].equals("order") && pair[1].equals("2")) {
			secondOrder = true;
		    }
		    if (pair[0].equals("create-forest")) {
			createForest = pair[1].equals("true") ? true : false;
		    }
		    if (pair[0].equals("decode-type")) {
			decodeType = pair[1];
		    }
		    if (pair[0].equals("format")) {
			format = pair[1];
		    }
		    if (pair[0].equals("relational-features")) {
			useRelationalFeatures = pair[1].equals("true") ? true : false;
		    }
		    if (pair[0].equals("discourse-mode")) {
			discourseMode = pair[1].equals("true") ? true : false;
		    }
		}

	 }


	 public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("FLAGS [");
		sb.append("train-file: " + trainfile);
		sb.append(" | ");
		sb.append("test-file: " + testfile);
		sb.append(" | ");
		sb.append("gold-file: " + goldfile);
		sb.append(" | ");
		sb.append("output-file: " + outfile);
		sb.append(" | ");
		sb.append("model-name: " + modelName);
		sb.append(" | ");
		sb.append("train: " + train);
		sb.append(" | ");
		sb.append("test: " + test);
		sb.append(" | ");
		sb.append("eval: " + eval);
		sb.append(" | ");
		sb.append("loss-type: " + lossType);
		sb.append(" | ");
		sb.append("second-order: " + secondOrder);
		sb.append(" | ");
		sb.append("training-iterations: " + numIters);
		sb.append(" | ");
		sb.append("training-k: " + trainK);
		sb.append(" | ");
		sb.append("decode-type: " + decodeType);
		sb.append(" | ");
		sb.append("create-forest: " + createForest);
		sb.append(" | ");
		sb.append("format: " + format);
		sb.append(" | ");
		sb.append("relational-features: " + useRelationalFeatures);
		sb.append(" | ");
		sb.append("discourse-mode: " + discourseMode);
		sb.append("]\n");
		return sb.toString();
	}

}
