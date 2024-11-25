import os

import torch
from torchvision import models, transforms
from torchvision.models import ResNet50_Weights
from PIL import Image

def load_classes(classes_file):
    """
    Load class labels from a file.
    """
    if not os.path.exists(classes_file):
        raise FileNotFoundError(f"Classes file not found: {classes_file}")
    with open(classes_file, "r") as f:
        classes = [line.strip() for line in f.readlines()]
    return classes

def load_model(model_path, num_classes):
    """
    Load the trained model with the specified number of classes.
    """
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"Model file not found: {model_path}")

    # Load ResNet50 and replace the final layer with the trained number of classes
    model = models.resnet50(weights=ResNet50_Weights.DEFAULT)
    model.fc = torch.nn.Linear(model.fc.in_features, num_classes)

    # Load model weights
    model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))
    model.eval()  # Set the model to evaluation mode
    return model

def predict(model, image_path, transform, classes):
    """
    Perform prediction on a single image.
    """
    if not os.path.exists(image_path):
        raise FileNotFoundError(f"Image file not found: {image_path}")

    image = Image.open(image_path).convert('RGB')  # Open and convert image to RGB
    input_tensor = transform(image).unsqueeze(0)  # Add batch dimension

    with torch.no_grad():
        outputs = model(input_tensor)
        _, predicted = torch.max(outputs, 1)

    predicted_label = classes[predicted.item()]
    return predicted_label

if __name__ == "__main__":
    # User inputs for file paths
    model_path = input("Enter the path to the model file (e.g., tsn_model.pth): ")
    classes_file = input("Enter the path to the classes file (e.g., classes.txt): ")
    image_path = input("Enter the path to the image file for prediction: ")

    try:
        # Load classes and model
        classes = load_classes(classes_file)
        model = load_model(model_path, len(classes))

        # Define transformations
        transform = transforms.Compose([
            transforms.Resize((128, 128)),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        ])

        # Predict the label
        predicted_label = predict(model, image_path, transform, classes)
        print(f"Predicted Label: {predicted_label}")

    except Exception as e:
        print(f"Error: {e}")
