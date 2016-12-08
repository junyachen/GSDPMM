package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;

public class Model{
	int K; 
	double alpha;
	double beta;
	String dataset;
	String ParametersStr;
	int V; 
	int D; 
	int iterNum; 
	int[] z;
	
	int[] m_z=null;
	int[][] n_zv=null;
	int[] n_z=null;

	public Model(int K, int V, int D, int iterNum, double alpha, double beta, 
			String dataset, String ParametersStr)
	{
		this.dataset = dataset;
		this.ParametersStr = ParametersStr;
		this.alpha = alpha*D;
		this.beta = beta;
		this.K = K;
		this.V = V;
		this.iterNum = iterNum;
		
	}
	public void intialize(DocumentSet documentSet)
	{
		D = documentSet.D;
		z = new int[D];
		
		for(int d = 0; d < D; d++){
			z[d] = 0; // initialize all document in No. 0 cluster first time 
		}
	}
	
	public void intialize_cluster(int[] m_z,int[][] n_zv,int[] n_z,DocumentSet documentSet)
	{
		HashMap<Integer, Integer> countk = new HashMap<Integer, Integer>(); 
		D = documentSet.D;

		
		
		
		int j=-1;
		for(int i=0;i<D;i++){
			if (countk.containsKey(z[i])){
				countk.put(z[i],countk.get(z[i]));
			}else{
				j++;
				countk.put(z[i], j);
			}
		}

		K=countk.keySet().size();
		for(int i=0;i<D;i++){
			z[i]=countk.get(z[i]);
		}
		
		this.m_z= new int [K];
		this.n_zv= new int [K][V];
		this.n_z= new int [K];
		for(int k = 0; k < K; k++){
			this.n_z[k] = 0;
			this.m_z[k] = 0;
			for(int t = 0; t < V; t++){
				this.n_zv[k][t] = 0;
			}
		}
		
		for(int d = 0; d < D; d++){
			Document document = documentSet.documents.get(d);
			int cluster = z[d];
		
			this.m_z[cluster] ++ ; 
			for(int w = 0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				this.n_zv[cluster][wordNo] += wordFre; // in one cluster, count one word occur time
				this.n_z[cluster] += wordFre; //in one cluster, count all words number
			}
		}
	}
	
	public void gibbsSampling(DocumentSet documentSet)
	{
		for(int i = 0; i < iterNum; i++){
			intialize_cluster(m_z,n_zv,n_z,documentSet);
		
			for(int d = 0; d < D; d++){
				Document document = documentSet.documents.get(d);
				
				int cluster = z[d];
				m_z[cluster]--;
				if(m_z[cluster]<0){
					System.out.println("mz cluster < 0 "+cluster +" " +m_z[cluster]);
				}
				
				for(int w = 0; w < document.wordNum; w++){
					int wordNo = document.wordIdArray[w];
					int wordFre = document.wordFreArray[w];
					n_zv[cluster][wordNo] -= wordFre;
				
					n_z[cluster] -= wordFre;
					
					
				}

				int choose_cluster = sampleCluster(d, document);
				z[d] = choose_cluster;
				if(choose_cluster<K){
					m_z[choose_cluster]++;
					for(int w = 0; w < document.wordNum; w++){
						int wordNo = document.wordIdArray[w];
						int wordFre = document.wordFreArray[w];
						n_zv[choose_cluster][wordNo] += wordFre; 
						n_z[choose_cluster] += wordFre; 
					}
				}else if(choose_cluster==K){
					
					intialize_cluster(m_z,n_zv,n_z,documentSet);
				}
				
			}
		
		}
		intialize_cluster(m_z,n_zv,n_z,documentSet);
	}

	private int sampleCluster(int d, Document document)
	{ 
		double[] prob = new double[K+1];
		
		for(int k = 0; k < K; k++){
			prob[k] = (m_z[k]) / (D - 1 + alpha);
			double valueOfRule2 = 1.0;
			int i = 0;
			for(int w=0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				
				for(int j = 0; j < wordFre; j++){
					
					valueOfRule2 *= (n_zv[k][wordNo] + beta + j) / (n_z[k] + V*beta + i);
					i++;
				}
				
				
			}
			prob[k] = prob[k] * valueOfRule2 ; 
			
		}
			
		prob[K]= (alpha) / (D - 1 + alpha);
		double valueOfRule3 = 1.0;
		int i = 0;
		for(int w=0; w < document.wordNum; w++){
			int wordFre = document.wordFreArray[w];
			for(int j = 0; j < wordFre; j++){
				valueOfRule3 *= (beta + j) /( beta*V + i);
				i++;
			}
			
		}
		prob[K] = prob[K] * valueOfRule3 ;
		
		for(int k = 1; k < K+1; k++){
			prob[k] += prob[k - 1];
		}
		
		double thred = Math.random() * prob[K];

	
		int kChoosed;
		for(kChoosed = 0; kChoosed < K+1; kChoosed++){
			if(thred < prob[kChoosed]){
				break;
			}
		}
		
		return kChoosed;
	}
	
	public void output(DocumentSet documentSet, String outputPath) throws Exception
	{
		String outputDir = outputPath + dataset + ParametersStr + "/";
		
		File file = new File(outputDir);
		if(!file.exists()){
			if(!file.mkdirs()){
				System.out.println("Failed to create directory:" + outputDir);
			}
		}
		
		outputClusteringResult(outputDir, documentSet);
	}

	public void outputClusteringResult(String outputDir, DocumentSet documentSet) throws Exception
	{
		HashMap<Integer, Integer> count = new HashMap<Integer, Integer>(); 
		
		String outputPath = outputDir + dataset + "ClusteringResult_DP.txt";
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(outputPath), "UTF-8"));
		for(int d = 0; d < documentSet.D; d++){
			int topic = z[d];
			writer.write(topic + "\n");
			
			if (!count.containsKey(topic)){
				count.put(topic, 1);
			}else{
				count.put(topic, count.get(topic) + 1);
			}
		}
		Iterator iterator = count.keySet().iterator();
		int i=0;
		while (iterator.hasNext()){
			i=(Integer) iterator.next();
			//System.out.println("topic : "+i+" number :"+count.get(i));
		}
		System.out.println("k size "+K);
		System.out.println("count size "+count.keySet().size());
		System.out.println("count "+count.keySet());
		System.out.println("count "+count.values());
		
		
		
		writer.flush();
		writer.close();
	}
}
