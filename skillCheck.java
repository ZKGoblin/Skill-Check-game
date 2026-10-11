import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class skillCheck extends JFrame {
    private NodePanel panel;
    private JLabel scoreLabel;
    private Timer timer;
    private int score = 0;
    private int activeNode = 0;
    private int targetNode = 0;
    private boolean gameOver = false;
    private int speed = 0;
    private int speed1 = 600;
    private int speed2 = 500;
    private int speed3 = 400;
    private int speed4 = 300;
    private int speed5 = 200;
    private int speed6 = 100;
    private boolean clockwise = true;
    private final int NODE_COUNT = 16;
    private final Random random = new Random();
    private int misses = 0;
    private int round = 0;
    private final Map<Integer, String> nodeKeyMap = new HashMap<>();
    private String mode; // Store selected mode

    // Constructor with mode parameter
    public skillCheck(String mode) {
        this.mode = mode;
        initGame();
    }

    private void initGame() {
        setTitle("Skill Check Node Game - Mode: " + mode);
        setSize(600, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new NodePanel();
        add(panel, BorderLayout.CENTER);

        scoreLabel = new JLabel("Score: 0 | Lives left: 3", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(scoreLabel, BorderLayout.SOUTH);

        String[] keys = {"r", "g", "b", "y"};
        for (String key : keys) {
            final String assignedKey = key;
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(assignedKey.toUpperCase()), "hit_" + assignedKey);
            getRootPane().getActionMap()
                .put("hit_" + assignedKey, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!gameOver) handleHit(assignedKey);
                    }
                });
        }

        panel.setNodeColor(12, Color.RED);
        panel.setNodeSize(12, 50);
        nodeKeyMap.put(12, "r");

        panel.setNodeColor(4, Color.GREEN);
        panel.setNodeSize(4, 50);
        nodeKeyMap.put(4, "g");

        panel.setNodeColor(0, Color.BLUE);
        panel.setNodeSize(0, 50);
        nodeKeyMap.put(0, "b");

        panel.setNodeColor(8, Color.YELLOW);
        panel.setNodeSize(8, 50);
        nodeKeyMap.put(8, "y");

        setNewTargetNode();
        speed = speed1;
        startTimer();
        setVisible(true);
    }

    private void updateLives() {
        int lives = 3 - misses;
        scoreLabel.setText("Score: " + score + " | Lives left: " + lives);
    }

    private void handleHit(String keyPressed) {
        String requiredKey = nodeKeyMap.get(targetNode);
        if (activeNode == targetNode && requiredKey != null && requiredKey.equalsIgnoreCase(keyPressed)) {
            score++;
            if ("medium".equalsIgnoreCase(mode)) {
                clockwise = !clockwise;
            }
            updateLives();
            setNewTargetNode();
            increaseSpeed();
            round = 0;
        } else {
            score--;
            misses++;
            if (misses == 3) {
                endGame();
                return;
            }
        }
        updateLives();
        increaseSpeed();
    }

    private void increaseSpeed() {
        if (score >= 20) {
            speed = speed6;
        } else if (score >= 14) {
            speed = speed5;
        } else if (score >= 9) {
            speed = speed4;
        } else if (score >= 6) {
            speed = speed3;
        } else if (score >= 3) {
            speed = speed2;
        } else {
            speed = speed1;
        }
        restartTimer();
    }

    private final int[] allowedTargets = {0, 4, 8, 12};

    private void setNewTargetNode() {
        int newTarget;
        do {
            newTarget = allowedTargets[random.nextInt(allowedTargets.length)];
        } while (newTarget == targetNode);
        targetNode = newTarget;
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (clockwise) {
                    activeNode = (activeNode + 1) % NODE_COUNT;
                } else {
                    activeNode = (activeNode - 1 + NODE_COUNT) % NODE_COUNT; 
                }

                if (activeNode == (targetNode + 1) % NODE_COUNT) {
                    round++;
                    if (round == 3) {
                        misses++;
                        score--;
                        round = 0;
                        updateLives();
                        if (misses == 3) {
                            endGame();
                            return;
                        }
                    }
                }

                panel.repaint();
            }
        }, 0, speed);
    }

    private void restartTimer() {
        timer.cancel();
        startTimer();
    }

    private void endGame() {
        gameOver = true;
        timer.cancel();
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Game Over!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // Inner class for drawing the nodes
    class NodePanel extends JPanel {
        private final Color[] nodeColors = new Color[NODE_COUNT];
        private final int[] nodeSizes = new int[NODE_COUNT];

        public NodePanel() {
            for (int i = 0; i < NODE_COUNT; i++) {
                nodeColors[i] = Color.GRAY;
                nodeSizes[i] = 30;
            }
        }

        public void setNodeColor(int index, Color color) {
            if (index >= 0 && index < NODE_COUNT) {
                nodeColors[index] = color;
                repaint();
            }
        }

        public void setNodeSize(int index, int size) {
            if (index >= 0 && index < NODE_COUNT && size > 0) {
                nodeSizes[index] = size;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawNodes((Graphics2D) g);
        }

        private void drawNodes(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = 170;

            for (int i = 0; i < NODE_COUNT; i++) {
                double angle = Math.toRadians(i * 360.0 / NODE_COUNT);
                int x = centerX + (int) (radius * Math.cos(angle));
                int y = centerY + (int) (radius * Math.sin(angle));

                int size = nodeSizes[i];
                int drawX = x - size / 2;
                int drawY = y - size / 2;

                if (i == activeNode) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(nodeColors[i]);
                }
                g2d.fillOval(drawX, drawY, size, size);

                if (i == targetNode) {
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(6));
                } else {
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                }
                g2d.drawOval(drawX, drawY, size, size);
            }

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(centerX - 5, centerY - 5, 10, 10);
        }
    }

    // Start menu and game launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Easy", "Medium", "Hard"};
            String selectedMode = (String) JOptionPane.showInputDialog(
                null,
                "Select Game Mode:",
                "Skill Check Mode Select",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
            );

            if (selectedMode != null) {
                new skillCheck(selectedMode);
            }
        });
    }
}
