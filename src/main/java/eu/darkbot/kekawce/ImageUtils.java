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
    public static ImageIcon NO_CONNECTION_IMG = getIcon(ImageUtils.class.getResource("/no-internet-icon.png"));

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
        LOADING_GIF = new ImageIcon(getBytes(LOADING_URL));
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
