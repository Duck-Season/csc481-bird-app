# CSC481 Bird App
##### *by Team Duck Season*
###### *(name is a work in progress)*

An Android app for identifying various species of North American birds using object detection. Intended for educational, non-commercial purposes, such as while birdwatching, hiking, or even while walking around your local park.

This app is part of our capstone project for La Salle University's [CSC481/Project Implementation course](https://catalog.lasalle.edu/undergraduate/courses-az/csc/).

## Features
* Take pictures of birds with your camera!
* Upload pictures from your photo gallery!
* Use machine learning to scan for 200 different species of North American birds!
    * Supported bird species can be found [here](https://github.com/Duck-Season/csc481-bird-app/blob/aidan/app/src/main/assets/labels.txt). Our current list mostly focuses on songbirds, though this is subject to change in the future.
* Save scanned pictures for review!
    * Saves the location of the scan based on geolocational/EXIF data.

## Future Plans
* Extended camera features, i.e. front/rear flipping, tap-to-focus, zoom in/out, etc.
* Save file management features, i.e. deleting scans, adding scans to named folders, etc.
* Filtering scans for certain species the user is looking for,
* Sharing scans to Social Media (e.g. Twitter, Facebook, etc.) to show off the birds you found,
* Linking scan results to external articles (e.g. eBird, Inaturalist, etc.) for more information.

## How to Download

Downloads with instructions for the latest version of the app can be found in the Releases section of this repo, which you can also click to [here](https://github.com/Duck-Season/csc481-bird-app/releases). The app requires a device that at least runs Android 7.0, though consistent testing has only been done with an Android 13 phone.

## Developers
* Aidan Hurin
* Dastan Utegenov
* Ricco Little

## Credits
* [Ultralytics YOLO11](https://docs.ultralytics.com/models/yolo11/): object detection model used for processing images.
* [Caltech-UCSD Birds-200-2011](https://www.vision.caltech.edu/datasets/cub_200_2011/): image dataset used for training the YOLOv11 model.
    * __The dataset is not included in the repository__, but the model currently is.
* [Google Colaboratory](https://developers.google.com/colab): for preparing the dataset and training the model.
