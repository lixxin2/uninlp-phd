package edu.hitsz.nlp.sequence;

import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.util.FeatureTemplate;
import edu.hitsz.util.ArgumentsParser;
import java.io.*;

public class SequenceExtractFeature
{

    public SequenceExtractFeature()
    {
    }

    static void usage()
    {
        System.out.println("Usage:");
        System.out.println("  Help: -h");
        System.out.println("  Features from template: -t trainFile -e templateFile -f featureFile");
        System.out.println("  Features from specific type: -t trainFile -y type -f featureFile");
        System.out.println("    Type: pos, chunkpos, poschunk");
        System.exit(1);
    }

    static void extractFeature(String args[])
    {
        ConllFile trainFile = new ConllFile();
        String trainName = "";
        String templateName = "";
        String typeName = "";
        String feaName = "";
        String shortArgs = "ht:e:y:f:";
        ArgumentsParser newParser = new ArgumentsParser();
        newParser.parseCmdLine(args, shortArgs);
        if(newParser.containsArgument("t"))
            trainName = newParser.getArgument("t");
        if(newParser.containsArgument("e"))
            templateName = newParser.getArgument("e");
        if(newParser.containsArgument("y"))
            typeName = newParser.getArgument("y");
        if(newParser.containsArgument("f"))
            feaName = newParser.getArgument("f");
        if(newParser.containsOption("h"))
            usage();
        else
        if(newParser.containsArgument("t") && newParser.containsArgument("e") && newParser.containsArgument("f"))
        {
            FeatureTemplate newTemplate = new FeatureTemplate();
            newTemplate.readFromFile(templateName);
            trainFile.readFrom(trainName, 0);
            try
            {
                FileWriter feaFile = new FileWriter(feaName);
                //trainFile.extractAndStoreFeatures(feaFile, newTemplate);
                feaFile.close();
            }
            catch(IOException e)
            {
                System.out.println((new StringBuilder("IOException: ")).append(e).toString());
            }
        } else
        if(newParser.containsArgument("t") && newParser.containsArgument("y") && newParser.containsArgument("f"))
        {
            trainFile.readFrom(trainName, 0);
            try
            {
                FileWriter feaFile = new FileWriter(feaName);
                trainFile.extractAndStoreFeatures(feaFile, typeName);
                feaFile.close();
            }
            catch(IOException e)
            {
                System.out.println((new StringBuilder("IOException: ")).append(e).toString());
            }
        } else
        {
            usage();
        }
    }

    public static void main(String args[])
    {
        extractFeature(args);
    }
}

