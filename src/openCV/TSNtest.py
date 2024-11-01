import os
import cv2
from flask import Flask, Response, jsonify
import numpy as np
import torch
import torchvision.transforms as transforms
from torchvision.models.video import r3d_18, R3D_18_Weights
from flask_cors import CORS
import time

app = Flask(__name__)
CORS(app)  # 모든 도메인에서 접근 허용

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

# YOLOv5 모델 로드
yolo_model = torch.hub.load('ultralytics/yolov5', 'yolov5s', pretrained=True)
yolo_model.eval()

base_directory = "C:\\work_oneteam\\one-team-SA-proj\\SoccerNet\\england_epl\\qwe"

# 비디오와 트래킹 파일을 찾는 함수
def find_videos_and_tracking():
    video_tracking_pairs = []
    for folder in os.listdir(base_directory):
        folder_path = os.path.join(base_directory, folder)
        if os.path.isdir(folder_path):
            video_file = next((f for f in os.listdir(folder_path) if f.endswith("1_224p.mkv")), None)
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

def process_with_tsn(frames):
    try:
        input_frames = torch.stack([preprocess_frame(f) for f in frames])  # (frames, channels, height, width)
        input_frames = input_frames.permute(1, 0, 2, 3).unsqueeze(0)  # (1, channels, frames, height, width)

        with torch.no_grad():
            predictions = model(input_frames)  # 모델 예측 수행
            label = torch.argmax(predictions, dim=1).item()
            print("Predicted Label:", label)  # 예측 결과 확인용 출력
        return label
    except Exception as e:
        print("Error during prediction:", e)
        return None

def process_with_yolo(frame):
    # YOLO 모델로 객체 검출 수행
    results = yolo_model(frame)
    for *box, conf, cls in results.xyxy[0]:  # 바운딩 박스 정보와 클래스, 신뢰도 추출
        x1, y1, x2, y2 = map(int, box)
        label = f'{yolo_model.names[int(cls)]} {conf:.2f}'
        cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)  # 바운딩 박스 그리기
        cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
    return frame

def generate_frames(video_path, tracking_path):
    cap = cv2.VideoCapture(video_path)
    segment_frames = []
    segment_size = 8  # TSN의 세그먼트 크기

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        # TSN 세그먼트에 맞게 프레임을 추가
        segment_frames.append(frame)
        if len(segment_frames) == segment_size:
            label = process_with_tsn(segment_frames)
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
                time.sleep(0.05)  # 약 20 FPS로 재생 속도 조절
            segment_frames = []  # 세그먼트 초기화 후 반복

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
