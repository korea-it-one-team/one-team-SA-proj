import os
from flask import Flask, request, send_file, jsonify
import cv2 #openCV
import numpy as np
import io
from PIL import Image
from ultralytics import YOLO
import threading

app = Flask(__name__)

#이미지 테스트
@app.route('/process', methods=['POST'])
def process_image():
    # 요청에서 이미지를 받음
    file = request.files['image']
    np_img = np.frombuffer(file.read(), np.uint8)
    img = cv2.imdecode(np_img, cv2.IMREAD_COLOR)

    # OpenCV로 이미지를 흑백으로 변환
    gray_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # 변환된 이미지를 메모리에서 파일로 변환 (PIL로 변환 후 메모리로 저장)
    pil_img = Image.fromarray(gray_img)
    img_io = io.BytesIO()
    pil_img.save(img_io, 'JPEG')
    img_io.seek(0)

    # 흑백 이미지를 반환
    return send_file(img_io, mimetype='image/jpeg')

# 글로벌 상태 변수
processing_status = {
    "status": "idle",  # 처리 상태: idle, processing, completed
    "progress": 0      # 진행률: 0 ~ 100
}

# YOLOv8 모델 로드
model = YOLO('yolov8s.pt')

def update_status(video_path):
    global processing_status

    cap = cv2.VideoCapture(video_path)

    # 저장할 동영상 경로 (Java static 폴더 경로로 변경)
    save_path = os.path.join("C:/work_oneteam/one-team-SA-proj/src/main/resources/static/video", "processed_video.mp4")
    fourcc = cv2.VideoWriter_fourcc(*'avc1')  # 코덱
    out = cv2.VideoWriter(save_path, fourcc, 20.0, (int(cap.get(3)), int(cap.get(4))))

    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    processed_frames = 0

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        # YOLOv8을 사용해 객체 감지
        results = model(frame)

        # 감지된 객체 중 공(class_id 32)에 해당하는 객체에 박스 그리기
        for result in results:
            for box in result.boxes:
                if box.cls == 32:  # YOLOv8에서 축구공 class_id: 32 (COCO 데이터셋 기준)
                    x1, y1, x2, y2 = map(int, box.xyxy[0])  # 바운딩 박스 좌표
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)  # 초록색 박스

        # 결과를 새로운 비디오 파일에 저장
        out.write(frame)

        # 진행률 업데이트
        processed_frames += 1
        processing_status['progress'] = int((processed_frames / total_frames) * 100)

    cap.release()
    out.release()

    # 상태 완료로 설정
    processing_status['status'] = "completed"

    # 원본 비디오 파일 삭제 (복사해 둔 temp_video 삭제)
    if os.path.exists(video_path):
        os.remove(video_path)
        print(f"원본 비디오 파일 {video_path} 삭제 완료.")

@app.route('/download_video', methods=['GET'])
def download_video():
    # Java 프로젝트의 static 폴더에 저장된 동영상 경로로 수정
    video_path = os.path.join("C:/work_oneteam/one-team-SA-proj/src/main/resources/static/video", "processed_video.mp4")

    # 동영상 파일을 다운로드하여 클라이언트로 전송
    return send_file(video_path, as_attachment=True)

@app.route('/process_video', methods=['POST'])
def process_video():
    # 요청에서 동영상 파일을 받음
    file = request.files['video']
    video_bytes = np.frombuffer(file.read(), np.uint8)
    video_path = "temp_video.mp4"

    # 동영상 파일을 저장
    with open(video_path, 'wb') as f:
        f.write(video_bytes)

    # 동영상 처리 시작
    global processing_status
    processing_status['status'] = "processing"
    processing_status['progress'] = 0

    # 동영상 처리 비동기 스레드에서 실행
    threading.Thread(target=update_status, args=(video_path,)).start()

    return jsonify({"message": "동영상 처리를 시작했습니다."})

@app.route('/status', methods=['GET'])
def get_status():
    # 현재 처리 상태 반환
    return jsonify(processing_status)

@app.route('/video-status', methods=['GET'])
def video_status():
    global processing_status
    return jsonify(processing_status)

@app.route('/health', methods=['GET'])
def health_check():
    return "Flask 서버가 실행 중입니다.", 200

@app.route('/shutdown', methods=['GET'])
def shutdown():
    shutdown_server = request.environ.get('werkzeug.server.shutdown')
    if shutdown_server is None:
        print("Werkzeug 서버가 아니므로 프로세스를 강제 종료합니다.")
        os._exit(0)  # Python 프로세스 종료
    else:
        shutdown_server()
        return 'Flask 서버가 종료되었습니다.'

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)