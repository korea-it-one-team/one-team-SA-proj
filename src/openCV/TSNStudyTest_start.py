import sys
sys.path.append('C:/work_oneteam/one-team-SA-proj/TSM_model')
import os
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
from torchvision import transforms
from torchvision.models import resnet101, ResNet101_Weights
from ops.models import TSN  # TSM 모델
from ops.dataset import TSNDataSet  # 비디오 데이터셋
from sklearn.metrics import precision_score, recall_score

# 기본 경로 설정
base_dir = r'C:\work_oneteam\학습데이터최종\상황데이터\england_efl\2019-2020\BlackburnRovers_NottinghamForest'

# 클래스 파일 로딩
classes_file = os.path.join(base_dir, 'classes.txt')  # 클래스 파일 경로 설정
try:
    with open(classes_file, 'r') as f:
        classes = [line.strip() for line in f.readlines()]
except FileNotFoundError:
    raise Exception(f"클래스 파일 {classes_file}을(를) 찾을 수 없습니다.")

num_classes = len(classes)  # 클래스 수

# 데이터 전처리 설정
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

# 데이터셋 로딩
train_list_file = os.path.join(base_dir, 'lable.txt')  # 레이블 파일 경로 설정
try:
    train_dataset = TSNDataSet(
        root_path=base_dir,
        list_file=train_list_file,
        num_segments=3,
        new_length=1,
        modality='RGB',
        transform=transform
    )
except FileNotFoundError:
    raise Exception(f"레이블 파일 {train_list_file}을(를) 찾을 수 없습니다.")

# 데이터로더 설정
# DataLoader에서 batch_size가 8로 설정된 예시
train_loader = DataLoader(train_dataset,  shuffle=True, num_workers=4,batch_size=8)

# 모델 초기화 (ResNet101 백본 사용)
backbone_weights = ResNet101_Weights.DEFAULT
# 모델 초기화 (ResNet101 백본 사용)
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')  # GPU/CPU 설정
model = TSN(
    num_class=num_classes,
    num_segments=1,
    modality='RGB',
    base_model='resnet101',
    new_length=1,
    consensus_type='avg',
    dropout=0.5,
    img_feature_dim=256  # 특징 차원 크기 (예시값)
).to(device)

# 모델에 device 속성 추가
model.device = device

# 학습 루프에서 참조
if __name__ == '__main__':
    # 손실 함수와 옵티마이저 설정
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=0.001)

    # 모델 학습 루프
    num_epochs = 10
    for epoch in range(num_epochs):
        model.train()
        running_loss = 0.0
        all_labels = []
        all_preds = []

        for inputs, labels in train_loader:
            inputs, labels = inputs.to(model.device), labels.to(model.device)

            optimizer.zero_grad()
            outputs = model(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()

            running_loss += loss.item()

            # 예측값과 실제값 저장
            _, predicted = torch.max(outputs, 1)
            all_labels.extend(labels.cpu().numpy())
            all_preds.extend(predicted.cpu().numpy())

        # 정확도, Precision, Recall 계산
        train_accuracy = (torch.tensor(all_preds) == torch.tensor(all_labels)).float().mean().item()
        train_precision = precision_score(all_labels, all_preds, average='weighted')
        train_recall = recall_score(all_labels, all_preds, average='weighted')


        avg_loss = running_loss / len(train_loader)
        print(f"Epoch {epoch+1}, Train Loss: {avg_loss:.4f}, Accuracy: {train_accuracy:.4f}, Precision: {train_precision:.4f}, Recall: {train_recall:.4f}")

    # 모델 저장
    output_dir = os.path.join(base_dir, 'saved_models')
    os.makedirs(output_dir, exist_ok=True)
    torch.save(model, os.path.join(output_dir, 'tsn_model_full.pth'))
    torch.save(model.state_dict(), os.path.join(output_dir, 'tsn_model_state_dict.pth'))

    print(f"모델이 {output_dir}에 성공적으로 저장되었습니다.")
