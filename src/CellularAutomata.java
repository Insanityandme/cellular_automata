import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CellularAutomata {
    private final int BOARD_ROWS = 48;
    private final int BOARD_COLS = BOARD_ROWS;
    private int[][] currentBoard = createEmptyBoard();
    private int[][] nextBoard = createEmptyBoard();
    private final ArrayList<HashMap<String, Integer>> GoL;
    private final ArrayList<HashMap<String, Integer>> briansBrain;
    private final ArrayList<HashMap<String, Integer>> seeds;
    private final ButtonGroup radioButtonGrp;
    private final JLabel generations;
    private int generationCounter = 0;
    private final Timer timer;

    private final JSlider speed;

    public CellularAutomata() {
        JFrame window = new JFrame("Cellular Automata");
        DrawPane drawPane = new DrawPane();
        int WINDOW_WIDTH = 900;
        int WINDOW_HEIGHT = 700;
        window.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        window.setResizable(false);
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
                currentBoard = createEmptyBoard();
                drawPane.setBoard(currentBoard);
                drawPane.paintComponent(drawPane.getGraphics());
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
        speed.setValue(100);
        speed.setMaximum(250);
        speed.setMinimum(0);

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

        String[] choices = {"Game of life", "Brians brain", "Seeds"};
        JComboBox<String> automataList = new JComboBox<>(choices);
        automataList.setSize(100, 50);
        automataList.setLocation(635, 300);
        automataList.setSelectedIndex(0);
        window.add(automataList);

        window.add(drawPane);

        GoL = new ArrayList<>();
        HashMap<String, Integer> loseState = new HashMap<>();
        HashMap<String, Integer> winState = new HashMap<>();
        loseState.put("53", 1);
        loseState.put("default", 0);
        winState.put("62", 1);
        winState.put("53", 1);
        winState.put("default", 0);
        GoL.add(loseState);
        GoL.add(winState);

        seeds = new ArrayList<>();
        HashMap<String, Integer> loseStateSeeds = new HashMap<>();
        HashMap<String, Integer> winStateSeeds = new HashMap<>();
        loseStateSeeds.put("62", 1);
        loseStateSeeds.put("default", 0);
        winStateSeeds.put("default", 0);
        seeds.add(loseStateSeeds);
        seeds.add(winStateSeeds);

        briansBrain = new ArrayList<>();
        HashMap<String, Integer> loseStateBrian = new HashMap<>();
        HashMap<String, Integer> winStateBrian = new HashMap<>();
        HashMap<String, Integer> dyingStateBrian = new HashMap<>();
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

        AtomicReference<ArrayList<HashMap<String, Integer>>> chosenAutomata = new AtomicReference<>(GoL);

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
                drawPane.paintComponent(drawPane.getGraphics());

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
            drawPane.paintComponent(drawPane.getGraphics());

            generationCounter++;
            String counter = String.format("generations: %d", generationCounter);
            generations.setText(counter);
        };

        timer = new Timer(speed.getValue(), al);
        PlayButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timer.start();
                generationCounter = 0;
                generations.setText("generations: " + generationCounter);
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
        private final int CELL_WIDTH = BOARD_WIDTH / BOARD_ROWS;
        private final int CELL_HEIGHT = BOARD_HEIGHT / BOARD_COLS;
        private final Color[] stateColors = {
                Color.GRAY,
                Color.RED,

                new Color(150, 255, 150),
                new Color(150, 200, 255),
        };

        public DrawPane() {
            setSize(BOARD_WIDTH, BOARD_HEIGHT);
            setVisible(true);
            setBackground(Color.GRAY);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getX() > 0 && e.getX() < (BOARD_WIDTH - 25) && e.getY() > 0 && e.getY() < (BOARD_HEIGHT - 25)) {
                        int col = e.getX() / CELL_WIDTH;
                        int row = e.getY() / CELL_HEIGHT;
                        currentBoard[row][col] = getIndexOfRadioButton();

                        setBoard(currentBoard);
                        paintComponent(getGraphics());
                    }
                }

            });

            addMouseWheelListener(e -> {
                if (e.getWheelRotation() < 0)
                    System.out.println("mouse wheel up");
                else {
                    System.out.println("mouse wheel down");
                }
            });

            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int col = e.getX() / CELL_WIDTH;
                    int row = e.getY() / CELL_HEIGHT;
                    if (0 <= row && row < BOARD_ROWS) {
                        if (0 <= col && col < BOARD_COLS) {
                            currentBoard[row][col] = getIndexOfRadioButton();

                            setBoard(currentBoard);
                            paintComponent(getGraphics());
                        }
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    /*
                    if (e.getX() > 0 && e.getX() < BOARD_WIDTH && e.getY() > 0 && e.getY() < BOARD_HEIGHT) {
                        int col = e.getX() / CELL_WIDTH;
                        int row = e.getY() / CELL_HEIGHT;
                        currentBoard[row][col] = getIndexOfRadioButton();

                        setBoard(currentBoard);
                        paintComponent(getGraphics());
                    }

                     */
                }
            });
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
        public void paintComponent(Graphics g) {
            int CELL_WIDTH = BOARD_WIDTH / BOARD_ROWS;
            int CELL_HEIGHT = BOARD_HEIGHT / BOARD_COLS;

            for (int r = 0; r < board.length; ++r) {
                for (int c = 0; c < board[r].length; ++c) {
                    int x = c * CELL_WIDTH;
                    int y = r * CELL_HEIGHT;
                    g.setColor(stateColors[board[r][c]]);
                    g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                }
            }
        }
    }

    public int[][] createEmptyBoard() {
        int[][] board = new int[BOARD_COLS][BOARD_ROWS];

        for (int[] ints : board) {
            Arrays.fill(ints, 0);
        }

        return board;
    }

    public void computeNextBoard(ArrayList<HashMap<String, Integer>> automaton, int[][] current, int[][] next) {
        int[] nbors = new int[automaton.size()];

        for (int r = 0; r < current.length; ++r) {
            for (int c = 0; c < current[r].length; ++c) {
                String currentNbors = countNbors(current, nbors, r, c);
                HashMap<String, Integer> transition = automaton.get(current[r][c]);

                if (transition.get(currentNbors) != null) next[r][c] = transition.get(currentNbors);
                if (transition.get(currentNbors) == null) next[r][c] = transition.get("default");
            }
        }

    }

    public String countNbors(int[][] board, int[] nbors, int r0, int c0) {
        Arrays.fill(nbors, 0);

        for (int dr = -1; dr <= 1; ++dr) {
            for (int dc = -1; dc <= 1; ++dc) {
                if (dr != 0 || dc != 0) {
                    int r = dr + r0;
                    int c = dc + c0;
                    if (0 <= r && r < BOARD_ROWS) {
                        if (0 <= c && c < BOARD_COLS) {
                            nbors[board[r][c]]++;
                        }
                    }
                }
            }
        }

        return Arrays.toString(nbors).replaceAll("\\[|]|,|\\s", "");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CellularAutomata::new);
    }
}