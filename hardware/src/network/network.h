#ifndef NETWORK_H
#define NETWORK_H

/** Runs once at boot — connects to WiFi with a 20-second timeout. */
void initWiFi();

/**
 * Call from loop(). If WiFi is disconnected, attempts a reconnect using
 * the same credentials defined in secrets.h.
 * @return true if connected (either was already or just reconnected).
 */
bool reconnectIfNeeded();

#endif
