#!/usr/bin/python3
from vosk import Model, KaldiRecognizer
import socket
import json
import os
from datetime import datetime

# Configuration
UDP_IP = "0.0.0.0"
UDP_PORT = 5555
MODEL_PATH = os.path.expanduser("~/vosk-models/en-model")
LOG_FILE = "/var/log/minecraft_voice.log"

# Initialize Vosk
model = Model(MODEL_PATH)
rec = KaldiRecognizer(model, 16000)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))

print(f"Listening for voice packets on {UDP_IP}:{UDP_PORT}")

while True:
    data, addr = sock.recvfrom(4096)
    if rec.AcceptWaveform(data):
        result = json.loads(rec.Result())
        text = result.get("text", "").strip()
        if text:
            timestamp = datetime.now().isoformat()
            log_entry = f"[{timestamp}] {addr[0]}: {text}\n"
            with open(LOG_FILE, "a") as f:
                f.write(log_entry)