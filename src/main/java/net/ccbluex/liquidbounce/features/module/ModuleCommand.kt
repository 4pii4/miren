/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.TextColorUtils.gray
import net.ccbluex.liquidbounce.utils.TextColorUtils.green
import net.ccbluex.liquidbounce.utils.TextColorUtils.red
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.Block
import java.util.*

/**
 * Module command
 *
 * @author SenkJu
 */
class ModuleCommand(val module: Module, val values: List<Value<*>> = module.values) :
    Command(module.name.lowercase(Locale.getDefault()), emptyArray()) {

    init {
        if (values.isEmpty())
            throw IllegalArgumentException("Values are empty!")
    }

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val values = values.filter { it !is NoteValue }
        val valueNames = values
            .filter { it !is FontValue }
            .joinToString(separator = "/") { it.name.lowercase(Locale.getDefault()) }

        val moduleName = module.name.lowercase(Locale.getDefault())

        if (args.size < 2) {
            chatSyntax(if (values.size == 1) "$moduleName $valueNames <value>" else "$moduleName <$valueNames>")
            return
        }

        val value = module.getValue(args[1])

        if (value == null) {
            chatSyntax("$moduleName <$valueNames>")
            return
        }

        //TODO: add a way to set font
        when (value) {
            is BlockValue -> {
                when (args.size) {
                    2 -> {
                        chat("${highlightModule(module)}.${gray(args[1])}=${BlockUtils.getBlockName(value.get())}(id=${value.get()})")
                        return
                    }
                    3 -> {
                        var id: Int

                        try {
                            id = args[2].toInt()
                        } catch (exception: NumberFormatException) {
                            id = Block.getIdFromBlock(Block.getBlockFromName(args[2]))

                            if (id <= 0) {
                                chat("Block ${red(args[2])} does not exist!")
                                return
                            }
                        }

                        value.set(id)
                        chat("${highlightModule(module)}.${gray(args[1])}=${BlockUtils.getBlockName(value.get())}(id=${value.get()})")
                        playEdit()
                        return
                    }

                    else -> {
                        chat(red("Invalid syntax"))
                        return
                    }
                }
            }

            is BoolValue -> {
                value.set(!value.get())
            }

            is TextValue -> {
                when (args.size) {
                    2 -> { }
                    else -> {
                        value.set(StringUtils.toCompleteString(args, 2))
                    }
                }
            }

            is IntegerValue -> {
                when (args.size) {
                    2 -> { }
                    3 -> {
                        val number = args[2].toIntOrNull()
                        if (number == null) {
                            chat("${args[2]} cannot be converted to a number")
                            return
                        }
                        value.set(number)
                    }
                    else -> {
                        chat(red("Invalid syntax"))
                        return
                    }
                }
            }

            is FloatValue -> {
                when (args.size) {
                    2 -> { }
                    3 -> {
                        val number = args[2].toFloatOrNull()
                        if (number == null) {
                            chat("${args[2]} cannot be converted to a number")
                            return
                        }
                        value.set(number)
                    }
                    else -> {
                        chat(red("Invalid syntax"))
                        return
                    }
                }
            }

            is ListValue -> {
                when (args.size) {
                    2 -> { }
                    3 -> {
                        val v = args[2]
                        if (value.values.map { it.lowercase() }.contains(v.lowercase())) {
                            chat("${red(v)} is not a valid value")
                            return
                        }
                        value.set(v)
                    }
                    else -> {
                        chat(red("Invalid syntax"))
                        return
                    }
                }
            }
        }

        chat("${highlightModule(module)}.${gray(args[1])}=${value.get()}")
        playEdit()
    }


    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()
        val values = values.filter { it !is NoteValue }

        return when (args.size) {
            1 -> values
                .filter { it !is FontValue && it.name.startsWith(args[0], true) }
                .map { it.name.lowercase(Locale.getDefault()) }
            2 -> {
                when(module.getValue(args[0])) {
                    is BlockValue -> {
                        return Block.blockRegistry.keys
                            .map { it.resourcePath.lowercase(Locale.getDefault()) }
                            .filter { it.startsWith(args[1], true) }
                    }
                    is ListValue -> {
                        values.forEach { value ->
                            if (!value.name.equals(args[0], true))
                                return@forEach
                            if (value is ListValue)
                                return value.values.filter { it.startsWith(args[1], true) }
                        }
                        return emptyList()
                    }                    
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
