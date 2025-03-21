/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.file.configs;

import com.google.gson.*;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.features.special.AutoReconnect;
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof;
import net.ccbluex.liquidbounce.features.special.MacroManager;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.altgenerator.GuiTheAltening;
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.newVer.InfClickGui;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.value.Value;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class ValuesConfig extends FileConfig {

    /**
     * Constructor of config
     *
     * @param file of config
     */
    public ValuesConfig(final File file) {
        super(file);
    }

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Override
    protected void loadConfig() throws IOException {
        final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(getFile())));

        if(jsonElement instanceof JsonNull)
            return;

        final JsonObject jsonObject = (JsonObject) jsonElement;

        final Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        while(iterator.hasNext()) {
            final Map.Entry<String, JsonElement> entry = iterator.next();

            if (entry.getKey().equalsIgnoreCase("NewGui")) {
                JsonObject positions = entry.getValue().getAsJsonObject();
                if (positions.has("StartX"))
                    InfClickGui.Companion.getInstance().setWindowXStart(positions.get("StartX").getAsFloat());
                if (positions.has("StartY"))
                    InfClickGui.Companion.getInstance().setWindowYStart(positions.get("StartY").getAsFloat());
                if (positions.has("EndX"))
                    InfClickGui.Companion.getInstance().setWindowXEnd(positions.get("EndX").getAsFloat());
                if (positions.has("EndY"))
                    InfClickGui.Companion.getInstance().setWindowYEnd(positions.get("EndY").getAsFloat());
                if (positions.has("SideWidth"))
                    InfClickGui.Companion.getInstance().setSideWidth(positions.get("SideWidth").getAsFloat());
            }

            if (entry.getKey().equalsIgnoreCase("CommandPrefix")) {
                LiquidBounce.commandManager.setPrefix(entry.getValue().getAsCharacter());
            } else if (entry.getKey().equalsIgnoreCase("ShowRichPresence")) {
                LiquidBounce.clientRichPresence.setShowRichPresenceValue(entry.getValue().getAsBoolean());
            } else if (entry.getKey().equalsIgnoreCase("SemiRandomFormat")) {
                GuiAltManager.Companion.getGenerateCracked().setText(entry.getValue().getAsString());
            } else if (entry.getKey().equalsIgnoreCase("targets")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("TargetPlayer"))
                    EntityUtils.targetPlayer = jsonValue.get("TargetPlayer").getAsBoolean();
                if (jsonValue.has("TargetMobs"))
                    EntityUtils.targetMobs = jsonValue.get("TargetMobs").getAsBoolean();
                if (jsonValue.has("TargetAnimals"))
                    EntityUtils.targetAnimals = jsonValue.get("TargetAnimals").getAsBoolean();
                if (jsonValue.has("TargetInvisible"))
                    EntityUtils.targetInvisible = jsonValue.get("TargetInvisible").getAsBoolean();
                if (jsonValue.has("TargetDead"))
                    EntityUtils.targetDead = jsonValue.get("TargetDead").getAsBoolean();
            } else if (entry.getKey().equalsIgnoreCase("macros")) {
                JsonArray jsonValue = entry.getValue().getAsJsonArray();
                for (final JsonElement macroElement : jsonValue) {
                    JsonObject macroObject = macroElement.getAsJsonObject();
                    JsonElement keyValue = macroObject.get("key");
                    JsonElement commandValue = macroObject.get("command");

                    MacroManager.INSTANCE.addMacro(keyValue.getAsInt(), commandValue.getAsString());
                }
            } else if (entry.getKey().equalsIgnoreCase("features")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("DarkMode"))
                    LiquidBounce.INSTANCE.setDarkMode(jsonValue.get("DarkMode").getAsBoolean());
                if (jsonValue.has("AntiForge"))
                    AntiForge.enabled = jsonValue.get("AntiForge").getAsBoolean();
                if (jsonValue.has("AntiForgeFML"))
                    AntiForge.blockFML = jsonValue.get("AntiForgeFML").getAsBoolean();
                if (jsonValue.has("AntiForgeProxy"))
                    AntiForge.blockProxyPacket = jsonValue.get("AntiForgeProxy").getAsBoolean();
                if (jsonValue.has("AntiForgePayloads"))
                    AntiForge.blockPayloadPackets = jsonValue.get("AntiForgePayloads").getAsBoolean();
                if (jsonValue.has("BungeeSpoof"))
                    BungeeCordSpoof.enabled = jsonValue.get("BungeeSpoof").getAsBoolean();
                if (jsonValue.has("AutoReconnectDelay"))
                    AutoReconnect.INSTANCE.setDelay(jsonValue.get("AutoReconnectDelay").getAsInt());
            } else if (entry.getKey().equalsIgnoreCase("thealtening")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("API-Key"))
                    GuiTheAltening.Companion.setApiKey(jsonValue.get("API-Key").getAsString());
            } else if (entry.getKey().equalsIgnoreCase("Background")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("Enabled"))
                    GuiBackground.Companion.setEnabled(jsonValue.get("Enabled").getAsBoolean());

                if (jsonValue.has("Particles"))
                    GuiBackground.Companion.setParticles(jsonValue.get("Particles").getAsBoolean());
            } else {
                final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());

                if(module != null) {
                    final JsonObject jsonModule = (JsonObject) entry.getValue();

                    for(final Value moduleValue : module.getValues()) {
                        final JsonElement element = jsonModule.get(moduleValue.getName());

                        if(element != null) moduleValue.fromJson(element);
                    }
                }
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Override
    protected void saveConfig() throws IOException {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("CommandPrefix", LiquidBounce.commandManager.getPrefix());
        jsonObject.addProperty("ShowRichPresence", LiquidBounce.clientRichPresence.getShowRichPresenceValue());
        jsonObject.addProperty("SemiRandomFormat", GuiAltManager.Companion.getGenerateCracked().getText());

        final JsonObject jsonNewGui = new JsonObject();
        jsonNewGui.addProperty("StartX", InfClickGui.Companion.getInstance().getWindowXStart());
        jsonNewGui.addProperty("StartY", InfClickGui.Companion.getInstance().getWindowYStart());
        jsonNewGui.addProperty("EndX", InfClickGui.Companion.getInstance().getWindowXEnd());
        jsonNewGui.addProperty("EndY", InfClickGui.Companion.getInstance().getWindowYEnd());
        jsonNewGui.addProperty("SideWidth", InfClickGui.Companion.getInstance().getSideWidth());
        jsonObject.add("NewGui", jsonNewGui);

        final JsonObject jsonTargets = new JsonObject();
        jsonTargets.addProperty("TargetPlayer", EntityUtils.targetPlayer);
        jsonTargets.addProperty("TargetMobs", EntityUtils.targetMobs);
        jsonTargets.addProperty("TargetAnimals", EntityUtils.targetAnimals);
        jsonTargets.addProperty("TargetInvisible", EntityUtils.targetInvisible);
        jsonTargets.addProperty("TargetDead", EntityUtils.targetDead);
        jsonObject.add("targets", jsonTargets);

        final JsonArray jsonMacros = new JsonArray();
        MacroManager.INSTANCE.getMacroMapping().forEach((k, v) -> {
            final JsonObject jsonMacro = new JsonObject();
            jsonMacro.addProperty("key", k);
            jsonMacro.addProperty("command", v);
            jsonMacros.add(jsonMacro);
        });
        jsonObject.add("macros", jsonMacros);

        final JsonObject jsonFeatures = new JsonObject();
        jsonFeatures.addProperty("DarkMode", LiquidBounce.INSTANCE.getDarkMode());
        jsonFeatures.addProperty("AntiForge", AntiForge.enabled);
        jsonFeatures.addProperty("AntiForgeFML", AntiForge.blockFML);
        jsonFeatures.addProperty("AntiForgeProxy", AntiForge.blockProxyPacket);
        jsonFeatures.addProperty("AntiForgePayloads", AntiForge.blockPayloadPackets);
        jsonFeatures.addProperty("BungeeSpoof", BungeeCordSpoof.enabled);
        jsonFeatures.addProperty("AutoReconnectDelay", AutoReconnect.INSTANCE.getDelay());
        jsonObject.add("features", jsonFeatures);

        final JsonObject theAlteningObject = new JsonObject();
        theAlteningObject.addProperty("API-Key", GuiTheAltening.Companion.getApiKey());
        jsonObject.add("thealtening", theAlteningObject);

        final JsonObject backgroundObject = new JsonObject();
        backgroundObject.addProperty("Enabled", GuiBackground.Companion.getEnabled());
        backgroundObject.addProperty("Particles", GuiBackground.Companion.getParticles());
        jsonObject.add("Background", backgroundObject);

        LiquidBounce.moduleManager.getModules().stream().filter(module -> !module.getValues().isEmpty()).forEach(module -> {
            final JsonObject jsonModule = new JsonObject();
            module.getValues().forEach(value -> jsonModule.add(value.getName(), value.toJson()));
            jsonObject.add(module.getName(), jsonModule);
        });

        final PrintWriter printWriter = new PrintWriter(new FileWriter(getFile()));
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject));
        printWriter.close();
    }
}
