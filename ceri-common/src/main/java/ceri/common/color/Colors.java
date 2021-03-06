package ceri.common.color;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import ceri.common.util.BasicUtil;

/**
 * Opaque color presets.
 */
public enum Colors {

	// java awt colors

	/** <div style="border:1px solid;width:40px;height:20px;background-color:#000000;"/> */
	awtBlack(Color.black.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#0000ff;"/> */
	awtBlue(Color.blue.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00ffff;"/> */
	awtCyan(Color.cyan.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#404040;"/> */
	awtDarkGray(Color.darkGray.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#808080;"/> */
	awtGray(Color.gray.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00ff00;"/> */
	awtGreen(Color.green.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#c0c0c0;"/> */
	awtLightGray(Color.lightGray.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff00ff;"/> */
	awtMagenta(Color.magenta.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffc800;"/> */
	awtOrange(Color.orange.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffafaf;"/> */
	awtPink(Color.pink.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff0000;"/> */
	awtRed(Color.red.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffffff;"/> */
	awtWhite(Color.white.getRGB()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffff00;"/> */
	awtYellow(Color.yellow.getRGB()),

	// CIE illuminants: https://en.wikipedia.org/wiki/Standard_illuminant#Illuminant_series_D

	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffcdd;"/> */
	cieD50(XyzColor.CIE_D50.argb()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffeea;"/> */
	cieD55(XyzColor.CIE_D55.argb()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffffff;"/> */
	cieD65(XyzColor.CIE_D65.argb()),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f7ffff;"/> */
	cieD75(XyzColor.CIE_D75.argb()),

	// Color temperatures: includes ANSI C78.377 bins (2700K-6500K)

	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffc100;"/> */
	white1700K(ColorSpaces.cctToRgb(1700)), // amber
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffcd00;"/> */
	white1900K(ColorSpaces.cctToRgb(1900)),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffda49;"/> */
	white2200K(ColorSpaces.cctToRgb(2200)), // ultra-warm
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffe369;"/> */
	white2500K(ColorSpaces.cctToRgb(2500)),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffe87a;"/> */
	white2700K(ColorSpaces.cctToRgb(2700)), // soft
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffed8e;"/> */
	white3000K(ColorSpaces.cctToRgb(3000)),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fff3ab;"/> */
	white3500K(ColorSpaces.cctToRgb(3500)), // warm
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fff6c1;"/> */
	white4000K(ColorSpaces.cctToRgb(4000)),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fff9d3;"/> */
	white4500K(ColorSpaces.cctToRgb(4500)), // neutral
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffbe2;"/> */
	white5000K(ColorSpaces.cctToRgb(5000)),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffcf3;"/> */
	white5700K(ColorSpaces.cctToRgb(5700)), // cool
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffdff;"/> */
	white6500K(ColorSpaces.cctToRgb(6500)), // daylight
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fcfeff;"/> */
	white7500K(ColorSpaces.cctToRgb(7500)),

	// LED custom colors

	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffc100;"/> */
	amber(0xffc100), // 1700K
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fff3ab;"/> */
	warmWhite(0xfff3ab), // 3500K

	// X11 colors: https://en.wikipedia.org/wiki/Web_colors#X11_color_names

	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f0f8ff;"/> */
	aliceBlue(0xf0f8ff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#faebd7;"/> */
	antiqueWhite(0xfaebd7),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00ffff;"/> */
	aqua(0x00ffff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#7fffd4;"/> */
	aquamarine(0x7fffd4),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f0ffff;"/> */
	azure(0xf0ffff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f5f5dc;"/> */
	beige(0xf5f5dc),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffe4c4;"/> */
	bisque(0xffe4c4),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#000000;"/> */
	black(0x000000),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffebcd;"/> */
	blanchedAlmond(0xffebcd),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#0000ff;"/> */
	blue(0x0000ff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#8a2be2;"/> */
	blueViolet(0x8a2be2),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#a52a2a;"/> */
	brown(0xa52a2a),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#deb887;"/> */
	burlyWood(0xdeb887),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#5f9ea0;"/> */
	cadetBlue(0x5f9ea0),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#7fff00;"/> */
	chartreuse(0x7fff00),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#d2691e;"/> */
	chocolate(0xd2691e),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff7f50;"/> */
	coral(0xff7f50),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#6495ed;"/> */
	cornflowerBlue(0x6495ed),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fff8dc;"/> */
	cornsilk(0xfff8dc),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#dc143c;"/> */
	crimson(0xdc143c),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00ffff;"/> */
	cyan(0x00ffff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00008b;"/> */
	darkBlue(0x00008b),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#008b8b;"/> */
	darkCyan(0x008b8b),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#b8860b;"/> */
	darkGoldenRod(0xb8860b),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#a9a9a9;"/> */
	darkGray(0xa9a9a9),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#006400;"/> */
	darkGreen(0x006400),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#bdb76b;"/> */
	darkKhaki(0xbdb76b),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#8b008b;"/> */
	darkMagenta(0x8b008b),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#556b2f;"/> */
	darkOliveGreen(0x556b2f),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff8c00;"/> */
	darkOrange(0xff8c00),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#9932cc;"/> */
	darkOrchid(0x9932cc),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#8b0000;"/> */
	darkRed(0x8b0000),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#e9967a;"/> */
	darkSalmon(0xe9967a),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#8fbc8f;"/> */
	darkSeaGreen(0x8fbc8f),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#483d8b;"/> */
	darkSlateBlue(0x483d8b),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#2f4f4f;"/> */
	darkSlateGray(0x2f4f4f),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00ced1;"/> */
	darkTurquoise(0x00ced1),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#9400d3;"/> */
	darkViolet(0x9400d3),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff1493;"/> */
	deepPink(0xff1493),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00bfff;"/> */
	deepSkyBlue(0x00bfff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#696969;"/> */
	dimGray(0x696969),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#1e90ff;"/> */
	dodgerBlue(0x1e90ff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#b22222;"/> */
	fireBrick(0xb22222),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffaf0;"/> */
	floralWhite(0xfffaf0),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#228b22;"/> */
	forestGreen(0x228b22),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff00ff;"/> */
	fuchsia(0xff00ff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#dcdcdc;"/> */
	gainsboro(0xdcdcdc),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f8f8ff;"/> */
	ghostWhite(0xf8f8ff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffd700;"/> */
	gold(0xffd700),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#daa520;"/> */
	goldenRod(0xdaa520),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#808080;"/> */
	gray(0x808080),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#008000;"/> */
	green(0x008000),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#adff2f;"/> */
	greenYellow(0xadff2f),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f0fff0;"/> */
	honeyDew(0xf0fff0),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff69b4;"/> */
	hotPink(0xff69b4),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#cd5c5c;"/> */
	indianRed(0xcd5c5c),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#4b0082;"/> */
	indigo(0x4b0082),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffff0;"/> */
	ivory(0xfffff0),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f0e68c;"/> */
	khaki(0xf0e68c),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#e6e6fa;"/> */
	lavender(0xe6e6fa),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fff0f5;"/> */
	lavenderBlush(0xfff0f5),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#7cfc00;"/> */
	lawnGreen(0x7cfc00),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffacd;"/> */
	lemonChiffon(0xfffacd),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#add8e6;"/> */
	lightBlue(0xadd8e6),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f08080;"/> */
	lightCoral(0xf08080),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#e0ffff;"/> */
	lightCyan(0xe0ffff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fafad2;"/> */
	lightGoldenRodYellow(0xfafad2),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#d3d3d3;"/> */
	lightGray(0xd3d3d3),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#90ee90;"/> */
	lightGreen(0x90ee90),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffb6c1;"/> */
	lightPink(0xffb6c1),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffa07a;"/> */
	lightSalmon(0xffa07a),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#20b2aa;"/> */
	lightSeaGreen(0x20b2aa),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#87cefa;"/> */
	lightSkyBlue(0x87cefa),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#778899;"/> */
	lightSlateGray(0x778899),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#b0c4de;"/> */
	lightSteelBlue(0xb0c4de),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffffe0;"/> */
	lightYellow(0xffffe0),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00ff00;"/> */
	lime(0x00ff00),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#32cd32;"/> */
	limeGreen(0x32cd32),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#faf0e6;"/> */
	linen(0xfaf0e6),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff00ff;"/> */
	magenta(0xff00ff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#800000;"/> */
	maroon(0x800000),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#66cdaa;"/> */
	mediumAquaMarine(0x66cdaa),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#0000cd;"/> */
	mediumBlue(0x0000cd),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ba55d3;"/> */
	mediumOrchid(0xba55d3),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#9370db;"/> */
	mediumPurple(0x9370db),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#3cb371;"/> */
	mediumSeaGreen(0x3cb371),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#7b68ee;"/> */
	mediumSlateBlue(0x7b68ee),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00fa9a;"/> */
	mediumSpringGreen(0x00fa9a),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#48d1cc;"/> */
	mediumTurquoise(0x48d1cc),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#c71585;"/> */
	mediumVioletRed(0xc71585),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#191970;"/> */
	midnightBlue(0x191970),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f5fffa;"/> */
	mintCream(0xf5fffa),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffe4e1;"/> */
	mistyRose(0xffe4e1),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffe4b5;"/> */
	moccasin(0xffe4b5),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffdead;"/> */
	navajoWhite(0xffdead),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#000080;"/> */
	navy(0x000080),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fdf5e6;"/> */
	oldLace(0xfdf5e6),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#808000;"/> */
	olive(0x808000),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#6b8e23;"/> */
	oliveDrab(0x6b8e23),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffa500;"/> */
	orange(0xffa500),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff4500;"/> */
	orangeRed(0xff4500),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#da70d6;"/> */
	orchid(0xda70d6),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#eee8aa;"/> */
	paleGoldenRod(0xeee8aa),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#98fb98;"/> */
	paleGreen(0x98fb98),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#afeeee;"/> */
	paleTurquoise(0xafeeee),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#db7093;"/> */
	paleVioletRed(0xdb7093),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffefd5;"/> */
	papayaWhip(0xffefd5),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffdab9;"/> */
	peachPuff(0xffdab9),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#cd853f;"/> */
	peru(0xcd853f),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffc0cb;"/> */
	pink(0xffc0cb),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#dda0dd;"/> */
	plum(0xdda0dd),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#b0e0e6;"/> */
	powderBlue(0xb0e0e6),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#800080;"/> */
	purple(0x800080),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#663399;"/> */
	rebeccaPurple(0x663399),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff0000;"/> */
	red(0xff0000),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#bc8f8f;"/> */
	rosyBrown(0xbc8f8f),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#4169e1;"/> */
	royalBlue(0x4169e1),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#8b4513;"/> */
	saddleBrown(0x8b4513),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fa8072;"/> */
	salmon(0xfa8072),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f4a460;"/> */
	sandyBrown(0xf4a460),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#2e8b57;"/> */
	seaGreen(0x2e8b57),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fff5ee;"/> */
	seaShell(0xfff5ee),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#a0522d;"/> */
	sienna(0xa0522d),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#c0c0c0;"/> */
	silver(0xc0c0c0),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#87ceeb;"/> */
	skyBlue(0x87ceeb),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#6a5acd;"/> */
	slateBlue(0x6a5acd),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#708090;"/> */
	slateGray(0x708090),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#fffafa;"/> */
	snow(0xfffafa),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#00ff7f;"/> */
	springGreen(0x00ff7f),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#4682b4;"/> */
	steelBlue(0x4682b4),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#d2b48c;"/> */
	tan(0xd2b48c),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#008080;"/> */
	teal(0x008080),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#d8bfd8;"/> */
	thistle(0xd8bfd8),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ff6347;"/> */
	tomato(0xff6347),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#40e0d0;"/> */
	turquoise(0x40e0d0),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ee82ee;"/> */
	violet(0xee82ee),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f5deb3;"/> */
	wheat(0xf5deb3),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffffff;"/> */
	white(0xffffff),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#f5f5f5;"/> */
	whiteSmoke(0xf5f5f5),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#ffff00;"/> */
	yellow(0xffff00),
	/** <div style="border:1px solid;width:40px;height:20px;background-color:#9acd32;"/> */
	yellowGreen(0x9acd32);

	private static final Map<Integer, Colors> lookup = lookup();
	public final int argb;

	/**
	 * Lookup argb int by name. Returns null if not found.
	 */
	public static Integer argb(String name) {
		Colors c = from(name);
		return c == null ? null : c.argb;
	}

	/**
	 * Lookup color by name. Returns null if not found.
	 */
	public static Color color(String name) {
		Colors c = from(name);
		return c == null ? null : c.color();
	}

	/**
	 * Lookup name by rgb int. Returns null if not found.
	 */
	public static String name(int rgb) {
		Colors c = from(rgb);
		return c == null ? null : c.name();
	}
	
	/**
	 * Lookup name by color. Returns null if not found.
	 */
	public static String name(Color color) {
		return name(color.getRGB());
	}
	
	/**
	 * Lookup by name.
	 */
	public static Colors from(String name) {
		return BasicUtil.valueOf(Colors.class, name, null);
	}

	/**
	 * Lookup by color, ignoring alpha.
	 */
	public static Colors from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Lookup by rgb, ignoring alpha.
	 */
	public static Colors from(int rgb) {
		return lookup.get(ColorUtil.argb(rgb));
	}

	/**
	 * Provide a random entry.
	 */
	public static Colors random() {
		Colors[] values = Colors.values();
		return values[ThreadLocalRandom.current().nextInt(values.length)];
	}

	Colors(int rgb) {
		argb = ColorUtil.argb(rgb);
	}

	/**
	 * Calculate lightness of the color, by converting sRGB to CIELUV L* with D65 illuminant.
	 */
	public double lightness() {
		return LuvColor.Ref.CIE_D65.l(argb);
	}

	/**
	 * Provide
	 */
	public Color color() {
		return ColorUtil.color(argb);
	}

	private static Map<Integer, Colors> lookup() {
		Map<Integer, Colors> map = new HashMap<>();
		for (Colors c : Colors.values())
			map.put(c.argb, c);
		return Collections.unmodifiableMap(map);
	}
}
