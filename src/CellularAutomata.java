
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CellularAutomata {
    private int BOARD_ROWS = 100;
    private int BOARD_COLS = 100;
    private int[][] currentBoard = createEmptyBoard();
    private int[][] nextBoard = createEmptyBoard();
    private final ButtonGroup radioButtonGrp;
    private final JLabel generations;
    private int generationCounter = 0;
    private final Timer timer;
    private final JSlider speed;
    private final JComboBox<Integer> brushSizeList;

    public CellularAutomata() {
        JFrame window = new JFrame("Cellular Automata");
        DrawPane drawPane = new DrawPane();
        int WINDOW_WIDTH = 900;
        int WINDOW_HEIGHT = 700;
        window.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        window.setResizable(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setLayout(null);

        JLabel States = new JLabel("States");
        States.setSize(100, 40);
        States.setLocation(635, 110);
        window.add(States);

        JRadioButton RadioButtonDead = new JRadioButton("Dead");
        RadioButtonDead.setSize(100, 40);
        RadioButtonDead.setLocation(635, 140);

        JRadioButton RadioButtonAlive = new JRadioButton("Alive");
        RadioButtonAlive.setSize(100, 40);
        RadioButtonAlive.setLocation(635, 170);

        JRadioButton RadioButtonDying = new JRadioButton("Dying");
        RadioButtonDying.setSize(100, 40);
        RadioButtonDying.setLocation(635, 200);
        RadioButtonDying.setVisible(false);

        radioButtonGrp = new ButtonGroup();
        radioButtonGrp.add(RadioButtonDead);
        radioButtonGrp.add(RadioButtonAlive);
        radioButtonGrp.add(RadioButtonDying);
        radioButtonGrp.setSelected(RadioButtonAlive.getModel(), true);

        window.add(RadioButtonDead);
        window.add(RadioButtonAlive);
        window.add(RadioButtonDying);

        JButton nextButton = new JButton("Next");
        nextButton.setSize(60, 25);
        nextButton.setLocation(635, 20);
        window.add(nextButton);

        JButton PlayButton = new JButton("Play");
        PlayButton.setSize(60, 25);
        PlayButton.setLocation(735, 20);
        window.add(PlayButton);

        JButton StopButton = new JButton("Stop");
        StopButton.setSize(60, 25);
        StopButton.setLocation(735, 60);
        window.add(StopButton);

        JButton ClearButton = new JButton("Clear");
        ClearButton.setSize(70, 25);
        ClearButton.setLocation(805, 20);
        window.add(ClearButton);

        ClearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                drawPane.clearCanvas(drawPane.getGraphics());
                currentBoard = createEmptyBoard();
                drawPane.setBoard(currentBoard);

                generationCounter = 0;
                generations.setText("generations: " + generationCounter);
            }
        });

        generations = new JLabel("generations: 0");
        generations.setSize(100, 30);
        generations.setLocation(735, 110);
        window.add(generations);

        JLabel speedText = new JLabel("Speed");
        speedText.setSize(100, 30);
        speedText.setLocation(735, 150);
        window.add(speedText);

        speed = new JSlider();
        speed.setSize(120, 30);
        speed.setLocation(735, 190);
        speed.setMajorTickSpacing(125);
        speed.setMinorTickSpacing(25);
        speed.setValue(1);
        speed.setMaximum(250);
        speed.setMinimum(1);

        JLabel value = new JLabel("value: " + speed.getValue());
        value.setSize(100, 30);
        value.setLocation(735, 225);
        window.add(value);

        speed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                value.setText("value: " + speed.getValue());
                timer.setDelay(speed.getValue());
            }
        });

        speed.setPaintLabels(true);
        window.add(speed);

        String[] automataChoice = {"Game of life", "Brians brain", "Seeds"};
        JComboBox<String> automataList = new JComboBox<>(automataChoice);
        automataList.setSize(100, 50);
        automataList.setLocation(635, 300);
        automataList.setSelectedIndex(0);
        window.add(automataList);

        Integer[] brushSize = {1, 2, 4, 8};
        brushSizeList = new JComboBox<>(brushSize);
        brushSizeList.setSize(100, 50);
        brushSizeList.setLocation(755, 300);
        brushSizeList.setSelectedIndex(0);
        window.add(brushSizeList);
        window.add(drawPane);

        final ArrayList<TreeMap<String, Integer>> GoL = new ArrayList<>();
        final ArrayList<TreeMap<String, Integer>> briansBrain = new ArrayList<>();
        final ArrayList<TreeMap<String, Integer>> seeds = new ArrayList<>();

        TreeMap<String, Integer> loseState = new TreeMap<>();
        TreeMap<String, Integer> winState = new TreeMap<>();
        loseState.put("53", 1);
        loseState.put("default", 0);
        winState.put("62", 1);
        winState.put("53", 1);
        winState.put("default", 0);
        GoL.add(loseState);
        GoL.add(winState);

        TreeMap<String, Integer> loseStateSeeds = new TreeMap<>();
        TreeMap<String, Integer> winStateSeeds = new TreeMap<>();
        loseStateSeeds.put("62", 1);
        loseStateSeeds.put("default", 0);
        winStateSeeds.put("default", 0);
        seeds.add(loseStateSeeds);
        seeds.add(winStateSeeds);

        TreeMap<String, Integer> loseStateBrian = new TreeMap<>();
        TreeMap<String, Integer> winStateBrian = new TreeMap<>();
        TreeMap<String, Integer> dyingStateBrian = new TreeMap<>();
        loseStateBrian.put("026", 1);
        loseStateBrian.put("125", 1);
        loseStateBrian.put("224", 1);
        loseStateBrian.put("323", 1);
        loseStateBrian.put("422", 1);
        loseStateBrian.put("521", 1);
        loseStateBrian.put("620", 1);
        loseStateBrian.put("default", 0);
        winStateBrian.put("default", 2);
        dyingStateBrian.put("default", 0);
        briansBrain.add(loseStateBrian);
        briansBrain.add(winStateBrian);
        briansBrain.add(dyingStateBrian);

        AtomicReference<ArrayList<TreeMap<String, Integer>>> chosenAutomata = new AtomicReference<>(GoL);

        automataList.addActionListener(e -> {
            int automataIndex = automataList.getSelectedIndex();

            if (automataIndex == 1) {
                chosenAutomata.set(briansBrain);
                RadioButtonDying.setVisible(true);
            } else if (automataIndex == 2) {
                chosenAutomata.set(seeds);
                RadioButtonDying.setVisible(false);
            } else if (automataIndex == 0) {
                chosenAutomata.set(GoL);
                RadioButtonDying.setVisible(false);
            }
        });

        nextButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                computeNextBoard(chosenAutomata.get(), currentBoard, nextBoard);

                int[][] temp = currentBoard;
                currentBoard = nextBoard;
                nextBoard = temp;

                drawPane.setBoard(currentBoard);
                drawPane.paint(drawPane.getGraphics());

                generationCounter++;
                String counter = String.format("generations: %d", generationCounter);
                generations.setText(counter);
            }
        });

        ActionListener al = e -> {
            computeNextBoard(chosenAutomata.get(), currentBoard, nextBoard);

            int[][] temp = currentBoard;
            currentBoard = nextBoard;
            nextBoard = temp;

            drawPane.setBoard(currentBoard);
            drawPane.paint(drawPane.getGraphics());

            generationCounter++;
            String counter = String.format("generations: %d", generationCounter);
            generations.setText(counter);
        };

        timer = new Timer(speed.getValue(), al);
        PlayButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timer.start();
            }
        });

        StopButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timer.stop();
            }
        });

        window.setVisible(true);
    }

    private class DrawPane extends JPanel {
        private int[][] board = new int[BOARD_ROWS][BOARD_COLS];
        private final int BOARD_WIDTH = 600;
        private final int BOARD_HEIGHT = 600;
        private int CELL_WIDTH = BOARD_WIDTH / BOARD_ROWS;
        private int CELL_HEIGHT = BOARD_HEIGHT / BOARD_COLS;
        private boolean paintBG = false;
        private final Color[] stateColors = {
                Color.GRAY,
                Color.RED,

                new Color(150, 255, 150),
                new Color(150, 200, 255),
        };

        public DrawPane() {
            setSize(BOARD_WIDTH, BOARD_HEIGHT);
            setBackground(Color.GRAY);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int col = e.getX() / CELL_WIDTH;
                    int row = e.getY() / CELL_HEIGHT;
                    if (0 <= row && row < BOARD_ROWS) {
                        if (0 <= col && col < BOARD_COLS) {
                            brushSize(col, row);
                        }
                    }

                    // if (e.getX() > 0 && e.getX() < BOARD_WIDTH && e.getY() > 0 && e.getY() < BOARD_HEIGHT) {
                    //     // TODO make sure that we are within bounds, or make sure that col computes col correctly
                    //     int col = e.getX() / CELL_WIDTH;
                    //     int row = e.getY() / CELL_HEIGHT;

                    //     System.out.println("ROWS: " + BOARD_ROWS);
                    //     System.out.println("COLS: " + BOARD_COLS);

                    //     if (col < currentBoard.length && row < currentBoard.length) {
                    //         brushSize(col, row);
                    //     }
                    // }
                }

            });

            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int col = e.getX() / CELL_WIDTH;
                    int row = e.getY() / CELL_HEIGHT;
                    if (0 <= row && row < BOARD_ROWS) {
                        if (0 <= col && col < BOARD_COLS) {
                            brushSize(col, row);
                        }
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                }
            });

            /*
            addMouseWheelListener(e -> {
                if (e.getWheelRotation() < 0) {
                    BOARD_ROWS -= 2;
                    BOARD_COLS -= 2;
                } else {
                    BOARD_ROWS += 2;
                    BOARD_COLS += 2;
                }

                CELL_WIDTH = BOARD_WIDTH / BOARD_ROWS;
                CELL_HEIGHT = BOARD_HEIGHT / BOARD_COLS;

                currentBoard = fillEmptyBoard(currentBoard);
                nextBoard = createEmptyBoard();

                paint(getGraphics());
            });
             */

        }

        private void brushSize(int col, int row) {
            int index = brushSizeList.getSelectedIndex();
            int size = brushSizeList.getItemAt(index);

            switch (size) {
                case 1:
                    currentBoard[row][col] = getIndexOfRadioButton();
                    break;
                case 2:
                    currentBoard[row][col] = getIndexOfRadioButton();
                    currentBoard[row - 1][col] = getIndexOfRadioButton();
                    break;
                case 4:
                    currentBoard[row][col] = getIndexOfRadioButton();
                    currentBoard[row - 1][col - 1] = getIndexOfRadioButton();
                    currentBoard[row][col - 1] = getIndexOfRadioButton();
                    currentBoard[row - 1][col] = getIndexOfRadioButton();
                    break;
                case 8:
                    currentBoard[row][col] = getIndexOfRadioButton();
                    currentBoard[row][col - 1] = getIndexOfRadioButton();
                    currentBoard[row - 1][col] = getIndexOfRadioButton();
                    currentBoard[row - 1][col - 1] = getIndexOfRadioButton();

                    currentBoard[row + 1][col] = getIndexOfRadioButton();
                    currentBoard[row + 1][col + 1] = getIndexOfRadioButton();
                    currentBoard[row][col + 1] = getIndexOfRadioButton();
                    currentBoard[row + 1][col] = getIndexOfRadioButton();
            }

            setBoard(currentBoard);
            paint(getGraphics());
        }

        public void setBoard(int[][] board) {
            this.board = board;
        }

        public int getIndexOfRadioButton() {
            Enumeration<AbstractButton> enumeration = radioButtonGrp.getElements();
            int count = 0;

            while (enumeration.hasMoreElements()) {
                if (enumeration.nextElement().isSelected()) {
                    break;
                }
                count++;
            }

            return count;
        }

        @Override
        public void paint(Graphics g) {
            // Rectangle rect = g.getClipBounds();
            // temporary solution to paint the background once...
            if (!paintBG)
                super.paintComponent(g);

            paintBG = true;

            int CELL_WIDTH = BOARD_WIDTH / BOARD_ROWS;
            int CELL_HEIGHT = BOARD_HEIGHT / BOARD_COLS;

            for (int r = 0; r < board.length; ++r) {
                for (int c = 0; c < board[r].length; ++c) {
                    int x = c * CELL_WIDTH;
                    int y = r * CELL_HEIGHT;
                    if (stateColors[currentBoard[r][c]] != stateColors[nextBoard[r][c]]) {
                        g.setColor(stateColors[board[r][c]]);

                        g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                        // rect.setRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                    }
                }
            }
        }

        public void clearCanvas(Graphics g) {
            int CELL_WIDTH = BOARD_WIDTH / BOARD_ROWS;
            int CELL_HEIGHT = BOARD_HEIGHT / BOARD_COLS;

            for (int r = 0; r < board.length; ++r) {
                for (int c = 0; c < board[r].length; ++c) {
                    int x = c * CELL_WIDTH;
                    int y = r * CELL_HEIGHT;

                    if (stateColors[currentBoard[r][c]] != Color.GRAY) {
                        g.setColor(Color.GRAY);
                        g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                    }
                }
            }
        }
    }

    public int[][] createEmptyBoard() {
        int[][] board = new int[BOARD_COLS][BOARD_ROWS];

        for (int[] ints : board) {
            for (int i = 0; i < ints.length; i++)
                ints[i] = 0;
        }

        return board;
    }

    public int[][] fillEmptyBoard(int[][] previousBoard) {
        int[][] board = new int[BOARD_COLS][BOARD_ROWS];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i < previousBoard.length && j < previousBoard.length) {
                    board[i][j] = previousBoard[i][j];
                }
            }
        }

        return board;
    }

    public void computeNextBoard(ArrayList<TreeMap<String, Integer>> automaton, int[][] current, int[][] next) {
        int[] nbors = new int[automaton.size()];

        // NOTE: Padding is used here,
        // essentially using a cell on each side that is not displayed but used for computing
        // so that I can avoid more conditional logic
        for (int r = 1; r < current.length - 1; ++r) {
            for (int c = 1; c < current[r].length - 1; ++c) {

                // All this computes the currentNbors
                for (int i = 0; i < nbors.length; i++)
                    nbors[i] = 0;
                for (int dr = -1; dr <= 1; ++dr) {
                    for (int dc = -1; dc <= 1; ++dc) {
                        if (dr != 0 || dc != 0) {
                            int r0 = dr + r;
                            int c0 = dc + c;
                            nbors[current[r0][c0]]++;
                        }
                    }
                }
                StringBuilder builder = new StringBuilder();
                for (int i : nbors) {
                    builder.append(i);
                }
                String currentNbors = builder.toString();

                TreeMap<String, Integer> transition = automaton.get(current[r][c]);

                Integer temp = transition.get(currentNbors);
                if (temp != null) {
                    next[r][c] = temp;
                }
                if (temp == null) {
                    next[r][c] = transition.get("default"); // .get O("default") ->
                }
            }
        }

    }

    public static void main(String[] args) {
        new CellularAutomata();
    }
}
