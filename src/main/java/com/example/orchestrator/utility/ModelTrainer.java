package com.example.orchestrator.utility;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.SerializationHelper;
import weka.classifiers.Evaluation;

import java.util.Random;

public class ModelTrainer {

    public static void main(String[] args) throws Exception {
        DataSource source = new DataSource("training-data.csv");
        Instances dataset = source.getDataSet();
        dataset.setClassIndex(dataset.numAttributes() - 1); // This index is what we want to predict, in this case it's 5
        Classifier model = new M5P();
        model.buildClassifier(dataset);
        // Evaluation code that I tried but didn't really understand
        //  Evaluation eval = new Evaluation(dataset);
        // eval.crossValidateModel(model, dataset, 10, new Random(1));
        //   System.out.println(eval.toSummaryString());
        SerializationHelper.write("replica-model.model", model);
        System.out.println("Model saved as replica-model.model");
    }
}
