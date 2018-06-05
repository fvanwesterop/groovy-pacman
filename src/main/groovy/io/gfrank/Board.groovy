package io.gfrank

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Event
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.Timer

class Board extends JPanel implements ActionListener {

    private Dimension d
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14)

    private Image ii
    private final Color dotColor = new Color(192, 192, 0)
    private Color mazeColor

    private inGame = false
    private dying = false

    private final BLOCK_SIZE = 24
    private final N_BLOCKS = 15
    private final Integer SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE
    
    private final PAC_ANIM_DELAY = 2
    private final PACMAN_ANIM_COUNT = 4
    private final MAX_GHOSTS = 12
    private final PACMAN_SPEED = 6

    private Integer pacAnimCount = PAC_ANIM_DELAY
    private Integer pacAnimDir = 1
    private Integer pacmanAnimPos = 0
    private Integer N_GHOSTS = 6
    private Integer pacsLeft, score
    private List<Integer> dx, dy
    private List<Integer> ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed

    private Image ghost
    private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down
    private Image pacman3up, pacman3down, pacman3left, pacman3right
    private Image pacman4up, pacman4down, pacman4left, pacman4right

    private int pacman_x, pacman_y, pacmand_x, pacmand_y
    private int req_dx, req_dy, view_dx, view_dy

    private final levelData = [
            19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
            25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
            1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
            1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
            1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
            9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    ] as List<Short>

    private final validSpeeds = [1, 2, 3, 4, 6, 8] as List<Short>
    private final Integer maxSpeed = 6

    private Integer currentSpeed = 3
    private screenData
    private Timer timer

    Board() {

        loadImages()
        initVariables()
        initBoard()
    }

    private void initBoard() {

        addKeyListener(new TAdapter())

        setFocusable(true)

        setBackground(Color.black)
        setDoubleBuffered(true)
    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS]
        mazeColor = new Color(5, 100, 5)
        d = new Dimension(400, 400)
        ghost_x = new int[MAX_GHOSTS]
        ghost_dx = new int[MAX_GHOSTS]
        ghost_y = new int[MAX_GHOSTS]
        ghost_dy = new int[MAX_GHOSTS]
        ghostSpeed = new int[MAX_GHOSTS]
        dx = new int[4]
        dy = new int[4]

        timer = new Timer(40, this)
        timer.start()
    }

    @Override
    void addNotify() {
        super.addNotify()

        initGame()
    }

    private void doAnim() {

        pacAnimCount--

        if (pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY
            pacmanAnimPos = pacmanAnimPos + pacAnimDir

            if (pacmanAnimPos == (PACMAN_ANIM_COUNT - 1) || pacmanAnimPos == 0) {
                pacAnimDir = -pacAnimDir
            }
        }
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death()

        } else {

            movePacman()
            drawPacman(g2d)
            moveGhosts(g2d)
            checkMaze()
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48))
        g2d.fillRect(50, (SCREEN_SIZE / 2 - 30) as Integer, (SCREEN_SIZE - 100) as Integer, 50)
        g2d.setColor(Color.white)
        g2d.drawRect(50, (SCREEN_SIZE / 2 - 30) as Integer, (SCREEN_SIZE - 100) as Integer, 50)

        String pressToStartMsg = "Press s to start."
        Font messageFont = new Font("Helvetica", Font.BOLD, 14)
        FontMetrics fontMetrics = this.getFontMetrics(messageFont)

        g2d.setColor(Color.white)
        g2d.setFont(messageFont)
        def x = ((SCREEN_SIZE - fontMetrics.stringWidth(pressToStartMsg)) / 2) as Integer
        def y = (SCREEN_SIZE / 2) as Integer
        g2d.drawString(pressToStartMsg, x, y)
    }

    private void drawScore(Graphics2D g) {

        g.setFont(smallFont)
        g.setColor(new Color(96, 128, 255))

        def s = "Score: " + score
        g.drawString(s, (SCREEN_SIZE / 2 + 96) as Float, (SCREEN_SIZE + 16) as Float)

        (0..pacsLeft).each { n ->
            g.drawImage(pacman3left, (n * 28 + 8) as Integer, (SCREEN_SIZE + 1) as Integer, this)
        }
    }

    private void checkMaze() {

        def finished = true
        Short i = 0

        def k = N_BLOCKS * N_BLOCKS
        while (i < k && finished) {

            if ((screenData[i] & 48) != 0) {
                finished = false
            }
            i++
        }

        if (finished) {

            score += 50

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++
            }

            initLevel()
        }
    }

    private void death() {

        pacsLeft--

        if (pacsLeft == 0) {
            inGame = false
        }

        continueLevel()
    }

    private void moveGhosts(Graphics2D g2d) {

        short i
        int pos
        int count

        for (i = 0; i < N_GHOSTS; i++) {

            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE)

                count = 0

                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1
                    dy[count] = 0
                    count++
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0
                    dy[count] = -1
                    count++
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1
                    dy[count] = 0
                    count++
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0
                    dy[count] = 1
                    count++
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0
                        ghost_dy[i] = 0
                    } else {
                        ghost_dx[i] = -ghost_dx[i]
                        ghost_dy[i] = -ghost_dy[i]
                    }

                } else {

                    count = (int) (Math.random() * count)

                    if (count > 3) {
                        count = 3
                    }

                    ghost_dx[i] = dx[count]
                    ghost_dy[i] = dy[count]
                }

            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i])
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i])
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1)

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true
            }
        }
    }

    private void drawGhost(Graphics2D g2d, Integer x, Integer y) {

        g2d.drawImage(ghost, x, y, this)
    }

    private void movePacman() {

        int pos
        short ch

        if (req_dx == -pacmand_x && req_dy == -pacmand_y) {
            pacmand_x = req_dx
            pacmand_y = req_dy
            view_dx = pacmand_x
            view_dy = pacmand_y
        }

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE)
            ch = screenData[pos]

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15)
                score++
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacmand_x = req_dx
                    pacmand_y = req_dy
                    view_dx = pacmand_x
                    view_dy = pacmand_y
                }
            }

            // Check for standstill
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
                pacmand_x = 0
                pacmand_y = 0
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y
    }

    private void drawPacman(Graphics2D g2d) {

        if (view_dx == -1) {
            drawPacnanLeft(g2d)
        } else if (view_dx == 1) {
            drawPacmanRight(g2d)
        } else if (view_dy == -1) {
            drawPacmanUp(g2d)
        } else {
            drawPacmanDown(g2d)
        }
    }

    private void drawPacmanUp(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2up, pacman_x + 1, pacman_y + 1, this)
                break
            case 2:
                g2d.drawImage(pacman3up, pacman_x + 1, pacman_y + 1, this)
                break
            case 3:
                g2d.drawImage(pacman4up, pacman_x + 1, pacman_y + 1, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this)
                break
        }
    }

    private void drawPacmanDown(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2down, pacman_x + 1, pacman_y + 1, this)
                break
            case 2:
                g2d.drawImage(pacman3down, pacman_x + 1, pacman_y + 1, this)
                break
            case 3:
                g2d.drawImage(pacman4down, pacman_x + 1, pacman_y + 1, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this)
                break
        }
    }

    private void drawPacnanLeft(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2left, pacman_x + 1, pacman_y + 1, this)
                break
            case 2:
                g2d.drawImage(pacman3left, pacman_x + 1, pacman_y + 1, this)
                break
            case 3:
                g2d.drawImage(pacman4left, pacman_x + 1, pacman_y + 1, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this)
                break
        }
    }

    private void drawPacmanRight(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2right, pacman_x + 1, pacman_y + 1, this)
                break
            case 2:
                g2d.drawImage(pacman3right, pacman_x + 1, pacman_y + 1, this)
                break
            case 3:
                g2d.drawImage(pacman4right, pacman_x + 1, pacman_y + 1, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this)
                break
        }
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0
        int x, y

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {

            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(mazeColor)
                g2d.setStroke(new BasicStroke(2))

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1)
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y)
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1)
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1)
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(dotColor)
                    g2d.fillRect(x + 11, y + 11, 2, 2)
                }

                i++
            }
        }
    }

    private void initGame() {

        pacsLeft = 3
        score = 0
        initLevel()
        N_GHOSTS = 6
        currentSpeed = 3
    }

    private void initLevel() {

        int i
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i]
        }

        continueLevel()
    }

    private void continueLevel() {

        short i
        int dx = 1
        int random

        for (i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE
            ghost_x[i] = 4 * BLOCK_SIZE
            ghost_dy[i] = 0
            ghost_dx[i] = dx
            dx = -dx
            random = (int) (Math.random() * (currentSpeed + 1))

            if (random > currentSpeed) {
                random = currentSpeed
            }

            ghostSpeed[i] = validSpeeds[random]
        }

        pacman_x = 7 * BLOCK_SIZE
        pacman_y = 11 * BLOCK_SIZE
        pacmand_x = 0
        pacmand_y = 0
        req_dx = 0
        req_dy = 0
        view_dx = -1
        view_dy = 0
        dying = false
    }

    private void loadImages() {

        ghost = loadImageIcon("ghost")
        pacman1 = loadImageIcon("pacman")
        pacman2up = loadImageIcon("up1")
        pacman3up = loadImageIcon("up2")
        pacman4up = loadImageIcon("up3")
        pacman2down = loadImageIcon("down1")
        pacman3down = loadImageIcon("down2")
        pacman4down = loadImageIcon("down3")
        pacman2left = loadImageIcon("left1")
        pacman3left = loadImageIcon("left2")
        pacman4left = loadImageIcon("left3")
        pacman2right = loadImageIcon("right1")
        pacman3right = loadImageIcon("right2")
        pacman4right = loadImageIcon("right3")

    }

    private Image loadImageIcon(String imageName) {
        String urllBase = "/images"
        String imgExtension= "png"
        URL iconUrl = getClass().getResource(String.format("%s/%s.%s", urllBase, imageName, imgExtension))
        ImageIcon icon = new ImageIcon(iconUrl)
        return icon.getImage()
    }

    @Override
    void paintComponent(Graphics g) {
        super.paintComponent(g)

        doDrawing(g)
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g

        g2d.setColor(Color.black)
        g2d.fillRect(0, 0, d.width as Integer, d.height as Integer)

        drawMaze(g2d)
        drawScore(g2d)
        doAnim()

        if (inGame) {
            playGame(g2d)
        } else {
            showIntroScreen(g2d)
        }

        g2d.drawImage(ii, 5, 5, this)
        Toolkit.getDefaultToolkit().sync()
        g2d.dispose()
    }

    class TAdapter extends KeyAdapter {

        @Override
        void keyPressed(KeyEvent e) {

            int key = e.getKeyCode()

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1
                    req_dy = 0
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1
                    req_dy = 0
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0
                    req_dy = -1
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0
                    req_dy = 1
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop()
                    } else {
                        timer.start()
                    }
                }
            } else {
                if (key == 83) {
                    inGame = true
                    initGame()
                }
            }
        }

        @Override
        void keyReleased(KeyEvent e) {

            int key = e.getKeyCode()

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                req_dx = 0
                req_dy = 0
            }
        }
    }

    @Override
    void actionPerformed(ActionEvent e) {
        repaint()
    }
}