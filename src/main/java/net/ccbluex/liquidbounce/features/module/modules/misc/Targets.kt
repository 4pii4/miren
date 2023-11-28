package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "Targets", category = ModuleCategory.MISC, description = "Wrapper module for .target", createCommand = false)
class Targets : Module() {
    private val targetInvisible by object: BoolValue("Invisible", false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetInvisible = newValue
        }

        override fun get() = EntityUtils.targetInvisible
    }

    private val targetPlayer by object: BoolValue("Player", true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetPlayer = newValue
        }

        override fun get() = EntityUtils.targetPlayer
    }

    private val targetMobs by object: BoolValue("Mobs", true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetMobs = newValue
        }

        override fun get() = EntityUtils.targetMobs
    }

    private val targetAnimals by object: BoolValue("Animals", false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetAnimals = newValue
        }

        override fun get() = EntityUtils.targetAnimals
    }

    private val targetDead by object: BoolValue("Dead", false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetDead = newValue
        }

        override fun get() = EntityUtils.targetDead
    }
}