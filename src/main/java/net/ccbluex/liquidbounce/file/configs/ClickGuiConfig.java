/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.file.configs;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoClickGui;

import java.io.File;
import java.io.IOException;

public class ClickGuiConfig extends FileConfig {

    /**
     * Constructor of config
     *
     * @param file of config
     */
    public ClickGuiConfig(final File file) {
        super(file);
    }

    /**
     * Load config from file
     */
    @Override
    protected void loadConfig() throws IOException {
        AstolfoClickGui.Companion.getInstance().loadConfig();
        LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.valuesConfig);
    }

    /**
     * Save config to file
     *
     * @throws IOException exception
     */
    @Override
    protected void saveConfig() throws IOException {
        AstolfoClickGui.Companion.getInstance().saveConfig();
        LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.valuesConfig);
    }
}