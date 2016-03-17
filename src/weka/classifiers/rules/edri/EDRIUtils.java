package weka.classifiers.rules.edri;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by suhel on 12/03/16.
 */
public class EDRIUtils {

    public static String[] classNames(Instances data) {
        return itemNames(data, data.classIndex());
    }

    public static String[] itemNames(Instances data, int attIndex) {
        Attribute attr = data.attribute(attIndex);
        String[] result = new String[attr.numValues()];
        for (int i = 0; i < result.length; i++) {
            result[i] = attr.value(i);
        }
        return result;
    }

    public static String formatIntPattern(int maxDigit) {
        int digits = (int) (Math.ceil(Math.log10(maxDigit+1)));
        return  "%0"+ digits +"d";
    };

    public static String[] attrNames(Instances data) {
        String[] result = new String[data.numAttributes()];
        for (int i = 0; i < result.length; i++) {
            result[i] = data.attribute(i).name();
        }
        return result;
    }



    //Class should be the last attribute
    public static int[][] itemsLabelOfAttr(Instances data, int attIndex) {
        int numAttributes = data.numAttributes() - 1;
        int numClasses = data.attribute(data.classIndex()).numValues();
        int numElements = data.attribute(attIndex).numValues();

        int[][] result = new int[numElements][];

        for (int i = 0; i < result.length; i++) {
            result[i] = new int[numClasses];
        }

        Enumeration instIteretor = data.enumerateInstances();
        while (instIteretor.hasMoreElements()) {
            Instance inst = (Instance) instIteretor.nextElement();
            int item = (int) inst.value(attIndex);
            int cls = (int) inst.classValue();
            result[item][cls]++;
        }

        return result;
    }

    public static int bestItemForClass(int[][] items, int cls) {
        return -1;
    };

    public static String toString(int[][] items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            sb.append("" + items[i][0]);
            for (int j = 1; j < items[i].length; j++) {
                sb.append("," + items[i][j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }


    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public static int[] sumLines(int[][] items) {
        int[] result = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            for (int j = 0; j < items[i].length; j++) {
                result[i] += items[i][j];
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
//        String command = "-t "+ inFile + " -T "+ inFile + " -no-cv";
//        runClassifier(new Prism(), args);


        String p = formatIntPattern(1);
        System.out.println(p);
        System.out.println(String.format(p, 77777733));

    }

}
