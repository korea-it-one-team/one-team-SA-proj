import os
import json
import subprocess

# 기본 경로 설정
base_dir = r"C:\work_oneteam\one-team-SA-proj\SoccerNet"
output_dir = r"C:\work_oneteam\one-team-SA-proj\TSN_preprocessed"
frame_interval = 1  # 1초 간격으로 프레임 추출
frame_rate = 30  # 초당 30프레임

train_list_file = os.path.join(output_dir, "train_list.txt")
classes_file = os.path.join(output_dir, "classes.txt")

label_map = {}
current_label_id = 0

def generate_frames(video_path, output_dir):
    """
    Generate frames from video.
    """
    os.makedirs(output_dir, exist_ok=True)
    ffmpeg_path = r"C:\ffmpeg-7.1-essentials_build\bin\ffmpeg.exe"

    try:
        # FFmpeg 명령어 실행
        subprocess.run([
            ffmpeg_path,
            "-i", video_path,
            "-vf", f"fps=1/{frame_interval}",
            os.path.join(output_dir, "frame_%04d.jpg")
        ], check=True)
    except subprocess.CalledProcessError as e:
        print(f"Error extracting frames from {video_path}: {e}")

def process_labels_and_generate_train_list():
    """
    Process Labels-v2.json and Labels-cameras.json, generate train_list.txt, and create classes.txt.
    """
    global current_label_id

    os.makedirs(output_dir, exist_ok=True)
    total_annotations = 0
    skipped_annotations = 0

    with open(train_list_file, "w") as train_list:
        # 모든 경기 폴더 탐색 (재귀적으로)
        for root, dirs, files in os.walk(base_dir):
            for file in files:
                # Labels-v2.json 및 Labels-cameras.json 파일 확인
                if file in ["Labels-v2.json", "Labels-cameras.json"]:
                    label_file = os.path.join(root, file)

                    # 비디오 파일 확인
                    video_files = [os.path.join(root, f"1_224p.mkv"), os.path.join(root, f"2_224p.mkv")]
                    available_videos = [video for video in video_files if os.path.exists(video)]

                    if not available_videos:
                        print(f"No videos found in {root}, skipping...")
                        continue

                    # 라벨 파일 읽기
                    with open(label_file, "r") as f:
                        labels = json.load(f)

                    # 비디오 처리
                    for video_path in available_videos:
                        # 프레임 디렉토리 설정
                        match_folder = os.path.relpath(root, base_dir)  # 상대 경로로 경기 폴더 얻기
                        video_frames_dir = os.path.join(output_dir, match_folder, os.path.basename(video_path).split(".")[0] + "_frames")
                        generate_frames(video_path, video_frames_dir)

                        # 라벨-프레임 매핑
                        for annotation in labels.get("annotations", []):
                            total_annotations += 1
                            label = annotation["label"]

                            try:
                                position_ms = int(annotation["position"])  # 밀리초 값을 정수로 변환
                                position_s = position_ms / 1000.0  # 초 단위로 변환
                            except (ValueError, TypeError):
                                print(f"Invalid position value in {label_file}: {annotation['position']}")
                                skipped_annotations += 1
                                continue

                            frame_number = int(position_s / frame_interval)  # 프레임 번호 계산
                            frame_name = f"frame_{frame_number:04d}.jpg"
                            frame_path = os.path.join(video_frames_dir, frame_name)

                            if os.path.exists(frame_path):
                                # 클래스 ID 생성 및 매핑
                                if label not in label_map:
                                    label_map[label] = current_label_id
                                    current_label_id += 1

                                train_list.write(f"{frame_path} {label_map[label]}\n")
                            else:
                                skipped_annotations += 1
                                print(f"Skipped: {frame_path} (Match: {match_folder}, Label: {label}, Position: {position_ms}ms)")

    # classes.txt 생성
    with open(classes_file, "w") as classes:
        for label, label_id in sorted(label_map.items(), key=lambda x: x[1]):
            classes.write(f"{label}\n")

    print(f"Total annotations: {total_annotations}")
    print(f"Skipped annotations: {skipped_annotations}")

if __name__ == "__main__":
    process_labels_and_generate_train_list()
    print(f"Classes and train_list.txt generated in {output_dir}")
