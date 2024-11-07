import os
import cv2
import time
from flask import Flask, Response, jsonify, request
from SoccerNet.Downloader import SoccerNetDownloader

# 데이터 경로 설정
local_directory = "C:/work_oneteam/one-team-SA-proj/SoccerNet"

if not os.path.exists(local_directory):
    os.makedirs(local_directory)

mySoccerNetDownloader = SoccerNetDownloader(LocalDirectory=local_directory)
mySoccerNetDownloader.password = "s0cc3rn3t"

app = Flask(__name__)

def load_tracking_data(tracking_file):
    """ det.txt 파일에서 트래킹 데이터를 로드 """
    tracking_data = {}
    with open(tracking_file, 'r') as f:
        for line in f:
            frame_id, obj_id, x, y, w, h, confidence, _, _, _ = map(float, line.strip().split(','))
            frame_id = int(frame_id)
            if frame_id not in tracking_data:
                tracking_data[frame_id] = []
            tracking_data[frame_id].append((int(obj_id), int(x), int(y), int(w), int(h)))
    return tracking_data

def generate_frames(tracking_folder):
    """ 트래킹 데이터와 프레임을 생성하고 스트리밍합니다. """
    tracking_file = os.path.join(tracking_folder, "det", "det.txt")
    image_folder = os.path.join(tracking_folder, "img1")

    # 트래킹 데이터 로드
    tracking_data = load_tracking_data(tracking_file)

    # 프레임 생성 및 스트리밍
    frame_idx = 1  # 프레임 번호는 1부터 시작하는 경우가 많음
    while True:
        frame_path = os.path.join(image_folder, f"{frame_idx:06d}.jpg")  # 예: "000001.jpg"
        if not os.path.exists(frame_path):
            break  # 더 이상 프레임이 없을 경우 종료

        frame = cv2.imread(frame_path)

        # 현재 프레임에 해당하는 트래킹 데이터 가져오기
        if frame_idx in tracking_data:
            for obj_id, x, y, w, h in tracking_data[frame_idx]:
                # 바운딩 박스 및 ID 표시
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                cv2.putText(frame, f"ID: {obj_id}", (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

        # JPEG로 인코딩 후 바이너리로 변환
        ret, buffer = cv2.imencode('.jpg', frame)
        frame = buffer.tobytes()

        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')

        frame_idx += 1
        # time.sleep(0.03)  # 프레임 속도 조절 (30fps)

@app.route('/video_feed')
def video_feed():
    tracking_folder = request.args.get("tracking_folder")
    if not tracking_folder:
        return "Error: 'tracking_folder' parameter is required", 400
    return Response(generate_frames(tracking_folder), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/status', methods=['GET'])
def status_handler():
    return jsonify({"status": "Server is running"})

@app.route('/download_tracking_data', methods=['POST'])
def download_tracking_data():
    try:
        mySoccerNetDownloader.downloadDataTask(task="tracking", split=["train", "test", "challenge"])
        mySoccerNetDownloader.downloadDataTask(task="tracking-2023", split=["train", "test", "challenge"])
        return jsonify({"message": "Tracking data downloaded successfully"})
    except Exception as e:
        return jsonify({"message": f"Error downloading tracking data: {str(e)}"}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
