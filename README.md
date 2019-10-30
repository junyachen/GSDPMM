# GSDPMM
The datasets are in format of JSON like follows:   
   {"text": "centrepoint winter white gala london", "cluster": 65}   
   {"text": "mourinho seek killer instinct", "cluster": 96}   
   {"text": "roundup golden globe won seduced johansson voice", "cluster": 72}   
   {"text": "travel disruption mount storm cold air sweep south florida", "cluster": 140}   
   {"text": "wes welker blame costly turnover", "cluster": 89}   
         	......   
	   
The output of GSDPMM are D (the number of documents in the dataset) lines. Each line contains the estimated cluster for that document.

# Citation 

Please cite the following paper for the data usage:

@article{chen2019nonparametric, title={A nonparametric model for online topic discovery with word embeddings}, author={Chen, Junyang and Gong, Zhiguo and Liu, Weiwen}, journal={Information Sciences}, volume={504}, pages={32--47}, year={2019}, publisher={Elsevier} }
