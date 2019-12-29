package io.gfrank.pacman.sound

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/**
 * See <a href="https://www.codejava.net/coding/how-to-play-back-audio-in-java-with-examples">this article</a> for an example using {@link Clip}
 * playing with an {@link javax.sound.sampled.LineListener} attached that receives {@link javax.sound.sampled.LineEvent}'s
 * to get notified when playing a clip ends.
 * <p/>
 * Also check:
 * <ul>
 *     <li><a href="http://mrclan.com/fastdl/tfc/sound/">this collection of wave tables</a>
 *     <li><a href="https://www.findsounds.com/ISAPI/search.dll?start=11&keywords=pacman%20pac%20man&seed=14">findsounds.com</a>
 *     <li><a href="http://sweetsoundeffects.com/">sweetsoundeffects.com</a>
 */
class SoundSystem {

    Clip themeSong = AudioSystem.getClip()

    SoundSystem() {
        themeSong.open(AudioSystem.getAudioInputStream(getClass().getResource('pacman-theme.wav')));
    }

    def playTheme() {
        themeSong.loop(Clip.LOOP_CONTINUOUSLY)
    }
}
