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
    video_path = os.path.join("C:\\work_oneteam\\학습데이터 최종\\상황데이터", league, year, game, "1_720p.mp4")
    json_path = os.path.join("C:\\work_oneteam\\학습데이터 최종\\상황데이터", league, year, game, "Labels-ball.json")
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

# 데이터셋 전처리 함수
def preprocess_data(video_path, json_path, num_segments=3, new_length=1, transform=None, output_dir=None):
    # JSON 파일 로드
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    annotations = data["annotations"]

    # 클래스 정의: 레이블과 클래스 매핑
    class_mapping = {label: idx for idx, label in enumerate(set(annotation["label"] for annotation in annotations))}

    # 비디오의 전체 프레임 수
    total_frames = get_video_frames(video_path)

    # 프레임과 레이블을 저장할 리스트
    frames = []
    labels = []  # 레이블 리스트 초기화

    frame_labels = []  # 프레임 번호, 경로 및 레이블을 저장할 리스트
    frame_paths = []   # 프레임 경로를 저장할 리스트

    # 각 세그먼트 저장
    total_frame_index = 0  # 전체 비디오에서의 프레임 번호를 추적
    total_frame_index1 = -1
    for annotation in annotations:
        game_time = annotation["gameTime"]
        label = annotation["label"]
        position_ms = int(annotation["position"])  # position은 이미 밀리초로 제공됨

        # position을 사용하여 해당 프레임 번호 계산
        frame_index = position_to_frame(position_ms, video_path)

        # 해당 프레임을 로드
        frame = load_frame(video_path, frame_index)
        if frame is None:
            print(f"Failed to load frame at index {frame_index}")  # 프레임 로드 실패 확인
        else:
            frames.append(frame)
            labels.append(label)  # 레이블 리스트에 추가
            total_frame_index1 = total_frame_index1 + 1
            # 경로와 레이블 저장
            frame_path = os.path.join(output_dir, f"frame_{total_frame_index1}.jpg")
            print(f"frame_path : {frame_path}")
            frame_paths.append(frame_path)
            frame_labels.append((frame_path, frame_index, class_mapping[label]))  # 경로, 프레임 번호, 클래스 인덱스 저장

    # transform이 있다면, 프레임에 변환 적용
    if transform:
        frames = [transform(frame) for frame in frames]

    # 세그먼트 샘플링
    segment_length = len(frames) // num_segments
    segments = [frames[i * segment_length : (i + 1) * segment_length] for i in range(num_segments)]

    # 저장할 폴더 생성 (파일이 존재하지 않으면)
    if output_dir and not os.path.exists(output_dir):
        os.makedirs(output_dir)



    for i, segment in enumerate(segments):
        segment_dir = os.path.join(output_dir, f"segment_{i+1}")
        os.makedirs(segment_dir, exist_ok=True)  # 세그먼트 폴더 생성

        for j, frame in enumerate(segment):
            # frame_index는 해당 프레임의 정확한 번호를 추적
            frame_index = total_frame_index + j

            # 각 프레임을 저장하는 경로 설정
            frame_filename = os.path.join(segment_dir, f"frame_{frame_index}.jpg")
            frame_pil = transforms.ToPILImage()(frame)  # Tensor -> PIL Image로 변환
            frame_pil.save(frame_filename)

            # frame_labels에 (frame_path, frame_index, class_idx) 추가
            frame_labels.append((frame_filename, frame_index, class_mapping[label]))

        # 전체 프레임 번호 업데이트
        total_frame_index += len(segment)

    # 레이블 파일 저장: 프레임 경로, 프레임 번호, 클래스 인덱스를 기록
    label_file_path = os.path.join(output_dir, "labels.txt")
    with open(label_file_path, 'w', encoding='utf-8') as label_file:
        for frame_path, frame_index, class_idx in frame_labels:
            # frame_path는 실제 경로를 저장하고, frame_index와 class_idx는 classes.txt에서 저장된 ID를 사용
            label_file.write(f"{frame_path} {class_idx}\n")

    # 클래스 매핑 저장
    class_file_path = os.path.join(output_dir, "classes.txt")
    with open(class_file_path, 'w', encoding='utf-8') as class_file:
        for label, idx in class_mapping.items():
            # idx는 classes.txt에서 사용할 ID
            class_file.write(f"{idx} {label}\n")

    return segments, frame_labels



# 여러 경기에 대해 전처리 실행
transform = transforms.Compose([
    transforms.Resize((224, 224)),  # 이미지 크기 조정 (필요에 따라 수정)
    transforms.ToTensor()           # 이미지를 텐서로 변환
])

for game in games:
    video_path, json_path = get_video_and_json_paths(league, year, game)

    if os.path.exists(video_path) and os.path.exists(json_path):
        # 저장할 경로 설정
        output_dir = os.path.join("C:\\work_oneteam\\학습데이터 최종\\상황데이터", league, year, game)

        segments, frame_labels = preprocess_data(video_path, json_path, num_segments=3, new_length=1, transform=transform, output_dir=output_dir)

        # 결과 출력
        print(f"게임: {game}")
        print("Processed segments:", len(segments))
        print("Labels:", frame_labels)
    else:
        print(f"경기 '{game}'에 대한 비디오 또는 JSON 파일을 찾을 수 없습니다.")
