package ceri.common.color;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ceri.common.util.BasicUtil;

/**
 * X11 color names. https://en.wikipedia.org/wiki/Web_colors#X11_color_names
 */
public enum X11Color {
	aliceBlue(0xf0f8ff),
	antiqueWhite(0xfaebd7),
	aqua(0x00ffff),
	aquamarine(0x7fffd4),
	azure(0xf0ffff),
	beige(0xf5f5dc),
	bisque(0xffe4c4),
	black(0x000000),
	blanchedAlmond(0xffebcd),
	blue(0x0000ff),
	blueViolet(0x8a2be2),
	brown(0xa52a2a),
	burlyWood(0xdeb887),
	cadetBlue(0x5f9ea0),
	chartreuse(0x7fff00),
	chocolate(0xd2691e),
	coral(0xff7f50),
	cornflowerBlue(0x6495ed),
	cornsilk(0xfff8dc),
	crimson(0xdc143c),
	cyan(0x00ffff),
	darkBlue(0x00008b),
	darkCyan(0x008b8b),
	darkGoldenRod(0xb8860b),
	darkGray(0xa9a9a9),
	darkGreen(0x006400),
	darkKhaki(0xbdb76b),
	darkMagenta(0x8b008b),
	darkOliveGreen(0x556b2f),
	darkOrange(0xff8c00),
	darkOrchid(0x9932cc),
	darkRed(0x8b0000),
	darkSalmon(0xe9967a),
	darkSeaGreen(0x8fbc8f),
	darkSlateBlue(0x483d8b),
	darkSlateGray(0x2f4f4f),
	darkTurquoise(0x00ced1),
	darkViolet(0x9400d3),
	deepPink(0xff1493),
	deepSkyBlue(0x00bfff),
	dimGray(0x696969),
	dodgerBlue(0x1e90ff),
	fireBrick(0xb22222),
	floralWhite(0xfffaf0),
	forestGreen(0x228b22),
	fuchsia(0xff00ff),
	gainsboro(0xdcdcdc),
	ghostWhite(0xf8f8ff),
	gold(0xffd700),
	goldenRod(0xdaa520),
	gray(0x808080),
	green(0x008000),
	greenYellow(0xadff2f),
	honeyDew(0xf0fff0),
	hotPink(0xff69b4),
	indianRed(0xcd5c5c),
	indigo(0x4b0082),
	ivory(0xfffff0),
	khaki(0xf0e68c),
	lavender(0xe6e6fa),
	lavenderBlush(0xfff0f5),
	lawnGreen(0x7cfc00),
	lemonChiffon(0xfffacd),
	lightBlue(0xadd8e6),
	lightCoral(0xf08080),
	lightCyan(0xe0ffff),
	lightGoldenRodYellow(0xfafad2),
	lightGray(0xd3d3d3),
	lightGreen(0x90ee90),
	lightPink(0xffb6c1),
	lightSalmon(0xffa07a),
	lightSeaGreen(0x20b2aa),
	lightSkyBlue(0x87cefa),
	lightSlateGray(0x778899),
	lightSteelBlue(0xb0c4de),
	lightYellow(0xffffe0),
	lime(0x00ff00),
	limeGreen(0x32cd32),
	linen(0xfaf0e6),
	magenta(0xff00ff),
	maroon(0x800000),
	mediumAquaMarine(0x66cdaa),
	mediumBlue(0x0000cd),
	mediumOrchid(0xba55d3),
	mediumPurple(0x9370db),
	mediumSeaGreen(0x3cb371),
	mediumSlateBlue(0x7b68ee),
	mediumSpringGreen(0x00fa9a),
	mediumTurquoise(0x48d1cc),
	mediumVioletRed(0xc71585),
	midnightBlue(0x191970),
	mintCream(0xf5fffa),
	mistyRose(0xffe4e1),
	moccasin(0xffe4b5),
	navajoWhite(0xffdead),
	navy(0x000080),
	oldLace(0xfdf5e6),
	olive(0x808000),
	oliveDrab(0x6b8e23),
	orange(0xffa500),
	orangeRed(0xff4500),
	orchid(0xda70d6),
	paleGoldenRod(0xeee8aa),
	paleGreen(0x98fb98),
	paleTurquoise(0xafeeee),
	paleVioletRed(0xdb7093),
	papayaWhip(0xffefd5),
	peachPuff(0xffdab9),
	peru(0xcd853f),
	pink(0xffc0cb),
	plum(0xdda0dd),
	powderBlue(0xb0e0e6),
	purple(0x800080),
	rebeccaPurple(0x663399),
	red(0xff0000),
	rosyBrown(0xbc8f8f),
	royalBlue(0x4169e1),
	saddleBrown(0x8b4513),
	salmon(0xfa8072),
	sandyBrown(0xf4a460),
	seaGreen(0x2e8b57),
	seaShell(0xfff5ee),
	sienna(0xa0522d),
	silver(0xc0c0c0),
	skyBlue(0x87ceeb),
	slateBlue(0x6a5acd),
	slateGray(0x708090),
	snow(0xfffafa),
	springGreen(0x00ff7f),
	steelBlue(0x4682b4),
	tan(0xd2b48c),
	teal(0x008080),
	thistle(0xd8bfd8),
	tomato(0xff6347),
	turquoise(0x40e0d0),
	violet(0xee82ee),
	wheat(0xf5deb3),
	white(0xffffff),
	whiteSmoke(0xf5f5f5),
	yellow(0xffff00),
	yellowGreen(0x9acd32);

	private static final Map<Integer, X11Color> lookup = createLookup();
	public final Color color;

	private X11Color(int rgb) {
		color = new Color(rgb);
	}

	private static Map<Integer, X11Color> createLookup() {
		Map<Integer, X11Color> map = new HashMap<>();
		for (X11Color c : X11Color.values())
			map.put(c.color.getRGB() & 0xffffff, c);
		return Collections.unmodifiableMap(map);
	}

	public static X11Color from(String name) {
		return BasicUtil.valueOf(X11Color.class, name, null);
	}
	
	public static X11Color from(Color color) {
		return from(color.getRGB());
	}

	public static X11Color from(int rgb) {
		return lookup.get(rgb & 0xffffff);
	}

}