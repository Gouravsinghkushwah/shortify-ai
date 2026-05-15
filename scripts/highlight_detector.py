import cv2
import sys
import json
import os
import traceback


def log(msg):
    try:
        sys.stdout.write("[PYTHON LOG] " + str(msg) + "\n")
        sys.stdout.flush()
    except:
        pass  # NEVER crash due to logging


def detect_highlights(video_path):

    log("SCRIPT STARTED")
    log("Received video path: " + video_path)

    if not os.path.exists(video_path):
        log("Video file does NOT exist")
        return {"timestamps": []}

    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        log("OpenCV cannot open video")
        return {"timestamps": []}

    fps = cap.get(cv2.CAP_PROP_FPS)

    if fps is None or fps <= 1:
        fps = 25
        log("FPS fallback to 25")

    prev_frame = None
    timestamps = []
    frame_number = 0

    try:
        while True:
            ret, frame = cap.read()

            if not ret:
                break

            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

            if prev_frame is not None:
                diff = cv2.absdiff(prev_frame, gray)
                score = diff.mean()

                if score > 10:
                    seconds = frame_number / fps
                    timestamps.append(round(seconds, 2))

            prev_frame = gray
            frame_number += 1

    except Exception as e:
        log("Processing error: " + str(e))
        log(traceback.format_exc())

    cap.release()

    timestamps = sorted(list(set(timestamps)))[:10]

    log("Detected timestamps: " + str(timestamps))

    return {"timestamps": timestamps}


if __name__ == "__main__":
    try:
        if len(sys.argv) < 2:
            print(json.dumps({"timestamps": []}))
            sys.exit(0)

        video_path = sys.argv[1]

        result = detect_highlights(video_path)

        # 🔥 FINAL JSON OUTPUT (IMPORTANT)
        print(json.dumps(result))
        sys.stdout.flush()

    except Exception as e:
        log("FATAL ERROR: " + str(e))
        log(traceback.format_exc())

        print(json.dumps({"timestamps": []}))
        sys.stdout.flush()