package game;
import net.sf.javabdd.BDD;
import tau.smlab.syntech.games.controller.symbolic.SymbolicController;
import tau.smlab.syntech.games.controller.symbolic.SymbolicControllerReaderWriter;
import tau.smlab.syntech.jtlv.BDDPackage;
import tau.smlab.syntech.jtlv.Env;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * holds the UI components and the main application
 *
 */
@SuppressWarnings("serial")
public class Crossroads extends JPanel {
    private GameBoard gameBoard;
    private BDD currentState;
    private SymbolicController ctrl;
    private boolean initialState = true;
    private int lineMax = 5;
    private int controllerInterval = 10;
    private static JSlider verticalSlider;
    private static JSlider horizontalSlider;
    private static JSlider controllerSlider;

    /**
     * constructor.
     * sets up the game board
     * @throws IOException
     */
    private Crossroads() throws IOException {
        gameBoard = new GameBoard();
    }
    /**
     * initialize an infinite loop that iterates every 30 ms:
     * gets user input from sliders
     * sets environment state based on the cars in the game board
     * controls the traffic lights by the spectra synthesized controller in the specified interval
     * repaints the updated game board
     * @throws Exception
     */
    private void run() throws Exception {
        loadController();
        long i = 0;
        while (true) {
            getUserInputFromSliders();
            if (i % controllerInterval == 0) {
                updateSpectraState();
            }
            gameBoard.updateGameBoard();
            i++;
            repaint();
            Thread.sleep(30);
        }
    }
    /**
     * allows the user to interact with the game by setting 3 parameters in the sliders:
     * a. the frequency of the incoming cars in the vertical road
     * b. the frequency of the incoming cars in the horizontal road
     * c. the time interval in which spectra updates the traffic lights
     */
    private void getUserInputFromSliders() {
        if (!verticalSlider.getValueIsAdjusting() & gameBoard.verticalMax != verticalSlider.getValue()) {
            gameBoard.verticalMax = verticalSlider.getValue();
            gameBoard.nextNorth = gameBoard.getRandomInt(gameBoard.verticalMin, gameBoard.verticalMax);
            gameBoard.nextSouth = gameBoard.getRandomInt(gameBoard.verticalMin, gameBoard.verticalMax);
            gameBoard.northTurn = 0;
            gameBoard.southTurn = 0;

        }
        if (!horizontalSlider.getValueIsAdjusting() & gameBoard.horizontalMax != horizontalSlider.getValue()) {
            gameBoard.horizontalMax = horizontalSlider.getValue();
            gameBoard.nextEast = gameBoard.getRandomInt(gameBoard.horizontalMin, gameBoard.horizontalMax);
            gameBoard.nextWest = gameBoard.getRandomInt(gameBoard.horizontalMin, gameBoard.horizontalMax);
            gameBoard.eastTurn = 0;
            gameBoard.westTurn = 0;

        }
        if (!controllerSlider.getValueIsAdjusting() & controllerInterval != controllerSlider.getValue()) {
            controllerInterval = controllerSlider.getValue();
        }
    }
    /**
     * initializes the spectra controller from the synthesized spectra file
     */
    private void loadController() {
        BDDPackage.setCurrPackage(BDDPackage.JTLV);
        try {
            ctrl = SymbolicControllerReaderWriter.readSymbolicController("out/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentState = ctrl.initial().id();
        initialState = true;
    }
    /**
     * where the magic happens.
     * based on the environment state (cars on the road) 
     * determines the possible next traffic light actions
     * based on the spectra spec.
     * chooses one of the possible actions randomly.
     * 
     * @throws InterruptedException
     */
    private void updateSpectraState() throws InterruptedException {
        if (initialState) {
            BDD one = currentState.satOne(Env.globalUnprimeVars());
            currentState.free();
            currentState = one;
            initialState = false;
        } else {
            BDD succs = ctrl.succ(currentState);
            BDD succsWithVehicles = setVehiclesState(succs);
            succs.free();
            java.util.List<BDD> systemChoices = new ArrayList<>();
            BDD.BDDIterator it = new BDD.BDDIterator(succsWithVehicles, Env.globalUnprimeVars());
            while (it.hasNext()) {
                systemChoices.add(it.next());
            }
            int pick = (int) Math.floor(Math.random() * systemChoices.size());
            currentState = systemChoices.get(pick).id();
            Env.free(systemChoices);
            succsWithVehicles.free();
        }
        String state = currentState.toStringWithDomains(Env.stringer);
        String[] stateVals = state.replace("<", "").replace(">", "").replace(" ", "").split(",");
        SystemState systemState = getSystemState(stateVals);
        controlLightsWithSpectra(systemState);
    }

    /**
     * prints all of the variable values in a nice string
     * and gets the actual values of the system vars to control the traffic lights.
     * @param stateVals the values of the current state
     * @return an object of the current SystemState
     */
    private SystemState getSystemState(String[] stateVals) {
        Color verticalLight = null;
        Color horizontalLight = null;
        int verticalQueue = 0;
        int horizontalQueue = 0;
        int verticalBlinks = 0;
        int horizontalBlinks = 0;
        boolean verticalCarCrossing = false;
        boolean horizontalCarCrossing = false;
        for (String s : stateVals) {
            String[] val = s.split(":");
            if ("verticalLights".equals(val[0])) {
                verticalLight = Color.valueOf(val[1]);
                continue;
            }
            if ("horizontalLights".equals(val[0])) {
                horizontalLight = Color.valueOf(val[1]);
                continue;
            }
            if ("carsWaitingInVerticalRoad".equals(val[0])) {
                verticalQueue = Integer.valueOf(val[1]);
                continue;
            }
            if ("carsWaitingInHorizontalRoad".equals(val[0])) {
                horizontalQueue = Integer.valueOf(val[1]);
                continue;
            }
            if ("verticalBlinks".equals(val[0])) {
                verticalBlinks = Integer.valueOf(val[1]);
                continue;
            }
            if ("horizontalBlinks".equals(val[0])) {
                horizontalBlinks = Integer.valueOf(val[1]);
                continue;
            }
            if ("verticalCarCrossing".equals(val[0])) {
                verticalCarCrossing = Boolean.valueOf(val[1]);
                continue;
            }
            if ("horizontalCarCrossing".equals(val[0])) {
                horizontalCarCrossing = Boolean.valueOf(val[1]);
            }
        }
        System.out.println("vertical: " + verticalLight + " horizontal: " + horizontalLight +
                " vertical queue: " + verticalQueue + " horizontal queue: " + horizontalQueue +
                " vertical blinks: " + verticalBlinks + " horizontal blinks: " + horizontalBlinks +
                " vertical crossing: " + verticalCarCrossing + " horizontal crossing: " + horizontalCarCrossing);
        return new SystemState(verticalLight, horizontalLight);
    }

    /**
     * sets the environment variables in the synthesized spec according to the cars on the board
     * calculates number of waiting cars in each road as the sum of waiting cars on both ways.
     * @param succs the next allowed states
     * @return the subset of states filtered to the newly assigned environment vars
     */
    private BDD setVehiclesState(BDD succs) {
        int waitingNorth = gameBoard.getIntersection().getWaitingList(Direction.SOUTH).size();
        int waitingSouth = gameBoard.getIntersection().getWaitingList(Direction.NORTH).size();
        int waitingEast = gameBoard.getIntersection().getWaitingList(Direction.WEST).size();
        int waitingWest = gameBoard.getIntersection().getWaitingList(Direction.EAST).size();
        int verticalWaiting = waitingNorth + waitingSouth < lineMax ? waitingNorth + waitingSouth : lineMax;
        int horizontalWaiting = waitingEast + waitingWest < lineMax ? waitingEast + waitingWest : lineMax;
        String carMainCrossing = String.valueOf(gameBoard.isVerticalPassing());
        String carSideCrossing = String.valueOf(gameBoard.isHorizontalPassing());
        return succs.and(Env.getBDDValue("carsWaitingInVerticalRoad", verticalWaiting))
                .and(Env.getBDDValue("carsWaitingInHorizontalRoad", horizontalWaiting))
                .and(Env.getBDDValue("verticalCarCrossing", carMainCrossing))
                .and(Env.getBDDValue("horizontalCarCrossing", carSideCrossing));
    }
    
    /**
     * controls the traffic lights according to the selected system state.
     * @param systemState
     */
    private void controlLightsWithSpectra(SystemState systemState) {
        gameBoard.getIntersection().getEntrance(Direction.NORTH).setLight(systemState.getVerticalLight());
        gameBoard.getIntersection().getEntrance(Direction.SOUTH).setLight(systemState.getVerticalLight());
        gameBoard.getIntersection().getEntrance(Direction.EAST).setLight(systemState.getHorizontalLight());
        gameBoard.getIntersection().getEntrance(Direction.WEST).setLight(systemState.getHorizontalLight());
    }
    /**
     * sets up the UI of the application
     * @param crossroadsGame an instance of the game board
     */
    private static void createAndShowGUI(Crossroads crossroadsGame) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        JFrame window = new JFrame("Crossroads");
        window.setLayout(new FlowLayout());
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(40, new JLabel("High"));
        labelTable.put(1000, new JLabel("Low"));

        verticalSlider = new JSlider(JSlider.HORIZONTAL, 40, 1000, 75);
        verticalSlider.setInverted(true);
        verticalSlider.setLabelTable(labelTable);
        verticalSlider.setPaintLabels(true);

        horizontalSlider = new JSlider(JSlider.HORIZONTAL, 40, 1000, 75);
        horizontalSlider.setInverted(true);
        horizontalSlider.setLabelTable(labelTable);
        horizontalSlider.setPaintLabels(true);

        Hashtable<Integer, JLabel> controllerlabelTable = new Hashtable<>();
        controllerlabelTable.put(1, new JLabel("0.03"));
        controllerlabelTable.put(33, new JLabel("1"));
        controllerlabelTable.put(66, new JLabel("2"));

        controllerSlider = new JSlider(JSlider.HORIZONTAL, 1, 66, 10);
        controllerSlider.setInverted(true);
        controllerSlider.setLabelTable(controllerlabelTable);
        controllerSlider.setPaintLabels(true);

        JLabel verticalSliderLabel = new JLabel("Vertical Car Frequency");
        JLabel horizontalSliderLabel = new JLabel("Horizontal Car Frequency");
        JLabel controlSliderLabel = new JLabel("traffic control interval (sec)");

        controlPanel.add(controlSliderLabel);
        controlPanel.add(controllerSlider);
        controlPanel.add(verticalSliderLabel);
        controlPanel.add(verticalSlider);
        controlPanel.add(horizontalSliderLabel);
        controlPanel.add(horizontalSlider);

        window.add(controlPanel);
        window.add(crossroadsGame);
        window.pack();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setResizable(false);
    }
    /**
     * finds the maximum number of cars specified in the spectra file
     * in type CarsCount = Int(0..<max number>);
     * using regex. 
     * @param line: a chunk of the spectra file
     * @return the max number of cars or -1 if no match was found
     */
    private int findMaxLineCount(String line) {
        int result = -1;
        line = line.replaceAll("\\s+", "");
        Pattern p = Pattern.compile("(?<=CarsCount=Int\\([0-9]\\.\\.)[0-9]+(?=\\))");
        Matcher m = p.matcher(line);
        if (m.find()) {
            String foundPattern = m.group();
            result = Integer.parseInt(foundPattern);
        }
        return result;
    }
    /**
     * gets the maximum number of cars allowed in the spectra file
     * in type CarsCount = Int(0..<max number>);
     * and overrides the default value of 5.
     * prevents an overflow of value not expected by the spec.
     * @throws IOException
     */
    private void getMaxCarsFromSpectraFile() throws IOException {
        String fileToBeExtracted = "SpectraCrossroads/CrossRoads.spectra";
        String zipPackage = "./out/spec.zip";
        FileInputStream fileInputStream = new FileInputStream(zipPackage);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ZipInputStream zin = new ZipInputStream(bufferedInputStream);
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.getName().equals(fileToBeExtracted)) {
                byte[] buffer = new byte[9000];
                while ((zin.read(buffer)) != -1) {
                    String spec = new String(buffer);
                    int newMax = findMaxLineCount(spec);
                    if (newMax > 0) {
                        lineMax = newMax;
                        break;
                    }
                }
                break;
            }
        }
        zin.close();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
        gameBoard.draw(g);
    }

    public static void main(String[] args) throws Exception {
        Crossroads crossroadsGame = new Crossroads();
        crossroadsGame.getMaxCarsFromSpectraFile();
        createAndShowGUI(crossroadsGame);
        crossroadsGame.run();
    }


}
