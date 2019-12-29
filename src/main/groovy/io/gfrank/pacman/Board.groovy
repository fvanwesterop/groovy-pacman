package io.gfrank.pacman

import groovy.util.logging.Slf4j
import io.gfrank.pacman.gui.Maze

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.security.InvalidParameterException
import java.util.List

import static io.gfrank.pacman.Board.GameState.*
import static java.awt.Color.black
import static java.awt.Toolkit.defaultToolkit
import static java.awt.event.KeyEvent.*

@Slf4j
class Board extends JPanel implements ActionListener {

    List screenData = []


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

    // positions of ghosts
    List<Integer> ghosts_x
    List<Integer> ghosts_y

    // headings (directions) of ghosts
    List<Integer> ghosts_dx
    List<Integer> ghosts_dy

    // speeds of ghosts
    List<Integer> ghosts_speed

    final dx = []
    final dy = []

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

        new ImageIcon(Maze.class.getResource("${imageName}.png")).image
    }

    enum GameState {
        RUNNING, PAUSED, STOPPED, LEVEL_LOST, LEVELCOMPLETE
    }
    def gameState = STOPPED
    def dying = false
    Integer currentSpeed = 3
    Integer pacAnimCount = PAC_ANIM_DELAY
    Integer pacAnimDir = 1
    Integer pacmanAnimPos = 0
    Integer numberOfGhosts = 4
    Integer pacsLeft, score

    int pacman_x, pacman_y, pacman_dx, pacman_dy
    int req_dx, req_dy, view_dx, view_dy


    Timer timer

    Maze maze

    Board() {
        maze = new Maze(blockCount: N_BLOCKS, blockSize: BLOCK_SIZE, mazeColor: mazeColor, dotColor: dotColor)
        add(maze)
        addKeyListener(new GameKeyListener())
        setFocusable(true)
        setBackground(backGroundColor)
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE + pacman3left.getHeight(null) + 5))
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

        for (int n = 1; n < pacsLeft; n++) {
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
        loadNextLevel()
        maze.screenData = this.screenData

        /*
         * create and start a timer that wil fire ActionEvents at 1/framerate [ms] intervals.
         * Each event will cause a repaint of the screen and is essentially the heartbeat of the game.
         */
        int framerate = 50 // unit: [Hz]
        timer = new Timer((1 / framerate * 1000) as int, this)
        timer.start()
    }

    def startGame() {
        log.debug('-----------')
        log.debug('startGame():')
        log.debug('-----------')
        resetGame()
        loadNextLevel()
        startLevel()
        log.debug('-----------')
    }

    def nextLevel() {
        log.debug('-----------')
        log.debug('nextLevel():')
        log.debug('-----------')

        pacsLeft += 2
        score += 50


        if (numberOfGhosts < MAX_GHOSTS) {
            numberOfGhosts++
        }

        if (currentSpeed < maxSpeed) {
            currentSpeed++
        }

        loadNextLevel()
        startLevel()
        log.debug('-----------')
    }

    def resetGame() {
        log.debug('resetGame()')
        pacsLeft = 3
        score = 0
        numberOfGhosts = 4
        currentSpeed = 3
    }

    def loadNextLevel() {
        log.debug('loadLevel()')
        screenData.addAll(LevelData.levelOne)
    }

    def startLevel() {
        log.debug('startLevel()')

        short ghostId
        int dx = 1

        ghosts_x = []; ghosts_dx = []
        ghosts_y = []; ghosts_dy = []
        ghosts_speed = []

        for (ghostId = 0; ghostId < numberOfGhosts; ghostId++) {

            // set ghosts start positions
            ghosts_x[ghostId] = 4 * BLOCK_SIZE
            ghosts_dx[ghostId] = dx
            dx = -dx

            ghosts_y[ghostId] = 4 * BLOCK_SIZE
            ghosts_dy[ghostId] = 0

            // set a randomspeed for each ghost
            int random = (int) (Math.random() * (currentSpeed + 1))
            if (random > currentSpeed) {
                random = currentSpeed
            }
            ghosts_speed[ghostId] = validSpeeds[random]
        }

        // set pacman start position and state
        pacman_x = 7 * BLOCK_SIZE
        pacman_y = 11 * BLOCK_SIZE
        pacman_dx = 0
        pacman_dy = 0
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
            pacsLeft--
            if (pacsLeft == 0) {
                gameState = LEVEL_LOST
            } else {
                startLevel()
            }

        } else {
            calcPostitionPacman()
            movePacman(g2d)
            calcPosAndMoveGhosts(g2d)

            if (isLevelComplete()) {

                nextLevel()
            }
        }
    }

    def paintIntroScreen(Graphics2D g2d) {
        drawSplashBox(g2d)
        drawSplashText(g2d, 'Press s to start, q to quit.')
    }

    def paintGameOverScreen(Graphics2D g2d) {
        drawSplashBox(g2d)
        drawSplashText(g2d, 'Game Over! Press any key to exit.')
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

    def isLevelComplete() {
        // if maze field has bit #5 set it contains a pill
        def pillsLeft = screenData.findAll { (it & 16) > 0 }.size() - 179
        log.debug 'pills left: {}', pillsLeft
        return pillsLeft == 0
    }

    def calcPosAndMoveGhosts(Graphics2D g2d) {

        short ghostId
        int pos
        int count

        for (ghostId = 0; ghostId < numberOfGhosts; ghostId++) {

            if (ghosts_x[ghostId] % BLOCK_SIZE == 0 && ghosts_y[ghostId] % BLOCK_SIZE == 0) {

                pos = ghosts_x[ghostId] / BLOCK_SIZE + N_BLOCKS * (int) (ghosts_y[ghostId] / BLOCK_SIZE)

                count = 0

                if ((screenData[pos] & 1) == 0 && ghosts_dx[ghostId] != 1) {
                    dx[count] = -1
                    dy[count] = 0
                    count++
                }

                if ((screenData[pos] & 2) == 0 && ghosts_dy[ghostId] != 1) {
                    dx[count] = 0
                    dy[count] = -1
                    count++
                }

                if ((screenData[pos] & 4) == 0 && ghosts_dx[ghostId] != -1) {
                    dx[count] = 1
                    dy[count] = 0
                    count++
                }

                if ((screenData[pos] & 8) == 0 && ghosts_dy[ghostId] != -1) {
                    dx[count] = 0
                    dy[count] = 1
                    count++
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghosts_dx[ghostId] = 0
                        ghosts_dy[ghostId] = 0
                    } else {
                        ghosts_dx[ghostId] = -ghosts_dx[ghostId]
                        ghosts_dy[ghostId] = -ghosts_dy[ghostId]
                    }

                } else {

                    count = (int) (Math.random() * count)

                    if (count > 3) {
                        count = 3
                    }

                    ghosts_dx[ghostId] = dx[count]
                    ghosts_dy[ghostId] = dy[count]
                }
            }

            log.debug('ghost #{}/{} : speed={}, position=[{}, {}], direction=[{}, {}]', ghostId, numberOfGhosts, ghosts_speed[ghostId], ghosts_x[ghostId], ghosts_y[ghostId], ghosts_dx[ghostId], ghosts_dy[ghostId])

            ghosts_x[ghostId] = ghosts_x[ghostId] + (ghosts_dx[ghostId] * ghosts_speed[ghostId])
            ghosts_y[ghostId] = ghosts_y[ghostId] + (ghosts_dy[ghostId] * ghosts_speed[ghostId])

            drawGhost(g2d, ghosts_x[ghostId] + 1, ghosts_y[ghostId] + 1)

            // detect if pacman collides with ghost
            if (gameState == RUNNING
                    && pacman_x > (ghosts_x[ghostId] - 12) && pacman_x < (ghosts_x[ghostId] + 12)
                    && pacman_y > (ghosts_y[ghostId] - 12) && pacman_y < (ghosts_y[ghostId] + 12)
            ) {
                log.debug('pacman collided with ghost')
                dying = true
            }
        }
    }

    def drawGhost(Graphics2D g2d, Integer x, Integer y) {
        g2d.drawImage(ghost, x, y, this)
    }

    def calcPostitionPacman() {

        int pos
        short ch

        if (req_dx == -pacman_dx && req_dy == -pacman_dy) {
            pacman_dx = req_dx
            pacman_dy = req_dy
            view_dx = pacman_dx
            view_dy = pacman_dy
        }

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = (int) (pacman_x / BLOCK_SIZE) + (N_BLOCKS * (int) (pacman_y / BLOCK_SIZE))
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
                    pacman_dx = req_dx
                    pacman_dy = req_dy
                    view_dx = pacman_dx
                    view_dy = pacman_dy
                }
            }

            // Check for standstill
            if ((pacman_dx == -1 && pacman_dy == 0 && (ch & 1) != 0)
                    || (pacman_dx == 1 && pacman_dy == 0 && (ch & 4) != 0)
                    || (pacman_dx == 0 && pacman_dy == -1 && (ch & 2) != 0)
                    || (pacman_dx == 0 && pacman_dy == 1 && (ch & 8) != 0)) {
                pacman_dx = 0
                pacman_dy = 0
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacman_dx
        pacman_y = pacman_y + PACMAN_SPEED * pacman_dy
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
     * Each event is just a trigger to repaint the {@link Board}.
     *
     * The implementation just calls {@link Board#paint(java.awt.Graphics)}, which in turn calls {@link Board#paintBoard(java.awt.Graphics2D)}
     * @param event
     */
    @Override
    void actionPerformed(ActionEvent event) {
        // causes this component
        repaint()
    }

    /**
     * Called by
     * @param g
     */
    @Override
    void paint(Graphics g) {
        super.paintComponent(g)
        paintBoard(g as Graphics2D)
    }

    /**
     * this is essentially the main loop: it is implicitly called by the configured {@link #timer}
     * @param g2d
     * @return
     */
    def paintBoard(Graphics2D g2d) {

        drawBackGround(g2d, backGroundColor)
        maze.paintMaze(g2d)
        drawScore(g2d)
        doAnimation()

        switch (gameState) {

            case RUNNING:
                continueGame(g2d)
                break

            case LEVEL_LOST:
                paintGameOverScreen(g2d)
                break

            case LEVELCOMPLETE:
                nextLevel()
                break

            case STOPPED:
                paintIntroScreen(g2d)
                break

            case PAUSED:
                paintPausedScreen(g2d)
                pauseGame()
                break

            default:
                throw new InvalidParameterException("bug: unsupported game state '$gameState' detected")
        }
        getDefaultToolkit().sync()
    }

    class GameKeyListener extends KeyAdapter {

        @Override
        void keyPressed(KeyEvent keyEvent) {

            switch (gameState) {
                case RUNNING:
                    handlePlayingModeKey(keyEvent)
                    break
                case LEVEL_LOST:
                    handleGameOverModeKey(keyEvent)
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

        def handlePlayingModeKey(KeyEvent event) {

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
                    gameState = STOPPED
                    break
                case VK_SPACE:
                    gameState = PAUSED
                    break
            }
        }

        def handleGameOverModeKey(KeyEvent event) {
            exitGame()
        }

        def handleStoppedModeKey(KeyEvent event) {

            switch (event.keyCode) {
                case VK_S:
                    gameState = RUNNING
                    startGame()
                    break
                case VK_Q:
                    exitGame()
                    break

            }
        }

        def handlePauzedModeKey(KeyEvent event) {

            if (event.keyCode == VK_SPACE) {
                gameState = RUNNING
                resumeGame()
            }
        }

    }


}