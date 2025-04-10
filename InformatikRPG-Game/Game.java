import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

class Game {
    private final JFrame frame;
    private final JProgressBar playerFocusBar;
    private final JProgressBar challengeProgressBar;
    private final JTextArea logArea;
    private final JButton challengeButton;
    private final JButton restButton;
    private final Player player;
    private Opponent opponent;
    private final JLabel breakLabel;
    private final GamePanel gamePanel;

    public Game() {
        player = new Player("Held");
        opponent = new Opponent("Troll der Rätsel", 50, 5);

        frame = new JFrame("Learnattack");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLayout(new BorderLayout());

        UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel focusPanel = new JPanel(new GridLayout(1, 2));
        playerFocusBar = createFocusBar("Konzentration", player.concentration);
        challengeProgressBar = createFocusBar("Aufgabe", opponent.difficulty);
        focusPanel.add(playerFocusBar);
        focusPanel.add(challengeProgressBar);
        topPanel.add(focusPanel, BorderLayout.CENTER);

        breakLabel = new JLabel("Pausen: " + player.breaks);
        breakLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        JPanel breakPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        breakPanel.add(breakLabel);
        topPanel.add(breakPanel, BorderLayout.SOUTH);
        frame.add(topPanel, BorderLayout.NORTH);

        gamePanel = new GamePanel();
        frame.add(gamePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        challengeButton = new JButton("HERAUSFORDERN");
        challengeButton.setPreferredSize(new Dimension(200, 50));
        challengeButton.setFont(new Font("Monospaced", Font.BOLD, 32));
        challengeButton.addActionListener(this::handleChallenge);
        restButton = new JButton("PAUSE");
        restButton.setFont(new Font("Monospaced", Font.BOLD, 32));
        restButton.setPreferredSize(new Dimension(200, 50));
        restButton.addActionListener(e -> {
            if (player.breaks > 0) {
                player.rest();
                breakLabel.setText("Pausen: " + player.breaks);
                updateBars();
                if(player.breaks == 0){
                    restButton.setEnabled(false);
                }
            }
        });

        JButton calculatorButton = new JButton("🖩");
        calculatorButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JButton bookButton = new JButton("📖");
        bookButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(challengeButton);
        buttonPanel.add(restButton);
        buttonPanel.add(calculatorButton);
        buttonPanel.add(bookButton);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(1280, 60));
        bottomPanel.add(scrollPane, BorderLayout.SOUTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void handleChallenge(ActionEvent e) {
        if (!player.isConcentrated() || !opponent.isChallenging()) return;

        StringBuilder log = new StringBuilder();

        challengeButton.setEnabled(false);

        int effort = player.faceChallenge(opponent);
        log.append(player.name).append(" meistert eine Aufgabe von ")
           .append(opponent.name).append(" und erzielt ")
           .append(effort).append(" Punkte!\n");

        gamePanel.showLightBurst();
        updateBars();

        if (!opponent.isChallenging()) {
            log.append("Du hast die Herausforderung von ").append(opponent.name).append(" bestanden!\n");
            player.xp += 50;
            log.append("Du erhältst 50 Erfahrungspunkte. Aktuelle XP: ").append(player.xp).append("\n");

            if (player.xp >= 50) {
                player.levelUp();
                log.append("LEVEL UP! Du bist jetzt Stufe ").append(player.level)
                   .append(" – deine mentale Stärke steigt.\n");
            }

            switch (player.stage) {
                case 1 -> {
                    log.append("Neue Herausforderung: PROFESSOR GODZILLA!\n");
                    opponent = new Opponent("Professor Godzilla", 100, 10);
                    gamePanel.setEnemyImage("src/resources/Images/professor_godzilla.png");
                    player.stage = 2;
                }
                case 2 -> {
                    log.append("Neue Herausforderung: DR. MOSSMÜLLER!\n");
                    opponent = new Opponent("Dr. Mossmüller", 150, 10);
                    gamePanel.setEnemyImage("src/resources/Images/dr_mossmueller.png");
                    player.stage = 3;
                }
                case 3 -> {
                    log.append("Finale Herausforderung: GARY – die KI der Rätsel!\n");
                    opponent = new Opponent("Gary", 175, 15);
                    gamePanel.setEnemyImage("src/resources/Images/gary_ai.png");
                    player.stage = 4;
                }
                default -> {
                    log.append("Alle Prüfungen bestanden! Du bist ein wahrer Meister der Gedanken!\n");
                    log.append("Du hast das Abiturzeugnis wirklich verdient!\n");
                    challengeButton.setEnabled(false);
                }
            }
            updateBars();
            challengeButton.setEnabled(true);
        } else {
            counterChallenge();
            Timer cooldown = new Timer(500, e1 -> challengeButton.setEnabled(true));
            cooldown.setRepeats(false);
            cooldown.start();
        }

        logArea.append(log.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void counterChallenge() {
        int delay = new Random().nextInt(2000) + 500;
        Timer timer = new Timer(delay, e -> {
            if (opponent.isChallenging() && player.isConcentrated()) {
                StringBuilder log = new StringBuilder();
                int stress = opponent.challengeBack(player);
                log.append(opponent.name).append(" stellt eine Gegenaufgabe. Du verlierst ")
                   .append(stress).append(" Konzentrationspunkte.\n");
                gamePanel.shakeEnemy();
                updateBars();
                if (!player.isConcentrated()) {
                    log.append("Du bist mental erschöpft. DUELL BEENDET!\n");
                    challengeButton.setEnabled(false);
                    restButton.setEnabled(false);
                }
                logArea.append(log.toString());
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
        timer.setRepeats(false);
        timer.start();
        updateBars();
    }

    public void updateBars() {
        playerFocusBar.setValue(player.concentration);
        playerFocusBar.setString("Konzentration: " + player.concentration + "%");
        if (player.concentration < 30) playerFocusBar.setForeground(Color.RED);
        else if (player.concentration < 50) playerFocusBar.setForeground(Color.YELLOW);
        else playerFocusBar.setForeground(Color.GREEN);

        challengeProgressBar.setMaximum(opponent.maxDifficulty);
        challengeProgressBar.setValue(opponent.difficulty);
        challengeProgressBar.setString("Aufgabe: " + opponent.difficulty + "/" + opponent.maxDifficulty);
        if (opponent.difficulty < opponent.maxDifficulty * 0.4) challengeProgressBar.setForeground(Color.RED);
        else if (opponent.difficulty < opponent.maxDifficulty * 0.7) challengeProgressBar.setForeground(Color.YELLOW);
        else challengeProgressBar.setForeground(Color.GREEN);
    }

    private JProgressBar createFocusBar(String label, int value) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setString(label + ": " + value);
        bar.setStringPainted(true);
        bar.setPreferredSize(new Dimension(1280, 30));
        return bar;
    }

    class GamePanel extends JPanel {
        private Image playerImage;
        private Image enemyImage;
        private Image lightBurstImage;
        private int playerX = 50, playerY = 300;
        private int enemyX = 800, enemyY = 300;

        public GamePanel() {
            setBackground(Color.WHITE);
            playerImage = new ImageIcon("src/resources/Images/player_thinker.png").getImage();
            enemyImage = new ImageIcon("src/resources/Images/professor_godzilla.png").getImage();
            lightBurstImage = new ImageIcon("src/resources/Images/light_burst.png").getImage();
        }

        public void setEnemyImage(String path) {
            enemyImage = new ImageIcon(path).getImage();
            repaint();
        }

        public void shakeEnemy() {
            int originalX = enemyX;
            Timer shake = new Timer(50, new AbstractAction() {
                int count = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    enemyX = originalX + (int)(Math.random() * 10 - 5);
                    repaint();
                    if (++count > 5) {
                        ((Timer) e.getSource()).stop();
                        enemyX = originalX;
                        repaint();
                    }
                }
            });
            shake.start();
        }

        public void showLightBurst() {
            Timer anim = new Timer(50, new AbstractAction() {
                int count = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    Graphics g = getGraphics();
                    g.drawImage(lightBurstImage, playerX + 80, playerY - 50, null);
                    count++;
                    if (count > 3) ((Timer) e.getSource()).stop();
                }
            });
            anim.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (playerImage != null) g.drawImage(playerImage, playerX, playerY, this);
            if (enemyImage != null) g.drawImage(enemyImage, enemyX, enemyY, this);
        }
    }

    static class Player {
        String name;
        int concentration = 100;
        int skill = 10;
        int xp = 0;
        int level = 1;
        int stage = 1;
        int breaks = 3;

        Player(String name) {
            this.name = name;
        }

        boolean isConcentrated() {
            return concentration > 0;
        }

        int faceChallenge(Opponent o) {
            int points = new Random().nextInt(skill) + 1;
            o.difficulty = Math.max(0, o.difficulty - points);
            return points;
        }

        void levelUp() {
            level++;
            xp = 0;
            skill += 5;
        }

        void rest() {
            if (breaks > 0) {
                concentration = 100;
                breaks -= 1;
            }
        }
    }

    static class Opponent {
        String name;
        int difficulty;
        int challenge;
        int maxDifficulty;

        Opponent(String name, int difficulty, int challenge) {
            this.name = name;
            this.difficulty = difficulty;
            this.challenge = challenge;
            this.maxDifficulty = difficulty;
        }

        boolean isChallenging() {
            return difficulty > 0;
        }

        int challengeBack(Player p) {
            int stress = new Random().nextInt(challenge) + 1;
            p.concentration = Math.max(0, p.concentration - stress);
            return stress;
        }
    }

    public static void main(String[] args) {
        new Game();
    }
}
