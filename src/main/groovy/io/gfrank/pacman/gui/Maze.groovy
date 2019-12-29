package io.gfrank.pacman.gui


import groovy.util.logging.Slf4j

import javax.swing.*
import java.awt.*

@Slf4j
class Maze extends JPanel {

    java.util.List<Short> screenData = []
    Integer blockCount
    Integer blockSize
    Color mazeColor
    Color dotColor

    @Override
    void paint(Graphics g) {
        super.paintComponent(g)
        paintMaze(g as Graphics2D)
        log.info 'painting...'

    }

    @Override
    void repaint(Rectangle r) {
        super.repaint(r)
        log.info 're-painting...'

    }

    def paintMaze(Graphics2D g2d) {


        def boardSize = blockCount * blockSize
        short i = 0
        int x, y

        for (y = 0; y < boardSize; y += blockSize) {

            for (x = 0; x < boardSize; x += blockSize) {

                g2d.setColor(mazeColor)
                g2d.setStroke(new BasicStroke(4))

                if ((screenData[i] & 1) > 0) { // 1th bit 'on'? draw vertical line on the left side of the block
                    g2d.drawLine(x, y, x, y + blockSize - 1)
                }

                if ((screenData[i] & 2) > 0 ) { // 2nd bit 'on'? draw horizontal line to the top of the block
                    g2d.drawLine(x, y, x + blockSize - 1, y)
                }

                if ((screenData[i] & 4) > 0) { // 3d bit 'on'? draw vertical line on the right side of the block
                    g2d.drawLine(x + blockSize - 1, y, x + blockSize - 1,  y + blockSize - 1)
                }

                if ((screenData[i] & 8) > 0) { // 4th bit 'on'? draw horizontal line at the bottom of the block
                    g2d.drawLine(x, y + blockSize - 1, x + blockSize - 1, y + blockSize - 1)
                }

                if ((screenData[i] & 16) > 0) { // 5th bit 'on'? draw small pill
                    g2d.setColor(dotColor)
                    g2d.fillOval(x + 21 , y + 21, 6, 6)
                }

                i++
            }
        }
    }



}
