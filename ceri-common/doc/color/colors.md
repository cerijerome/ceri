# Colors

Eye cone cell peak wavelength sensitivity:
- Short "S" = 420 nm – 440 nm (<blue)
- Middle "M" = 530 nm – 540 nm (green)
- Long "L" = 560 nm – 580 nm (>yellow)
- LMS color space
- Tristimulus values are 3 cone stimulus values, usually in CIEXYZ space
- Light sources may emit a spectrum of wavelength ranges
- Light source emissions are additive
- Color can conceptually be divided into brightness and chromaticity
- Infinitely many spectra may result in the same color (metamers / metamerism)

CIE(International Commission on Illumination)
- Standards for light, illumination, color, color spaces
- CIE 1931 2º field of view (Standard Observer)
- CIE 1964 10º field of view (Supplementary Standard Observer)
- Varied cone distribution means different tristimulis values by FOV

Color Model
- method to describe a color
- examples: RGB, CMYK, HSV(B), HSL, Pantone numbers
- maps to a reference or absolute color space (otherwise arbitrary)
- mapped subset of color space is a gamut
- mapped color model is its own color space

Color Spaces
- reproducible representation of a color across mediums
- CIELAB, CIEXYZ reference spaces, cover visible colors
- other examples: sRGB, Adobe RGB

CIE XYZ (Tristimulus) color space
- all color sensations for average eyesight
- standard reference for many other color spaces
- Y = luminance (brightness), XZ plane = chromaticities for that luminance
- Arbitrary units, but usually Y chosen as 1 or 100 for brightest white

CIE xyY color space
- CIE xyY color space is the XYZ plane of X + Y + Z = 1 and the same Y
- xy coords provide a chromaticity diagram
- Horseshoe curve consists of single wavelengths (monochromatic)
- Connecting lower line are non-spectral purples (not visible)
- White point is at (1/3, 1/3)
- Planckian Locus is the color temperature line in the xy color space

CIE LUV color space
- L*u*v* is based on color perception; lightness and uniform chromaticity
- Calculated relative to a reference point

Color Temperature
- Temperature of an ideal black-body radiator matching given color (in K)
- Correlated Color Temperature (CCT) = approx for non black-body spectrum lights
- <5000K (warm - yellow/red), >5000K (cold - blue)
- Match (1700K), horizon daylight (5000K), sun (5780K), vertical daylight (5500-6000K) 
- Mired M = 1,000,000 / T (in K), has more uniform changes
- White point for digital displays usually D65=6500K (sRGB is 6500K)

Standard Illuminants
- Theoretical source of visible light for comparing colors
- A, B, C, D50, D55, D65, D75, E (equal-energy), F1-12 (fluorescent)
- Daylight: D50 = 5000K, D55 = 5500K, D65 = 6500K, D75 = 7500K

sRGB color space
- Standard for digital displays, RGB images (8-bit per channel)
- D65 white point
- XYZ mapping needs gamma correction and matrix transform

ICC Color Profile
- Int'l Color Consortium; profile for a device
- Usually specifies map from CIE XYZ to its own color space (scan/display/print)

Sources
- <https://en.wikipedia.org/wiki/CIE_1931_color_space>
- <http://www.brucelindbloom.com/index.html?Eqn_T_to_xy.html>
- <http://www.vendian.org/mncharity/dir3/blackbody/UnstableURLs/bbr_color.html>
- <http://www.midnightkite.com/color.html>

sRGB-XYZ matrices
- Many variations on mapping XYZ<->RGB(linear)
- <http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html>  
  Calculated from (x, y) for red, green, blue, and ref white (X, Y, Z)  
  **RGB > XYZ**  
  +0.4124564 +0.3575761 +0.1804375  
  +0.2126729 +0.7151522 +0.0721750  
  +0.0193339 +0.1191920 +0.9503041  
  **XYZ > RGB**  
  +3.2404542 -1.5371385 -0.4985314  
  -0.9692660 +1.8760108 +0.0415560  
  +0.0556434 -0.2040259 +1.0572252  
- <https://www.w3.org/Graphics/Color/srgb>  
  <https://easyrgb.com/en/math.php> also seems to use this, but truncated  
  **XYZ > RGB**  
  +3.2406255 -1.5372080 -0.4986286  
  -0.9689307 +1.8757561 +0.0415175  
  +0.0557101 -0.2040211 +1.0569959  
- <https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation>  
  **XYZ > RGB**  
  +3.24096994 -1.53738318 -0.49861076  
  -0.96924364 +1.87596750 +0.04155506  
  +0.05563008 -0.20397696 +1.05697151  
