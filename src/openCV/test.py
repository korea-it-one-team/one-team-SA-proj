from flask import Flask, request, send_file
import cv2 #openCV
import numpy as np
import io
from PIL import Image

app = Flask(__name__)

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

if __name__ == '__main__':
    app.run(debug=True, port=5000)