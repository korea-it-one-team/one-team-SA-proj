import os
from flask import Flask, request, send_file, jsonify
import cv2 #openCV
import numpy as np
import io
from PIL import Image

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

#욜로
def load_yolo_model():
    net = cv2.dnn.readNet('yolov3.weights', 'yolov3.cfg')
    layer_names = net.getLayerNames()
    output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
    return net, output_layers

def process_yolo_frame(frame, net, output_layers):
    height, width = frame.shape[:2]
    blob = cv2.dnn.blobFromImage(frame, 0.00392, (416, 416), (0, 0, 0), True, crop=False)
    net.setInput(blob)
    outputs = net.forward(output_layers)

    boxes = []
    confidences = []
    class_ids = []

    for output in outputs:
        for detection in output:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            if confidence > 0.5:  # 신뢰도 임계값
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * height)
                w = int(detection[2] * width)
                h = int(detection[3] * height)
                x = int(center_x - w / 2)
                y = int(center_y - h / 2)

                boxes.append([x, y, w, h])
                confidences.append(float(confidence))
                class_ids.append(class_id)

    return boxes, confidences, class_ids

#동영상 테스트
@app.route('/process_video', methods=['POST'])
def process_video():
    # 요청에서 동영상 파일을 받음
    file = request.files['video']
    video_bytes = np.frombuffer(file.read(), np.uint8)
    video_path = "temp_video.mp4"

    # 동영상 파일을 저장
    with open(video_path, 'wb') as f:
        f.write(video_bytes)

    cap = cv2.VideoCapture(video_path)
    net, output_layers = load_yolo_model()

    # 저장할 동영상 설정
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')  # mp4 코덱
    out = cv2.VideoWriter('processed_video.mp4', fourcc, 20.0, (int(cap.get(3)), int(cap.get(4))))

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        # YOLO를 통해 객체 감지
        boxes, confidences, class_ids = process_yolo_frame(frame, net, output_layers)

        # 공에 해당하는 class_id만 필터링하여 박스 그리기
        for i in range(len(boxes)):
            if class_ids[i] == 0:  # YOLO에서 공을 의미하는 class_id로 변경 필요
                x, y, w, h = boxes[i]
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)  # 초록색 박스

        # 결과를 새로운 비디오 파일에 저장
        out.write(frame)

    cap.release()
    out.release()

    # 변환된 동영상을 클라이언트로 반환
    return send_file('processed_video.mp4', as_attachment=True)

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