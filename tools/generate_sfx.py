"""Generate original mono PCM battle sounds; no external audio assets required."""
import math
import random
import struct
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "core/src/main/resources/audio"
RATE = 22050
SOUNDS = {
    "hit": (0.13, [180, 65], 0.45),
    "shot": (0.20, [850, 240], 0.06),
    "skill": (0.30, [460, 100], 0.25),
    "heal": (0.45, [523, 659, 784], 0.0),
    "shield": (0.38, [330, 660, 990], 0.0),
    "interrupt": (0.24, [1200, 400, 140], 0.18),
    "wave": (0.48, [110, 45], 0.6),
    "start": (0.42, [330, 440, 660], 0.0),
    "victory": (0.85, [523, 659, 784, 1047], 0.0),
    "defeat": (0.75, [392, 330, 262, 196], 0.0),
}


def main():
    ROOT.mkdir(parents=True, exist_ok=True)
    rng = random.Random(7)
    for name, (duration, notes, noise) in SOUNDS.items():
        samples = []
        phase = 0.0
        count = int(RATE * duration)
        for i in range(count):
            progress = i / count
            note_pos = progress * len(notes)
            frequency = notes[min(int(note_pos), len(notes) - 1)]
            if name in {"hit", "shot", "skill", "wave"}:
                frequency = notes[0] + (notes[-1] - notes[0]) * progress
            phase += 2 * math.pi * frequency / RATE
            envelope = min(1.0, i / (RATE * 0.008)) * (1 - progress) ** 1.5
            tone = math.sin(phase) * 0.8 + math.sin(phase * 2) * 0.2
            value = (tone * (1 - noise) + rng.uniform(-1, 1) * noise) * envelope * 0.65
            samples.append(struct.pack("<h", int(value * 32767)))
        with wave.open(str(ROOT / f"{name}.wav"), "wb") as output:
            output.setparams((1, 2, RATE, count, "NONE", "not compressed"))
            output.writeframes(b"".join(samples))


if __name__ == "__main__":
    main()
