package eu.darkbot.kekawce;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtils {

    public static URL       KEKW_ICON_URL, CHECKMARK_URL, LOADING_URL, WARNING_URL;
    public static ImageIcon KEKW_ICON, CHECKMARK_IMG, LOADING_GIF, WARNING_IMG;

    private static String BASE64_NO_CONNECTION_STRING = "data:image/png;base64,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";
    public static ImageIcon NO_CONNECTION_IMG = new ImageIcon(javax.xml.bind.DatatypeConverter.parseBase64Binary(BASE64_NO_CONNECTION_STRING.split(",")[1]));

    static {
        try {
            KEKW_ICON_URL = new URL("https://gist.github.com/kekawce/0c7f520595aca02583a76dec53d3bb0b/raw/icon.png");
            CHECKMARK_URL = new URL("https://gist.github.com/kekawce/0c7f520595aca02583a76dec53d3bb0b/raw/checkmark.png");
            LOADING_URL   = new URL("https://gist.github.com/kekawce/0c7f520595aca02583a76dec53d3bb0b/raw/loading.gif");
            WARNING_URL   = new URL("https://gist.github.com/kekawce/0c7f520595aca02583a76dec53d3bb0b/raw/warning.png");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        KEKW_ICON     = getIcon(KEKW_ICON_URL);
        CHECKMARK_IMG = getIcon(CHECKMARK_URL);
        LOADING_GIF   = new ImageIcon(getBytes(LOADING_URL));
        WARNING_IMG   = getIcon(WARNING_URL);
    }

    public static ImageIcon getIcon(URL url) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(ImageIO.read(url));
        } catch (IOException e) {
            icon = new ImageIcon(url);
            e.printStackTrace();
        }
        return icon.getIconWidth() == -1
                ? null
                : icon;
    }

    public static ImageIcon getIcon(URL url, int width, int height) {
        ImageIcon icon = getIcon(url);
        return icon == null
                ? null
                : new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public static boolean areAllImagesLoaded() {
        return !(KEKW_ICON == null || CHECKMARK_IMG == null || LOADING_GIF == null || WARNING_IMG == null);
    }

    public static void setIconImage(JDialog jDialog) {
        setIconImage(jDialog, false);
    }

    public static void setIconImage(JDialog jDialog, boolean invokeLater) {
        if (KEKW_ICON == null) return;
        if (invokeLater)
            SwingUtilities.invokeLater(() -> jDialog.setIconImage(KEKW_ICON.getImage()));
        else
            jDialog.setIconImage(KEKW_ICON.getImage());
    }

    private static byte[] getBytes(URL url) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openConnection().getInputStream()) {
            int n;
            byte[] buffer = new byte[1024];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toByteArray();
    }
}
