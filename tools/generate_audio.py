"""
tools/generate_audio.py
Procedurally generates 16-bit 44.1kHz PCM CC0 audio assets for Metro Blitz:
- sfx_jump.wav
- sfx_slide.wav
- sfx_coin.wav
- sfx_hit.wav
- sfx_tap.wav
- sfx_countdown.wav
- bgm_main.wav (seamless upbeat 8-bar cartoon runner loop)
"""
import os
import math
import wave
import struct

SAMPLE_RATE = 44100

def ensure_raw_dir(output_dir):
    os.makedirs(output_dir, exist_ok=True)

def write_wav(filepath, samples):
    with wave.open(filepath, 'wb') as wf:
        wf.setnchannels(1) # Mono
        wf.setsampwidth(2) # 16-bit
        wf.setframerate(SAMPLE_RATE)
        # Pack to 16-bit signed PCM
        raw_data = bytearray()
        for s in samples:
            val = max(-32767, min(32767, int(s * 32767)))
            raw_data.extend(struct.pack('<h', val))
        wf.writeframes(raw_data)
    print(f"Generated: {filepath} ({len(samples)} samples, {len(samples)/SAMPLE_RATE:.2f}s)")

def gen_jump():
    duration = 0.22
    total_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        progress = t / duration
        # Exponential upward frequency sweep from 300Hz to 700Hz
        freq = 300.0 * (2.33 ** progress)
        phase = 2.0 * math.pi * freq * t
        # Envelope: fast attack, smooth decay
        env = math.sin(math.pi * progress) ** 0.8
        sample = (math.sin(phase) + 0.3 * math.sin(phase * 2.0)) * 0.7 * env
        samples.append(sample)
    return samples

def gen_slide():
    duration = 0.25
    total_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        progress = t / duration
        # Downward swoop from 280Hz to 130Hz
        freq = 280.0 - 150.0 * progress
        phase = 2.0 * math.pi * freq * t
        env = math.sin(math.pi * progress) * (1.0 - 0.5 * progress)
        sample = (math.sin(phase) + 0.25 * math.sin(phase * 3.0)) * 0.6 * env
        samples.append(sample)
    return samples

def gen_coin():
    duration = 0.24
    total_samples = int(duration * SAMPLE_RATE)
    samples = []
    # 2 notes: B5 (987.77 Hz) for 0.08s, then E6 (1318.51 Hz) for remainder
    t_switch = 0.08
    phase = 0.0
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        if t < t_switch:
            freq = 987.77
            env = (1.0 - (t / t_switch) * 0.3)
        else:
            freq = 1318.51
            t_rem = (t - t_switch) / (duration - t_switch)
            env = math.exp(-3.5 * t_rem)
        phase += 2.0 * math.pi * freq * (1.0 / SAMPLE_RATE)
        sample = (math.sin(phase) + 0.35 * math.sin(phase * 2.0)) * 0.8 * env
        samples.append(sample)
    return samples

def gen_hit():
    duration = 0.35
    total_samples = int(duration * SAMPLE_RATE)
    samples = []
    phase = 0.0
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        progress = t / duration
        # Rapid downward thud from 180Hz down to 50Hz
        freq = max(50.0, 180.0 * (1.0 - progress * 1.5))
        phase += 2.0 * math.pi * freq * (1.0 / SAMPLE_RATE)
        env = math.exp(-6.0 * progress)
        # Add a tiny punch transient in first 15ms
        punch = math.sin(2.0 * math.pi * 120.0 * t) * (1.0 if t < 0.015 else 0.0)
        sample = (math.sin(phase) * 0.8 + punch * 0.4) * env
        samples.append(sample)
    return samples

def gen_tap():
    duration = 0.05
    total_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        progress = t / duration
        freq = 880.0 - 300.0 * progress
        env = math.exp(-12.0 * progress)
        sample = math.sin(2.0 * math.pi * freq * t) * 0.7 * env
        samples.append(sample)
    return samples

def gen_countdown():
    duration = 0.08
    total_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        progress = t / duration
        freq = 1046.50 # C6 crisp click
        env = math.exp(-10.0 * progress)
        sample = math.sin(2.0 * math.pi * freq * t) * 0.75 * env
        samples.append(sample)
    return samples

def gen_bgm():
    # 8-bar upbeat catchy cartoon runner loop at 128 BPM
    # 1 bar = 4 beats = 4 * (60 / 128) = 1.875s
    # 8 bars = 15.0 seconds
    bpm = 128.0
    beat_dur = 60.0 / bpm
    total_duration = beat_dur * 32.0 # 32 beats = 8 bars = 15.0s
    total_samples = int(total_duration * SAMPLE_RATE)
    samples = [0.0] * total_samples

    # Pentatonic melody notes in C major: C4, D4, E4, G4, A4, C5, D5, E5
    pitches = {
        'C3': 130.81, 'G3': 196.00, 'A3': 220.00, 'F3': 174.61,
        'C4': 261.63, 'D4': 293.66, 'E4': 329.63, 'G4': 392.00, 'A4': 440.00,
        'C5': 523.25, 'D5': 587.33, 'E5': 659.25, 'G5': 783.99
    }

    # 16-step melody pattern repeated twice for 8 bars
    melody = [
        ('C5', 0.5), ('E5', 0.5), ('G5', 0.5), ('A4', 0.5),
        ('C5', 0.75), ('D5', 0.25), ('C5', 1.0),
        ('E5', 0.5), ('D5', 0.5), ('C5', 0.5), ('G4', 0.5),
        ('A4', 1.0), ('C5', 1.0),
        # Bar 5-8 variation
        ('C5', 0.5), ('E5', 0.5), ('G5', 0.5), ('A4', 0.5),
        ('D5', 0.5), ('E5', 0.5), ('D5', 1.0),
        ('C5', 0.5), ('D5', 0.5), ('E5', 0.5), ('G4', 0.5),
        ('C5', 1.5), (None, 0.5)
    ]

    # Synthesize melody
    cur_beat = 0.0
    for note, dur in melody:
        if note and note in pitches:
            freq = pitches[note]
            start_s = int(cur_beat * beat_dur * SAMPLE_RATE)
            note_samples = int(dur * beat_dur * SAMPLE_RATE)
            for j in range(note_samples):
                idx = start_s + j
                if idx < total_samples:
                    t = j / SAMPLE_RATE
                    p = j / note_samples
                    env = math.exp(-2.0 * p) * (1.0 - math.exp(-50.0 * p))
                    sig = (math.sin(2 * math.pi * freq * t) +
                           0.3 * math.sin(4 * math.pi * freq * t) +
                           0.15 * math.sin(6 * math.pi * freq * t)) * 0.22 * env
                    samples[idx] += sig
        cur_beat += dur

    # Bassline on eighth notes (root notes C, G, A, F)
    bass_roots = ['C3', 'C3', 'G3', 'G3', 'A3', 'A3', 'F3', 'F3']
    for bar in range(8):
        root = bass_roots[bar]
        freq = pitches[root]
        for eighth in range(8):
            start_s = int((bar * 4.0 + eighth * 0.5) * beat_dur * SAMPLE_RATE)
            b_samples = int(0.4 * beat_dur * SAMPLE_RATE)
            for j in range(b_samples):
                idx = start_s + j
                if idx < total_samples:
                    p = j / b_samples
                    env = math.exp(-3.5 * p)
                    sig = (math.sin(2 * math.pi * freq * (j / SAMPLE_RATE)) +
                           0.4 * math.sin(4 * math.pi * freq * (j / SAMPLE_RATE))) * 0.28 * env
                    samples[idx] += sig

    # Soft rhythmic drum pulse (kick on 1 and 3, snare blip on 2 and 4)
    for beat in range(32):
        start_s = int(beat * beat_dur * SAMPLE_RATE)
        is_snare = (beat % 2 == 1)
        dur_drum = 0.12 if not is_snare else 0.08
        d_samples = int(dur_drum * SAMPLE_RATE)
        for j in range(d_samples):
            idx = start_s + j
            if idx < total_samples:
                p = j / d_samples
                env = math.exp(-8.0 * p)
                if not is_snare:
                    # Kick: pitch drop 120 -> 40 Hz
                    f = 120.0 * (1.0 - p * 0.8)
                    sig = math.sin(2 * math.pi * f * (j / SAMPLE_RATE)) * 0.35 * env
                else:
                    # Snare blip: 300 Hz pop
                    sig = math.sin(2 * math.pi * 320.0 * (j / SAMPLE_RATE)) * 0.18 * env
                samples[idx] += sig

    # Soft master limiter
    peak = max(abs(s) for s in samples)
    if peak > 0.95:
        samples = [s / (peak / 0.95) for s in samples]

    return samples

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    raw_dir = os.path.join(base_dir, "..", "app", "src", "main", "res", "raw")
    ensure_raw_dir(raw_dir)

    write_wav(os.path.join(raw_dir, "sfx_jump.wav"), gen_jump())
    write_wav(os.path.join(raw_dir, "sfx_slide.wav"), gen_slide())
    write_wav(os.path.join(raw_dir, "sfx_coin.wav"), gen_coin())
    write_wav(os.path.join(raw_dir, "sfx_hit.wav"), gen_hit())
    write_wav(os.path.join(raw_dir, "sfx_tap.wav"), gen_tap())
    write_wav(os.path.join(raw_dir, "sfx_countdown.wav"), gen_countdown())
    write_wav(os.path.join(raw_dir, "bgm_main.wav"), gen_bgm())
    print("All audio generated successfully!")

if __name__ == "__main__":
    main()
