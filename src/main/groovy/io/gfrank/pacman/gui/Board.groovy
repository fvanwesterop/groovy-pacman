package io.gfrank.pacman.gui

import groovy.util.logging.Slf4j

import java.awt.Color
import java.awt.Dimension
import java.awt.Event
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.Timer
import java.security.InvalidParameterException

import static io.gfrank.pacman.gui.Board.GameState.PAUSED
import static io.gfrank.pacman.gui.Board.GameState.RUNNING
import static io.gfrank.pacman.gui.Board.GameState.STOPPED
import static java.awt.Color.black
import static java.awt.Toolkit.defaultToolkit
import static java.awt.event.KeyEvent.VK_DOWN
import static java.awt.event.KeyEvent.VK_ESCAPE
import static java.awt.event.KeyEvent.VK_LEFT
import static java.awt.event.KeyEvent.VK_Q
import static java.awt.event.KeyEvent.VK_RIGHT
import static java.awt.event.KeyEvent.VK_S
import static java.awt.event.KeyEvent.VK_SPACE
import static java.awt.event.KeyEvent.VK_UP

@Slf4j
class Board extends JPanel implements ActionListener {

    static final levelOneData = [
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

    def screenData = [N_BLOCKS * N_BLOCKS] as List<Short>


    static final validSpeeds = [1, 2, 3, 4, 6, 8] as List<Short>
    static final Integer maxSpeed = 6

    static final smallFont = new Font("Helvetica", Font.BOLD, 14)
    static final dotColor = new Color(242, 212, 12)
    static final mazeColor = new Color(0, 0, 200)
    static final backGroundColor = black

    static final Integer BLOCK_SIZE = 48
    static final Integer N_BLOCKS = 15
    static final Integer BOARD_SIZE = N_BLOCKS * BLOCK_SIZE

    static final Integer PAC_ANIM_DELAY = 2
    static final Integer PACMAN_ANIM_COUNT = 4
    static final Integer MAX_GHOSTS = 24
    static final Integer PACMAN_SPEED = 6

    final ghost_x = new ArrayList(MAX_GHOSTS)
    final ghost_y = new ArrayList(MAX_GHOSTS)
    final ghost_dx = new ArrayList(MAX_GHOSTS)
    final ghost_dy = new ArrayList(MAX_GHOSTS)
    final ghostSpeed = new ArrayList(MAX_GHOSTS)
    final dx = new ArrayList(4)
    final dy = new ArrayList(4)

    static final ghost = loadImageIcon("blinky-left-small")
    static final pacman1 = loadImageIcon("pacman")
    static final pacman2up = loadImageIcon("up1")
    static final pacman3up = loadImageIcon("up2")
    static final pacman4up = loadImageIcon("up3")
    static final pacman2down = loadImageIcon("down1")
    static final pacman3down = loadImageIcon("down2")
    static final pacman4down = loadImageIcon("down3")
    static final pacman2left = loadImageIcon("left1")
    static final pacman3left = loadImageIcon("left2")
    static final pacman4left = loadImageIcon("left3")
    static final pacman2right = loadImageIcon("right1")
    static final pacman3right = loadImageIcon("right2")
    static final pacman4right = loadImageIcon("right3")

    static loadImageIcon(String imageName) {
        new ImageIcon(Board.class.getResource("${imageName}.png")).image
    }

    enum GameState {
        RUNNING, PAUSED, STOPPED
    }
    def gameMode = STOPPED
    def dying = false
    Integer currentSpeed = 3
    Integer pacAnimCount = PAC_ANIM_DELAY
    Integer pacAnimDir = 1
    Integer pacmanAnimPos = 0
    Integer nuberOfGhosts = 6
    Integer pacsLeft, score

    int pacman_x, pacman_y, pacmand_x, pacmand_y
    int req_dx, req_dy, view_dx, view_dy


    Timer timer

    Maze maze

    Board() {
        maze = new Maze(blockCount: N_BLOCKS, blockSize: BLOCK_SIZE, mazeColor: mazeColor, dotColor: dotColor)
        add(maze)
        addKeyListener(new GameKeyListener())
        setFocusable(true)
        setBackground(backGroundColor)
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE + pacman3left.getHeight() + 5))
        setDoubleBuffered(true)
        initGame()
    }


    def drawBackGround(Graphics2D g2d, Color bgColor) {
        g2d.setColor(bgColor)
        g2d.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE)
    }


    def drawScore(Graphics2D g2d) {

        g2d.setFont(smallFont)
        g2d.setColor(new Color(96, 128, 255))

        g2d.drawString("Score: $score", (BOARD_SIZE - 96) as Float, (BOARD_SIZE + 16) as Float)

        (1..<pacsLeft).each { n ->
            g2d.drawImage(pacman3left, ((n - 1) * 28 + 8) as Integer, (BOARD_SIZE + 1) as Integer, this)
        }
    }

    def doAnimation() {

        pacAnimCount--

        if (pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY
            pacmanAnimPos = pacmanAnimPos + pacAnimDir

            if (pacmanAnimPos == (PACMAN_ANIM_COUNT - 1) || pacmanAnimPos == 0) {
                pacAnimDir = -pacAnimDir
            }
        }
    }

    def initGame() {
        log.debug('initGame()')

        resetGame()
        loadLevel()

        maze.screenData = this.screenData

        int framerate = 40
        timer = new Timer(framerate, this)
        timer.start()
    }

    def startGame() {
        log.debug('-----------')
        log.debug('startGame():')
        log.debug('-----------')
        resetGame()
        loadLevel()
        startLevel()
        log.debug('-----------')
    }

    def resetGame() {
        log.debug('resetGame()')
        pacsLeft = 4
        score = 0
        nuberOfGhosts = 6
        currentSpeed = 3
    }

    def loadLevel() {
        log.debug('loadLevel()')
        (0..N_BLOCKS * N_BLOCKS).each {
            screenData[it] = levelOneData[it]
        }
    }

    def startLevel() {
        log.debug('startLevel()')

        short i
        int dx = 1

        for (i = 0; i < nuberOfGhosts; i++) {

            // set ghosts start positions
            ghost_y[i] = 4 * BLOCK_SIZE
            ghost_x[i] = 4 * BLOCK_SIZE
            ghost_dy[i] = 0
            ghost_dx[i] = dx
            dx = -dx

            // set a randomspeed for each ghost
            int random = (int) (Math.random() * (currentSpeed + 1))
            if (random > currentSpeed) {
                random = currentSpeed
            }
            ghostSpeed[i] = validSpeeds[random]
        }

        // set pacman start position and state
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

    def pauseGame() {
        log.debug('pauseGame()')
        timer.stop()
    }

    def resumeGame() {
        log.debug('resumeGame()')
        timer.start()
    }

    def exitGame() {
        log.debug('exitGame() - SHUTDOWN')
        System.exit(0)
    }

    def continueGame(Graphics2D g2d) {

        if (dying) {
            death()
        } else {
            calcPosPacman()
            movePacman(g2d)
            calcPosAndMoveGhosts(g2d)
            checkMaze()
        }
    }

    def paintIntroScreen(Graphics2D g2d) {
        drawSplashBox(g2d)
        drawSplashText(g2d, 'Press s to start, q to quit.')
    }

    def paintPausedScreen(Graphics2D g2d) {
        drawSplashBox(g2d)
        drawSplashText(g2d, 'Paused. Press spacebar to resume.')
    }

    def drawSplashBox(Graphics2D g2d) {

        int x = 50
        int y = (BOARD_SIZE / 2 - 30)
        int w = (BOARD_SIZE - 100)
        int h = 50

        // draw box
        g2d.setColor(new Color(0, 32, 48))
        g2d.fillRect(x, y, w, h)

        // draw border
        g2d.setColor(Color.white)
        g2d.drawRect(x, y, w, h)
    }

    def drawSplashText(Graphics2D g2d, String text) {

        Font messageFont = new Font("Helvetica", Font.BOLD, 14)
        FontMetrics fontMetrics = this.getFontMetrics(messageFont)

        g2d.setColor(Color.white)
        g2d.setFont(messageFont)
        def x = ((BOARD_SIZE - fontMetrics.stringWidth(text)) / 2) as Integer
        def y = (BOARD_SIZE / 2) as Integer
        g2d.drawString(text, x, y)
    }

    def checkMaze() {

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

            if (nuberOfGhosts < MAX_GHOSTS) {
                nuberOfGhosts++
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++
            }

            loadLevel()
            startLevel()
        }
    }

    def death() {
        pacsLeft--
        if (pacsLeft == 0) {
            gameMode = STOPPED
        }
        startLevel()
    }

    def calcPosAndMoveGhosts(Graphics2D g2d) {

        short i
        int pos
        int count

        for (i = 0; i < nuberOfGhosts; i++) {

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
                    && gameMode == RUNNING) {

                dying = true
            }
        }
    }

    def drawGhost(Graphics2D g2d, Integer x, Integer y) {
        g2d.drawImage(ghost, x, y, this)
    }

    def calcPosPacman() {

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

    def movePacman(Graphics2D g2d) {

        if (view_dx == -1) {
            drawPacmanLeft(g2d)
        } else if (view_dx == 1) {
            drawPacmanRight(g2d)
        } else if (view_dy == -1) {
            drawPacmanUp(g2d)
        } else {
            drawPacmanDown(g2d)
        }
    }

    def drawPacmanUp(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2up, pacman_x + 12, pacman_y + 12, this)
                break
            case 2:
                g2d.drawImage(pacman3up, pacman_x + 12, pacman_y + 12, this)
                break
            case 3:
                g2d.drawImage(pacman4up, pacman_x + 12, pacman_y + 12, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 12, pacman_y + 12, this)
                break
        }
    }

    def drawPacmanDown(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2down, pacman_x + 12, pacman_y + 12, this)
                break
            case 2:
                g2d.drawImage(pacman3down, pacman_x + 12, pacman_y + 12, this)
                break
            case 3:
                g2d.drawImage(pacman4down, pacman_x + 12, pacman_y + 12, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 12, pacman_y + 12, this)
                break
        }
    }

    def drawPacmanLeft(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2left, pacman_x + 12, pacman_y + 12, this)
                break
            case 2:
                g2d.drawImage(pacman3left, pacman_x + 12, pacman_y + 12, this)
                break
            case 3:
                g2d.drawImage(pacman4left, pacman_x + 12, pacman_y + 12, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 12, pacman_y + 12, this)
                break
        }
    }

    def drawPacmanRight(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2right, pacman_x + 12, pacman_y + 12, this)
                break
            case 2:
                g2d.drawImage(pacman3right, pacman_x + 12, pacman_y + 12, this)
                break
            case 3:
                g2d.drawImage(pacman4right, pacman_x + 12, pacman_y + 12, this)
                break
            default:
                g2d.drawImage(pacman1, pacman_x + 12, pacman_y + 12, this)
                break
        }
    }

    /**
     * Events are sent by the {@link Timer} instance and are fired at the framerate.
     * Each event is just a trigger to repaint the {@link Board}
     * @param event
     */
    @Override
    void actionPerformed(ActionEvent event) {
        repaint()
    }

    @Override
    void paint(Graphics g) {
        super.paintComponent(g)
        paintBoard(g as Graphics2D)
    }

    def paintBoard(Graphics2D g2d) {

        drawBackGround(g2d, backGroundColor)
        maze.drawMaze(g2d)
        drawScore(g2d)
        doAnimation()

        switch (gameMode) {

            case RUNNING:
                continueGame(g2d)
                break
            case STOPPED:
                paintIntroScreen(g2d)
                break
            case PAUSED:
                paintPausedScreen(g2d)
                pauseGame()
                break
            default:
                throw new InvalidParameterException("bug: unsupported game state '$gameMode' detected")
        }
        getDefaultToolkit().sync()
    }

    class GameKeyListener extends KeyAdapter {

        @Override
        void keyPressed(KeyEvent keyEvent) {

            switch(gameMode) {
                case RUNNING:
                    handleRunningModeKey(keyEvent)
                    break
                case STOPPED:
                    handleStoppedModeKey(keyEvent)
                    break
                case PAUSED:
                    handlePauzedModeKey(keyEvent)
                    break
            }
        }

        @Override
        void keyReleased(KeyEvent e) {
            int key = e.getKeyCode()
            if (key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN) {
                req_dx = 0; req_dy = 0
            }
        }

        def handleRunningModeKey(KeyEvent event) {

            switch (event.keyCode) {
                case VK_LEFT:
                    req_dx = -1; req_dy = 0
                    break
                case VK_RIGHT:
                    req_dx = 1; req_dy = 0
                    break
                case VK_UP:
                    req_dx = 0; req_dy = -1
                    break
                case VK_DOWN:
                    req_dx = 0; req_dy = 1
                    break
                case VK_ESCAPE:
                    gameMode = STOPPED
                    break
                case VK_SPACE:
                    gameMode = PAUSED
                    break
            }
        }

        def handleStoppedModeKey(KeyEvent event) {

            switch (event.keyCode) {
                case VK_S:
                    gameMode = RUNNING
                    startGame()
                    break
                case VK_Q:
                    exitGame()
                    break

            }
        }

        def handlePauzedModeKey(KeyEvent event) {

            if (event.keyCode == VK_SPACE) {
                gameMode = RUNNING
                resumeGame()
            }
        }

    }


}