package mud.chance;

import java.util.Random;

/**
 * This Roller provides additional functionality for "dice roll" calculations
 * using a shared Random object.
 * 
 * 
 *
 * @author Japhez
 */
public class Roller {

    /*
     I made sure this was threadsafe.
     Likely, this will not use it in the game as it's is used for battle.
     Changed it due to the code analysis warning warning that I can
     weaken the
     ArrayList reference to a regular List and that wasn't desired. It
      suggested changing it to ThreadLocal.
      // TODO: 10/28/18 Possibly remove this along with the weapon stuff. 
      @author Patrick Palczewski
     */

    private static final ThreadLocal<Random> random = new ThreadLocal<>();


    /**
     * Returns a value between the passed lower and upper bounds (inclusive)
     *
     * @param lower the lower value
     * @param upper the upper value
     * @return a random integer between the upper and lower values
     */
    public final int rollBetween(int lower, int upper) {
        //Example lower 5, upper 10
        //10 - 5 = 5 (random number between 0 and 5)
        //We add the lower bound to the result, resulting in a value
        //Between 5 and 10, which is what we want
        return random.get().nextInt(upper - lower) + lower;
    }

    /**
     * Rolls a single die with the given number of sides and returns the result.
     *
     * @param numberOfSides the number of sides on the die
     * @return the resulting die face value
     */
    public final int rollDie(int numberOfSides) {
        return rollDice(1, numberOfSides);
    }

    /**
     * Rolls the given number of dice with the given number of sides and returns
     * the result.
     *
     * @param numberOfDice the number of dice
     * @param numberOfSides the number of sides to each die
     * @return the value of the dice added up
     */
    public final int rollDice(int numberOfDice, int numberOfSides) {
        int value = 0;
        for (int i = 0; i < numberOfDice; i++) {
            value += (random.get().nextInt(numberOfSides) + 1);
        }
        return value;
    }
}
