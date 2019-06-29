package io.gfrank

import io.gfrank.pacman.gui.Board

import javax.swing.JFrame

import static java.awt.EventQueue.invokeLater

class Game extends JFrame {

    static main(def args) {

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
