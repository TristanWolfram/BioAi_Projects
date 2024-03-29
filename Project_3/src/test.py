import cv2 as cv
import numpy as np

img = np.zeros(shape=(10, 10))

cv.imwrite("test.png", img)
