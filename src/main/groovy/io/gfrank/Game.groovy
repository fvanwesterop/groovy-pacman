package io.gfrank

import java.awt.EventQueue
import javax.swing.JFrame

class Game extends JFrame {

    Game() {

        initUI()
    }

    private void initUI() {

        add(new Board())
        setTitle("Game")
        setDefaultCloseOperation(EXIT_ON_CLOSE)
        setSize(380, 420)
        setLocationRelativeTo(null)
        setVisible(true)
    }

    static void main(def args) {

        EventQueue.invokeLater(new Runnable() {

            @Override
            void run() {
                Game ex = new Game()
                ex.setVisible(true)
            }
        })
    }
}
