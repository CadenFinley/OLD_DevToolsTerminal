
/**
 *
 * @author Caden Finley
 * @version 1.0
 */
public abstract class UnitConversionEngine {

    private static final String[] distanceUnits = {"miles", "kilometers", "yards", "meters", "feet", "inches", "centimeters", "millimeters"};
    private static final String[] temperatureUnits = {"celsius", "fahrenheit", "kelvin"};
    private static final String[] weightUnits = {"pounds", "kilograms", "ounces", "grams", "milligrams", "micrograms", "stones", "metric tons"};
    private static final String[] volumeUnits = {"gallons", "liters", "quarts", "pints", "cups", "fluid ounces", "tablespoons", "teaspoons"};
    private static final String[] speedUnits = {"miles per hour", "kilometers per hour", "meters per second", "feet per second", "knots"};

    public static double convertUnit(double unit, String fromUnit, String toUnit, String type) {
        if (fromUnit.equals(toUnit)) {
            return unit;
        }
        if (!checkIfTypesMatch(fromUnit, toUnit, type)) {
            System.out.println("Invalid, types dont match");
            return 0.0;
        }
        double value = convertUnitReturn(unit, fromUnit, toUnit, type);
        return value;
    }

    private static boolean checkIfTypesMatch(String fromUnit, String toUnit, String type) {
        String[] units;
        switch (type) {
            case "distance" ->
                units = distanceUnits;
            case "temperature" ->
                units = temperatureUnits;
            case "weight" ->
                units = weightUnits;
            case "volume" ->
                units = volumeUnits;
            case "speed" ->
                units = speedUnits;
            default -> {
                return false;
            }
        }
        return contains(units, fromUnit) && contains(units, toUnit);
    }

    private static boolean contains(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static double convertUnitReturn(double unit, String fromUnit, String toUnit, String type) {
        return switch (type) {
            case "distance" ->
                convertDisance(unit, fromUnit, toUnit);
            case "temperature" ->
                convertTemperature(unit, fromUnit, toUnit);
            case "weight" ->
                convertWeight(unit, fromUnit, toUnit);
            case "volume" ->
                convertVolume(unit, fromUnit, toUnit);
            case "speed" ->
                convertSpeed(unit, fromUnit, toUnit);
            default ->
                -1.0;
        };
    }

    private static double convertDisance(double distance, String fromUnit, String toUnit) {
        return switch (toUnit) {
            case "miles" ->
                convertToMiles(distance, fromUnit);
            case "kilometers" ->
                convertToKilometers(distance, fromUnit);
            case "yards" ->
                convertToYards(distance, fromUnit);
            case "meters" ->
                convertToMeters(distance, fromUnit);
            case "feet" ->
                convertToFeet(distance, fromUnit);
            case "inches" ->
                convertToInches(distance, fromUnit);
            case "centimeters" ->
                convertToCentimeters(distance, fromUnit);
            case "millimeters" ->
                convertToMillimeters(distance, fromUnit);
            default ->
                -1.0;
        };
    }

    private static double convertTemperature(double temperature, String fromUnit, String toUnit) {
        return switch (toUnit) {
            case "celsius" ->
                convertToCelsius(temperature, fromUnit);
            case "fahrenheit" ->
                convertToFahrenheit(temperature, fromUnit);
            case "kelvin" ->
                convertToKelvin(temperature, fromUnit);
            default ->
                -1.0;
        };
    }

    private static double convertWeight(double weight, String fromUnit, String toUnit) {
        return switch (toUnit) {
            case "pounds" ->
                convertToPounds(weight, fromUnit);
            case "kilograms" ->
                convertToKilograms(weight, fromUnit);
            case "ounces" ->
                convertToOunces(weight, fromUnit);
            case "grams" ->
                convertToGrams(weight, fromUnit);
            case "milligrams" ->
                convertToMilligrams(weight, fromUnit);
            case "micrograms" ->
                convertToMicrograms(weight, fromUnit);
            case "stones" ->
                convertToStones(weight, fromUnit);
            case "metric tons" ->
                convertToMetricTons(weight, fromUnit);
            default ->
                -1.0;
        };
    }

    private static double convertVolume(double volume, String fromUnit, String toUnit) {
        return switch (toUnit) {
            case "gallons" ->
                convertToGallons(volume, fromUnit);
            case "liters" ->
                convertToLiters(volume, fromUnit);
            case "quarts" ->
                convertToQuarts(volume, fromUnit);
            case "pints" ->
                convertToPints(volume, fromUnit);
            case "cups" ->
                convertToCups(volume, fromUnit);
            case "fluid ounces" ->
                convertToFlOunces(volume, fromUnit);
            case "tablespoons" ->
                convertToTablespoons(volume, fromUnit);
            case "teaspoons" ->
                convertToTeaspoons(volume, fromUnit);
            default ->
                -1.0;
        };
    }

    private static double convertSpeed(double speed, String fromUnit, String toUnit) {
        return switch (toUnit) {
            case "miles per hour" ->
                convertToMilesPerHour(speed, fromUnit);
            case "kilometers per hour" ->
                convertToKilometersPerHour(speed, fromUnit);
            case "meters per second" ->
                convertToMetersPerSecond(speed, fromUnit);
            case "feet per second" ->
                convertToFeetPerSecond(speed, fromUnit);
            case "knots" ->
                convertToKnots(speed, fromUnit);
            default ->
                -1.0;
        };
    }

    private static double convertToMiles(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "kilometers" ->
                distance * 0.621371;
            case "yards" ->
                distance / 1760;
            case "meters" ->
                distance / 1609.34;
            case "feet" ->
                distance / 5280;
            case "inches" ->
                distance / 63360;
            default ->
                -1.0;
        };
    }

    private static double convertToKilometers(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "miles" ->
                distance / 0.621371;
            case "yards" ->
                distance / 1093.61;
            case "meters" ->
                distance / 1000;
            case "feet" ->
                distance / 3280.84;
            case "inches" ->
                distance / 39370.1;
            default ->
                -1.0;
        };
    }

    private static double convertToYards(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "miles" ->
                distance * 1760;
            case "kilometers" ->
                distance * 1093.61;
            case "meters" ->
                distance * 1.09361;
            case "feet" ->
                distance * 3;
            case "inches" ->
                distance * 36;
            default ->
                -1.0;
        };
    }

    private static double convertToMeters(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "miles" ->
                distance * 1609.34;
            case "kilometers" ->
                distance * 1000;
            case "yards" ->
                distance / 1.09361;
            case "feet" ->
                distance / 3.28084;
            case "inches" ->
                distance / 39.3701;
            default ->
                -1.0;
        };
    }

    private static double convertToFeet(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "miles" ->
                distance * 5280;
            case "kilometers" ->
                distance * 3280.84;
            case "yards" ->
                distance * 3;
            case "meters" ->
                distance * 3.28084;
            case "inches" ->
                distance * 12;
            default ->
                -1.0;
        };
    }

    private static double convertToInches(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "miles" ->
                distance * 63360;
            case "kilometers" ->
                distance * 39370.1;
            case "yards" ->
                distance * 36;
            case "meters" ->
                distance * 39.3701;
            case "feet" ->
                distance * 12;
            default ->
                -1.0;
        };
    }

    private static double convertToCentimeters(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "miles" ->
                distance * 160934;
            case "kilometers" ->
                distance * 100000;
            case "yards" ->
                distance * 91.44;
            case "meters" ->
                distance * 100;
            case "feet" ->
                distance * 30.48;
            case "inches" ->
                distance * 2.54;
            default ->
                -1.0;
        };
    }

    private static double convertToMillimeters(double distance, String fromUnit) {
        return switch (fromUnit) {
            case "miles" ->
                distance * 1609340;
            case "kilometers" ->
                distance * 1000000;
            case "yards" ->
                distance * 914.4;
            case "meters" ->
                distance * 1000;
            case "feet" ->
                distance * 304.8;
            case "inches" ->
                distance * 25.4;
            default ->
                -1.0;
        };
    }

    private static double convertToCelsius(double temperature, String fromUnit) {
        return switch (fromUnit) {
            case "fahrenheit" ->
                (temperature - 32) * 5 / 9;
            case "kelvin" ->
                temperature - 273.15;
            default ->
                -1.0;
        };
    }

    private static double convertToFahrenheit(double temperature, String fromUnit) {
        return switch (fromUnit) {
            case "celsius" ->
                temperature * 9 / 5 + 32;
            case "kelvin" ->
                temperature * 9 / 5 - 459.67;
            default ->
                -1.0;
        };
    }

    private static double convertToKelvin(double temperature, String fromUnit) {
        return switch (fromUnit) {
            case "celsius" ->
                temperature + 273.15;
            case "fahrenheit" ->
                (temperature + 459.67) * 5 / 9;
            default ->
                -1.0;
        };
    }

    private static double convertToPounds(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "kilograms" ->
                weight * 2.20462;
            case "ounces" ->
                weight / 16;
            case "grams" ->
                weight / 453.592;
            case "milligrams" ->
                weight / 453592;
            case "micrograms" ->
                weight / 453592000;
            case "stones" ->
                weight * 0.0714286;
            case "metric tons" ->
                weight * 2204.62;
            default ->
                -1.0;
        };
    }

    private static double convertToKilograms(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "pounds" ->
                weight / 2.20462;
            case "ounces" ->
                weight / 35.274;
            case "grams" ->
                weight / 1000;
            case "milligrams" ->
                weight / 1000000;
            case "micrograms" ->
                weight / 1000000000;
            case "stones" ->
                weight * 6.35029;
            case "metric tons" ->
                weight * 1000;
            default ->
                -1.0;
        };
    }

    private static double convertToOunces(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "pounds" ->
                weight * 16;
            case "kilograms" ->
                weight * 35.274;
            case "grams" ->
                weight / 28.3495;
            case "milligrams" ->
                weight / 28349.5;
            case "micrograms" ->
                weight / 28349500;
            case "stones" ->
                weight * 224;
            case "metric tons" ->
                weight * 35274;
            default ->
                -1.0;
        };
    }

    private static double convertToGrams(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "pounds" ->
                weight * 453.592;
            case "kilograms" ->
                weight * 1000;
            case "ounces" ->
                weight * 28.3495;
            case "milligrams" ->
                weight / 1000;
            case "micrograms" ->
                weight / 1000000;
            case "stones" ->
                weight * 6350.29;
            case "metric tons" ->
                weight * 1000000;
            default ->
                -1.0;
        };
    }

    private static double convertToMilligrams(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "pounds" ->
                weight * 453592;
            case "kilograms" ->
                weight * 1000000;
            case "ounces" ->
                weight * 28349.5;
            case "grams" ->
                weight * 1000;
            case "micrograms" ->
                weight / 1000;
            case "stones" ->
                weight * 6350290;
            case "metric tons" ->
                weight * 1000000000;
            default ->
                -1.0;
        };
    }

    private static double convertToMicrograms(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "pounds" ->
                weight * 453592000;
            case "kilograms" ->
                weight * 1000000000;
            case "ounces" ->
                weight * 28349500;
            case "grams" ->
                weight * 1000000;
            case "milligrams" ->
                weight * 1000;
            case "stones" ->
                weight * 6350290000.0;
            case "metric tons" ->
                weight * 1000000000000.0;
            default ->
                -1.0;
        };
    }

    private static double convertToStones(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "pounds" ->
                weight * 0.0714286;
            case "kilograms" ->
                weight * 0.157473;
            case "ounces" ->
                weight / 224;
            case "grams" ->
                weight / 6350.29;
            case "milligrams" ->
                weight / 6350290;
            case "micrograms" ->
                weight / 6350290000.0;
            case "metric tons" ->
                weight * 157.473;
            default ->
                -1.0;
        };
    }

    private static double convertToMetricTons(double weight, String fromUnit) {
        return switch (fromUnit) {
            case "pounds" ->
                weight / 2204.62;
            case "kilograms" ->
                weight / 1000;
            case "ounces" ->
                weight / 35274;
            case "grams" ->
                weight / 1000000;
            case "milligrams" ->
                weight / 1000000000;
            case "micrograms" ->
                weight / 1000000000000.0;
            case "stones" ->
                weight / 157.473;
            default ->
                -1.0;
        };
    }

    private static double convertToGallons(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "liters" ->
                volume * 0.264172;
            case "quarts" ->
                volume / 4;
            case "pints" ->
                volume / 8;
            case "cups" ->
                volume / 16;
            case "fluid ounces" ->
                volume / 128;
            case "tablespoons" ->
                volume / 256;
            case "teaspoons" ->
                volume / 768;
            default ->
                -1.0;
        };
    }

    private static double convertToLiters(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "gallons" ->
                volume / 0.264172;
            case "quarts" ->
                volume / 1.05669;
            case "pints" ->
                volume / 2.11338;
            case "cups" ->
                volume / 4.22675;
            case "fluid ounces" ->
                volume / 33.814;
            case "tablespoons" ->
                volume / 67.628;
            case "teaspoons" ->
                volume / 202.884;
            default ->
                -1.0;
        };
    }

    private static double convertToQuarts(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "gallons" ->
                volume * 4;
            case "liters" ->
                volume * 1.05669;
            case "pints" ->
                volume / 2;
            case "cups" ->
                volume / 4;
            case "fluid ounces" ->
                volume / 32;
            case "tablespoons" ->
                volume / 64;
            case "teaspoons" ->
                volume / 192;
            default ->
                -1.0;
        };
    }

    private static double convertToPints(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "gallons" ->
                volume * 8;
            case "liters" ->
                volume * 2.11338;
            case "quarts" ->
                volume * 2;
            case "cups" ->
                volume / 2;
            case "fluid ounces" ->
                volume / 16;
            case "tablespoons" ->
                volume / 32;
            case "teaspoons" ->
                volume / 96;
            default ->
                -1.0;
        };
    }

    private static double convertToCups(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "gallons" ->
                volume * 16;
            case "liters" ->
                volume * 4.22675;
            case "quarts" ->
                volume * 4;
            case "pints" ->
                volume * 2;
            case "fluid ounces" ->
                volume / 8;
            case "tablespoons" ->
                volume / 16;
            case "teaspoons" ->
                volume / 48;
            default ->
                -1.0;
        };
    }

    private static double convertToFlOunces(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "gallons" ->
                volume * 128;
            case "liters" ->
                volume * 33.814;
            case "quarts" ->
                volume * 32;
            case "pints" ->
                volume * 16;
            case "cups" ->
                volume * 8;
            case "tablespoons" ->
                volume / 2;
            case "teaspoons" ->
                volume / 6;
            default ->
                -1.0;
        };
    }

    private static double convertToTablespoons(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "gallons" ->
                volume * 256;
            case "liters" ->
                volume * 67.628;
            case "quarts" ->
                volume * 64;
            case "pints" ->
                volume * 32;
            case "cups" ->
                volume * 16;
            case "fluid ounces" ->
                volume * 2;
            case "teaspoons" ->
                volume / 3;
            default ->
                -1.0;
        };
    }

    private static double convertToTeaspoons(double volume, String fromUnit) {
        return switch (fromUnit) {
            case "gallons" ->
                volume * 768;
            case "liters" ->
                volume * 202.884;
            case "quarts" ->
                volume * 192;
            case "pints" ->
                volume * 96;
            case "cups" ->
                volume * 48;
            case "fluid ounces" ->
                volume * 6;
            case "tablespoons" ->
                volume * 3;
            default ->
                -1.0;
        };
    }

    private static double convertToMilesPerHour(double speed, String fromUnit) {
        return switch (fromUnit) {
            case "kilometers per hour" ->
                speed / 1.60934;
            case "meters per second" ->
                speed * 2.23694;
            case "feet per second" ->
                speed * 1.46667;
            case "knots" ->
                speed * 1.15078;
            default ->
                -1.0;
        };
    }

    private static double convertToKilometersPerHour(double speed, String fromUnit) {
        return switch (fromUnit) {
            case "miles per hour" ->
                speed * 1.60934;
            case "meters per second" ->
                speed * 3.6;
            case "feet per second" ->
                speed * 1.09728;
            case "knots" ->
                speed * 1.852;
            default ->
                -1.0;
        };
    }

    private static double convertToMetersPerSecond(double speed, String fromUnit) {
        return switch (fromUnit) {
            case "miles per hour" ->
                speed / 2.23694;
            case "kilometers per hour" ->
                speed / 3.6;
            case "feet per second" ->
                speed / 3.28084;
            case "knots" ->
                speed / 1.94384;
            default ->
                -1.0;
        };
    }

    private static double convertToFeetPerSecond(double speed, String fromUnit) {
        return switch (fromUnit) {
            case "miles per hour" ->
                speed / 1.46667;
            case "kilometers per hour" ->
                speed / 1.09728;
            case "meters per second" ->
                speed * 3.28084;
            case "knots" ->
                speed / 1.68781;
            default ->
                -1.0;
        };
    }

    private static double convertToKnots(double speed, String fromUnit) {
        return switch (fromUnit) {
            case "miles per hour" ->
                speed / 1.15078;
            case "kilometers per hour" ->
                speed / 1.852;
            case "meters per second" ->
                speed * 1.94384;
            case "feet per second" ->
                speed * 1.68781;
            default ->
                -1.0;
        };
    }
}
