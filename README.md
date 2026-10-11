# Úloha 1: Rasterizace úsečky a n-úhelníku

Aplikace v Javě (Swing) pro předmět PGRF1. Uživatel myší kreslí úsečky a n-úhelník pomocí pružné čáry. Pixely počítá vlastní triviální algoritmus úsečky.

## Spuštění

V IntelliJ IDEA otevři projekt a spusť třídu `Main`.

Z příkazové řádky (Java 14+):

```bash
javac -d out $(find src -name "*.java")
java -cp out Main
```

## Ovládání

| vstup | akce |
|---|---|
| levé tlačítko | podle režimu kreslí úsečku, nebo přidává vrchol polygonu |
| tažení levým | pružná čára: úsečka se natahuje za myší, nový vrchol polygonu se posouvá |
| pravé tlačítko + tažení | v nástroji **EDIT** a režimu polygonu chytí nejbližší vrchol (do 10 px) a přesune ho |
| pravé tlačítko | v nástroji **DELETE** smaže nejbližší vrchol |
| **M** | přepne režim kreslení: polygon ↔ úsečky |
| **N** | přepne nástroj pravého tlačítka: EDIT ↔ DELETE |
| **C** | smaže plátno, polygon i všechny úsečky |

Po spuštění je zapnutý režim polygonu a nástroj EDIT.

## Struktura

```
src/
├── Main.java                    vstupní bod, vytvoří okno a controller
├── controller/Controller2D      obsluha myši a kláves, překreslení scény
├── Context/                     Color (barva kreslení), DrawMode, ToolMode
├── model/                       Point2D, Line, Polygon (vrcholy, hledání nejbližšího)
├── raster/                      rozhraní Raster a jeho implementace nad BufferedImage
├── rasterize/                   LineRasterizer (předek), LineRasterizerTrivial, PolygonRasterizer
└── view/                        Window a Panel, který zobrazuje raster
```

## Algoritmus úsečky

`LineRasterizerTrivial` vychází z rovnice přímky `y = k·x + q`:

- `k = (y2 − y1) / (x2 − x1)` je směrnice, `q = y1 − k·x1` je posun.
- Pro `|k| ≤ 1` cyklus prochází osu x a pro každé x dopočítá zaokrouhlené y.
- Pro `|k| > 1` (strmá úsečka) prochází osu y a dopočítá `x = (y − q) / k`. Jinak by v úsečce vznikaly mezery.
- Svislou úsečku (`x1 == x2`) kreslí zvlášť, protože u ní by se dělilo nulou.

Polygon se kreslí jako jednotlivé úsečky mezi sousedními vrcholy. Poslední vrchol se spojí s prvním, takže je obrazec vždy uzavřený.

Pixely mimo plátno `RasterBufferedImage` přeskočí, takže tažení mimo okno nic neshodí.

---

*Tento README napsal Claude (AI asistent od Anthropicu).*
