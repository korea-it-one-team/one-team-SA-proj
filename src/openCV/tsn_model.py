# tsn_model.py

class TSN:
    def __init__(self, model_weights_path):
        # TSN 모델 초기화
        self.model = self.load_model(model_weights_path)

    def load_model(self, model_weights_path):
        # 모델 로딩 코드 (예: PyTorch 사용)
        pass

    def train(self, image):
        # 모델 학습 코드
        pass
