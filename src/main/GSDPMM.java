package main;
import java.util.HashMap;

public class GSDPMM
{
	int K;
	double alpha;
	double beta;
	int iterNum;
	String dataset;
	
	HashMap<String, Integer> wordToIdMap;
	int V; // vocabular size
	int D; // number of documents
	DocumentSet documentSet;
	String dataDir = "data/"; 
	String outputPath = "result/";
	
	public GSDPMM(int K, double alpha, double beta, int iterNum, String dataset)
	{
		this.K = K;
		this.alpha = alpha;
		this.beta = beta;
		this.iterNum = iterNum;
		this.dataset = dataset;
		this.wordToIdMap = new HashMap<String, Integer>();
	}
	public static void main(String args[]) throws Exception
	{
		int K = 1; //Default K = 1
		double alpha = 0.003;
		double beta = 0.02;
		int iterNum = 100;
		String dataset = "Tweet";
		GSDPMM gsdpmm = new GSDPMM(K, alpha, beta, iterNum, dataset);
		
		long startTime = System.currentTimeMillis();				
		gsdpmm.getDocuments();
		long endTime = System.currentTimeMillis();
		System.out.println("getDocuments Time Used:" + (endTime-startTime)/1000.0 + "s");
		
		startTime = System.currentTimeMillis();	
		gsdpmm.runGSDMM();
		endTime = System.currentTimeMillis();
		System.out.println("gibbsSampling Time Used:" + (endTime-startTime)/1000.0 + "s");
	}
	
	public void getDocuments() throws Exception
	{
		documentSet = new DocumentSet(dataDir + dataset, wordToIdMap);
		D=documentSet.D; //number of documents in the dataset
		V = wordToIdMap.size();
		System.out.println("number of word in the vocabulary : "+V);
	}
	
	public void runGSDMM() throws Exception
	{
		String ParametersStr = "K"+K+"iterNum"+ iterNum +"alpha" + String.format("%.3f", alpha)
								+ "beta" + String.format("%.3f", beta);
		Model model = new Model(K, V, D, iterNum,alpha, beta, dataset,  ParametersStr);
		model.intialize(documentSet);
		model.gibbsSampling(documentSet);
		model.output(documentSet, outputPath);
	}
}
