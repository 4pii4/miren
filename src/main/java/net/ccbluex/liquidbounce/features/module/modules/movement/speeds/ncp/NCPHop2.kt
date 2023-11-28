package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.stopXZ

class NCPHop2 : SpeedMode("NCPHop2") {

    override fun onDisable() {
        airSpeedReset()
        strafeZero()
        super.onDisable()
    }

    private fun airSpeedReset() {
        mc.thePlayer.speedInAir = 0.02f
    }

    private fun strafeZero() {
        strafe(0f)
    }

    override fun onUpdate() {
        if (mc.thePlayer.isInLava || mc.thePlayer.isInWater
            || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb) {
            strafeZero()
            return
        }

        if (isMoving) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                strafe(0.03f)
            } else {
                mc.thePlayer.speedInAir = 0.065f
                strafe(0.2f)
            }

            // Prevent from getting flag while airborne/falling & fall damage
            if (mc.thePlayer.isAirBorne && mc.thePlayer.fallDistance >= 3) {
                strafeZero()
                airSpeedReset()

                if (mc.thePlayer.hurtTime > 0) {
                    mc.thePlayer.stopXZ()
                }
            }

        } else {
            strafeZero()
            mc.thePlayer.stopXZ()
        }
    }
}