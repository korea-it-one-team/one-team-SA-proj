from flask import Flask, jsonify, request
import os
import torch
from torch.utils.data import DataLoader, Dataset
from torchvision import transforms, models
from torchvision.datasets.folder import default_loader
from torchvision.models import ResNet50_Weights
from sklearn.metrics import precision_score, recall_score
from torch.amp import GradScaler, autocast


# TSN 데이터셋 정의
class TSNDataset(Dataset):
    def __init__(self, train_list, classes_file, transform=None):
        self.data = []
        self.labels = []
        self.transform = transform
        self.label_map = self.load_classes(classes_file)

        with open(train_list, "r") as f:
            for line_number, line in enumerate(f, start=1):
                try:
                    path, label_id = line.strip().rsplit(" ", 1)
                    if label_id not in self.label_map:
                        print(f"Unknown label ID '{label_id}' at line {line_number}, skipping...")
                        continue
                    label = int(label_id)
                    if os.path.exists(path):
                        self.data.append(path)
                        self.labels.append(label)
                    else:
                        print(f"Frame not found: {path}, skipping...")
                except ValueError as e:
                    print(f"Invalid format at line {line_number}: {line.strip()} - Error: {e}")

        if len(self.data) == 0:
            raise ValueError("No valid data found in train_list.txt")

    def load_classes(self, classes_file):
        label_map = {}
        try:
            with open(classes_file, "r") as f:
                for idx, line in enumerate(f):
                    label_map[str(idx)] = line.strip()  # 숫자 ID와 클래스 이름 동기화
        except FileNotFoundError:
            raise ValueError(f"Classes file not found: {classes_file}")
        return label_map

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        try:
            image = default_loader(self.data[idx])
            label = self.labels[idx]

            if self.transform:
                image = self.transform(image)

            return image, label
        except Exception as e:
            print(f"Error loading frame {self.data[idx]}: {e}")
            return None

# Flask 앱 초기화
app = Flask(__name__)

# 학습 API
@app.route("/train_tsn", methods=["POST"])
def train_tsn():
    train_list_path = request.form.get("train_list", "C:/work_oneteam/one-team-SA-proj/TSN_preprocessed/train_list.txt")
    classes_file = request.form.get("classes_file", "C:/work_oneteam/one-team-SA-proj/TSN_preprocessed/classes.txt")
    num_epochs = int(request.form.get("num_epochs", 10))
    batch_size = int(request.form.get("batch_size", 16))
    learning_rate = float(request.form.get("learning_rate", 0.001))
    model_save_path = request.form.get("model_save_path", "tsn_model.pth")

    # 데이터셋 준비
    transform = transforms.Compose([
        transforms.Resize((224, 224)),  # 이미지 크기 조정
        transforms.ToTensor(),  # Tensor로 변환
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])  # 정규화
    ])

    train_dataset = TSNDataset(train_list_path, classes_file, transform=transform)

    # 라벨 값 확인
    n_classes = len(set(train_dataset.labels))
    print(f"Number of classes: {n_classes}")
    print(f"Labels in dataset: {set(train_dataset.labels)}")

    # 모델 정의
    model = models.resnet50(weights=ResNet50_Weights.DEFAULT)
    model.fc = torch.nn.Linear(model.fc.in_features, n_classes)

    # 손실 함수 및 옵티마이저
    criterion = torch.nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate)

    # Mixed Precision 학습 도구 초기화
    scaler = GradScaler()

    # 학습 루프
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model.to(device)

    for epoch in range(num_epochs):
        model.train()
        running_loss = 0.0
        all_labels = []
        all_predictions = []

        for batch in DataLoader(train_dataset, batch_size=batch_size, shuffle=True, num_workers=8, pin_memory=True):
            if batch is None or any(item is None for item in batch):
                print(f"Skipping invalid batch: {batch}")
                continue

            images, labels = batch
            try:
                images, labels = images.to(device, non_blocking=True), labels.to(device, non_blocking=True)
            except Exception as e:
                print(f"Batch processing error: {e}, batch content: {batch}")
                continue

            optimizer.zero_grad()

            # Mixed Precision Forward
            with autocast(device_type="cuda"):  # device_type 명시적으로 추가
                outputs = model(images)
                loss = criterion(outputs, labels)

            scaler.scale(loss).backward()
            scaler.step(optimizer)
            scaler.update()

            running_loss += loss.item()

            # Accuracy 및 Precision/Recall 계산
            _, preds = torch.max(outputs, 1)
            all_labels.extend(labels.cpu().numpy())
            all_predictions.extend(preds.cpu().numpy())

        # Epoch 별 Accuracy, Precision, Recall 계산
        if len(all_labels) > 0:  # 데이터가 있는 경우에만 계산
            accuracy = sum(p == l for p, l in zip(all_predictions, all_labels)) / len(all_labels)
            precision = precision_score(all_labels, all_predictions, average="macro", zero_division=0)
            recall = recall_score(all_labels, all_predictions, average="macro", zero_division=0)

            print(f"Epoch {epoch + 1}/{num_epochs}, Loss: {running_loss / len(train_dataset):.4f}, "
                  f"Accuracy: {accuracy:.4f}, Precision: {precision:.4f}, Recall: {recall:.4f}")
        else:
            print(f"Epoch {epoch + 1}/{num_epochs}, No valid predictions made.")


    # 모델 저장
    torch.save(model, model_save_path)
    print(f"Model training complete and saved to {model_save_path}")

    return jsonify({"message": "Model training complete", "model_path": model_save_path})

# 메인 실행
if __name__ == "__main__":
    app.run(debug=True)