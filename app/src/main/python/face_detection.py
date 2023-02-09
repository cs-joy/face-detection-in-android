import numpy as np
import cv2
from PIL import Image
import base64
import io
import face_recognition

def main(img_string):
    decode_data = base64.b64decode(img_string)
    np_data = np.fromstring(decode_data, np.uint8)
    img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)
    rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    gray_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    face_locations = face_recognition.face_locations(gray_img)
    for (top, right, bottom, left) in face_locations:
        cv2.rectangle(rgb_img, (left, top), (right, bottom), (0, 0, 255), 8)
    pil_img = Image.fromarray(rgb_img)
    buffer = io.BytesIO();
    pil_img.save(buffer, format="PNG")
    image_str = base64.b64encode(buffer.getvalue())

    return "" + str(image_str, 'utf-8')