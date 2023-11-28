/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.font;

import com.google.gson.*;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.FontUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Fonts {
    // in fact these "roboto medium" is product sans lol
    @FontDetails(fontName = "Roboto Medium", fontSize = 35)
    public static GameFontRenderer font35;

    @FontDetails(fontName = "Roboto Medium", fontSize = 40)
    public static GameFontRenderer font40;

    @FontDetails(fontName = "Roboto Medium", fontSize = 72)
    public static GameFontRenderer font72;

    @FontDetails(fontName = "Roboto Medium", fontSize = 30)
    public static GameFontRenderer fontSmall;

    @FontDetails(fontName = "Roboto Medium", fontSize = 24)
    public static GameFontRenderer fontTiny;

    @FontDetails(fontName = "Roboto Medium", fontSize = 30)
    public static GameFontRenderer fontMedium;

    @FontDetails(fontName = "Roboto Medium", fontSize = 52)
    public static GameFontRenderer fontLarge;

    @FontDetails(fontName = "SFUI Regular", fontSize = 35)
    public static GameFontRenderer fontSFUI35;

    @FontDetails(fontName = "SFUI Regular", fontSize = 40)
    public static GameFontRenderer fontSFUI40;

    @FontDetails(fontName = "Roboto Bold", fontSize = 180)
    public static GameFontRenderer fontBold180;

    @FontDetails(fontName = "Tahoma Bold", fontSize = 35)
    public static GameFontRenderer fontTahoma;

    @FontDetails(fontName = "Tahoma Bold", fontSize = 30)
    public static GameFontRenderer fontTahoma30;

    public static TTFFontRenderer fontTahomaSmall;

    @FontDetails(fontName = "Bangers", fontSize = 45)
    public static GameFontRenderer fontBangers;

    @FontDetails(fontName = "Small Pixel", fontSize = 40)
    public static GameFontRenderer fontSmallPixel;

    @FontDetails(fontName = "Rubik", fontSize = 40)
    public static GameFontRenderer fontRubik;

    @FontDetails(fontName = "Tenacity", fontSize = 32)
    public static GameFontRenderer fontTenacity;

    @FontDetails(fontName = "Icon", fontSize = 32)
    public static GameFontRenderer fontIcon;


    @FontDetails(fontName = "Minecraft Font", fontSize = 30)
    public static final FontRenderer minecraftFont = Minecraft.getMinecraft().fontRendererObj;

    @FontDetails(fontName = "Astolfo", fontSize = 40)
    public static FontRenderer astolfoFont;

    @FontDetails(fontName = "Minecraft Native Font", fontSize = 40)
    public static FontRenderer minecraftNativeFont;


    private static final List<GameFontRenderer> CUSTOM_FONT_RENDERERS = new ArrayList<>();

    public static void loadFonts() {
        long l = System.currentTimeMillis();

        ClientUtils.logger.info("Loading Fonts.");

//        downloadFonts();

        font35 = new GameFontRenderer(getFontFromResource("Roboto-Medium.ttf", 35));
        font40 = new GameFontRenderer(getFontFromResource("Roboto-Medium.ttf", 40));
        font72 = new GameFontRenderer(getFontFromResource("Roboto-Medium.ttf", 72));
        fontSmall = new GameFontRenderer(getFontFromResource("Roboto-Medium.ttf", 30));
        fontTiny = new GameFontRenderer(getFontFromResource("Roboto-Medium.ttf", 24));
        fontMedium = new GameFontRenderer(getFontFromResource("Roboto-Medium.ttf", 30));
        fontLarge = new GameFontRenderer(getFontFromResource("Roboto-Medium.ttf", 60));
        fontSFUI35 = new GameFontRenderer(getFontFromResource("sfui.ttf", 35));
        fontSFUI40 = new GameFontRenderer(getFontFromResource("sfui.ttf", 40));
        fontBold180 = new GameFontRenderer(getFontFromResource("Roboto-Bold.ttf", 180));
        fontTahoma = new GameFontRenderer(getFontFromResource("TahomaBold.ttf", 35));
        fontTahoma30 = new GameFontRenderer(getFontFromResource("TahomaBold.ttf", 30));
        fontTahomaSmall = new TTFFontRenderer(getFontFromResource("Tahoma.ttf", 11));
        fontBangers = new GameFontRenderer(getFontFromResource("Bangers-Regular.ttf", 45));
        fontSmallPixel = new GameFontRenderer(getFontFromResource("SmallPixel.ttf", 40));
        fontRubik = new GameFontRenderer(getFontFromResource("rubik.ttf", 40));
        fontTenacity = new GameFontRenderer(getFontFromResource("tenacity.ttf", 32));
        fontIcon = new GameFontRenderer(getFontFromResource("icon.ttf", 32));
        astolfoFont = new FontRenderer(Minecraft.getMinecraft().gameSettings, new ResourceLocation("liquidbounce+/font/astolfo.png"), Minecraft.getMinecraft().renderEngine, false);
        minecraftNativeFont = new FontRenderer(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);
        Minecraft.getMinecraft().mcResourceManager.registerReloadListener(astolfoFont);
        Minecraft.getMinecraft().mcResourceManager.registerReloadListener(minecraftNativeFont);

        try {
            CUSTOM_FONT_RENDERERS.clear();

            final File fontsFile = new File(LiquidBounce.fileManager.fontsDir, "fonts.json");

            if (fontsFile.exists()) {
                final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(fontsFile)));

                if (jsonElement instanceof JsonNull)
                    return;

                final JsonArray jsonArray = (JsonArray) jsonElement;

                for (final JsonElement element : jsonArray) {
                    if (element instanceof JsonNull)
                        return;

                    final JsonObject fontObject = (JsonObject) element;

                    CUSTOM_FONT_RENDERERS.add(new GameFontRenderer(getFontFromFile(fontObject.get("fontFile").getAsString(), fontObject.get("fontSize").getAsInt())));
                }
            } else {
                fontsFile.createNewFile();

                final PrintWriter printWriter = new PrintWriter(new FileWriter(fontsFile));
                printWriter.println(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonArray()));
                printWriter.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        FontUtils.INSTANCE.updateCache();
        ClientUtils.logger.info("Loaded Fonts. (" + (System.currentTimeMillis() - l) + "ms)");
    }

//    private static void downloadFonts() {
//        try {
//            final File fontsArchive = new File(LiquidBounce.fileManager.fontsDir, "roboto.zip");
//            final File mntsbFile = new File(LiquidBounce.fileManager.fontsDir, "mntsb.ttf");
//            final File sfuiFile = new File(LiquidBounce.fileManager.fontsDir, "sfui.ttf");
//            final File prodSansFile = new File(LiquidBounce.fileManager.fontsDir, "Roboto-Medium.ttf");
//            final File prodBoldFile = new File(LiquidBounce.fileManager.fontsDir, "Roboto-Bold.ttf");
//            final File tahomaFile = new File(LiquidBounce.fileManager.fontsDir, "TahomaBold.ttf");
//            final File tahomaReFile = new File(LiquidBounce.fileManager.fontsDir, "Tahoma.ttf");
//            final File bangersFile = new File(LiquidBounce.fileManager.fontsDir, "Bangers-Regular.ttf");
//
//            if (!fontsArchive.exists() || !sfuiFile.exists() || !prodSansFile.exists() || !prodBoldFile.exists() || !tahomaFile.exists() || !tahomaReFile.exists() || !bangersFile.exists() || !mntsbFile.exists()) {
//                ClientUtils.logger.info("Downloading fonts...");
//                HttpUtils.download(String.format("%s/fonts/fonts.zip", LiquidBounce.CLIENT_CLOUD), fontsArchive);
//                ClientUtils.logger.info("Extract fonts...");
//                extractZip(fontsArchive.getPath(), LiquidBounce.fileManager.fontsDir.getPath());
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static FontRenderer getFontRenderer(final String name, final int size) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                final Object o = field.get(null);

                if (o instanceof FontRenderer) {
                    final FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    if (fontDetails.fontName().equals(name) && fontDetails.fontSize() == size)
                        return (FontRenderer) o;
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (final GameFontRenderer liquidFontRenderer : CUSTOM_FONT_RENDERERS) {
            final Font font = liquidFontRenderer.getDefaultFont().getFont();

            if (font.getName().equals(name) && font.getSize() == size)
                return liquidFontRenderer;
        }

        return minecraftFont;
    }

    public static Object[] getFontDetails(final FontRenderer fontRenderer) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                final Object o = field.get(null);

                if (o.equals(fontRenderer)) {
                    final FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    return new Object[] {fontDetails.fontName(), fontDetails.fontSize()};
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (fontRenderer instanceof GameFontRenderer) {
            final Font font = ((GameFontRenderer) fontRenderer).getDefaultFont().getFont();

            return new Object[] {font.getName(), font.getSize()};
        }

        return null;
    }

    public static List<FontRenderer> getFonts() {
        final List<FontRenderer> fonts = new ArrayList<>();

        for (final Field fontField : Fonts.class.getDeclaredFields()) {
            try {
                fontField.setAccessible(true);

                final Object fontObj = fontField.get(null);

                if (fontObj instanceof FontRenderer) fonts.add((FontRenderer) fontObj);
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        fonts.addAll(Fonts.CUSTOM_FONT_RENDERERS);

        return fonts;
    }

    private static Font getFontFromFile(final String fontName, final int size) {
        try {
            final InputStream inputStream = new FileInputStream(new File(LiquidBounce.fileManager.fontsDir, fontName));
            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();

            return new Font("default", Font.PLAIN, size);
        }
    }

    private static Font getFontFromResource(final String fontName, final int size) {
        try {
            String fontPath = String.format("assets/minecraft/liquidbounce+/font/%s", fontName);
            InputStream inputStream = Fonts.class.getClassLoader().getResourceAsStream(fontPath);
            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;

        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("default", Font.PLAIN, size);
        }
    }

    private static void extractZip(final String zipFile, final String outputFolder) {
        final byte[] buffer = new byte[1048576];

        try {
            final File folder = new File(outputFolder);

            if(!folder.exists()) folder.mkdir();

            final ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(outputFolder + File.separator + zipEntry.getName());
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                int i;
                while ((i = zipInputStream.read(buffer)) > 0)
                    fileOutputStream.write(buffer, 0, i);

                fileOutputStream.close();
                zipEntry = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
