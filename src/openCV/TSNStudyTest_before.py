import os
import json
import cv2
from torchvision import transforms
from PIL import Image

# 리그, 연도, 여러 경기 경로 지정
league = "england_efl"
year = "2019-2020"
games = ["BlackburnRovers_NottinghamForest", "Brentford_BristolCity", "HullCity_SheffieldWednesday", "LeedsUnited_WestBromwich"]

# 비디오 파일과 JSON 파일 경로 생성 함수
def get_video_and_json_paths(league, year, game):
    video_path = os.path.join("C:\\work_oneteam\\학습데이터최종\\상황데이터", league, year, game, "1_720p.mp4")
    json_path = os.path.join("C:\\work_oneteam\\학습데이터최종\\상황데이터", league, year, game, "Labels-ball.json")
    return video_path, json_path

# 비디오의 전체 프레임 수를 계산하는 함수
def get_video_frames(video_path):
    cap = cv2.VideoCapture(video_path)
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    cap.release()
    return total_frames

# position(밀리초)을 기반으로 비디오에서 해당 프레임 번호를 계산하는 함수
def position_to_frame(position_ms, video_path):
    cap = cv2.VideoCapture(video_path)
    fps = cap.get(cv2.CAP_PROP_FPS)  # 초당 프레임 수 (FPS)
    cap.release()

    # position_ms를 해당 프레임 번호로 변환
    frame_index = int(position_ms / (1000 / fps))  # 1초를 초당 프레임 수로 나누어 해당 프레임 번호 추정
    return frame_index

# 비디오에서 특정 프레임을 추출하는 함수
def load_frame(video_path, frame_index):
    cap = cv2.VideoCapture(video_path)
    cap.set(cv2.CAP_PROP_POS_FRAMES, frame_index)

    ret, frame = cap.read()
    cap.release()

    if ret:
        # OpenCV에서 읽은 이미지를 PIL로 변환
        return Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
    else:
        return None

# 저장된 frame 폴더의 이미지 개수를 기반으로 총 프레임 개수를 계산
def get_total_frames_from_folder(frame_folder_path):
    if not os.path.exists(frame_folder_path):
        raise FileNotFoundError(f"Frame folder not found: {frame_folder_path}")
    # 이미지 파일 개수 계산
    total_frames = len([f for f in os.listdir(frame_folder_path) if os.path.isfile(os.path.join(frame_folder_path, f))])
    return total_frames


# 데이터셋 전처리 함수
def preprocess_data(video_path, json_path, transform=None, output_dir=None):
    # JSON 파일 로드
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    annotations = data["annotations"]

    # 클래스 정의: 레이블과 클래스 매핑
    class_mapping = {label: idx for idx, label in enumerate(set(annotation["label"] for annotation in annotations))}

    # 프레임과 레이블을 저장할 리스트
    frame_labels = []  # 프레임 번호 및 레이블을 저장할 리스트

    total_frame_index = 0  # 전체 비디오에서의 프레임 번호를 추적

    # `frame` 하위 폴더 경로 생성
    frame_folder_path = os.path.join(output_dir, "frame")
    if output_dir and not os.path.exists(frame_folder_path):
        os.makedirs(frame_folder_path)

    for annotation in annotations:
        label = annotation["label"]
        position_ms = int(annotation["position"])  # position은 이미 밀리초로 제공됨

        # position을 사용하여 해당 프레임 번호 계산
        frame_index = position_to_frame(position_ms, video_path)

        # 해당 프레임을 로드
        frame = load_frame(video_path, frame_index)
        if frame is None:
            print(f"Failed to load frame at index {frame_index}")  # 프레임 로드 실패 확인
        else:
            # 프레임 저장 경로 설정
            frame_path = os.path.join(frame_folder_path, f"frame_{total_frame_index}.jpg")

            # 변환 전 원본 프레임 저장
            frame.save(frame_path)

            # transform이 있다면 변환 적용 후 저장
            if transform:
                transformed_frame = transform(frame)
                # Tensor를 PIL로 다시 변환
                transformed_frame = transforms.ToPILImage()(transformed_frame)
                transformed_frame.save(frame_path)

            # 프레임 번호와 클래스 인덱스 저장
            frame_labels.append((frame_path, class_mapping[label]))
            total_frame_index += 1

    # 레이블 파일 저장: 프레임 경로, 프레임 번호, 클래스 인덱스를 기록
    frame_folder = os.path.join(output_dir, "frame")
    label_file_path = os.path.join(output_dir, "labels.txt")
    total_frames = get_total_frames_from_folder(frame_folder)

    # 레이블 데이터를 8:2로 나누기
    train_data, val_data = train_test_split(frame_labels, test_size=0.2, random_state=42)

    # train_label.txt 저장
    train_label_file = os.path.join(output_dir, "train_label.txt")
    with open(train_label_file, 'w', encoding='utf-8') as train_file:
        for frame_path, class_idx in train_data:
            train_file.write(f"{frame_path} {total_frames} {class_idx}\n")

    # val_label.txt 저장
    val_label_file = os.path.join(output_dir, "val_label.txt")
    with open(val_label_file, 'w', encoding='utf-8') as val_file:
        for frame_path, class_idx in val_data:
            val_file.write(f"{frame_path} {total_frames} {class_idx}\n")


    # 클래스 매핑 저장
    class_file_path = os.path.join(output_dir, "classes.txt")
    with open(class_file_path, 'w', encoding='utf-8') as class_file:
        for label, idx in class_mapping.items():
            # idx는 classes.txt에서 사용할 ID
            class_file.write(f"{idx} {label}\n")

    return frame_labels

# 여러 경기에 대해 전처리 실행
transform = transforms.Compose([
    transforms.Resize((224, 224)),  # 이미지 크기 조정 (필요에 따라 수정)
    transforms.ToTensor()           # 이미지를 텐서로 변환
])

for game in games:
    video_path, json_path = get_video_and_json_paths(league, year, game)

    if os.path.exists(video_path) and os.path.exists(json_path):
        # 저장할 경로 설정
        output_dir = os.path.join("C:\\work_oneteam\\학습데이터최종\\상황데이터", league, year, game)

        frame_labels = preprocess_data(video_path, json_path, transform=transform, output_dir=output_dir)

        # 결과 출력
        print(f"게임: {game}")
    else:
        print(f"경기 '{game}'에 대한 비디오 또는 JSON 파일을 찾을 수 없습니다.")
