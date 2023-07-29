#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <libusb.h>

/*
 * Build:
 *
 * gcc $(pkg-config --cflags --libs libusb-1.0) libusb-hotplug.c -o libusb-hotplug;
 *   chmod a+x libusb-hotplug
 */
static int count = 0;

int hotplug_callback(struct libusb_context *ctx, struct libusb_device *dev,
	libusb_hotplug_event event, void *user_data) {
	static libusb_device_handle *dev_handle = NULL;
	struct libusb_device_descriptor desc;
	int rc;

	count++;
	(void) libusb_get_device_descriptor(dev, &desc);
	printf("Event #%03d: %d:%-7s { vendor=0x%04x, product=0x%04x, bus=0x%02x, address=0x%02x }\n",
		count, event, event == LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED ? "ARRIVED" : "LEFT",
		desc.idVendor, desc.idProduct, libusb_get_bus_number(dev), libusb_get_device_address(dev));

	if (LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED == event) {
		rc = libusb_open(dev, &dev_handle);
		if (LIBUSB_SUCCESS != rc) printf("Could not open USB device\n");
	} else if (LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT == event) {
		if (dev_handle) {
			libusb_close(dev_handle);
			dev_handle = NULL;
		}
	} else printf("Unhandled event %d\n", event);

	return 0;
}

int main(void) {
	libusb_hotplug_callback_handle callback_handle;
	int rc;

	libusb_init(NULL);

	rc = libusb_hotplug_register_callback(NULL,
		LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED | LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT, 0,
		LIBUSB_HOTPLUG_MATCH_ANY, LIBUSB_HOTPLUG_MATCH_ANY, LIBUSB_HOTPLUG_MATCH_ANY,
		hotplug_callback, NULL, &callback_handle);
	if (LIBUSB_SUCCESS != rc) {
		printf("Error creating a hotplug callback\n");
		libusb_exit(NULL);
		return EXIT_FAILURE;
	}
	while (1) {
		libusb_handle_events_completed(NULL, NULL);
		nanosleep(&(struct timespec ){ 0, 10000000UL }, NULL);
	}

	libusb_hotplug_deregister_callback(NULL, callback_handle);
	libusb_exit(NULL);

	return 0;
}

