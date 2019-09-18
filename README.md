# LabData
Project consists of small API/Library for dealing with lab data.

PlateData is first installment. Meant to be used with standard laboratory plate reader. Sample input excel file is called "template.xlxs"
Super class PlateData has support for reading in raw absorbance values into a 2D array for further downstream processing.

BCADAta is meant to integrate the plate data into a standard BCA asssay. Protein samples with known concentratino are loaded into first 2
or 3 columns depending on experimental setup. Unknown samples are loaded into rest of wells in duplicate or triplicate. Program is designed
to use line of best fit on known protein concentrations and compare to unknown samples.

BCAData is based off Perice BCA Protein Assay kit.

CTGData is class in the making which will support data anysis for cll titer glow assay.

Goal is to expand beyond just plate data and integrate with graphpad for graph construction. Also working on GUi and excel output methods. 
