import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Main {

    private static final Random random = new Random(System.currentTimeMillis());
     
    enum Agent {
        A,
        B
    }

    enum RoomState {
        D,
        C
    }

    enum Action {
        suck,
        right,
        left,
        noop
    }

    public static void main(String[] args) {
        
        if (args.length != 3) {
            System.out.println("Please enter Pa, Pb and Pc arguments.");
            System.exit(1);
        }
 
        final double[] actualProbabilities = {Double.parseDouble(args[0]),Double.parseDouble(args[1]),Double.parseDouble(args[2])};

        if(!isValidArguments(actualProbabilities)) {
            System.out.println("Probability arguments should be between 0 and 1!");
            System.exit(1);
        }
        double[] agentBeliefs = {1, 1, 1};
        int[] visited_count = {1, 1, 1};

        simulate(Agent.A, actualProbabilities, agentBeliefs.clone(), visited_count.clone());
        simulate(Agent.B, actualProbabilities, agentBeliefs.clone(), visited_count.clone());

    }

    static void simulate(Agent agent, double[] dirtyProbabilities, double[] agentBeliefs, int[] visited_count) {
        RoomState[] rooms = {RoomState.D, RoomState.D, RoomState.D}; // indicate being dirty with D, and being clean with C.
        int position = 1; // initially, the agent is in Room B.

        FileWriter writer;
        try {
            writer = new FileWriter(agent == Agent.A ? "a.txt" : "b.txt");
        } catch (IOException e) {
            throw new RuntimeException("Error creating the FileWriter.");
        }

        double totalScoreOfAgent = 0;
        Action action;
        for (int i = 0; i < 1000; i++) {

            if(rooms[position] == RoomState.D) {
                updateAgentBeliefs(agentBeliefs, position, visited_count);
                 action = Action.suck;
            }
            else {
                action = getAgentAction(rooms, position, agentBeliefs);
                visited_count[position]++;
            
            }


            var initialWorldCondition = getCurrentWorld(rooms, position);

            switch (action) {
                case suck: rooms[position] = RoomState.C; break;// clean current room
                case left: --position; break;// move to one left
                case right: ++position; break;// move to one right
                default: break;
            }

            double penalty = 0;
            var scoreForCurrentRooms = calculateAgentScoreForCurrentRooms(rooms); // get one point for each clean room

            if (agent == Agent.B) {
                if (action == Action.left || action == Action.right) { // if it is Agent B and if moved, apply move penalty
                    penalty = 0.5;
                }
            }

            var finalScoreForThisStep = scoreForCurrentRooms - penalty; // get eventual score of this step

            totalScoreOfAgent += finalScoreForThisStep; // add it to total score

            try {
                writer.write(initialWorldCondition + "\n" + action + "\n" + finalScoreForThisStep + "\n"); // append to the corresponding file
            } catch (java.io.IOException e) {
                System.out.println("File writing error!");
            }

            makeRandomRoomsDirty(rooms, dirtyProbabilities, position);
        }

        try {
            writer.write("Total score for this agent: " + totalScoreOfAgent); // append total score
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing the file resource.");
        }

        //System.out.println(Arrays.toString(agentBeliefs).replaceAll("[\\[\\],]", ""));
        System.out.println("Total score of the Agent "+agent+": "+totalScoreOfAgent);

       
    }

    static void updateAgentBeliefs( double[] agentBeliefs, int position, int[] visited_count) {
    
        double newChanceData = 1.0/visited_count[position];
        agentBeliefs[position] = ((agentBeliefs[position]*1.2)+(newChanceData*0.8))/2.0;
        visited_count[position] = 1;
        //System.out.println(agentBeliefs[0] +  "  "  +agentBeliefs[1] +  "  "  +  agentBeliefs[2]);
        
    }

    

    static String getCurrentWorld(RoomState[] rooms, int position) { // get current environment (i.e., B, D, C, C.) check project spec sample output.

        String currentRoom = "";
        switch (position) {
            case 0:
                currentRoom = "A";
                break;
            case 2: 
                currentRoom = "C";
                break;

            default: currentRoom = "B";
        };

        return currentRoom + ", " + rooms[0] + ", " + rooms[1] + ", " + rooms[2];

    }


    public static int findMaxPositionInArray(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }

        int maxPosition = 0;
        double max = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxPosition = i;
            }
        }

        return maxPosition;
    }


    static Action getAgentAction(RoomState[] rooms, int position, double[] agentBeliefs) {

        if (position == 0) {

            if(agentBeliefs[position] >= agentBeliefs[1] + agentBeliefs[2])
                    return Action.noop;        
            else return Action.right;
        }

        else if (position == 2) {

            if(agentBeliefs[position] >= agentBeliefs[1] + agentBeliefs[0])
                    return Action.noop;        
            else return Action.left;
        }

        else {

            int maxpos = findMaxPositionInArray(agentBeliefs);
            if(maxpos == 1) return Action.noop;
            else if (maxpos == 0) return Action.left;
            else return Action.right;
        }
    }

    static int calculateAgentScoreForCurrentRooms(RoomState[] rooms) { // calculate total score for the current state of the env
        int totalScore = 0;

        for(RoomState r: rooms) {
            if ( r == RoomState.C ) {
                ++totalScore; // increase score for every clean room.
            }
        }
        return totalScore;
    }

    static void makeRandomRoomsDirty(RoomState[] rooms, double[] beingDirtyProbabilities, int position) { // make rooms dirty with the probability provided by command line arguments
        for ( int i = 0; i < rooms.length; i++) {
            double randomValue = 0.1 * random.nextInt(11);
            if (randomValue <= beingDirtyProbabilities[i]) {
                rooms[i] = RoomState.D;
            }
        }

    }

    static boolean isValidArguments(double[] args) { // probabilities should be between 0 and 1
        var p1 = args[0];
        var p2 = args[1];
        var p3 = args[2];

        return !(p1 < 0) && !(p2 < 0) && !(p3 < 0) && !(p1 > 1) && !(p2 > 1) && !(p3 > 1);
    }


}
