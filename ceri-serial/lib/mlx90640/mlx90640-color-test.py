import time
import board
import busio
import adafruit_mlx90640

def ansi8BitColor(temp):
    minTemp, maxTemp = 18.0, 38.0
    ratio = 2 * (temp-minTemp) / (maxTemp - minTemp)
    # convert temp as hue to 8-bit rgb, each component 0-5
    b = int(max(0, 5*(1 - ratio)))
    r = int(max(0, 5*(ratio - 1)))
    g = 5 - b - r
    # get ansi escape 8-bit color code
    return 16 + (r * 36) + (g * 6) + b
 
i2c = busio.I2C(board.SCL, board.SDA, frequency=800000)

mlx = adafruit_mlx90640.MLX90640(i2c)
print("MLX addr detected on I2C", [hex(i) for i in mlx.serial_number])

mlx.refresh_rate = adafruit_mlx90640.RefreshRate.REFRESH_2_HZ

frame = [0] * 768
printed = False
while True:
    try:
        mlx.getFrame(frame)
    except ValueError:
        # these happen, no biggie - retry
        continue

    # move cursor back if printed already
    if printed:
        print("\x1b[24A", end="")

    for h in range(24):
        print("  ", end="")
        for w in range(32):
            t = frame[h*32 + w]
            color = ansi8BitColor(t)
            # print 2 spaces with background color
            print("\x1b[48;5;%dm  " % color, end="")
        # reset background for new line
        print("\x1b[m")
    printed = True
    
