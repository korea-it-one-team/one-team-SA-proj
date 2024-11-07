from flask import Flask, jsonify, request
from pathlib import Path
import cv2
from ultralytics import YOLO  # YOLOv8 라이브러리 사용
import threading

# Flask 애플리케이션 생성
app = Flask(__name__)

# SNMOT-060 폴더 경로 설정 (테스트용)
sn_folder = Path("C:/work_oneteam/one-team-SA-proj/SoccerNet/tracking/train/SNMOT-060")

# YOLO 학습용 데이터 전처리 함수
def preprocess_yolo_data(sn_folder):
    # det/det.txt 파일 읽기
    gt_file = sn_folder / 'det' / 'det.txt'
    img_folder = sn_folder / 'img1'

    # 이미지 크기를 정의하기 위해 첫 번째 이미지 읽기
    first_image_path = next(img_folder.iterdir())
    first_image = cv2.imread(str(first_image_path))
    img_height, img_width, _ = first_image.shape  # 이미지 크기 설정

    yolo_annotations = []
    with open(gt_file, 'r') as f:
        for line in f:
            # gt.txt 파일의 각 줄에서 필요한 정보 추출
            frame_id, obj_id, x, y, w, h, *_ = line.strip().split(',')
            x_center = (float(x) + float(w) / 2) / img_width
            y_center = (float(y) + float(h) / 2) / img_height
            width = float(w) / img_width
            height = float(h) / img_height
            yolo_annotations.append(f"{obj_id} {x_center} {y_center} {width} {height}")

    # YOLO 형식의 라벨 파일 저장
    yolo_labels_path = sn_folder / 'yolo_labels'
    yolo_labels_path.mkdir(exist_ok=True)
    for idx, annotation in enumerate(yolo_annotations):
        with open(yolo_labels_path / f"{idx:06d}.txt", 'w') as label_file:
            label_file.write(annotation)

# YOLOv8 모델 학습 함수
def train_yolo(sn_folder):
    # YOLOv8 모델 설정
    model = YOLO("yolov8n.pt")  # 미리 학습된 모델 사용

    # 데이터 전처리
    preprocess_yolo_data(sn_folder)

    # 학습 시작
    model.train(data='C:/work_oneteam/one-team-SA-proj/data.yaml', epochs=100, batch=4)

    # 모델 저장 경로 생성
    save_dir = Path("C:/work_oneteam/one-team-SA-proj/study")
    save_dir.mkdir(exist_ok=True)
    model.save(save_dir / "trained_yolov8_model.pt")

# 학습을 비동기로 실행하기 위한 함수
def run_training():
    train_yolo(sn_folder)

# 학습 시작 API 엔드포인트
@app.route('/start-training', methods=['POST'])
def start_training():
    # 새로운 스레드에서 학습 시작
    training_thread = threading.Thread(target=run_training)
    training_thread.start()
    return jsonify({"status": "YOLOv8 training started"}), 200

if __name__ == '__main__':
    app.run(port=5000)
