package edu.hitsz.ml.maxent.lindekang;

public class TestMe {

	
	
	public static void train(String trainFileName, String modelFileName) {
		
		GISTrainer trainer = new GISTrainer();
		trainer.setPrintDetails(true); // show the parameters during training

		trainer.setParams(0.1, 0.01, 10);
		trainer.setClasses(trainFileName);

		// initialize the number of classes in model
		MaxEntModel model = new MaxEntModel(trainer.getClasses().size());

		EventSet trainEventSet = new EventSet();
		trainer.readEvents(trainFileName, trainEventSet); // read the training data

		trainer.train(model, trainEventSet);     // train the model
		//model.print(trainer);      // print the parameters
		model.store(modelFileName);

	}
	
	public static void predict(String modelFileName, String testFileName) {
		
		GISTrainer trainer = new GISTrainer();
		trainer.setPrintDetails(true); // show the parameters during training
		trainer.setParams(0.1, 0.01, 10);

		// initialize the number of classes in model
		MaxEntModel model = new MaxEntModel();
		model.read(modelFileName);

		EventSet test = new EventSet();
		trainer.readEvents(testFileName, test);   // read the testing data

		// test the model and print the error rate
		System.out.println("error rate="+trainer.test(test, model));
		
	}
	
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("USAGE: testme TRAIN MODEL TEST");
			System.exit(1);
		}
		String trainFileName = args[0];
		String modelFileName = args[1];
		String testFileName = args[2];
		train(trainFileName, modelFileName);
		predict(modelFileName, testFileName);


	}

}
