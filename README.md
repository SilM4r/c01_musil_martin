# Úloha 1: Rasterizace, vyplňování a ořezávání

Aplikace v Javě (Swing) pro předmět PGRF1. Uživatel myší kreslí úsečky a n-úhelník pomocí pružné čáry. Pixely počítá vlastní triviální algoritmus úsečky. Polygon jde vyplnit (SeedFill, ScanLine) a ořezat jiným polygonem (Sutherland-Hodgman), úsečky jdou ořezat obdélníkem (Cohen-Sutherland).

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
| levé tlačítko | podle režimu kreslí úsečku, přidává vrchol polygonu, nebo vrchol ořezávacího polygonu |
| tažení levým | pružná čára: úsečka se natahuje za myší, nový vrchol polygonu se posouvá |
| pravé tlačítko + tažení | v nástroji **EDIT** chytí nejbližší vrchol polygonu nebo ořezávače (do 10 px) a přesune ho |
| pravé tlačítko | v nástroji **DELETE** smaže nejbližší vrchol polygonu nebo ořezávače |
| kliknutí na plátno | v nástroji **SeedFill** vyplní oblast od místa kliknutí, v nástroji **ScanLine** vyplní polygon |
| **M** | přepne režim kreslení: polygon → ořezávač → úsečky → polygon |
| **N** | přepne nástroj: EDIT → DELETE → SeedFill → ScanLine → EDIT |
| **Shift** | zapne/vypne zarovnání na 0°, 45° a 90° (stačí i držet Shift při kreslení) |
| **V** | ořízne polygon ořezávacím polygonem (Sutherland-Hodgman) |
| **B** | ořízne všechny úsečky obdélníkovým oknem (Cohen-Sutherland) |
| **C** | smaže plátno, polygon, ořezávač i všechny úsečky |

Po spuštění je zapnutý režim polygonu a nástroj EDIT. Vše jde ovládat i z bočního panelu, kde se navíc vybírají barvy obrysu, výplně a ořezávače.

## Struktura

```
src/
├── Main.java                    vstupní bod, vytvoří okno a controller
├── controller/Controller2D      obsluha myši a kláves, překreslení scény
├── Context/                     Color (barvy kreslení, výplně a ořezávače), DrawMode, ToolMode
├── model/                       Point2D, Line, Edge, Polygon (vrcholy, hledání nejbližšího, orientace, obálka)
├── Clipper/                     rozhraní Clipper, SutherlandHodgman (polygon), CohenSutherland (úsečky)
├── Filler/                      rozhraní Filler, SeedFill, ScanLineFill
├── raster/                      rozhraní Raster a jeho implementace nad BufferedImage
├── rasterize/                   LineRasterizer (předek), LineRasterizerTrivial, PolygonRasterizer
└── view/                        Window, Panel (zobrazuje raster) a ControlPanel (boční UI)
```

## Algoritmus úsečky

`LineRasterizerTrivial` vychází z rovnice přímky `y = k·x + q`:

- `k = (y2 − y1) / (x2 − x1)` je směrnice, `q = y1 − k·x1` je posun.
- Pro `|k| ≤ 1` cyklus prochází osu x a pro každé x dopočítá zaokrouhlené y.
- Pro `|k| > 1` (strmá úsečka) prochází osu y a dopočítá `x = (y − q) / k`. Jinak by v úsečce vznikaly mezery.
- Svislou úsečku (`x1 == x2`) kreslí zvlášť, protože u ní by se dělilo nulou.

Polygon se kreslí jako jednotlivé úsečky mezi sousedními vrcholy. Poslední vrchol se spojí s prvním, takže je obrazec vždy uzavřený.

Pixely mimo plátno `RasterBufferedImage` přeskočí, takže tažení mimo okno nic neshodí.

## Vyplňování

- **SeedFill** – 4-směrové záplavové vyplňování od místa kliknutí. Používá vlastní zásobník (`ArrayDeque`) místo rekurze, takže velké plochy neshodí `StackOverflowError`.
- **ScanLine** – vyplní polygon řádek po řádku: najde průsečíky řádku s orientovanými hranami, seřadí je a vyplní úseky mezi lichým a sudým průsečíkem. Polygon musí mít aspoň 3 vrcholy.

## Ořezávání

Ořezávač se kreslí v režimu **Polygon – Ořezávač** stejně jako běžný polygon, jen jinou barvou.

### Polygon – Sutherland-Hodgman (klávesa V)

Ořízne kreslený polygon konvexním ořezávacím polygonem. Ořezávač se nejdřív zorientuje proti směru hodinových ručiček, pak se polygon postupně ořízne každou hranou ořezávače. Zda vrchol leží uvnitř hrany, se určí skalárním součinem s normálou hrany. Výsledek nahradí původní polygon.

### Úsečky – Cohen-Sutherland (klávesa B)

Ořízne všechny úsečky obdélníkovým oknem. Ořezávač proto musí mít **přesně 4 vrcholy** a hrany **rovnoběžné s osami** (tolerance 2 px), jinak aplikace zobrazí varování. Nejsnáz se nakreslí se zapnutým Shiftem.

- Každý konec úsečky dostane 4bitový kód polohy (vlevo, vpravo, nad, pod oknem).
- Oba kódy nulové → úsečka leží celá uvnitř a zůstane beze změny.
- Kódy mají společný bit → úsečka leží celá na jedné vnější straně a zahodí se.
- Jinak se vnější konec nahradí průsečíkem s příslušnou hranou okna a test se opakuje.

Ořez je trvalý: ořezané úsečky nahradí původní a úsečky mimo okno zmizí.

---

*Tento README napsal Claude (AI asistent od Anthropicu).*
