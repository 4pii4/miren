package net.ccbluex.liquidbounce.utils

import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*

// from https://stackoverflow.com/a/18004334
object DesktopUtils {
    fun browse(url: String) {
        browse(URI(url))
    }

    fun browse(uri: URI): Boolean {
        return if (openSystemSpecific(uri.toString())) true else browseDESKTOP(uri)
    }

    fun open(file: File): Boolean {
        return if (openSystemSpecific(file.path)) true else openDESKTOP(file)
    }

    fun edit(file: File): Boolean {
        return if (openSystemSpecific(file.path)) true else editDESKTOP(file)
    }

    private fun openSystemSpecific(what: String): Boolean {
        val os = os
        if (os.isLinux) {
            if (runCommand("kde-open", "%s", what)) return true
            if (runCommand("gnome-open", "%s", what)) return true
            if (runCommand("xdg-open", "%s", what)) return true
        }
        if (os.isMac) {
            if (runCommand("open", "%s", what)) return true
        }
        return if (os.isWindows) {
            runCommand("explorer", "%s", what)
        } else false
    }

    private fun browseDESKTOP(uri: URI): Boolean {
        logOut("Trying to use Desktop.getDesktop().browse() with $uri")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                logErr("BROWSE is not supported.")
                return false
            }
            Desktop.getDesktop().browse(uri)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop browse.", t)
            false
        }
    }

    private fun openDESKTOP(file: File): Boolean {
        logOut("Trying to use Desktop.getDesktop().open() with $file")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                logErr("OPEN is not supported.")
                return false
            }
            Desktop.getDesktop().open(file)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop open.", t)
            false
        }
    }

    private fun editDESKTOP(file: File): Boolean {
        logOut("Trying to use Desktop.getDesktop().edit() with $file")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
                logErr("EDIT is not supported.")
                return false
            }
            Desktop.getDesktop().edit(file)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop edit.", t)
            false
        }
    }

    private fun runCommand(command: String, args: String, file: String): Boolean {
        logOut("Trying to exec:\n   cmd = $command\n   args = $args\n   %s = $file")
        val parts = prepareCommand(command, args, file)
        return try {
            val p = Runtime.getRuntime().exec(parts) ?: return false
            try {
                val retval = p.exitValue()
                if (retval == 0) {
                    logErr("Process ended immediately.")
                    false
                } else {
                    logErr("Process crashed.")
                    false
                }
            } catch (itse: IllegalThreadStateException) {
                logErr("Process is running.")
                true
            }
        } catch (e: IOException) {
            logErr("Error running command.", e)
            false
        }
    }

    private fun prepareCommand(command: String, args: String?, file: String): Array<String> {
        val parts: MutableList<String> = ArrayList()
        parts.add(command)
        if (args != null) {
            for (s in args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                parts.add(String.format(s, file).trim { it <= ' ' })
            }
        }
        return parts.toTypedArray()
    }

    private fun logErr(msg: String, t: Throwable) {
        System.err.println(msg)
        t.printStackTrace()
    }

    private fun logErr(msg: String) {
        System.err.println(msg)
    }

    private fun logOut(msg: String) {
        println(msg)
    }

    val os: EnumOS
        get() {
            val s = System.getProperty("os.name").lowercase(Locale.getDefault())
            if (s.contains("win")) {
                return EnumOS.windows
            }
            if (s.contains("mac")) {
                return EnumOS.macos
            }
            if (s.contains("solaris")) {
                return EnumOS.solaris
            }
            if (s.contains("sunos")) {
                return EnumOS.solaris
            }
            if (s.contains("linux")) {
                return EnumOS.linux
            }
            return if (s.contains("unix")) {
                EnumOS.linux
            } else {
                EnumOS.unknown
            }
        }

    enum class EnumOS {
        linux, macos, solaris, unknown, windows;

        val isLinux: Boolean
            get() = this == linux || this == solaris
        val isMac: Boolean
            get() = this == macos
        val isWindows: Boolean
            get() = this == windows
    }
}