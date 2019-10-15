package io.gfrank

import groovy.util.logging.Slf4j
import io.gfrank.pacman.gui.Board

import javax.swing.JFrame

import static java.awt.EventQueue.invokeLater

@Slf4j
class Game extends JFrame {

    static main(def args) {

        log.info('starting game..')

        invokeLater {

            def game = new Game()
            def board = new Board()

            game.contentPane.add(board)
            game.title = 'Pacman'
            game.defaultCloseOperation = EXIT_ON_CLOSE
            game.locationRelativeTo = null
            game.resizable = false

            game.pack()
            game.visible = true
        }
    }
}
