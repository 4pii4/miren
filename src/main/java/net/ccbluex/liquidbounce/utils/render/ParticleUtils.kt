/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.render

import net.vitox.ParticleGenerator

object ParticleUtils {
    private val particleGenerator = ParticleGenerator(100)
    @JvmStatic
    fun drawParticles(mouseX: Int, mouseY: Int) {
        particleGenerator.draw(mouseX, mouseY)
    }
}