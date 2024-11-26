from flask import Flask, jsonify, request
from pathlib import Path
import cv2
from ultralytics import YOLO
import threading
import configparser
import yaml

# Flask 애플리케이션 생성
app = Flask(__name__)

# SNMOT-060 폴더 경로 설정
sn_folder = Path("C:/work_oneteam/one-team-SA-proj/SoccerNet/tracking/train/SNMOT-060")
training_status = {"status": "idle", "message": ""}  # 학습 상태 관리

# gameinfo.ini 파싱 함수
def parse_gameinfo(gameinfo_path):
    config = configparser.ConfigParser()
    config.optionxform = str  # 대소문자 구분 설정
    config.read(gameinfo_path)
    if not Path(gameinfo_path).exists():
        print("⚠️ gameinfo.ini 파일이 존재하지 않습니다.")
        return {}
    if "Sequence" not in config:
        print("⚠️ 'Sequence' 섹션이 gameinfo.ini에 없습니다.")
        return {}
    class_mapping = {}
    tracklets = [key for key in config["Sequence"] if key.startswith("trackletID")]
    for tracklet in tracklets:
        try:
            tracklet_id = int(tracklet.split('_')[1])  # trackletID에서 ID 추출
            tracklet_info = config["Sequence"][tracklet]
            class_name, additional_info = tracklet_info.split(";")
            class_mapping[tracklet_id] = {"class_name": class_name.strip(), "info": additional_info.strip()}
        except Exception as e:
            print(f"⚠️ Error parsing {tracklet}: {e}")

    return class_mapping

def create_obj_id_mapping(gameinfo_path):
    class_mapping = parse_gameinfo(gameinfo_path)
    obj_id_mapping = {tracklet_id: i for i, tracklet_id in enumerate(class_mapping.keys())}
    return obj_id_mapping, class_mapping

# seqinfo.ini 파싱 함수
def parse_seqinfo(seqinfo_path):
    config = configparser.ConfigParser()
    config.read(seqinfo_path)
    if "Sequence" not in config:
        print("⚠️ 'Sequence' 섹션이 gameinfo.ini에 없습니다.")
    seq_info = {
        "name": config["Sequence"]["name"],
        "imDir": config["Sequence"]["imDir"],
        "frameRate": int(config["Sequence"]["frameRate"]),
        "seqLength": int(config["Sequence"]["seqLength"]),
        "imWidth": int(config["Sequence"]["imWidth"]),
        "imHeight": int(config["Sequence"]["imHeight"]),
        "imExt": config["Sequence"]["imExt"],
    }
    return seq_info

# YOLO 학습용 데이터 전처리 함수
def preprocess_yolo_data(gt_file, img_folder, yolo_labels_path, gameinfo_path, seqinfo_path, nc):
    yolo_labels_path = Path(yolo_labels_path)
    yolo_labels_path.mkdir(parents=True, exist_ok=True)
    seq_info = parse_seqinfo(seqinfo_path)
    img_folder = Path(img_folder)
    obj_id_mapping, class_mapping = create_obj_id_mapping(gameinfo_path)
    yolo_annotations = {}
    with open(gt_file, 'r') as f:
        for line in f:
            try:
                frame_id, obj_id, x, y, w, h, *_ = line.strip().split(',')
                frame_id = int(frame_id)
                obj_id = int(obj_id)
                if obj_id not in obj_id_mapping:
                    continue
                class_id = obj_id_mapping[obj_id]
                if class_id < 0 or class_id >= nc:
                    continue
                img_width = seq_info["imWidth"]
                img_height = seq_info["imHeight"]
                x_center = (float(x) + float(w) / 2) / img_width
                y_center = (float(y) + float(h) / 2) / img_height
                width = float(w) / img_width
                height = float(h) / img_height
                frame_key = f"{frame_id:06d}"
                if frame_key not in yolo_annotations:
                    yolo_annotations[frame_key] = []
                yolo_annotations[frame_key].append(f"{class_id} {x_center} {y_center} {width} {height}")
            except Exception as e:
                print(f"⚠️ Error processing line: {line}. Error: {e}")
    for img_file in img_folder.glob(f"*{seq_info['imExt']}"):
        frame_key = img_file.stem
        label_file_path = yolo_labels_path / f"{frame_key}.txt"
        annotations = yolo_annotations.get(frame_key, [])
        with open(label_file_path, 'w') as label_file:
            label_file.write("\n".join(annotations))
    print("YOLO 라벨 데이터 생성 완료!")

# data.yaml 자동 생성 함수
def generate_data_yaml_from_gameinfo(data_yaml_path, train_dir, val_dir, gameinfo_path):
    """
    gameinfo.ini를 활용하여 data.yaml 파일 생성 (중복 제거 없이)
    Args:
        data_yaml_path (str): 저장할 data.yaml 경로
        train_dir (str): 학습 이미지 디렉토리
        val_dir (str): 검증 이미지 디렉토리
        gameinfo_path (str): gameinfo.ini 파일 경로
    """
    obj_id_mapping, class_mapping = create_obj_id_mapping(gameinfo_path)

    # 클래스 이름 순서를 유지한 배열 생성
    class_names = [class_mapping[obj_id]["class_name"] for obj_id in sorted(class_mapping.keys())]

    # data.yaml 생성
    data_yaml = {
        "path": "C:/work_oneteam/one-team-SA-proj/SoccerNet/tracking",
        "train": train_dir,
        "val": val_dir,
        "nc": len(class_names),  # 총 클래스 수
        "names": class_names  # 원본 순서 유지
    }

    # data.yaml 저장
    with open(data_yaml_path, 'w') as f:
        yaml.dump(data_yaml, f, default_flow_style=False)

    print(f"data.yaml 파일이 생성되었습니다: {data_yaml_path}")
    print(f"클래스 목록: {class_names}")



def verify_label_class_consistency(data_yaml_path, yolo_labels_path):
    """
    data.yaml의 클래스 이름과 yolo_labels의 클래스 ID 일치 여부 확인
    """
    with open(data_yaml_path, 'r') as f:
        data_yaml = yaml.safe_load(f)

    yaml_class_names = data_yaml["names"]
    used_classes = set()

    for label_file in Path(yolo_labels_path).glob("*.txt"):
        with open(label_file, 'r') as f:
            for line in f:
                class_id = int(line.split()[0])
                used_classes.add(class_id)

    if len(yaml_class_names) != len(used_classes):
        print(f"⚠️ 클래스 수 불일치: data.yaml에는 {len(yaml_class_names)}개의 클래스가 있지만, 라벨에는 {len(used_classes)}개의 클래스가 사용되었습니다.")
    else:
        print("✅ 클래스 수가 일치합니다.")

    for class_id in used_classes:
        if class_id >= len(yaml_class_names):
            print(f"⚠️ 사용되지 않는 클래스 ID: {class_id} (data.yaml 범위를 초과)")
        else:
            print(f"✅ 클래스 ID {class_id}와 이름 '{yaml_class_names[class_id]}'이 일치합니다.")

# 캐시 파일 삭제
def clear_yolo_cache(data_dir):
    for cache_file in Path(data_dir).glob("*.cache"):
        cache_file.unlink()

# YOLO 학습 함수
def train_yolo(sn_folder):
    global training_status
    try:
        training_status["status"] = "running"
        training_status["message"] = "Preprocessing data..."
        print("Preprocessing YOLO data...")
        preprocess_yolo_data(
            gt_file=str(sn_folder / "gt/gt.txt"),
            img_folder=str(sn_folder / "img1"),
            yolo_labels_path=str(sn_folder / "yolo_labels"),
            gameinfo_path=str(sn_folder / "gameinfo.ini"),
            seqinfo_path=str(sn_folder / "seqinfo.ini"),
            nc=26
        )
        clear_yolo_cache(sn_folder)
        generate_data_yaml_from_gameinfo(
            data_yaml_path="C:/work_oneteam/one-team-SA-proj/data.yaml",
            train_dir="train_images",
            val_dir="val_images",
            gameinfo_path=str(sn_folder / "gameinfo.ini")
        )
        verify_label_class_consistency(
            data_yaml_path="C:/work_oneteam/one-team-SA-proj/data.yaml",
            yolo_labels_path="C:/work_oneteam/one-team-SA-proj/SoccerNet/tracking/train/SNMOT-060/yolo_labels"
        )
        training_status["message"] = "Training YOLO model..."
        print("Training YOLO model...")
        model = YOLO("yolov8n.pt")
        model.train(data="C:/work_oneteam/one-team-SA-proj/data.yaml", epochs=100, batch=4)
        save_dir = Path("C:/work_oneteam/one-team-SA-proj/study")
        save_dir.mkdir(exist_ok=True)
        model.save(save_dir / "trained_yolov8_model.pt")
        training_status["status"] = "completed"
        training_status["message"] = "Training completed successfully!"
    except Exception as e:
        training_status["status"] = "failed"
        training_status["message"] = f"Training failed: {e}"
        print(f"Training failed: {e}")

# 학습을 비동기로 실행하기 위한 함수
def run_training():
    train_yolo(sn_folder)

# 학습 시작 API 엔드포인트
@app.route('/start-training', methods=['POST'])
def start_training():
    global training_status
    try:
        training_status["status"] = "starting"
        training_status["message"] = "Starting YOLOv8 training..."
        threading.Thread(target=run_training, daemon=True).start()
        return jsonify({"status": "YOLOv8 training started"}), 200
    except Exception as e:
        training_status["status"] = "failed"
        training_status["message"] = f"Failed to start training: {e}"
        return jsonify({"status": "failed", "error": str(e)}), 500

# 학습 상태 확인 API 엔드포인트
@app.route('/training-status', methods=['GET'])
def training_status_endpoint():
    return jsonify(training_status), 200

if __name__ == '__main__':
    app.run(port=5000)
