import os
import cv2
from flask import Flask, Response, jsonify
import numpy as np
import torch
import torchvision.transforms as transforms
from torchvision.models.video import r3d_18, R3D_18_Weights
from flask_cors import CORS
from ultralytics import YOLO

app = Flask(__name__)
CORS(app)

# TSN 모델 설정
class SimpleTSN(torch.nn.Module):
    def __init__(self):
        super(SimpleTSN, self).__init__()
        self.base_model = r3d_18(weights=R3D_18_Weights.KINETICS400_V1)
        self.base_model.fc = torch.nn.Linear(self.base_model.fc.in_features, 101)  # 예: 101개 클래스

    def forward(self, x):
        x = self.base_model(x)
        return x

model = SimpleTSN()
model.eval()

# YOLOv8 모델 로드
yolo_model = YOLO('yolov8s.pt')  # 'yolov8s.pt'는 YOLOv8의 사전 훈련된 모델 파일입니다.

base_directory = "C:\\work_oneteam\\one-team-SA-proj\\SoccerNet\\england_epl\\qwe\\Manchester United1-1Arsenal"

# 비디오와 트래킹 파일을 찾는 함수
def find_videos_and_tracking():
    video_tracking_pairs = []
    for folder in os.listdir(base_directory):
        folder_path = os.path.join(base_directory, folder)
        if os.path.isdir(folder_path):
            video_file = next((f for f in os.listdir(folder_path) if f.endswith(".mkv")), None)
            tracking_file = next((f for f in os.listdir(folder_path) if f.endswith(".npy") or f.endswith(".json")), None)
            if video_file and tracking_file:
                video_tracking_pairs.append({
                    "video_path": os.path.join(folder_path, video_file),
                    "tracking_path": os.path.join(folder_path, tracking_file)
                })
    return video_tracking_pairs

# TSN 모델에 맞는 프레임 전처리
def preprocess_frame(frame):
    preprocess = transforms.Compose([
        transforms.ToPILImage(),
        transforms.Resize((128, 171)),
        transforms.CenterCrop((112, 112)),
        transforms.ToTensor()
    ])
    return preprocess(frame)

# 트래킹 데이터를 전처리하여 TSN 모델의 입력으로 변환
def preprocess_tracking_data(tracking_data, frame_shape):
    tracking_map = np.zeros(frame_shape[:2], dtype=np.uint8)  # 빈 맵 생성 (예: 높이, 너비만큼)
    for obj in tracking_data:
        # 각 객체의 x, y 좌표를 가져와서 프레임에 그리기
        x, y = int(obj['x']), int(obj['y'])
        cv2.circle(tracking_map, (x, y), radius=5, color=255, thickness=-1)  # 좌표에 원 그리기
    return tracking_map

# TSN과 트래킹 데이터를 결합하여 예측 수행
def process_with_tsn_and_tracking(frames, tracking_data):
    try:
        # TSN 모델에 맞는 프레임 전처리
        processed_frames = [preprocess_frame(f) for f in frames]
        tracking_map = preprocess_tracking_data(tracking_data, frames[0].shape)  # 트래킹 데이터 전처리
        tracking_map = torch.from_numpy(tracking_map).float().unsqueeze(0)  # 채널 차원 추가

        # 프레임과 트래킹 데이터를 하나의 입력으로 결합
        input_frames = torch.stack(processed_frames).unsqueeze(0)  # (1, frames, channels, height, width)
        combined_input = torch.cat([input_frames, tracking_map.unsqueeze(1)], dim=1)  # 채널 차원에서 결합

        # TSN 모델에 결합된 입력 전달
        with torch.no_grad():
            predictions = model(combined_input)
            label = torch.argmax(predictions, dim=1).item()
        return label
    except Exception as e:
        print("Error during prediction with tracking:", e)
        return None

def process_with_yolo(frame):
    # YOLO 모델로 객체 검출 수행
    results = yolo_model(frame)
    boxes = results[0].boxes  # 결과에서 바운딩 박스 정보 추출

    for box in boxes:
        x1, y1, x2, y2 = map(int, box.xyxy[0])  # 바운딩 박스 좌표
        conf = box.conf[0]  # 신뢰도
        cls = int(box.cls[0])  # 클래스

        # 클래스 0 (사람)과 클래스 32 (축구공)만 필터링
        if (cls == 0 or cls == 32) and is_in_play_area(x1, y1, x2, y2):  # 사람이거나 축구공이고 경기장 내에 있으면
            label = f'{yolo_model.model.names[cls]} {conf:.2f}'  # 라벨 생성
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)  # 바운딩 박스 그리기
            cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)  # 라벨 텍스트 그리기

    return frame

# 경기장 안에 있는지 확인하는 함수 (경기장 영역을 설정)
def is_in_play_area(x1, y1, x2, y2):
    # 예를 들어, 경기장의 좌측 상단과 우측 하단 좌표를 (100, 100)과 (1200, 600)으로 설정했다고 가정
    PLAY_AREA_X_MIN = 100
    PLAY_AREA_Y_MIN = 100
    PLAY_AREA_X_MAX = 1200
    PLAY_AREA_Y_MAX = 600

    # 객체의 바운딩 박스가 경기장 안에 있는지 확인
    if x1 > PLAY_AREA_X_MIN and y1 > PLAY_AREA_Y_MIN and x2 < PLAY_AREA_X_MAX and y2 < PLAY_AREA_Y_MAX:
        return True
    return False

def generate_frames(video_path, tracking_path):
    cap = cv2.VideoCapture(video_path)
    segment_frames = []
    segment_size = 8  # TSN의 세그먼트 크기

    # 트래킹 데이터 로드 및 형식 확인
    if tracking_path.endswith('.npy'):
        tracking_data = np.load(tracking_path, allow_pickle=True)
    else:
        tracking_data = None

    frame_idx = 0
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        # 프레임 업스케일링
        frame = cv2.resize(frame, (1280, 720))

        # TSN 세그먼트에 맞게 프레임을 추가
        segment_frames.append(frame)
        if len(segment_frames) == segment_size:
            # 현재 프레임에 해당하는 트래킹 데이터를 가져옴
            if tracking_data is not None and frame_idx < len(tracking_data):
                current_tracking_data = tracking_data[frame_idx] if isinstance(tracking_data[frame_idx], list) else []
            else:
                current_tracking_data = []  # 트래킹 데이터가 없을 경우 빈 리스트

            # TSN 예측 수행
            label = process_with_tsn_and_tracking(segment_frames, current_tracking_data)
            for f in segment_frames:
                # TSN 라벨 표시
                if label is not None:
                    cv2.putText(f, f"Class: {label}", (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2, cv2.LINE_AA)
                # YOLO 객체 검출 결과 오버레이
                f = process_with_yolo(f)
                ret, buffer = cv2.imencode('.jpg', f)
                frame = buffer.tobytes()
                yield (b'--frame\r\n'
                       b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')
            segment_frames = []  # 세그먼트 초기화 후 반복
        frame_idx += 1

@app.route('/video_feed')
def video_feed():
    video_tracking_pairs = find_videos_and_tracking()
    if not video_tracking_pairs:
        return jsonify({"message": "No videos and tracking data found"}), 404

    video_path = video_tracking_pairs[0]["video_path"]
    tracking_path = video_tracking_pairs[0]["tracking_path"]

    return Response(generate_frames(video_path, tracking_path), mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
