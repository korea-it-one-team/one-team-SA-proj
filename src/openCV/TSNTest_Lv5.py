
import sys
sys.path.append('C:/work_oneteam/one-team-SA-proj/TSM_model')
import torch.backends.cudnn as cudnn
import torch
import torch.nn.functional as F
from torchvision import transforms
from ops.transforms import IdentityTransform
from ops.transforms import GroupNormalize
import torch.optim
from PIL import Image
import json
import os
import cv2
from ops.models import TSN  # 모델 클래스를 적절히 불러와야 합니다.
from ops import temporal_shift
import opts
from ops import dataset_config
import re

from tensorboardX import SummaryWriter

# 1. 동영상에서 프레임 추출 함수 (1초마다 1프레임만 추출)
# def extract_frames(video_path, output_folder, frame_rate=1):
#     """
#     동영상에서 1초마다 1프레임을 추출합니다.
#     :param video_path: 동영상 파일 경로
#     :param output_folder: 프레임 저장 폴더
#     :param frame_rate: 초당 추출할 프레임 수 (기본 1이면 초당 1프레임)
#     """
#     os.makedirs(output_folder, exist_ok=True)
#     cap = cv2.VideoCapture(video_path)
#
#     # 비디오 정보 얻기
#     total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))  # 전체 프레임 수
#     fps = cap.get(cv2.CAP_PROP_FPS)  # 동영상의 실제 FPS
#
#     print(f"Total frames: {total_frames}, FPS: {fps}")
#
#     frame_idx = 0  # 추출할 프레임의 인덱스
#
#     # 초당 1프레임씩 추출하려면 `frame_rate`만큼 건너뛰며 프레임을 추출
#     interval = int(fps // frame_rate)  # 간격을 FPS / 1로 설정하여 초당 1프레임 추출
#
#     print(f"Interval between frames: {interval} frames")
#
#     while cap.isOpened():
#         ret, frame = cap.read()
#         if not ret:
#             break
#
#         if frame_idx % interval == 0:  # 초당 1프레임씩 추출
#             frame_path = os.path.join(output_folder, f"frame_{frame_idx:06d}.jpg")
#             cv2.imwrite(frame_path, frame)  # 프레임 저장
#
#         frame_idx += 1
#
#     cap.release()
#     print(f"Frames extracted to {output_folder}")


# 2. 모델 로드 함수 (모델 전체 로딩)
def load_model(model_path, device):
    """
    저장된 모델을 로드합니다.
    :param model_path: 모델 저장 경로
    :param device: 모델이 로드될 디바이스 (CPU/GPU)
    """

    checkpoint = torch.load(model_path)
    model.load_state_dict(checkpoint['state_dict'])

    # state_dict = torch.load(model_path)  # state_dict만 로드
    # model.load_state_dict(state_dict)  # 모델에 가중치 적용
    model.to(device)
    model.eval()  # 평가 모드로 전환
    return model


# 3. 프레임 예측 함수
def predict_frame(image_path, model, device, classes, confidence_threshold=0.8):
    """
    단일 프레임 예측.
    :param image_path: 이미지 파일 경로
    :param model: 학습된 모델
    :param device: 모델이 로드된 디바이스
    :param classes: 클래스 리스트
    :param confidence_threshold: 확률 기준 (기본값 50%)
    """
    transform = transforms.Compose([
        transforms.Resize((224, 224)),  # 모델이 요구하는 입력 크기로 수정
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
    image = Image.open(image_path).convert("RGB")
    input_tensor = transform(image).unsqueeze(0).to(device)

    with torch.no_grad():
        outputs = model(input_tensor)
        probabilities = F.softmax(outputs, dim=1)  # Softmax를 통해 각 클래스의 확률을 계산
        max_prob, predicted = torch.max(probabilities, 1)  # 가장 높은 확률과 해당 클래스 인덱스

    predicted_event = classes[predicted.item()]

    # 확률이 기준값 미만인 경우 예측을 무시
    if max_prob.item() < confidence_threshold:
        return None

    return predicted_event


# 4. 전체 처리 파이프라인
def process_video(video_path, model_path, classes_path, output_folder, predictions_file, model):
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    # 모델과 클래스 로드
    with open(classes_path, "r") as f:
        class_list = f.read().splitlines()


    # 프레임 추출
    # extract_frames(video_path, output_folder, frame_rate)

    predictions = []
    # 숫자로 끝나는 파일만 필터링
    frames = [
        frame for frame in os.listdir(output_folder)
        if re.match(r'^frame_\d+\.jpg$', frame)
    ]

    # 숫자를 기준으로 정렬
    for frame in sorted(frames, key=lambda x: int(x.split('_')[1].split('.')[0])):
        frame_path = os.path.join(output_folder, frame)

        # 예측 수행
        prediction = predict_frame(frame_path, model, device, class_list)

        if prediction is not None:
            predictions.append({
                "frame": frame,
                "prediction": prediction
            })

        print(f"Frame {frame} => Prediction: {prediction if prediction else 'None'}")

    # 결과 저장
    with open(predictions_file, "w") as f:
        json.dump(predictions, f, indent=4)  # 보기 좋게 결과 저장
    print(f"Predictions saved to {predictions_file}")


# 실행
if __name__ == "__main__":
    global args, best_prec1
    args = opts.parser.parse_args()

    num_class, args.train_list, args.val_list, args.root_path, prefix = dataset_config.return_dataset(args.dataset,
                                                                                                      args.modality)
    full_arch_name = args.arch
    if args.shift:
        full_arch_name += '_shift{}_{}'.format(args.shift_div, args.shift_place)
    if args.temporal_pool:
        full_arch_name += '_tpool'
    args.store_name = '_'.join(
        ['TSM', args.dataset, args.modality, full_arch_name, args.consensus_type,
         'e{}'.format(args.epochs)])
    if args.pretrain != 'imagenet':
        args.store_name += '_{}'.format(args.pretrain)
    if args.lr_type != 'step':
        args.store_name += '_{}'.format(args.lr_type)
    if args.dense_sample:
        args.store_name += '_dense'
    if args.non_local > 0:
        args.store_name += '_nl'
    if args.suffix is not None:
        args.store_name += '_{}'.format(args.suffix)
    print('storing name: ' + args.store_name)

    model = TSN(num_class, args.num_segments, args.modality,
                base_model=args.arch,
                consensus_type=args.consensus_type,
                dropout=args.dropout,
                img_feature_dim=args.img_feature_dim,
                partial_bn=not args.no_partialbn,
                pretrain=args.pretrain,
                is_shift=args.shift, shift_div=args.shift_div, shift_place=args.shift_place,
                fc_lr5=not (args.tune_from and args.dataset in args.tune_from),
                temporal_pool=args.temporal_pool,
                non_local=args.non_local)

    crop_size = model.crop_size
    scale_size = model.scale_size
    input_mean = model.input_mean
    input_std = model.input_std
    policies = model.get_optim_policies()
    train_augmentation = model.get_augmentation(flip=False if 'something' in args.dataset or 'jester' in args.dataset else True)

    # 장치 설정: GPU가 가능하면 CUDA, 아니면 CPU 사용
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    # 모델을 장치로 이동
    model = model.to(device)

    optimizer = torch.optim.SGD(policies,
                                args.lr,
                                momentum=args.momentum,
                                weight_decay=args.weight_decay)

    if args.resume:
        if args.temporal_pool:  # early temporal pool so that we can load the state_dict
            temporal_shift.make_temporal_pool(model.base_model, args.num_segments)
        if os.path.isfile(args.resume):
            print(("=> loading checkpoint '{}'".format(args.resume)))
            checkpoint = torch.load(args.resume)
            args.start_epoch = checkpoint['epoch']
            best_prec1 = checkpoint['best_prec1']
            model.load_state_dict(checkpoint['state_dict'])
            optimizer.load_state_dict(checkpoint['optimizer'])
            print(("=> loaded checkpoint '{}' (epoch {})"
                   .format(args.evaluate, checkpoint['epoch'])))
        else:
            print(("=> no checkpoint found at '{}'".format(args.resume)))

    if args.tune_from:
        print(("=> fine-tuning from '{}'".format(args.tune_from)))
        sd = torch.load(args.tune_from)
        sd = sd['state_dict']
        model_dict = model.state_dict()
        replace_dict = []
        for k, v in sd.items():
            if k not in model_dict and k.replace('.net', '') in model_dict:
                print('=> Load after remove .net: ', k)
                replace_dict.append((k, k.replace('.net', '')))
        for k, v in model_dict.items():
            if k not in sd and k.replace('.net', '') in sd:
                print('=> Load after adding .net: ', k)
                replace_dict.append((k.replace('.net', ''), k))

        for k, k_new in replace_dict:
            sd[k_new] = sd.pop(k)
        keys1 = set(list(sd.keys()))
        keys2 = set(list(model_dict.keys()))
        set_diff = (keys1 - keys2) | (keys2 - keys1)
        print('#### Notice: keys that failed to load: {}'.format(set_diff))
        if args.dataset not in args.tune_from:  # new dataset
            print('=> New dataset, do not load fc weights')
            sd = {k: v for k, v in sd.items() if 'fc' not in k}
        if args.modality == 'Flow' and 'Flow' not in args.tune_from:
            sd = {k: v for k, v in sd.items() if 'conv1.weight' not in k}
        model_dict.update(sd)
        model.load_state_dict(model_dict)

    if args.temporal_pool and not args.resume:
        temporal_shift.make_temporal_pool(model.base_model, args.num_segments)

    cudnn.benchmark = True

    # Data loading code
    if args.modality != 'RGBDiff':
        normalize = GroupNormalize(input_mean, input_std)
    else:
        normalize = IdentityTransform()

    if args.modality == 'RGB':
        data_length = 1
    elif args.modality in ['Flow', 'RGBDiff']:
        data_length = 5

    video_path = "C:/work_oneteam/one-team-SA-proj/videos/article/2024_11/1_720p.mp4"
    model_path = "C:/work_oneteam/학습데이터최종/상황데이터/ckpt.best.pth.tar"
    classes_path = "C:/work_oneteam/학습데이터최종/상황데이터/classes.txt"
    output_folder = "C:/work_oneteam/one-team-SA-proj/videos/article/frames"
    predictions_file = "C:/work_oneteam/one-team-SA-proj/videos/article/predictions.json"
    frame_rate = 1

    model = load_model(model_path, device)  # 모델 로드

    process_video(video_path, model_path, classes_path, output_folder, predictions_file, model)
