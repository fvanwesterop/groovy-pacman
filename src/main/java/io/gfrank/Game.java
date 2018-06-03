package io.gfrank;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class Game extends JFrame {

    public Game() {

        initUI();
    }

    private void initUI() {

        add(new Board());
        setTitle("Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 420);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                Game ex = new Game();
                ex.setVisible(true);
            }
        });
    }
}
