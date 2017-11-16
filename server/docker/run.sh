#!/bin/sh
chown -R $UID:$GID /polypot
exec su-exec $UID:$GID python /polypot/polypot_server.py
