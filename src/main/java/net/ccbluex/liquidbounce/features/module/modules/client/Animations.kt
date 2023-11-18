/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Animations", description = "Render items Animations", category = ModuleCategory.CLIENT)
object Animations : Module() {
    val sword by ListValue(
        "Style", arrayOf(
            "Normal", "SlideDown1", "SlideDown2", "Slide", "Slide1", "Minecraft", "Remix", "Exhibition",
            "Avatar", "Swang", "Tap1", "Tap2", "Poke", "Push1", "Push2", "Up", "Shield", "Akrien", "VisionFX", "Swong", "Swank",
            "SigmaOld", "ETB", "Rotate360", "SmoothFloat", "Strange", "Reverse", "Zoom", "Move", "Stab", "Jello", "1.7", "Flux", "Stella", "Tifality", "OldExhibition", "Smooth"
        ), "Minecraft"
    )

    // item general scale
    val Scale by FloatValue("Scale", 0.4f, 0f, 4f)

    // normal item position
    val itemPosX by FloatValue("ItemX", 0f, -1f, 1f)
    val itemPosY by FloatValue("ItemY", 0f, -1f, 1f)
    val itemPosZ by FloatValue("ItemZ", 0f, -1f, 1f)
    val itemDistance by FloatValue("ItemDistance", 1f, 1f, 5f)

    // change Position Blocking Sword
    val blockPosX by FloatValue("BlockingX", 0f, -1f, 1f)
    val blockPosY by FloatValue("BlockingY", 0f, -1f, 1f)
    val blockPosZ by FloatValue("BlockingZ", 0f, -1f, 1f)

    // modify item swing and rotate
    val SpeedSwing by IntegerValue("Swing-Speed", 4, 0, 20)

    // custom animation sword
    val mcSwordPos by FloatValue("MCPosOffset", 0.45f, 0f, 0.5f) { sword.equals("minecraft", ignoreCase = true) }

    // fake blocking bruh
    val fakeBlock by BoolValue("Fake-Block", false)

    // block not everything
    val blockEverything by BoolValue("Block-Everything", false)
    val swing by BoolValue("FluxSwing", false)

    // gui animations
    val guiAnimations by ListValue("Container-Animation", arrayOf("None", "Zoom", "Slide", "Smooth"), "None")
    val vSlideValue by ListValue("Slide-Vertical", arrayOf("None", "Upward", "Downward"), "Downward") { guiAnimations.equals("slide", ignoreCase = true) }
    val hSlideValue by ListValue("Slide-Horizontal", arrayOf("None", "Right", "Left"), "Right") { guiAnimations.equals("slide", ignoreCase = true) }
    val animTimeValue by IntegerValue("Container-AnimTime", 750, 0, 3000) { !guiAnimations.equals("none", ignoreCase = true) }
    val tabAnimations by ListValue("Tab-Animation", arrayOf("None", "Zoom", "Slide"), "Zoom")

    // block crack
    val noBlockParticles by BoolValue("NoBlockParticles", false)

    //1.7
    val oldBow by BoolValue("1.7Bow", false)
    val oldRod by BoolValue("1.7Rod", false)
    val oldSwing by BoolValue("1.7Swing", false)
}