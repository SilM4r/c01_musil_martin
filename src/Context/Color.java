package Context;

/**
 * Globální barvy aplikace.
 * Hodnoty jsou statické a přepisují se při výběru barvy v UI (Color Picker).
 */
public class Color {
    // Barva pro kreslení úseček a obrysu polygonu
    public static int COLOR = 0xffff00;

    // Barva pro vyplňování (např. Scan-Line, Seed-Fill)
    public static int FILL_COLOR = 0x00ff00;

    // Skutečná barva pozadí plátna (černá)
    public static int BACKGROUND_COLOR = 0x000000;
}
