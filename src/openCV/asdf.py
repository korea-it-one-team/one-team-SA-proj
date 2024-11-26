import os
import json

# SoccerNet 디렉토리 경로
base_dir = "C:/work_oneteam/one-team-SA-proj/SoccerNet"

# 출력 파일 경로
output_classes_file = "C:/work_oneteam/classes.txt"

def extract_classes_from_all_json(base_path, output_path):
    try:
        # 고유 클래스 저장
        unique_classes = set()

        # 모든 Labels-v2.json 파일 탐색
        for root, dirs, files in os.walk(base_path):
            for file in files:
                if file == "Labels-v2.json":
                    json_path = os.path.join(root, file)
                    try:
                        with open(json_path, "r", encoding="utf-8") as f:
                            data = json.load(f)

                        # 클래스 이름 추출
                        for annotation in data.get("annotations", []):
                            label = annotation.get("label")
                            if label:
                                unique_classes.add(label)
                    except Exception as e:
                        print(f"Error reading {json_path}: {e}")

        # 클래스 정렬 및 저장
        sorted_classes = sorted(unique_classes)
        with open(output_path, "w", encoding="utf-8") as f:
            for cls in sorted_classes:
                f.write(cls + "\n")

        print(f"Classes successfully extracted to {output_path}")
        print(f"Total classes: {len(sorted_classes)}")
    except Exception as e:
        print(f"Error during class extraction: {e}")

# 실행
extract_classes_from_all_json(base_dir, output_classes_file)
