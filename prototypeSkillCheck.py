import RPi.GPIO as GPIO
import time
import random

LED_PINS = [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]
BUTTON_PINS = [22, 25, 24, 23]  # your GPIOs
TARGET_NODES = [0, 4, 8, 12]

GPIO.setmode(GPIO.BCM)
for pin in LED_PINS:
    GPIO.setup(pin, GPIO.OUT)
    GPIO.output(pin, GPIO.LOW)

for pin in BUTTON_PINS:
    GPIO.setup(pin, GPIO.IN, pull_up_down=GPIO.PUD_UP)

def pick_target():
    return random.randint(0, 3)

def check_button():
    for i, pin in enumerate(BUTTON_PINS):
        if GPIO.input(pin) == GPIO.LOW:
            return i
    return None

def wait_for_button_release():
    while any(GPIO.input(pin) == GPIO.LOW for pin in BUTTON_PINS):
        time.sleep(0.01)

def main():
    try:
        while True:
            print("\nStarting new skill check...")
            target_index = pick_target()
            target_led = TARGET_NODES[target_index]
            print(f"Target LED: {target_led} | Correct Button GPIO: {BUTTON_PINS[target_index]}")

            pointer = 0
            speed = 1.0  # seconds per step
            last_move = time.monotonic()
            success = False

            while not success:
                now = time.monotonic()
                button = check_button()

                if button is not None:
                    print(f"Button {button + 1} pressed")
                    print(f"Pointer: {pointer}, Target: {target_led}")

                    if button == target_index and pointer == target_led:
                        print("SUCCESS!")
                        wait_for_button_release() 
                        success = True
                    else:
                        print("FAIL! Try again.")
                        wait_for_button_release()
                        time.sleep(1)
                        break

                    continue

                if now - last_move >= speed:
                    pointer = (pointer + 1) % len(LED_PINS)
                    last_move = now

                for i, pin in enumerate(LED_PINS):
                    if i == pointer or i == target_led:
                        GPIO.output(pin, GPIO.HIGH)
                    else:
                        GPIO.output(pin, GPIO.LOW)

                time.sleep(0.001)

    except KeyboardInterrupt:
        print("\nExiting.")
    finally:
        GPIO.cleanup()

if __name__ == "__main__":
    main()
