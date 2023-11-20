package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.misc.FileUtils
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.IOException
import java.io.InputStream

class ShaderUtil @JvmOverloads constructor(fragmentShaderLoc: String?, vertexShaderLoc: String? = "shaders/vertex.vsh") : MinecraftInstance() {
    private val programID: Int

    init {
        val program = GL20.glCreateProgram()
        try {
            val fragmentShaderID = createShader(mc.resourceManager.getResource(ResourceLocation(fragmentShaderLoc)).inputStream, GL20.GL_FRAGMENT_SHADER)
            GL20.glAttachShader(program, fragmentShaderID)
            val vertexShaderID = createShader(mc.resourceManager.getResource(ResourceLocation(vertexShaderLoc)).inputStream, GL20.GL_VERTEX_SHADER)
            GL20.glAttachShader(program, vertexShaderID)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        GL20.glLinkProgram(program)
        val status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS)
        check(status != 0) { "Shader failed to link!" }
        programID = program
    }

    fun init() {
        GL20.glUseProgram(programID)
    }

    fun unload() {
        GL20.glUseProgram(0)
    }

    fun setUniformf(name: String?, vararg args: Float) {
        val loc = GL20.glGetUniformLocation(programID, name)
        when (args.size) {
            1 -> GL20.glUniform1f(loc, args[0])
            2 -> GL20.glUniform2f(loc, args[0], args[1])
            3 -> GL20.glUniform3f(loc, args[0], args[1], args[2])
            4 -> GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun setUniformi(name: String?, vararg args: Int) {
        val loc = GL20.glGetUniformLocation(programID, name)
        if (args.size > 1) GL20.glUniform2i(loc, args[0], args[1]) else GL20.glUniform1i(loc, args[0])
    }

    private fun createShader(inputStream: InputStream, shaderType: Int): Int {
        val shader = GL20.glCreateShader(shaderType)
        GL20.glShaderSource(shader, FileUtils.readInputStream(inputStream))
        GL20.glCompileShader(shader)
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            println(GL20.glGetShaderInfoLog(shader, 4096))
            throw IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType))
        }
        return shader
    }

    companion object {
        @JvmStatic
        fun drawQuads(x: Float, y: Float, width: Float, height: Float) {
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(x, y)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(x, y + height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(x + width, y + height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
        }
    }
}