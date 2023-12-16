/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.misc

import net.ccbluex.liquidbounce.utils.DesktopUtils.browse
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane

object MiscUtils : MinecraftInstance() {
    fun showErrorPopup(title: String?, message: String?) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
    }

    fun showURL(url: String?) {
        try {
            browse(URI(url))
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun openFileChooser(): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()
        val fileChooser = JFileChooser()
        val frame = JFrame()
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
        frame.isVisible = true
        frame.toFront()
        frame.isVisible = false
        val action = fileChooser.showOpenDialog(frame)
        frame.dispose()
        return if (action == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile else null
    }

    fun saveFileChooser(): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()
        val fileChooser = JFileChooser()
        val frame = JFrame()
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
        frame.isVisible = true
        frame.toFront()
        frame.isVisible = false
        val action = fileChooser.showSaveDialog(frame)
        frame.dispose()
        return if (action == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile else null
    }
}
