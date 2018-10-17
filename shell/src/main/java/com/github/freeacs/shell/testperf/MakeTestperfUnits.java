package com.github.freeacs.shell.testperf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Must support the following:
 *
 * <p>- be able to generate a range of units. The range must be 100, 200, 300, etc... The form of
 * the unit-ids should be like this: 000000-TR069TestClient-000000000000 (the last twelve digit
 * number should change) - be able to add 3 columns for each unit: Country Place Service - The
 * number of countries must be specified as input args (as names). The countries are evenly
 * distributed among the range (if possible). - The number of cities must be specified as input args
 * (as names). - The service can be either "Voip 1-line" for uneven numbers and "Voip 2-line" for
 * even numbers.
 *
 * @author Morten
 */
public class MakeTestperfUnits {
  private static List<Country> countries;

  private static void populateCountries(File file) {
    try {
      countries = new ArrayList<>();
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.length() > 1) {
          countries.add(new Country(line));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void execute(String[] args) {
    try {
      int rangeLowerBound = Integer.parseInt(args[0]);
      int rangeUpperBound = Integer.parseInt(args[1]);
      File file = new File(args[2]);
      File outputFile1 = new File("internal/owera/units/testperf1.u");
      File outputFile2 = new File("internal/owera/units/testperf2.u");
      FileWriter fw1 = new FileWriter(outputFile1);
      FileWriter fw2 = new FileWriter(outputFile2);
      if ((rangeUpperBound - rangeLowerBound + 1) % 100 != 0) {
        if ((rangeUpperBound - rangeLowerBound) % 100 != 0) {
          throw new IllegalArgumentException("The range is not a multiple of 100");
        } else {
          rangeUpperBound--;
        }
      }
      if (!file.exists()) {
        throw new IllegalArgumentException("The file does not exist");
      } else {
        populateCountries(file);
      }
      System.out.println("The testperf unit file will be stored in owera/units directory.");
      System.out.println("The characteristics of the units can be changed by editing " + args[2]);
      System.out.println("Characteristics:");
      int multiple = (rangeUpperBound - rangeLowerBound + 1) / 100;

      for (int i = rangeLowerBound; i <= rangeUpperBound; i++) {
        String mac = String.format("%012d", i);
        StringBuilder output = new StringBuilder(String.format("000000-TR069TestClient-" + mac, i));
        int prevExcUpperLimitCountry = rangeLowerBound;
        for (Country country : countries) {
          if (country.getExcUpperLimit() == 0) {
            country.setIncLowerLimit(prevExcUpperLimitCountry);
            country.setExcUpperLimit(prevExcUpperLimitCountry + multiple * country.getPercent());
            prevExcUpperLimitCountry = country.getExcUpperLimit();
            City[] cities = country.getCities();
            int prevExcUpperLimitCity = country.getIncLowerLimit();
            for (City city : cities) {
              city.setIncLowerLimit(prevExcUpperLimitCity);
              city.setExcUpperLimit(
                  prevExcUpperLimitCity
                      + multiple * city.getPercent() * country.getPercent() / 100);
              prevExcUpperLimitCity = city.getExcUpperLimit();
            }
            country.getCities()[country.getCities().length - 1].setExcUpperLimit(
                country.getExcUpperLimit());
            System.out.println(country);
          }
          if (i >= country.getIncLowerLimit() && i < country.getExcUpperLimit()) {
            output.append(String.format(" %-15s", country.getName()));
            City[] cities = country.getCities();
            for (City city : cities) {
              if (i >= city.getIncLowerLimit() && i < city.getExcUpperLimit()) {
                output.append(String.format("%-15s", city.getName()));
              }
            }
          }
        }
        if (i % 2 == 0) {
          output.append("\"Voip 2-line\"");
          fw1.write(output + " " + mac + "\n");
        } else {
          output.append("\"Voip 1-line\"");
          fw2.write(output + " " + mac + "\n");
        }
      }
      fw1.close();
      fw2.close();
    } catch (Throwable t) {
      System.err.println("An error occurred:" + t);
    }
  }
}
