package net.ccbluex.liquidbounce.ui.client.clickgui.styles.newVer.extensions

import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils

fun Float.animSmooth(target: Float, speed: Float) = if (ClickGUI.fastRenderValue.get()) target else AnimationUtils.animate(target, this, speed * RenderUtils.deltaTime * 0.025F)
fun Float.animLinear(speed: Float, min: Float, max: Float) = if (ClickGUI.fastRenderValue.get()) { if (speed < 0F) min else max } else (this + speed).coerceIn(min, max)