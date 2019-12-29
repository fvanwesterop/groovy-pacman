package io.gfrank.pacman

import groovy.util.logging.Slf4j
import io.gfrank.pacman.sound.SoundSystem

import groovy.transform.CompileStatic

import java.awt.EventQueue
import javax.swing.JFrame

@Slf4j
@CompileStatic
class Game extends JFrame {

    static main(def args) {

        def soundSystem = new SoundSystem()

        EventQueue.invokeLater {
            Game game = new Game()
            game.pack()
            game.setVisible(true)
        }

        Thread.sleep(1000)
        soundSystem.playTheme()
    }

    Game() {
        initUI()
        log.info('starting game..')
    }

    void initUI() {
        add(new Board())
        title = 'Pacman'
        setDefaultCloseOperation( EXIT_ON_CLOSE)
        locationRelativeTo = null
        resizable = false
    }

}
