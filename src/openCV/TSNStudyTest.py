from tsn import TSN  # TSN 라이브러리 필요
from flask import Flask, jsonify, request
import numpy as np
import torch
import cv2


# Flask 애플리케이션 생성
app = Flask(__name__)

# TSN 학습용 데이터 전처리 함수
def preprocess_tsn_data(sn_folder):
    # img1 폴더에서 프레임 샘플링
    img_folder = sn_folder / 'img1'
    seqinfo_file = sn_folder / 'seqinfo.ini'

    # seqinfo.ini에서 프레임 속도 정보 가져오기
    with open(seqinfo_file, 'r') as f:
        lines = f.readlines()
        fps = int([line for line in lines if "frameRate" in line][0].split('=')[1].strip())

    # TSN에 맞게 프레임 샘플링
    sampled_frames = []
    for i, img_file in enumerate(sorted(img_folder.iterdir())):
        if i % fps == 0:
            frame = cv2.imread(str(img_file))
            sampled_frames.append(frame)

    return sampled_frames

# TSN 모델 학습 함수
def train_tsn(sn_folders):
    model = TSN(num_class=400, modality="RGB")  # 클래스 수와 TSN 모드 설정
    optimizer = torch.optim.Adam(model.parameters(), lr=1e-3)

    for sn_folder in sn_folders:
        frames = preprocess_tsn_data(sn_folder)

        # 모델 입력 데이터로 변환 및 학습
        frames_tensor = torch.tensor(frames, dtype=torch.float32).permute(0, 3, 1, 2)  # [N, C, H, W] 형식
        optimizer.zero_grad()

        outputs = model(frames_tensor)

        # 라벨을 sn_folder에 맞춰 준비하는 부분을 구현해야 합니다.
        # 예를 들어 실제 데이터 라벨을 가져오는 코드가 필요합니다.
        label = ...  # 라벨 값은 실제 데이터에 따라 정의 필요

        loss = torch.nn.functional.cross_entropy(outputs, torch.tensor([label]))  # label은 실제 라벨

        # 역전파 및 가중치 업데이트
        loss.backward()
        optimizer.step()

        # SNMOT-* 폴더에 대해 TSN 학습 실행
        train_tsn(sn_folders)

        # TSN 예제
        torch.save(model.state_dict(), "trained_tsn_model.pth")

        if __name__ == '__main__':
            app.run(port=5000)