package snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class AdvancedSnakeGame extends JPanel implements ActionListener, KeyListener {

    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    static final int UNIT = 25;

    ArrayList<Point> snake = new ArrayList<>();
    ArrayList<Fruit> fruits = new ArrayList<>();
    ArrayList<EnemySnake> enemies = new ArrayList<>();

    javax.swing.Timer timer;
    Random random = new Random();

    char direction = 'R';
    char nextDirection = 'R';

    Color[] colors = {
            new Color(220, 20, 60),
            new Color(30, 90, 220),
            new Color(255, 120, 0),
            new Color(255, 230, 40),
            new Color(40, 180, 60),
            new Color(145, 40, 180)
    };

    String[] fruitNames = {
            "jabolko",
            "borovnica",
            "pomaranca",
            "banana",
            "limeta",
            "grozdje"
    };

    Color snakeColor = colors[0];
    int colorIndex = 0;

    int score = 0;
    int level = 1;
    int combo = 0;
    int ticks = 0;
    int speed = 360;

    int nextColorChangeTick;

    Point goldenApple = null;
    int goldenAppleDisappearTick = 0;

    boolean running = true;

    public AdvancedSnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(115, 145, 95));
        setFocusable(true);
        addKeyListener(this);

        restartGame();
    }

    public void restartGame() {
        snake.clear();
        fruits.clear();
        enemies.clear();

        snake.add(new Point(8, 8));
        snake.add(new Point(7, 8));
        snake.add(new Point(6, 8));

        direction = 'R';
        nextDirection = 'R';

        score = 0;
        level = 1;
        combo = 0;
        ticks = 0;
        speed = 360;

        goldenApple = null;
        goldenAppleDisappearTick = 0;

        colorIndex = 0;
        snakeColor = colors[colorIndex];

        setNextColorChange();

        for (int i = 0; i < 8; i++) {
            spawnFruit();
        }

        ensureCorrectFruitExists();

        running = true;

        if (timer != null) {
            timer.stop();
        }

        timer = new javax.swing.Timer(speed, this);
        timer.start();
    }

    public void setNextColorChange() {
        int randomDelay = 45 + random.nextInt(55);
        nextColorChangeTick = ticks + randomDelay;
    }

    public Point randomFreePoint() {
        Point p;

        do {
            p = new Point(random.nextInt(WIDTH / UNIT), random.nextInt(HEIGHT / UNIT));
        } while (snake.contains(p) || fruitExistsAt(p) || enemyExistsAt(p) || p.equals(goldenApple));

        return p;
    }

    public void spawnFruit() {
        Point p = randomFreePoint();
        int index = random.nextInt(colors.length);

        fruits.add(new Fruit(p, colors[index], fruitNames[index]));
    }

    public void spawnGoldenApple() {
        if (goldenApple == null) {
            goldenApple = randomFreePoint();
            goldenAppleDisappearTick = ticks + 45;
        }
    }

    public boolean fruitExistsAt(Point p) {
        for (Fruit f : fruits) {
            if (f.position.equals(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean enemyExistsAt(Point p) {
        for (EnemySnake enemy : enemies) {
            for (Point part : enemy.body) {
                if (part.equals(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void ensureCorrectFruitExists() {
        for (Fruit f : fruits) {
            if (f.color.equals(snakeColor)) {
                return;
            }
        }

        Point p = randomFreePoint();
        fruits.add(new Fruit(p, snakeColor, fruitNames[colorIndex]));
    }

    public void spawnEnemySnake() {
        if (enemies.size() >= 3) {
            return;
        }

        int side = random.nextInt(4);
        char enemyDirection;
        Point start;

        if (side == 0) {
            start = new Point(0, random.nextInt(HEIGHT / UNIT));
            enemyDirection = 'R';
        } else if (side == 1) {
            start = new Point(WIDTH / UNIT - 1, random.nextInt(HEIGHT / UNIT));
            enemyDirection = 'L';
        } else if (side == 2) {
            start = new Point(random.nextInt(WIDTH / UNIT), 0);
            enemyDirection = 'D';
        } else {
            start = new Point(random.nextInt(WIDTH / UNIT), HEIGHT / UNIT - 1);
            enemyDirection = 'U';
        }

        enemies.add(new EnemySnake(start, enemyDirection));
    }

    public void move() {
        direction = nextDirection;

        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);

        if (direction == 'U') newHead.y--;
        if (direction == 'D') newHead.y++;
        if (direction == 'L') newHead.x--;
        if (direction == 'R') newHead.x++;

        if (newHead.x < 0) newHead.x = WIDTH / UNIT - 1;
        if (newHead.x >= WIDTH / UNIT) newHead.x = 0;
        if (newHead.y < 0) newHead.y = HEIGHT / UNIT - 1;
        if (newHead.y >= HEIGHT / UNIT) newHead.y = 0;

        snake.add(0, newHead);

        boolean ate = false;

        if (goldenApple != null && newHead.equals(goldenApple)) {
            score += 5;
            combo++;
            goldenApple = null;
        }

        for (int i = 0; i < fruits.size(); i++) {
            Fruit f = fruits.get(i);

            if (newHead.equals(f.position)) {
                if (f.color.equals(snakeColor)) {
                    combo++;
                    score += 10 + combo * 2;
                    ate = true;

                    fruits.remove(i);
                    spawnFruit();
                    ensureCorrectFruitExists();
                    increaseSpeed();
                    updateLevel();
                } else {
                    gameOver();
                    return;
                }
                break;
            }
        }

        if (!ate) {
            snake.remove(snake.size() - 1);
        }
    }

    public void increaseSpeed() {
        if (speed > 110) {
            speed -= 5;
            timer.setDelay(speed);
        }
    }

    public void updateLevel() {
        level = score / 100 + 1;
    }

    public void changeSnakeColor() {
        int newIndex;

        do {
            newIndex = random.nextInt(colors.length);
        } while (newIndex == colorIndex);

        colorIndex = newIndex;
        snakeColor = colors[colorIndex];
        combo = 0;

        ensureCorrectFruitExists();
        setNextColorChange();
    }

    public int getCountdownNumber() {
        int ticksLeft = nextColorChangeTick - ticks;

        if (ticksLeft <= 0) return 0;
        if (ticksLeft <= 5) return 1;
        if (ticksLeft <= 10) return 2;
        if (ticksLeft <= 15) return 3;

        return -1;
    }

    public void moveEnemySnakes() {
        if (ticks % 3 != 0) {
            return;
        }

        for (int i = enemies.size() - 1; i >= 0; i--) {
            EnemySnake enemy = enemies.get(i);
            enemy.move();

            if (enemy.isOutside()) {
                enemies.remove(i);
            }
        }
    }

    public void checkCollisions() {
        Point head = snake.get(0);

        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                gameOver();
                return;
            }
        }

        for (EnemySnake enemy : enemies) {
            for (Point enemyPart : enemy.body) {
                for (Point snakePart : snake) {
                    if (enemyPart.equals(snakePart)) {
                        gameOver();
                        return;
                    }
                }
            }
        }
    }

    public void gameOver() {
        running = false;
        timer.stop();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            ticks++;

            if (ticks >= nextColorChangeTick) {
                changeSnakeColor();
            }

            if (ticks % 90 == 0) {
                spawnFruit();
                ensureCorrectFruitExists();
            }

            if (ticks % 65 == 0) {
                spawnEnemySnake();
            }

            if (ticks % 130 == 0) {
                spawnGoldenApple();
            }

            if (goldenApple != null && ticks >= goldenAppleDisappearTick) {
                goldenApple = null;
            }

            move();
            moveEnemySnakes();
            checkCollisions();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawGrid(g);

        if (running) {
            drawGame(g);
        } else {
            drawGame(g);
            drawGameOver(g);
        }
    }

    public void drawGrid(Graphics g) {
        for (int x = 0; x < WIDTH; x += UNIT) {
            for (int y = 0; y < HEIGHT; y += UNIT) {
                if ((x / UNIT + y / UNIT) % 2 == 0) {
                    g.setColor(new Color(135, 160, 110));
                } else {
                    g.setColor(new Color(120, 145, 100));
                }

                g.fillRect(x, y, UNIT, UNIT);
            }
        }
    }

    public void drawGame(Graphics g) {
        for (Fruit f : fruits) {
            drawFruit(g, f);
        }

        drawGoldenApple(g);
        drawEnemySnakes(g);
        drawSnake(g);
        drawInfo(g);
    }

    public void drawSnake(Graphics g) {
        for (int i = snake.size() - 1; i >= 0; i--) {
            Point p = snake.get(i);

            g.setColor(snakeColor);

            if (i == 0) {
                g.fillRoundRect(p.x * UNIT, p.y * UNIT, UNIT + 5, UNIT + 5, 20, 20);

                g.setColor(Color.WHITE);
                g.fillOval(p.x * UNIT + 5, p.y * UNIT + 6, 8, 8);
                g.fillOval(p.x * UNIT + 15, p.y * UNIT + 6, 8, 8);

                g.setColor(Color.BLACK);
                g.fillOval(p.x * UNIT + 8, p.y * UNIT + 9, 3, 3);
                g.fillOval(p.x * UNIT + 18, p.y * UNIT + 9, 3, 3);
            } else {
                g.fillRoundRect(p.x * UNIT, p.y * UNIT, UNIT, UNIT, 18, 18);
            }
        }
    }

    public void drawInfo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));

        g.drawString("Score: " + score, 20, 30);
        g.drawString("Level: " + level, 160, 30);

        int countdown = getCountdownNumber();

        if (countdown != -1) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Sprememba barve čez " + countdown, 20, 65);
        }
    }

    public void drawEnemySnakes(Graphics g) {
        for (EnemySnake enemy : enemies) {
            for (int i = 0; i < enemy.body.size(); i++) {
                Point p = enemy.body.get(i);
                int x = p.x * UNIT;
                int y = p.y * UNIT;

                if (i == 0) {
                    g.setColor(new Color(35, 35, 35));
                } else {
                    g.setColor(new Color(75, 75, 75));
                }

                g.fillRoundRect(x + 3, y + 3, 19, 19, 14, 14);
            }
        }
    }

    public void drawGoldenApple(Graphics g) {
        if (goldenApple == null) return;

        int x = goldenApple.x * UNIT;
        int y = goldenApple.y * UNIT;

        g.setColor(new Color(255, 245, 80));
        g.fillOval(x + 1, y + 2, 23, 23);

        g.setColor(new Color(255, 180, 0));
        g.fillOval(x + 4, y + 5, 17, 17);

        g.setColor(Color.WHITE);
        g.fillOval(x + 6, y + 7, 5, 5);

        g.setColor(new Color(90, 50, 20));
        g.fillRect(x + 12, y, 3, 7);

        g.setColor(new Color(255, 255, 120));
        g.drawOval(x + 1, y + 2, 23, 23);
    }

    public void drawFruit(Graphics g, Fruit f) {
        int x = f.position.x * UNIT;
        int y = f.position.y * UNIT;

        g.setColor(f.color);

        if (f.name.equals("jabolko")) {
            g.fillOval(x + 3, y + 4, 18, 18);
            g.setColor(new Color(80, 50, 20));
            g.fillRect(x + 12, y + 1, 3, 6);
            g.setColor(new Color(40, 180, 60));
            g.fillOval(x + 15, y + 3, 7, 5);
        }

        else if (f.name.equals("borovnica")) {
            g.fillOval(x + 4, y + 4, 17, 17);
            g.setColor(Color.WHITE);
            g.drawOval(x + 8, y + 8, 6, 6);
        }

        else if (f.name.equals("pomaranca")) {
            g.fillOval(x + 3, y + 3, 19, 19);
            g.setColor(new Color(255, 190, 70));
            g.drawOval(x + 7, y + 7, 10, 10);
        }

        else if (f.name.equals("banana")) {
            g.setColor(new Color(255, 235, 40));
            g.fillArc(x + 2, y + 2, 22, 22, 210, 110);
            g.setColor(new Color(120, 80, 0));
            g.fillOval(x + 6, y + 16, 4, 4);
            g.fillOval(x + 17, y + 6, 4, 4);
        }

        else if (f.name.equals("limeta")) {
            g.setColor(new Color(40, 180, 60));
            g.fillOval(x + 3, y + 3, 19, 19);
            g.setColor(new Color(210, 255, 130));
            g.drawLine(x + 12, y + 4, x + 12, y + 21);
            g.drawLine(x + 4, y + 12, x + 21, y + 12);
        }

        else if (f.name.equals("grozdje")) {
            g.setColor(new Color(145, 40, 180));
            g.fillOval(x + 8, y + 2, 8, 8);
            g.fillOval(x + 4, y + 9, 8, 8);
            g.fillOval(x + 12, y + 9, 8, 8);
            g.fillOval(x + 8, y + 16, 8, 8);
        }
    }

    public void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("GAME OVER", 210, 260);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Score: " + score, 340, 320);
        g.drawString("Pritisni SPACE za ponovno igro", 200, 370);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT && direction != 'R') nextDirection = 'L';
        if (key == KeyEvent.VK_RIGHT && direction != 'L') nextDirection = 'R';
        if (key == KeyEvent.VK_UP && direction != 'D') nextDirection = 'U';
        if (key == KeyEvent.VK_DOWN && direction != 'U') nextDirection = 'D';

        if (key == KeyEvent.VK_SPACE && !running) {
            restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    static class Fruit {
        Point position;
        Color color;
        String name;

        Fruit(Point position, Color color, String name) {
            this.position = position;
            this.color = color;
            this.name = name;
        }
    }

    static class EnemySnake {
        ArrayList<Point> body = new ArrayList<>();
        char direction;

        EnemySnake(Point head, char direction) {
            this.direction = direction;

            body.add(new Point(head.x, head.y));

            if (direction == 'R') {
                body.add(new Point(head.x - 1, head.y));
                body.add(new Point(head.x - 2, head.y));
            } else if (direction == 'L') {
                body.add(new Point(head.x + 1, head.y));
                body.add(new Point(head.x + 2, head.y));
            } else if (direction == 'D') {
                body.add(new Point(head.x, head.y - 1));
                body.add(new Point(head.x, head.y - 2));
            } else if (direction == 'U') {
                body.add(new Point(head.x, head.y + 1));
                body.add(new Point(head.x, head.y + 2));
            }
        }

        void move() {
            Point head = body.get(0);
            Point newHead = new Point(head.x, head.y);

            if (direction == 'U') newHead.y--;
            if (direction == 'D') newHead.y++;
            if (direction == 'L') newHead.x--;
            if (direction == 'R') newHead.x++;

            body.add(0, newHead);
            body.remove(body.size() - 1);
        }

        boolean isOutside() {
            for (Point p : body) {
                if (p.x >= 0 && p.x < WIDTH / UNIT &&
                    p.y >= 0 && p.y < HEIGHT / UNIT) {
                    return false;
                }
            }

            return true;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Advanced Color Snake");

        AdvancedSnakeGame game = new AdvancedSnakeGame();

        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}