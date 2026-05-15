# import cv2
# import sys
# import json
#
# def detect_highlights(video_path):
#
#     cap = cv2.VideoCapture(video_path)
#
#     if not cap.isOpened():
#         return {
#             "success": False,
#             "error": "Cannot open video file"
#         }
#
#     fps = cap.get(cv2.CAP_PROP_FPS)
#     if fps == 0 or fps is None:
#         fps = 25  # fallback FPS
#
#     prev_frame = None
#     timestamps = []
#     frame_number = 0
#
#     while True:
#         ret, frame = cap.read()
#         if not ret:
#             break
#
#         gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
#
#         if prev_frame is not None:
#             diff = cv2.absdiff(prev_frame, gray)
#             score = diff.mean()
#
#             # highlight detection threshold
#             if score > 25:
#                 second = frame_number / fps
#                 timestamps.append(round(second, 2))  # FIXED precision
#
#         prev_frame = gray
#         frame_number += 1
#
#     cap.release()
#
#     # remove duplicates + sort
#     timestamps = sorted(list(set(timestamps)))[:8]
#
#     return {
#         "success": True,
#         "timestamps": timestamps
#     }
#
#
# if __name__ == "__main__":
#     try:
#         video_path = sys.argv[1]
#         result = detect_highlights(video_path)
#
#         # ONLY JSON OUTPUT (VERY IMPORTANT)
#         print(json.dumps(result))
#
#     except Exception as e:
#         print(json.dumps({
#             "success": False,
#             "error": str(e)
#         }))