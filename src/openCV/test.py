import os
import time

from flask import Flask, request, send_file, jsonify
import logging
import cv2 #openCV
import numpy as np
import io
from PIL import Image
from ultralytics import YOLO
from sklearn.cluster import KMeans
import threading

app = Flask(__name__)

# DLL 파일 경로 설정
os.environ['OPENH264_LIBRARY'] = r'C:\work_oneteam\one-team-SA-proj\libs\openh264-1.8.0-win64.dll'

#이미지 테스트
@app.route('/process', methods=['POST'])
def process_image():
    # 요청에서 이미지를 받음
    file = request.files['image']
    np_img = np.frombuffer(file.read(), np.uint8)
    img = cv2.imdecode(np_img, cv2.IMREAD_COLOR)

    # OpenCV로 이미지를 흑백으로 변환
    gray_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # 변환된 이미지를 메모리에서 파일로 변환 (PIL로 변환 후 메모리로 저장)
    pil_img = Image.fromarray(gray_img)
    img_io = io.BytesIO()
    pil_img.save(img_io, 'JPEG')
    img_io.seek(0)

    # 흑백 이미지를 반환
    return send_file(img_io, mimetype='image/jpeg')

# 글로벌 상태 변수
processing_status = {
    "status": "idle",  # 처리 상태: idle, processing, completed
    "progress": 0      # 진행률: 0 ~ 100
}

team_colors = []  # 초기 팀 색상 저장

# YOLOv8 모델 로드
model = YOLO('yolov8s.pt')

# 초기 프레임에서 두 가지 주요 색상 추출 (선수 유니폼 색상)
def detect_team_colors(video_path, num_frames=10):
    cap = cv2.VideoCapture(video_path)
    color_samples = []

    for _ in range(num_frames):
        ret, frame = cap.read()
        if not ret:
            break

        # YOLO로 person 클래스 탐지 (선수)
        results = model(frame)
        for result in results:
            for box in result.boxes:
                if box.cls == 0:  # 선수(class_id 0)
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    player_crop = frame[y1:y2, x1:x2]  # 선수 부분만 크롭
                    avg_color = identify_team_color(player_crop)  # 유니폼 색상 추출 (H, S만 사용)
                    color_samples.append(avg_color)  # 색상 정보 추가

    cap.release()

    # K-means 클러스터링으로 두 가지 주요 색상 찾기
    kmeans = KMeans(n_clusters=2)
    kmeans.fit(color_samples)
    global team_colors
    team_colors = kmeans.cluster_centers_  # 두 팀의 색상 중심

# 각 프레임에서 팀 색상에 따라 선수 구분
def identify_team(hsv_color):

    hsv_color_hs = hsv_color[:2]  # H와 S 값만 사용

    # 각 팀의 색상과 비교 (hsv_color_hs 사용)
    diff_team_1 = np.linalg.norm(hsv_color_hs - team_colors[0])
    diff_team_2 = np.linalg.norm(hsv_color_hs - team_colors[1])

    if diff_team_1 < diff_team_2:
        return 'team_1'
    else:
        return 'team_2'

def identify_team_color(player_crop):
    hsv_img = cv2.cvtColor(player_crop, cv2.COLOR_BGR2HSV)

    # 상체 부분을 이미지 높이 기준으로 나누어 중간 부분만 사용 (소매, 하의 제외)
    height, width, _ = hsv_img.shape
    torso_body = hsv_img[height // 4: 3 * height // 4, :]  # 몸통 부분 (가운데 1/2)

    # 피부색 필터링 (HSV 범위에서 피부색 제거)
    skin_lower = np.array([0, 30, 60], dtype=np.uint8)
    skin_upper = np.array([20, 150, 255], dtype=np.uint8)

    # 피부색을 제외한 상체 부분 추출
    mask_torso = cv2.inRange(torso_body, skin_lower, skin_upper)
    torso_filtered = cv2.bitwise_and(torso_body, torso_body, mask=cv2.bitwise_not(mask_torso))

    # HSV 히스토그램 계산 (가장 눈에 띄는 색상 추출)
    h_hist = cv2.calcHist([torso_filtered], [0], None, [180], [0, 180])
    s_hist = cv2.calcHist([torso_filtered], [1], None, [256], [0, 256])

    # 가장 빈도가 높은 H, S 값 추출
    dominant_h = np.argmax(h_hist)  # Hue 값에서 가장 빈도가 높은 색상
    dominant_s = np.argmax(s_hist)  # Saturation 값에서 가장 빈도가 높은 채도

    # 항상 일정한 크기의 배열 [H, S] 반환
    return np.array([dominant_h, dominant_s])

# 바닥을 초록색으로 필터링하는 함수
def is_on_green_field(frame, x1, y1, x2, y2):
    # 선수의 아래 부분을 기준으로 초록색 바닥인지 확인
    player_area = frame[y2-10:y2, x1:x2]  # 선수의 발 부분 (하단)
    hsv_player_area = cv2.cvtColor(player_area, cv2.COLOR_BGR2HSV)

    # 초록색 범위 설정 (HSV 기준)
    green_lower = np.array([35, 40, 40], dtype=np.uint8)
    green_upper = np.array([85, 255, 255], dtype=np.uint8)

    # 초록색 바닥 필터링
    mask = cv2.inRange(hsv_player_area, green_lower, green_upper)
    green_pixels_ratio = cv2.countNonZero(mask) / (player_area.size / 3)

    # 일정 비율 이상 초록색일 때만 True 반환 (예: 50% 이상)
    return green_pixels_ratio > 0.5

# 비디오 처리 함수
def update_status(video_path):
    global processing_status

    # 동영상 처리 시작 (초기 10프레임에서 팀 색상 감지)
    detect_team_colors(video_path, num_frames=10)

    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        return
    save_path = os.path.join("C:/work_oneteam/one-team-SA-proj/src/main/resources/static/video", "processed_video.mp4")
    fourcc = cv2.VideoWriter_fourcc(*'avc1')
    out = cv2.VideoWriter(save_path, fourcc, 20.0, (int(cap.get(3)), int(cap.get(4))))

    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    processed_frames = 0

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        # YOLO로 객체 탐지 및 처리 (생략된 코드)
        results = model(frame)

        for result in results:
            for box in result.boxes:
                if box.cls == 32:  # 공 (class_id 32)
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)  # 공은 초록색 박스

                # 다른 공 (예: baseball, tennisball)은 제외하려면 다른 클래스 ID를 무시
                if box.cls not in [32]:  # 다른 공들은 처리하지 않음
                    continue

                if box.cls == 0:  # 선수 (class_id 0)
                    x1, y1, x2, y2 = map(int, box.xyxy[0])

                    # 초록색 바닥 위에 있는 선수만 처리
                    if is_on_green_field(frame, x1, y1, x2, y2):
                        player_crop = frame[y1:y2, x1:x2]
                        avg_color = np.mean(cv2.cvtColor(player_crop, cv2.COLOR_BGR2HSV), axis=(0, 1))
                        team = identify_team(avg_color)  # 팀 판별

                        # 팀에 따라 박스 색상 변경
                        if team == 'team_1':
                            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 0, 255), 2)  # 팀 1 (빨간색)
                        else:
                            cv2.rectangle(frame, (x1, y1), (x2, y2), (255, 0, 0), 2)  # 팀 2 (파란색)

        # 처리된 프레임 저장
        out.write(frame)
        print(f"Frame {processed_frames + 1} saved")  # 프레임 저장 로그 추가

        processed_frames += 1
        processing_status['progress'] = int((processed_frames / total_frames) * 100)

    print("Video processing completed, now saving...")  # 로그 추가
    cap.release()
    out.release()

    # 상태를 "saving"으로 변경
    processing_status['status'] = "saving"

    # 동영상 파일이 정상적으로 저장되었는지 확인하는 추가 검증
    time.sleep(1)  # 파일 시스템에서 저장 완료까지의 잠깐의 딜레이

    # 파일 크기 확인 (예시: 최소 파일 크기를 1MB 이상으로 설정)
    if os.path.exists(save_path) and os.path.getsize(save_path) > 1 * 1024 * 1024:  # 1MB 이상
        print("File size check passed.")
    else:
        print("File size too small, video may be corrupted.")
        processing_status['status'] = "error"
        return

    # 동영상 무결성 확인: OpenCV로 다시 열어 프레임을 확인
    check_cap = cv2.VideoCapture(save_path)
    if check_cap.isOpened():
        ret, _ = check_cap.read()
        check_cap.release()
        if ret:
            print("Video integrity check passed.")
            processing_status['status'] = "completed"
        else:
            print("Error: Unable to read video frames, video may be corrupted.")
            processing_status['status'] = "error"
    else:
        print("Error: Unable to open video file, video may be corrupted.")
        processing_status['status'] = "error"

    # 원본 비디오 파일 삭제
    if os.path.exists(video_path):
        os.remove(video_path)
        print(f"원본 비디오 파일 {video_path} 삭제 완료.")

@app.route('/download_video', methods=['GET'])
def download_video():
    # Java 프로젝트의 static 폴더에 저장된 동영상 경로로 수정
    video_path = os.path.join("C:/work_oneteam/one-team-SA-proj/src/main/resources/static/video", "processed_video.mp4")

    # 동영상 파일을 다운로드하여 클라이언트로 전송
    return send_file(video_path, as_attachment=True)

@app.route('/process_video', methods=['POST'])
def process_video():
    # 요청에서 동영상 파일을 받음
    file = request.files['video']
    video_bytes = np.frombuffer(file.read(), np.uint8)
    video_path = "temp_video.mp4"

    # 동영상 파일을 저장
    with open(video_path, 'wb') as f:
        f.write(video_bytes)

    # 동영상 처리 시작
    global processing_status
    processing_status['status'] = "processing"
    processing_status['progress'] = 0

    # 동영상 처리 비동기 스레드에서 실행
    threading.Thread(target=update_status, args=(video_path,)).start()

    return jsonify({"message": "동영상 처리를 시작했습니다."})

@app.route('/status', methods=['GET'])
def get_status():
    # 현재 처리 상태 반환
    return jsonify(processing_status)

@app.route('/video-status', methods=['GET'])
def video_status():
    global processing_status
    return jsonify(processing_status)

@app.route('/health', methods=['GET'])
def health_check():
    return "Flask 서버가 실행 중입니다.", 200

@app.route('/shutdown', methods=['GET'])
def shutdown():
    shutdown_server = request.environ.get('werkzeug.server.shutdown')
    if shutdown_server is None:
        print("Werkzeug 서버가 아니므로 프로세스를 강제 종료합니다.")
        os._exit(0)  # Python 프로세스 종료
    else:
        shutdown_server()
        return 'Flask 서버가 종료되었습니다.'

if __name__ == '__main__':
    logging.basicConfig(filename='flask_app.log', level=logging.DEBUG)
    app.run(debug=True, host='0.0.0.0', port=5000)