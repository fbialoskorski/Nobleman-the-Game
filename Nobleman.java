import java.util.Random;
import java.util.Scanner;

/**
 * @author FilipB
 */
public class Nobleman {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private static Random randomGenerator = new Random();
    private static Scanner scanner = new Scanner(System.in);

    private int year = 1;
    private int peasants = 100;
    private int wheat = 2800;
    private int fields = 1000;
    private int fieldPrice;

    private int starved = 0;
    private int plagueVictims = 0;
    private int newPeasants = 5;
    private int wheatMined = 3000;
    private int wheatGeneratedPerField = 3;
    private int amountStolenByThieves = 200;

    private int cashPaidToPeasants;
    private int fieldsMaintained;

    private static final String OGH = "Good day to you, my Lord.";


    public void printIntroductoryParagraph() {
        System.out.println("You have been given an honor to raise the wealth of our kingdom.");
        System.out.println("Our King had given you and your family a piece of land to maintain and take care of.");
        System.out.println("Watch your peasants and they needs, buy additional fields to raise your income");
        System.out.println("Watch out for thieves, crop failures and plagues!");
        System.out.println();
        System.out.println("Food is the general currency, measured in wheat.");
        System.out.println();
        System.out.println("The following will help you in your decisions:");
        System.out.println("   * Each peasant needs at least 20 wheat per year to survive");
        System.out.println("   * Each peasant can maintain at most 10 fields");
        System.out.println("   * It takes 2 food to pay your King a tax for working on a field on his land");
        System.out.println("   * The price for fields fluctuates yearly, depending on kingdom's situation");
        System.out.println();
        System.out.println("Take a good care of our King's land, and you will be rewarded.");
        System.out.println("Do it poorly and you will be banished!");
    }

    public void play() {
        boolean stillInRule = true;

        printIntroductoryParagraph();

        while (year <= 10 && stillInRule) {
            fieldPrice = updateFieldPrice();
            printSummary();
            buyFields();
            sellFields();
            payPeasants();
            maintainFields();

            plagueVictims = checkForPlague();
            peasants = peasants - plagueVictims;

            if (countStarvedPeasants() >= 45) {
                stillInRule = false;
            }

            newPeasants = countNewHires();
            employees += newPeasants;
            cash += mineFood(fieldsMaintained);
            checkForThieves();
            fieldPrice = updateFieldPrice();
            year = year + 1;
        }
        printFinalScore();
    }

    private void printSummary() {
        System.out.print(ANSI_YELLOW);
        System.out.println("___________________________________________________________________");
        System.out.println("\n" + OGH + "!");
        System.out.printf("You are in year %s of your ten year rule.%n", year);

        if (plagueVictims > 0) {
            System.out.printf("A terrible plague wiped out %s of your team.%n", plagueVictims);

        }
        System.out.printf("In the previous year %s of your team starved to death.%n", starved);
        System.out.printf("In the previous year %s peasant(s) have joined our kingdom.%n", newPeasants);
        System.out.printf("The peasants head count is now " + peasants + "%n");
        System.out.printf("We have gathered %s food at %s wheat per field. %n", wheatMined, wheatGeneratedPerField);

        if (amountStolenByThieves > 0) {
            System.out.printf("*** Thieves stole %s food, leaving %s food in your storage.%n", amountStolenByThieves, wheat);
        } else {
            System.out.printf("We have %s of wheat in storage.%n", wheat);
        }
        System.out.printf("The kingdom owns %s fields for growing.%n", fields);
        System.out.printf("Fields currently cost %s wheat each.%n", fieldPrice);
        System.out.println();
        System.out.print(ANSI_RESET);
    }

    private void buyFields() {
        int fieldsToBuy;
        String question = "How many fields will you buy? ";

        fieldsToBuy = getNumber(question);
        int cost = fieldPrice * fieldsToBuy;

        while (cost > wheat) {
            jest(String.format("We have but %s wheat, not %s!", wheat, cost));
            fieldsToBuy = getNumber(question);
            cost = fieldPrice * fieldsToBuy;
        }
        wheat = wheat - cost;
        fields = fields + fieldsToBuy;
        System.out.printf("%s, you now have %s fields %n", OGH, fields);
        System.out.printf("and %s wheat.%n", wheat);
    }

    private void jest(String message) {
        System.out.printf("%s, you are dreaming!%n", OGH);
        System.out.println(message);
    }

    private void sellFields() {
        String question = "How many fields will you sell? ";
        int fieldsToSell = getNumber(question);

        while (fieldsToSell > fields) {
            jest(String.format("The kingdom owns only %s fields!", fields));
            fieldsToSell = getNumber(question);
        }
        wheat = wheat + fieldPrice * fieldsToSell;
        fields = fields - fieldsToSell;
        System.out.printf("%s, you now have %s fields%n", OGH, fields);
        System.out.printf("and %s wheat.%n", wheat);
    }

    private void payEmployees() {
        String question = "How much wheat will you distribute to the peasants? ";
        wheatPaidToPeasants = getNumber(question);

        while (wheatPaidToPeasants > wheat) {
            jest(String.format("We have but %s wheat!", wheat));
            wheatPaidToPeasants = getNumber(question);
        }
        wheat = wheat - wheatPaidToPeasants;
        System.out.printf("%s, %s wheat remain.%n", OGH, wheat);
    }

    private void maintainFields() {
        String question = "How much food will you send to you King? ";
        int maintenanceAmount = 0;
        boolean haveGoodAnswer = false;

        while (!haveGoodAnswer) {
            maintenanceAmount = getNumber(question);
            if (maintenanceAmount > wheat) {
                jest(String.format("We have but %s wheat left!", wheat));
            } else if (maintenanceAmount > 2 * fields) {
                jest(String.format("We have but %s fields available for growing!", fields));
            } else if (maintenanceAmount > 20 * peasants) {
                jest(String.format("We have but %s peasants to maintain the crops!", peasants));
            } else {
                haveGoodAnswer = true;
            }
        }
        fieldsMaintained = maintenanceAmount / 2;
        wheat = wheat - fieldsMaintained * 2;
        System.out.printf("%s, we now have %s wheat in storage.%n", OGH, wheat);
    }

    private int checkForPlague() {
        int victims;

        if (randomGenerator.nextDouble() < 0.15) {
            System.out.println("*** God Save us! A terrible plague wipes out half of the peasants! ***");
            victims = peasants / 2;
        } else {
            victims = 0;
        }
        return victims;
    }

    private int countStarvedPeasants() {
        int peasantsFed = cashPaidToPeasants / 20;
        int percentStarved = 0;

        if (peasantsFed >= peasants) {
            starved = 0;
            System.out.print(ANSI_GREEN);
            System.out.println("The kingdom's peasants are well fed and happy.");
        } else {
            starved = peasants - peasantsFed;
            System.out.print(ANSI_RED);
            System.out.printf("%s peasants starved to death.%n", starved);
            percentStarved = (100 * starved) / peasants;
            peasants = peasants - starved;
        }
        System.out.print(ANSI_RESET);
        return percentStarved;
    }

    private int countNewPeople() {
        int newPeasants;

        if (starved > 0) {
            newPeasants = 0;
        } else {
            newPeasants = (20 * fields + wheat) / (100 * peasants) + 1;
        }
        return newPeasants;
    }

    private int mineCrops(int fields) {
        wheatGeneratedPerField = randomGenerator.nextInt(5) + 1;
        wheatMined = wheatGeneratedPerField * fields;
        return wheatMined;
    }

    private void checkForThieves() {
        if (randomGenerator.nextInt(100) < 40) {
            int percentStolen = 10 + randomGenerator.nextInt(21);
            System.out.printf("*** Thieves steal %s percent of your wheat! ***%n", percentStolen);
            amountStolenByThieves = (percentStolen * wheat) / 100;
            wheat = wheat - amountStolenByThieves;
        } else {
            amountStolenByThieves = 0;
        }
    }

     */
    private int updateFieldPrice() {
        return 17 + randomGenerator.nextInt(10);
    }

    private void printFinalScore() {
        if (starved >= (45 * peasants) / 100) {
            System.out.print(ANSI_RED);
            System.out.println("My once-great lord,");
            System.out.printf("%s of your people starved during the last year of your incompetent reign!%n", starved);
            System.out.println("The few who remained stole your storaged food and left your land!");
            System.out.println();
            System.out.println("Your final rating: TERRIBLE.");
            System.out.print(ANSI_RESET);
            return;
        }

        int fieldScore = fields;
        if (20 * peasants < fieldScore) {
            fieldScore = 20 * peasants;
        }

        if (fieldScore < 600) {
            System.out.printf("Congratulations, %s%n", OGH);
            System.out.println("You have ruled wisely but not well.");
            System.out.println("You have led your people through ten difficult years,");
            System.out.printf("but your land have shrunk to a mere %s crop fields.%n", fields);
            System.out.println();
            System.out.println("Your final rating: ADEQUATE.");
        } else if (fieldScore < 800) {
            System.out.print(ANSI_YELLOW);
            System.out.printf("Congratulations, %s.%n", OGH);
            System.out.println("You  have ruled wisely, and expanded your king's land.");
            System.out.println();
            System.out.println("Your final rating: GOOD.");
        } else {
            System.out.print(ANSI_GREEN);
            System.out.printf("Congratulations, %s%n", OGH);
            System.out.println("You  have ruled wisely and well, earning gratitude of your King.");
            System.out.println("Your house will rule this land on behalf of his majesty for a years to come.");
            System.out.println("Altogether, a most impressive job!");
            System.out.println();
            System.out.println("Your final rating: SUPERB.");
        }
        System.out.print(ANSI_RESET);
    }

    private int getNumber(String message) {
        while (true) {
            System.out.print(message);
            var userInput = scanner.nextLine();
            try {
                return Integer.parseInt(userInput);
            } catch (Exception ignored) {
                System.out.printf("%s isn't a number!%n", userInput);
            }
        }
    }

    public static boolean getYesOrNo(String question) {
        String answer;

        while (true) {
            System.out.printf("%s%n", question);
            answer = scanner.nextLine();
            answer = answer.toLowerCase();

            if (answer.equals("y")) {
                return true;
            }

            if (answer.equals("n")) {
                return false;
            }
        }
    }
}
