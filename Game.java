import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

// RPG Game by pro coders lol
 // Julian
 // Maxi cheesimir

class Game {
    private final JFrame frame;
    private final JProgressBar playerHealthBar;
    private final JProgressBar enemyHealthBar;
    private final JTextArea logArea;
    private final JButton attackButton;
    private final JButton healButton;
    private final Player player;
    private Enemy enemy;
    private final JLabel potionLabel;

    public Game() {
        player = new Player("Held");
        enemy = new Enemy("Troll", 50, 5); // Erstes Level: Troll 50 HP; 5 AD
    
        // UI Window wird erzeugt
        frame = new JFrame("Rise of Heroes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720); // 16:9
        frame.setLayout(new BorderLayout());
    
        // Lebensbalken oben (über ganze Seitenbreite)
        UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel healthPanel = new JPanel(new GridLayout(1, 2)); 
        playerHealthBar = createHealthBar("Spieler", player.health);
        enemyHealthBar = createHealthBar("Gegner", enemy.health);
        healthPanel.add(playerHealthBar);
        healthPanel.add(enemyHealthBar);
        topPanel.add(healthPanel, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);
        
        potionLabel = new JLabel("Tränke: " + player.drinks);
        potionLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        JPanel potionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        potionPanel.add(potionLabel);
        topPanel.add(potionPanel, BorderLayout.SOUTH);
        frame.add(topPanel, BorderLayout.NORTH);
    
        // Buttons unten: Angriffs-Button und Heal-Button nebeneinander
        JPanel bottomPanel = new JPanel(new BorderLayout());
        attackButton = new JButton("ATTACK");
        attackButton.setPreferredSize(new Dimension(200, 50));
        attackButton.setFont(new Font("Monospaced", Font.BOLD, 39));
        attackButton.addActionListener(this::handleAttack);
        healButton = new JButton("HEAL");
        healButton.setFont(new Font("Monospaced", Font.BOLD, 39));
        healButton.setPreferredSize(new Dimension(200, 50));
        healButton.addActionListener(e -> {
            if (player.drinks > 0) {
                player.heal();
                potionLabel.setText("Tränke: " + player.drinks);
                updateBars();  
                if(player.drinks == 0){
                    healButton.setEnabled(false);
                }
            } else {
                healButton.setEnabled(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(attackButton);
        buttonPanel.add(healButton); 
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // Log area
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
    
    private void handleAttack(ActionEvent e) {
        if (!player.isAlive() || !enemy.isAlive()) return;
    
        StringBuilder log = new StringBuilder();
    
        attackButton.setEnabled(false);
    
        int playerDamage = player.attackEnemy(enemy);
        log.append(player.name).append(" greift ").append(enemy.name)
           .append(" an, und verrichtet ").append(playerDamage).append(" Schaden.\n");
    
        updateBars();
        
        if (!enemy.isAlive()) {
            log.append("Du hast dein Gegner besiegt!\n");
            player.xp += 50;
            log.append("Du erhältst 50 XP. Aktuelle XP: ").append(player.xp).append("\n");
    
            if (player.xp >= 50) {
                player.levelUp();
                log.append("LEVEL UP! Du bist jetzt Level ").append(player.level)
                   .append(" und machst ").append(player.attack).append(" Schaden.\n");
            }
    
            // Gegnerwechsel
            switch (player.stage) {
                case 1 -> {
                    log.append("Ein neuer Gegner erscheint: GODZILLA!\n");
                    enemy = new Enemy("Godzilla", 125, 10);
                    player.stage = 2;
                    updateBars();
                    attackButton.setEnabled(true);
                }
                case 2 -> {
                    log.append("Ein neuer Gegner erscheint: MOSSI MOSSMÜLLER!!\n");
                    enemy = new Enemy("Mossi", 150, 15);
                    player.stage = 3;
                    updateBars();
                    attackButton.setEnabled(true);
                }
                case 3 -> {
                    log.append("Ein neuer Gegner erscheint: GARY ARNZ!(Maschine)\n");
                    enemy = new Enemy("Gary", 175, 20);
                    player.stage = 4;
                    updateBars();
                    attackButton.setEnabled(true);
                }
                case 4 -> {
                    log.append("Ein neuer Gegner erscheint: LOSERD! (MatheMann)\n");
                    enemy = new Enemy("loserd", 200, 25);
                    player.stage=5;
                    updateBars();
                    attackButton.setEnabled(true);
                }
                default -> {
                    log.append("Du hast alle Gegner besiegt. Glückwunsch!");
                    attackButton.setEnabled(false);
                }
            }
        } else {
            attackEnemyWithDelay();  
            Timer cooldown = new Timer(500, e1 -> attackButton.setEnabled(true));
            cooldown.setRepeats(false);
            cooldown.start();
            updateBars();
        }
    
        updateBars();
        logArea.append(log.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());

    }
    
    private void attackEnemyWithDelay() {
        int delay = new Random().nextInt(2000) + 500;
        Timer timer = new Timer(delay, e -> {
            if (enemy.isAlive() && player.isAlive()) {
                StringBuilder log = new StringBuilder();
                int enemyDamage = enemy.attackPlayer(player);
                log.append(enemy.name)
                   .append(" greift an und verrichtet ")
                   .append(enemyDamage)
                   .append(" Schaden an dir.\n");
                updateBars();
                if (!player.isAlive()) {
                    log.append("Du bist gestorben! GAME OVER!");
                    attackButton.setEnabled(false);
                    healButton.setEnabled(false);
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
        // Spieler-Healthbar
        playerHealthBar.setValue(player.health);
        playerHealthBar.setString("Spieler: " + player.health + " HP");
        if (player.health < 40) {
            playerHealthBar.setForeground(Color.RED);
        } else if (player.health < 70) {
            playerHealthBar.setForeground(Color.YELLOW);
        } else {
            playerHealthBar.setForeground(Color.GREEN);
        }
    
        // Gegner-Healthbar mit dynamischem Maximum
        enemyHealthBar.setMaximum(enemy.maxHealth);
        enemyHealthBar.setValue(enemy.health);
        enemyHealthBar.setString("Gegner: " + enemy.health + "/" + enemy.maxHealth + " HP");
        if (enemy.health < enemy.maxHealth * 0.25) {
            enemyHealthBar.setForeground(Color.RED);
        } else if (enemy.health < enemy.maxHealth * 0.5) {
            enemyHealthBar.setForeground(Color.YELLOW);
        } else {
            enemyHealthBar.setForeground(Color.GREEN);
        }
    }
    
    private JProgressBar createHealthBar(String name, int value) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setString(name + ": " + value + " HP");
        bar.setStringPainted(true);
        bar.setPreferredSize(new Dimension(1280, 30)); // Jetzt über die ganze Seitenbreite
        return bar;
    }
    
    static class Player {
        String name;
        int health = 100;
        int attack = 10;
        int xp = 0;
        int level = 1;
        int stage = 1;
        int drinks = 4;
    
        Player(String name) {
            this.name = name;
        }
    
        boolean isAlive() {
            return health > 0;
        }
    
        int attackEnemy(Enemy e) {
            int dmg = new Random().nextInt(attack) + 1;
            e.health = Math.max(0, e.health - dmg);
            return dmg;
        }
    
        void levelUp() {
            level++;
            xp = 0;
            attack += 5;
        }
    
        void heal() {
            if (drinks > 0) {
                health = 100;
                drinks -= 1;
            }
        }
    }
    
    static class Enemy {
        String name;
        int health;
        int attack;
        int maxHealth; // Neues Feld für das maximale Leben
    
        Enemy(String name, int health, int attack) {
            this.name = name;
            this.health = health;
            this.attack = attack;
            this.maxHealth = health;
        }
    
        boolean isAlive() {
            return health > 0;
        }
    
        int attackPlayer(Player p) {
            int dmg = new Random().nextInt(attack) + 1;
            p.health = Math.max(0, p.health - dmg);
            return dmg;
        }
    }
    
    public static void main(String[] args) {
        new Game();
    }
}