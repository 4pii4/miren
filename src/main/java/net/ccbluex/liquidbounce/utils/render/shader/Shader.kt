/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.render.shader

import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.*
import java.io.File
import java.io.IOException
import java.nio.file.Files

abstract class Shader : MinecraftInstance {
    var programId: Int = 0
        private set
    private var uniformsMap: MutableMap<String, Int>? = null

    constructor(fragmentShader: String) {
        val vertexShaderID: Int
        val fragmentShaderID: Int

        try {
            val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/liquidbounce+/shader/vertex.vert")
            vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
            IOUtils.closeQuietly(vertexStream)

            val fragmentStream = javaClass.getResourceAsStream("/assets/minecraft/liquidbounce+/shader/fragment/$fragmentShader")
            fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
            IOUtils.closeQuietly(fragmentStream)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        if (vertexShaderID == 0 || fragmentShaderID == 0)
            return

        programId = ARBShaderObjects.glCreateProgramObjectARB()

        if (programId == 0)
            return

        ARBShaderObjects.glAttachObjectARB(programId, vertexShaderID)
        ARBShaderObjects.glAttachObjectARB(programId, fragmentShaderID)

        ARBShaderObjects.glLinkProgramARB(programId)
        ARBShaderObjects.glValidateProgramARB(programId)

        ClientUtils.logger.info("[Shader] Successfully loaded: $fragmentShader")
    }

    @Throws(IOException::class)
    constructor(fragmentShader: File) {
        val vertexShaderID: Int
        val fragmentShaderID: Int

        val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/liquidbounce+/shader/vertex.vert")
        vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
        IOUtils.closeQuietly(vertexStream)

        val fragmentStream = Files.newInputStream(fragmentShader.toPath())
        fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
        IOUtils.closeQuietly(fragmentStream)

        if (vertexShaderID == 0 || fragmentShaderID == 0)
            return

        programId = ARBShaderObjects.glCreateProgramObjectARB()

        if (programId == 0)
            return

        ARBShaderObjects.glAttachObjectARB(programId, vertexShaderID)
        ARBShaderObjects.glAttachObjectARB(programId, fragmentShaderID)

        ARBShaderObjects.glLinkProgramARB(programId)
        ARBShaderObjects.glValidateProgramARB(programId)

        ClientUtils.logger.info("[Shader] Successfully loaded: " + fragmentShader.name)
    }

    fun init() {
        GL20.glUseProgram(programId)
    }

    fun setUniformi(name: String?, vararg args: Int) {
        val loc = GL20.glGetUniformLocation(programId, name)
        if (args.size > 1) {
            GL20.glUniform2i(loc, args[0], args[1])
        } else {
            GL20.glUniform1i(loc, args[0])
        }
    }

    fun unload() {
        GL20.glUseProgram(0)
    }

    fun setUniformf(name: String?, vararg args: Float) {
        val loc = GL20.glGetUniformLocation(programId, name)
        when (args.size) {
            1 -> {
                GL20.glUniform1f(loc, args[0])
            }

            2 -> {
                GL20.glUniform2f(loc, args[0], args[1])
            }

            3 -> {
                GL20.glUniform3f(loc, args[0], args[1], args[2])
            }

            4 -> {
                GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
            }
        }
    }

    open fun startShader() {
        GL11.glPushMatrix()
        GL20.glUseProgram(programId)
        if (uniformsMap == null) {
            uniformsMap = HashMap()
            setupUniforms()
        }
        updateUniforms()
    }

    open fun stopShader() {
        GL20.glUseProgram(0)
        GL11.glPopMatrix()
    }

    abstract fun setupUniforms()
    abstract fun updateUniforms()
    private fun createShader(shaderSource: String, shaderType: Int): Int {
        var shader = 0
        return try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType)
            if (shader == 0) return 0
            ARBShaderObjects.glShaderSourceARB(shader, shaderSource)
            ARBShaderObjects.glCompileShaderARB(shader)
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) throw RuntimeException("Error creating shader: " + getLogInfo(shader))
            shader
        } catch (e: Exception) {
            ARBShaderObjects.glDeleteObjectARB(shader)
            throw e
        }
    }

    private fun getLogInfo(i: Int): String {
        return ARBShaderObjects.glGetInfoLogARB(i, ARBShaderObjects.glGetObjectParameteriARB(i, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB))
    }

    fun setUniform(uniformName: String, location: Int) {
        uniformsMap!![uniformName] = location
    }

    fun setupUniform(uniformName: String) {
        setUniform(uniformName, GL20.glGetUniformLocation(programId, uniformName))
    }

    fun getUniform(uniformName: String): Int {
        return uniformsMap!![uniformName]!!
    }

    companion object {
        @JvmStatic
        fun drawQuads(x: Float, y: Float, width: Float, height: Float) {
            GL11.glBegin(7)
            GL11.glTexCoord2f(0.0f, 0.0f)
            GL11.glVertex2f(x, y)
            GL11.glEnd()
        }

        fun drawQuad(x: Float, y: Float, width: Float, height: Float) {
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0.0f, 0.0f)
            GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
            GL11.glTexCoord2f(1.0f, 0.0f)
            GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
            GL11.glTexCoord2f(1.0f, 1.0f)
            GL11.glVertex2d((x + width).toDouble(), y.toDouble())
            GL11.glTexCoord2f(0.0f, 1.0f)
            GL11.glVertex2d(x.toDouble(), y.toDouble())
            GL11.glEnd()
        }

        fun drawTextureSpecifiedQuad(x: Float, y: Float, width: Float, height: Float) {
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0.0f, 1.0f)
            GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
            GL11.glTexCoord2f(1.0f, 1.0f)
            GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
            GL11.glTexCoord2f(1.0f, 0.0f)
            GL11.glVertex2d((x + width).toDouble(), y.toDouble())
            GL11.glTexCoord2f(0.0f, 0.0f)
            GL11.glVertex2d(x.toDouble(), y.toDouble())
            GL11.glEnd()
        }
    }
}
