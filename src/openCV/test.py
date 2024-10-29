import os
import time
from collections import Counter

from flask import Flask, request, send_file, jsonify
import logging
import cv2 #openCV
import numpy as np
import io
from PIL import Image
from ultralytics import YOLO
from sklearn.cluster import KMeans
import threading
import json

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

# 전역 변수로 팀 색상 저장
team_colors = {"home": None, "away": None}

# YOLOv8 모델 로드
model = YOLO('yolov8s.pt')

# JSON 파일에서 팀 색상 로드 함수
def load_team_colors(home_team, away_team):
    global team_colors
    try:
        # 절대 경로로 JSON 파일을 지정
        json_path = os.path.join(os.path.dirname(__file__), "team_colors.json")
        with open(json_path, "r") as file:
            color_data = json.load(file)

        # 홈팀 색상 정보 가져오기
        if home_team in color_data:
            home_colors = color_data[home_team].get("home", {})
            team_colors["home"] = extract_colors(home_colors)

        # 원정팀 색상 정보 가져오기
        if away_team in color_data:
            away_colors = color_data[away_team].get("away", {})
            team_colors["away"] = extract_colors(away_colors)

        # 결과 출력 확인
        logging.info(f"Loaded colors - Home: {team_colors['home']}, Away: {team_colors['away']}")
        return True if team_colors["home"] and team_colors["away"] else False

    except FileNotFoundError:
        logging.info("Error: team_colors.json file not found.")
        return False

# 색상 추출 함수 (RGB 값 그대로 사용)
def extract_colors(color_dict):
    colors = []
    for i in range(1, 5):
        color_key = f"primary_color" if i == 1 else f"secondary_color" if i == 2 else f"third_color" if i == 3 else f"fourth_color"
        rgb_color = color_dict.get(color_key)
        if rgb_color:
            colors.append(np.array(rgb_color))  # RGB 값 추가
            logging.info(f"Extracted color for {color_key}: {rgb_color}")
    return colors if colors else None

# 유니폼 색상 추출 및 팀 판별 함수
def identify_uniform_color_per_person(player_crop, team_colors_home, team_colors_away, similarity_threshold=30, majority_threshold=20):
    # 홈팀과 원정팀 색상 정보
    home_primary, home_secondary = team_colors_home[0], team_colors_home[1]
    away_primary, away_secondary = team_colors_away[0], team_colors_away[1]

    # 1. 피부색 필터링
    skin_lower = np.array([45, 34, 30], dtype=np.uint8)
    skin_upper = np.array([255, 224, 210], dtype=np.uint8)
    mask_skin = cv2.inRange(player_crop, skin_lower, skin_upper)
    body_filtered = cv2.bitwise_and(player_crop, player_crop, mask=cv2.bitwise_not(mask_skin))

    # 2. 유사한 색상 기준으로 팀 후보 수집
    team_votes = []
    # processed_pixel_count = 0  # 처리한 픽셀 수 제한

    for y in range(body_filtered.shape[0]):
        for x in range(body_filtered.shape[1]):
            # if processed_pixel_count >= max_pixels:
            #     break

            pixel_color = body_filtered[y, x]
            if np.any(pixel_color):
                # 홈팀과 원정팀 색상과의 유사성 비교
                if np.linalg.norm(pixel_color - home_primary) < similarity_threshold:
                    team_votes.append("home_team")
                elif np.linalg.norm(pixel_color - away_primary) < similarity_threshold:
                    team_votes.append("away_team")
                elif np.linalg.norm(pixel_color - home_secondary) < similarity_threshold:
                    team_votes.append("home_team")
                elif np.linalg.norm(pixel_color - away_secondary) < similarity_threshold:
                    team_votes.append("away_team")
                # else:
                #     team_votes.append("cannot_detected")  # 유사한 색상이 없으면 추가

                # 3. 중간에 결과 반환 조건
                count = Counter(team_votes)
                if count["home_team"] >= majority_threshold:
                    return "home_team"
                elif count["away_team"] >= majority_threshold:
                    return "away_team"

                # processed_pixel_count += 1  # 처리한 픽셀 수 증가

    # 4. 최종 투표 결과 확인
    if team_votes:
        team_count = Counter(team_votes)
        most_common_team, count = team_count.most_common(1)[0]
        logging.info(f"Team votes: {team_count}, Assigned to: {most_common_team}")
        return most_common_team
    else:
        logging.warning("Uniform color could not be detected. Assigning as 'cannot_detected'")
        return "cannot_detected"

# 필드 영역을 저해상도로 감지하고, 객체는 원본 해상도에서 탐지
def detect_green_field_low_res(frame):
    # 해상도 축소 (필드 감지만 저해상도로 처리)
    resized_frame = cv2.resize(frame, (0, 0), fx=0.5, fy=0.5)
    hsv_frame = cv2.cvtColor(resized_frame, cv2.COLOR_BGR2HSV)

    # 초록색 범위 설정 (HSV 기준)
    green_lower = np.array([30, 40, 40], dtype=np.uint8)
    green_upper = np.array([90, 255, 255], dtype=np.uint8)

    # 초록색 바닥 필터링
    mask = cv2.inRange(hsv_frame, green_lower, green_upper)

    # 컨투어 찾기 (필드 영역 탐색)
    contours, _ = cv2.findContours(mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    # 가장 큰 컨투어를 필드로 간주
    if contours:
        largest_contour = max(contours, key=cv2.contourArea)
        # 좌표를 원래 해상도로 다시 변환
        return largest_contour * 2  # 해상도 축소 비율을 고려하여 원래 크기로 복원

    return None  # 필드를 감지하지 못한 경우

# 비디오 처리 함수
def update_status(video_path):
    global processing_status
    global team_colors

    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        logging.error("Error: Could not open video file.")
        processing_status['status'] = "error"
        return

    # 팀 색상 초기화 확인
    if team_colors["home"] is None or team_colors["away"] is None:
        logging.error("Error: Team colors not initialized correctly.")
        processing_status['status'] = "error"
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

        # 필드 영역 감지
        field_contour = detect_green_field_low_res(frame)
        if field_contour is not None:
            cv2.drawContours(frame, [field_contour], -1, (0, 255, 255), 2)  # 필드 영역을 노란색으로 표시

        # 객체 탐지
        results = model(frame)
        for result in results:
            for box in result.boxes:
                bx1, by1, bx2, by2 = map(int, box.xyxy[0])

                # 공과 선수의 필드 내 포함 여부 확인
                if box.cls == 32 and cv2.pointPolygonTest(field_contour, ((bx1 + bx2) // 2, (by1 + by2) // 2), False) >= 0:
                    cv2.rectangle(frame, (bx1, by1), (bx2, by2), (0, 255, 0), 2)  # 공을 초록색 사각형으로 표시

                elif box.cls == 0 and cv2.pointPolygonTest(field_contour, ((bx1 + bx2) // 2, (by1 + by2) // 2), False) >= 0:
                    player_crop = frame[by1:by2, bx1:bx2]
                    team = identify_uniform_color_per_person(player_crop, team_colors["home"], team_colors["away"])

                    # 팀 판별 결과에 따른 박스 색상 결정
                    if team == "home_team":
                        cv2.rectangle(frame, (bx1, by1), (bx2, by2), (0, 0, 255), 2)  # 홈팀은 빨간색
                    elif team == "away_team":
                        cv2.rectangle(frame, (bx1, by1), (bx2, by2), (255, 0, 0), 2)  # 원정팀은 파란색
                    else:
                        cv2.rectangle(frame, (bx1, by1), (bx2, by2), (0, 0, 0), 2)  # 인식되지 않은 선수는 검은색

                    logging.info(f"Player color detected: {team}")

        # 처리된 프레임 저장 및 진행률 업데이트
        out.write(frame)
        logging.info(f"Frame {processed_frames + 1} saved")
        processed_frames += 1
        processing_status['progress'] = int((processed_frames / total_frames) * 100)

    logging.info("Video processing completed, now saving...")
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

# 동영상 처리 시작 전 홈팀 및 원정팀 정보를 JSON에서 불러오기
@app.route('/process_video', methods=['POST'])
def process_video():
    # 요청에서 동영상과 팀 정보 받음
    file = request.files['video']
    home_team = request.form.get('home_team')
    away_team = request.form.get('away_team')
    logging.info(f"Received home_team: {home_team}, away_team: {away_team}")

    # JSON에서 팀 색상 정보 불러오기
    if not load_team_colors(home_team, away_team):
        return jsonify({"error": "Failed to load team colors from JSON."}), 400

    # 동영상 파일 저장
    video_bytes = np.frombuffer(file.read(), np.uint8)
    video_path = "temp_video.mp4"
    with open(video_path, 'wb') as f:
        f.write(video_bytes)

    # 동영상 처리 상태 초기화
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