/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecg.signal.detection;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.math.plot.Plot2DPanel;

/**
 *
 *
 *
 * @author salma
 */
public class ECGSignalDetection {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                plotData(timeAxies(), dataAxies(512));

            }
        });

    }

    public static void plotData(double[] x, double[] y) {
        Plot2DPanel plot = new Plot2DPanel();
        plot.addLinePlot("autoCorrelation", x, y);
        JFrame frame = new JFrame("ECG");
        frame.setContentPane(plot);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    public static double[] timeAxies() {
        double[] simpleArray = new double[2000];
        double tempSampleTime = 0;
        for (int i = 0; i < 2000; i++) {
            simpleArray[i] = tempSampleTime;
            tempSampleTime += 1;
        }
        return simpleArray;
    }

    public static double[] dataAxies(int sampleRate) {

        ArrayList<Double> drivitive, squared, smooth, autoCorrelations;
        double[] dataArray = new double[2087];

        drivitive = fivePointsDervietive(readDataFromFile("Data1.txt"));
        squared = square(drivitive);
        smooth = smooth(squared);
        autoCorrelations = autoCorrelation(smooth);

        for (int i = 0; i < 2087; i++) {
            dataArray[i] = autoCorrelations.get(i);
        }

        System.out.println(findHeartRate(autoCorrelations));
        System.out.println(isAtrialFibrillation(autoCorrelations));
        return dataArray;

    }

    public static ArrayList<Double> readDataFromFile(String fileName) {
        ArrayList<Double> timeAxies = new ArrayList<>();
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = in.readLine()) != null) {
                timeAxies.add(Double.parseDouble(line));
            }
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ECGSignalDetection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ECGSignalDetection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timeAxies;
    }

    public static ArrayList<Double> fivePointsDervietive(ArrayList<Double> original) {
        ArrayList<Double> diffrentiated = new ArrayList<>();
        for (int i = 0; i < original.size(); i++) {
            if (i == 0) {
                diffrentiated.add((1 / (8 * 0.001953125)) * (2 * original.get(i + 1) + original.get(i + 2)));
            } else if (i == 1) {
                diffrentiated.add((1 / (8 * 0.001953125)) * (- 2 * original.get(i - 1) + 2 * original.get(i + 1) + original.get(i + 2)));
            } else if (i == original.size() - 1) {
                diffrentiated.add((1 / (8 * 0.001953125)) * (-original.get(i - 2) - 2 * original.get(i - 1)));
            } else if (i == original.size() - 2) {
                diffrentiated.add((1 / (8 * 0.001953125)) * (-original.get(i - 2) - 2 * original.get(i - 1) + 2 * original.get(i + 1)));
            } else {
                diffrentiated.add((1 / (8 * 0.001953125)) * (-original.get(i - 2) - 2 * original.get(i - 1) + 2 * original.get(i + 1) + original.get(i + 2)));

            }
        }
        return diffrentiated;
    }

    public static ArrayList<Double> square(ArrayList<Double> original) {
        ArrayList<Double> timeAxies2 = new ArrayList<>();
        for (Double original1 : original) {
            timeAxies2.add(original1 * original1);
        }
        return timeAxies2;
    }

    public static ArrayList<Double> smooth(ArrayList<Double> original) {
        ArrayList<Double> smoothed = new ArrayList<>();
        double sum = 0;

        for (int i = 0; i < original.size(); i++) {
            if (i < 31) {
                for (int j = 0; j <= i; j++) {
                    sum += original.get(j);

                }
                smoothed.add(sum / 31);
                sum = 0;
            } else {
                for (int j = i - 31; j <= i; j++) {
                    sum += original.get(j);

                }
                smoothed.add(sum / 31);
                sum = 0;
            }

        }
        return smoothed;
    }

    public static ArrayList<Double> autoCorrelation(ArrayList<Double> original) {
        ArrayList<Double> shifted;
        ArrayList<Double> autoCorrelations = new ArrayList<>();
        double autoCorrelathion = 0;
        for (int i = 0; i < 2087; i++) {
            shifted = shift(original, i);
            for (int j = 0; j < original.size(); j++) {
                autoCorrelathion += (original.get(j) * shifted.get(j));
            }
            autoCorrelations.add(autoCorrelathion);
            autoCorrelathion = 0;
        }
        return autoCorrelations;
    }

    public static ArrayList<Double> shift(ArrayList<Double> original, int shiftBy) {
        ArrayList<Double> shifted = new ArrayList<>();

        for (int i = 0; i <= shiftBy; i++) {
            shifted.add(0.0);
        }
        for (Double original1 : original) {
            shifted.add(original1);
        }

        return shifted;
    }

    public static double findHeartRate(ArrayList<Double> original) {
        double maxPeak = original.get(200);
        double maxPeakLag = 1;
        for (int i = 200; i < original.size(); i++) {
            if (original.get(i) > maxPeak) {
                maxPeak = original.get(i);
                maxPeakLag = i;
            }
        }
        return 60 / (maxPeakLag / 512.0);
    }

    public static boolean isAtrialFibrillation(ArrayList<Double> original) {
        double threshold = 5000000;
        boolean up = false;
        double maxPeakLag = 0;
        for (int i = 200; i < original.size(); i++) {
            if (original.get(i) > threshold) {
                up = true;
            }
            if (up && original.get(i) < threshold) {

                up = false;
                maxPeakLag += 1;
            }

        }
        System.out.println(maxPeakLag);
        return maxPeakLag > 7;
    }
}
