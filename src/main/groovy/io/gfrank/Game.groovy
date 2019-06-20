package io.gfrank

import io.gfrank.pacman.gui.Board

import javax.swing.JPanel
import java.awt.EventQueue
import javax.swing.JFrame

class Game extends JFrame {

    static void main(def args) {

        EventQueue.invokeLater(new Runnable() {

            @Override
            void run() {
                Game game = new Game()
                game.setTitle("Pacman")
                game.setDefaultCloseOperation(EXIT_ON_CLOSE)
                game.setLocationRelativeTo(null)
                game.setResizable(false)

                JPanel board = new Board()
                game.getContentPane().add(board)

                game.pack()
                game.setVisible(true)
            }
        })
    }
}
