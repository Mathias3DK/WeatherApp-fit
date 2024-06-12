package dk.mathiasS.weatherApp.model;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ClothingPredictor {
    private Instances trainingData;
    private Classifier classifier;

    public ClothingPredictor(String arffFilePath) throws Exception {
        // Load data from ARFF file
        InputStream inputStream = getClass().getResourceAsStream(arffFilePath);
        if (inputStream == null) {
            throw new Exception("ARFF file not found: " + arffFilePath);
        }
        DataSource source = new DataSource(inputStream);
        trainingData = source.getDataSet();
        trainingData.setClassIndex(trainingData.numAttributes() - 1);

        classifier = new J48();
    }
    public void buildClassifier() throws Exception {
        // Build classifier
        classifier.buildClassifier(trainingData);
    }

    public void saveArff(String arffFilePath) throws IOException {
        // Save training data as ARFF file
        BufferedWriter writer = new BufferedWriter(new FileWriter(arffFilePath));
        writer.write(trainingData.toString());
        writer.close();
        System.out.println("ARFF file created: " + arffFilePath);
    }

    public String predict(String outlook, double temperatureValue, long precipitation_probabilityValue, double humidityValue, boolean windy) throws Exception {
        // Create instance for prediction
        Instance instance = new DenseInstance(5);
        instance.setDataset(trainingData);

        // Set attribute values
        instance.setValue(trainingData.attribute("outlook"), outlook);
        instance.setValue(trainingData.attribute("temperature"), temperatureValue);
        instance.setValue(trainingData.attribute("precipitation_probability"), precipitation_probabilityValue);
        instance.setValue(trainingData.attribute("humidity"), humidityValue);
        instance.setValue(trainingData.attribute("windy"), windy ? "TRUE" : "FALSE");

        //TODO:
        // ADD WHOLE NEW IDEA - Rain possiblity if high take rain jacket on - Humidity is high, shorts and so on

        // Perform prediction
        double prediction = classifier.classifyInstance(instance);

        // Return the predicted class value
        return trainingData.classAttribute().value((int) prediction);
    }

    public static void main(String[] args) throws Exception {
        try {
            ClothingPredictor weatherPredictor = new ClothingPredictor("/model/weather_data.arff");

            // Build classifier
            weatherPredictor.buildClassifier();

            String[] outlooks = new String[]{"sunny", "overcast", "rainy", "snowy"};
            int pred_1 = 0;
            int pred_2 = 0;
            int pred_3 = 0;
            int pred_4 = 0;
            int pred_5 = 0;
            int pred_6 = 0;
            int pred_7 = 0;
            int pred_8 = 0;
            int pred_9 = 0;
            int pred_10 = 0;

            Random rand = new Random();
            for (int i = 0; i < 50; i++) {

                String outlook = outlooks[rand.nextInt(outlooks.length)];
                double randomValue = 1.0 + (30.0 - 1.0) * rand.nextDouble();
                double temp = (9.0 / 5.0) * randomValue + 32;
                double humidity = rand.nextDouble() * 100;
                int rain = new Random().nextInt(100);
                boolean windy = rand.nextBoolean();

                int prediction = Integer.parseInt(weatherPredictor.predict(outlook, ((9.0/5.0) * 5) + 32, rain, humidity, windy));
                System.out.println("Prediction " + (i + 1) + ": " + prediction + " (Outlook: " + outlook +
                        ", Temperature: " + (temp - 32) / 1.8 + " °C, rain_probability: " + rain + ", " + "Humidity: " + humidity + " %, Windy: " + windy + ")");

                switch (prediction) {
                    case 1:
                        pred_1++;
                        break;
                    case 2:
                        pred_2++;
                        break;
                    case 3:
                        pred_3++;
                        break;
                    case 4:
                        pred_4++;
                        break;
                    case 5:
                        pred_5++;
                        break;
                    case 6:
                        pred_6++;
                        break;
                    case 7:
                        pred_7++;
                        break;
                    case 8:
                        pred_8++;
                        break;
                    case 9:
                        pred_9++;
                        break;
                    case 10:
                        pred_10++;
                        break;
                }
            }

            System.out.println("Summary:");
            System.out.println("Wear 1 (T-shirt og shorts): " + pred_1);
            System.out.println("Wear 2 (T-shirt og bukser): " + pred_2);
            System.out.println("Wear 3 (Langærmet trøje og bukser): " + pred_3);
            System.out.println("Wear 4 (Sweatshirt og bukser): " + pred_4);
            System.out.println("Wear 5 (Trøje, jakke og bukser): " + pred_5);
            System.out.println("Wear 6 (Jakke og jeans): " + pred_6);
            System.out.println("Wear 7 (Regnjakke- eller paraply og bukser): " + pred_7);
            System.out.println("Wear 8 (Vinterjakke og bukser): " + pred_8);
            System.out.println("Wear 9 (Vinterjakke, hue og handsker): " + pred_9);

            System.out.println("Total instances: " + (pred_1 + pred_2 + pred_3 + pred_4 + pred_5 + pred_6 + pred_7 + pred_8 + pred_9 + pred_10));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ClothingPredictor weatherPredictor = new ClothingPredictor("/model/weather_data.arff");

        weatherPredictor.buildClassifier();
        System.out.println(weatherPredictor.classifier);

        J48 classifier = (J48) weatherPredictor.classifier;

        FileWriter writer = new FileWriter("decision_tree.dot");
        writer.write(classifier.graph());
        writer.close();

        System.out.println("Decision tree exported to 'decision_tree.dot'");

        try {

            Evaluation eval = new Evaluation(weatherPredictor.trainingData);
            eval.crossValidateModel(weatherPredictor.classifier, weatherPredictor.trainingData, 6, new Random(1));

            System.out.println(eval.toSummaryString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
