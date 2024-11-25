from pathlib import Path

# 경로 설정
sn_folder = Path("C:/work_oneteam/one-team-SA-proj/SoccerNet/tracking/train/SNMOT-060")
train_labels = Path("C:/work_oneteam/one-team-SA-proj/SoccerNet/tracking/train_labels")
val_labels = Path("C:/work_oneteam/one-team-SA-proj/SoccerNet/tracking/val_labels")

# 라벨 폴더 생성
train_labels.mkdir(exist_ok=True, parents=True)
val_labels.mkdir(exist_ok=True, parents=True)

# 이미지 해상도
img_width, img_height = 1920, 1080

# 학습용 라벨 생성: det.txt 기반
det_file = sn_folder / "det/det.txt"
if det_file.exists():
    with open(det_file, 'r') as f:
        for line in f:
            frame_id, _, x, y, w, h, *_ = line.strip().split(',')
            # 좌표 정규화
            x_center = (float(x) + float(w) / 2) / img_width
            y_center = (float(y) + float(h) / 2) / img_height
            width = float(w) / img_width
            height = float(h) / img_height

            # 클래스 ID는 고정 (0으로 설정)
            class_id = 0

            # 학습용 라벨 파일 생성
            label_file = train_labels / f"{frame_id.zfill(6)}.txt"
            with open(label_file, 'a') as lf:
                lf.write(f"{class_id} {x_center:.6f} {y_center:.6f} {width:.6f} {height:.6f}\n")
