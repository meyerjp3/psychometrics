/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.reliability;

import java.util.Formatter;


/**
 * Computes Huynh's decision consistency indices. This class is a translation of Huynh's
 * original FORTRAN code.
 *
 * @author J Patrick Meyer
 * 
 * 	A computer program to compute the reliability indices
 *	for decision in mastery testing and their standard
 *	errors of estimate based on the Beta-Binomial model.
 *	Input data are: N=number of items,
 *			M=number of examinees,
 *			K=number of classification categories
 *			XBAR=mean of test scores
 *			SD=standard deviation of test scores
 *			L=cut scores
 *	
 *	References:
 *	Huynh, H. (1979).  Computational and statistical inference for
 *		two reliability indices based on the beta binomial model.
 *		Journal of Educational Statistics, 4, 231 246.
 *	Huynh, H. (1981).  Adequacy of asymptotic normal theory in 
 *		estimating reliability of mastery tests based on the beta
 *		binomial model.  Journal of Educational Statistics,  6, 257 266.
 *	Huynh, H., & Saunders, J. C. (1980). Solutions for some
 *		technical problems in domain-referenced mastery testing.
 *		Columbia, SC: Department of Educational Research and
 *		Psychology, University of South Carolina.
 *
 */
public class Huynh {
	double A, B, f;
	int[] L;
	int N, M, K, KP1, KP2, NP1;
	double XBAR, SD, XP, SDP, XK, SDK, VA, VB, VAB, BFZ, DPA, DPB, DPCA, DPCB, DBFA, DBFB,
		DSA,DSB, SUMBF, DKA, DKB, VKP, VP;
	double[] F;
	double[] CF;
	double[] XA;
	double[] XB;
	double[] DA;
	double[] DB;
	
	public Huynh(int aN, int aM, int aK, double aMean, double aSd, int[] aCutScores){
		N=aN;
		M=aM;
		K=aK+1;
		XBAR=aMean;
		SD=aSd;
		F = new double[N+2];
		CF = new double[N+2];
		XA = new double[N+2];
		XB = new double[N+2];
		DA = new double[N+2];
		DB = new double[N+2];
		NP1=N+1;
		
		L=new int[(K+1)];
		for(int i=1;i<K;i++){
			L[i]=aCutScores[i-1];
		}
		L[K]=N+1;
		f=(double)N/((double)N-1.0)*(1.0-XBAR*((double)N-XBAR)/((double)N*(SD*SD)));
		if(f>0){
			A=(-1.0+1.0/f)*XBAR;
			B=-A+N/f-N;
		}else{
			System.out.println("NON-POSITIVE ESTIMATE KR21.");
			System.out.println("MOMENT ESTIMATES FOR ALPHA AND BETA DO NOT EXIST.");
			System.out.println("COMPUTATIONS DISCONTINUED FOR THIS CASE.");
		}
		
		KAPPA();
	}
	
	private void KAPPA(){
		double P, PC, A1, A2, A3, TWO=2.0;
		int ICUT, IM1, LL, LU;
		
		NEHY(N, A, B, F, CF);
		VARAB(N, A, B, VA, VB, VAB, M, F, XA, XB);
		ZERLAB(N, A, B, XA, XB, F);
		
		PC=Math.pow(CF[L[1]],2);
		
		DPCA=TWO*CF[L[1]]*XA[L[1]];
		DPCB=TWO*CF[L[1]]*XB[L[1]];
		
		A1=0;
		for(int I=2;I<=K;I++){
			IM1=I-1;
			A1=CF[L[I]]-CF[L[IM1]];
			PC+=A1*A1;
			DPCA+=TWO*A1*(XA[L[I]]-XA[L[IM1]]);
			DPCB+=TWO*A1*(XB[L[I]]-XB[L[IM1]]);
		}
		
		if(K>2){
			DPA=0.0;
			DPB=0.0;
			P=0.0;
			for(int I=1;I<=K;I++){
				LL=0;
				if(I>1){
					LL=L[I-1];
				}
				LU=L[I]-1;
				BF(N,LL,LU);
//				BF(N,LL,LU,A,B,BFZ,DBFA,DBFB,DSA,DSB,SUMBF);
				P+=SUMBF;
				DPA+=DSA;
				DPB+=DSB;
			}
		}else{
			ICUT=L[1]-1;	
			
//			These 5 lines used instead of commented code below.
//			These lines are not in original FORTRAN program.
			BF(N,0,ICUT);
			A1=CF[L[1]];
			P=1.0-2.0*(A1-SUMBF);
			DPA=-2.0*(XA[L[1]]-DSA);
			DPB=-2.0*(XB[L[1]]-DSB);
			
//			next 13 lines of code are translated from FORTRAN code
//			require directional call (A,B) vs. (B,A) to BF method
//			only needed for optimizing speed. Do not affect calculation
//			and doesn't work with my Java implementation of BF method.
//			computer speed is much better today (2006) than in 1977. This optimization
//			is not necessary.
//			if(2*L[1]<=N){
//				System.out.println("1111");
//				BF(N,0,ICUT,A,B,BFZ,DBFA,DBFB,DSA,DSB,SUMBF);
//				A1=CF[L[1]];
//				P=1.0-2.0*(A1-SUMBF);
//				DPA=-2.0*(XA[L[1]]-DSA);
//				DPB=-2.0*(XB[L[1]]-DSB);
//			}else{
//				System.out.println("2222");
//				BF(N,0,ICUT,B,A,BFZ,DBFB,DBFA,DSB,DSA,SUMBF);
//				A1=CF[L[2]]-CF[L[1]];
//				P=1.0-2.0*(A1-SUMBF);
//				DPA=-2.0*(XA[L[2]]-XA[L[1]]-DSA);
//				DPB=-2.0*(XB[L[2]]-XB[L[1]]-DSB);
//			}
		}
		A1=1.0-PC;
		A2=1.0-P;
		A3=A1*A1;
		DKA=(DPA*A1-DPCA*A2)/A3;
		DKB=(DPB*A1-DPCB*A2)/A3;
		
//		System.out.println("here: " + );
		
		VKP=VA*(DKA*DKA)+VB*(DKB*DKB)+2.0*VAB*DKA*DKB;
		VP=VA*(DPA*DPA)+VB*(DPB*DPB)+2.0*VAB*DPA*DPB;
		SDK=Math.sqrt(VKP);
		XP=P;
		SDP=Math.sqrt(VP);
		XK=(P-PC)/A1;
			
	}
	
	private void BF(int N, int LL, int LU){
		int N2=N+N;
		int IR=LU-LL+1;
		double DN=(double)N;
		double Z1=(double)N2+B;
		double Z1M1=Z1-1.0;
		double Z2=Z1+A;
		double DLL=(double)LL;
		double AA,T,X,Y,XA,XB,AAHOLD,XAHOLD,XBHOLD;
		
		
		if(LL!=0){
			X=DLL-1.0;
			Y=DLL-1.0;
			AA=BFZ*(DN-X)*(A+X+Y)/((X+1.0)*(Z1M1-X-Y));
			XA=DBFA+1.0/(X+A+Y);
			XB=DBFB-1.0/(Z1M1-X-Y);
			
			X=LL;
			AA=AA*(DN-Y)*(A+X+Y)/((Y+1.0)*(Z1M1-X-Y));
			XA+=1.0/(A+X+Y);
			XB+=-1.0/(Z1M1-X-Y);
			
		}else{
//			LL==0
			AA=1.0;
			XA=0.0;
			XB=0.0;
			for(int I=1;I<=N2;I++){
				T=(double)I;
				AA=AA*(Z1-T)/(Z2-T);
				XA-=1.0/(Z2-T);
				XB+=1.0/(Z1-T);
			}
			XB+=XA;
		}
		SUMBF=AA;
		DSA=XA*AA;
		DSB=XB*AA;
		
		if(IR==1){
//			GOTO 90
		}else{
//			System.out.println(AA);
			AAHOLD=AA;
			XAHOLD=XA;
			XBHOLD=XB;
			
			for(int I=2;I<=IR;I++){
				X=DLL+(double)(I-2);
				Y=DLL;
				AA=AAHOLD*(DN-X)*(A+X+Y)/((X+1.0)*(Z1M1-X-Y));
				XA=XAHOLD+1.0/(A+X+Y);
				XB=XBHOLD-1.0/(Z1M1-X-Y);
				
				DSA+=2.0*XA*AA;
				DSB+=2.0*XB*AA;
				SUMBF+=2.0*AA;
				
				AAHOLD=AA;
				XAHOLD=XA;
				XBHOLD=XB;
				
				X+=1.0;
				for(int J=2;J<=I;J++){
					Y=DLL+(double)J-2.0;
					AA=AA*(DN-Y)*(A+X+Y)/((Y+1.0)*(Z1M1-X-Y));
					XA+=1.0/(A+X+Y);
					XB-=1.0/(Z1M1-X-Y);
					if(I==J){
						SUMBF+=AA;
						DSA+=XA*AA;
						DSB+=XB*AA;
					}else{
						SUMBF+=2.0*AA;
						DSA+=2.0*XA*AA;
						DSB+=2.0*XB*AA;
					}
				}
			}
		}
//		90
		BFZ=AA;
		DBFA=XA;
		DBFB=XB;
	}
	
	private void NEHY(int N, double A, double B, double[] F, double[] CF){
		double Z1=(double)N+B;
		double Z2=(double)Z1+A;
		int KP1, KP2, IP1;
		int KK=0;
		
		F[1]=1.0;
		for(int I=1;I<=N;I++){
			F[1]=F[1]*(Z1-(double)I)/(Z2-(double)I);
		}
		
		while((KK-N)<0){
			KP1=KK+1;
			KP2=KK+2;
			F[KP2]=F[KP1]*(double)(N-KK)*(A+(double)KK)/((double)KP1*(Z1-(double)KP1));
			KK++;
		}
		CF[1]=F[1];
		for(int I=1;I<=N;I++){
			IP1=I+1;
			CF[IP1]=CF[I]+F[IP1];
		}
	}
	
	private void ZERLAB(int N, double A, double B, double[] XA, double[] XB, double[] F){
		double Z1,Z2;
		int IX, IP1, IM1;
		double ONE=1.0;
		XA[1]=0.0;
		XB[1]=0.0;
		Z1=(double)N+B;
		Z2=Z1+A;
		NP1=N+1;
		
		for(int I=1;I<=N;I++){
			XA[1]-=ONE/(Z2-(double)I);
			XB[1]+=ONE/(Z1-(double)I);
		}
		XB[1]+=XA[1];
		
		for(int I=1;I<=N;I++){
			IP1=I+1;
			IX=I-1;
			XA[IP1]=XA[I]+ONE/(A+(double)IX);
			XB[IP1]=XB[I]-ONE/(Z1-(double)I);
		}
		XA[1]*=F[1];
		XB[1]*=F[1];
		
		for(int I=2;I<=NP1;I++){
			IM1=I-1;
			XA[I]=XA[IM1]+XA[I]*F[I];
			XB[I]=XB[IM1]+XB[I]*F[I];
		}
	}
	
	private void VARAB(int N, double A, double B, double VA, double VB, double VAB, int M, double[] F,
			double[] DA, double[] DB){
		double B11,B12,B22,D;
		
		DERLAB(N, A, B, DA, DB);
		
		B11=0.0;
		B12=0.0;
		B22=0.0;
		
		for(int I=1;I<=NP1;I++){
			B11+=DA[I]*DA[I]*F[I];
			B12+=DA[I]*DB[I]*F[I];
			B22+=DB[I]*DB[I]*F[I];
		}
		B11*=M;
		B12*=M;
		B22*=M;
		D=B11*B22-B12*B12;
		this.VA=B22/D;
		this.VB=B11/D;
		this.VAB=-B12/D;
		
	}
	
	private void DERLAB(int N, double A, double B, double[] DA, double[] DB){
		double Z1,Z2;
		double ONE=1.0;
		int IX, IP1;
		DA[1]=0.0;
		DB[1]=0.0;
		Z1=(double)N+B;
		Z2=Z1+A;
		
		for(int I=1;I<=N;I++){
			DA[1]-=ONE/(Z2-(double)I);
			DB[1]+=ONE/(Z1-(double)I);
		}
		DB[1]+=DA[1];
		for(int I=1;I<=N;I++){
			IP1=I+1;
			IX=I-1;
			DA[IP1]=DA[I]+ONE/(A+(double)IX);
			DB[IP1]=DB[I]-ONE/(Z1-(double)I);
		}
	}
	
	public double rawAgreement(){
		return XP;
	}
	
	public double agreementSE(){
		return SDP;
	}
	
	public double kappa(){
		return XK;
	}
	
	public double kappaSE(){
		return SDK;
	}
	
	public double alpha(){
		return A;
	}
	
	public double beta(){
		return B;
	}

    public double kr21(){
        return f;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String f2="%.2f";

        f.format("%n");
        f.format("%35s", "               DECISION CONSISTENCY");
        f.format("%n");
        f.format("%-50s", "==================================================");
        f.format("%n");
        f.format("%-30s", "Huynh's Raw Agreement Index = ");
        f.format(f2, this.rawAgreement());
        f.format("%n");
        f.format("%5s", " ");
        f.format("%-29s", "Standard Error of Agreement: ");
        f.format(f2, this.agreementSE());
        f.format("%n");

        f.format("%5s", " ");
        f.format("%-30s", "95% Conf. Int. of Agreement: (");
        f.format(f2, this.rawAgreement()-1.96*this.agreementSE());
        f.format("%-2s", ", ");
        f.format(f2, this.rawAgreement()+1.96*this.agreementSE());
        f.format("%-1s", ")");
        f.format("%n");

        f.format("%-16s", "Huynh's Kappa = ");
        f.format(f2, this.kappa());
        f.format("%n");
        f.format("%5s", " ");
        f.format("%-25s", "Standard Error of Kappa: ");
        f.format(f2, this.kappaSE());
        f.format("%n");

        f.format("%5s", " ");
        f.format("%-26s", "95% Conf. Int. of Kappa: (");
        f.format(f2, this.kappa()-1.96*this.kappaSE());
        f.format("%-2s", ", ");
        f.format(f2, this.kappa()+1.96*this.kappaSE());
        f.format("%-1s", ")");
        f.format("%n");


        f.format("%-7s", "KR-21: ");
        f.format(f2, this.kr21());
        f.format("%n");
        f.format("%-21s", "Beta-binomial alpha: ");
        f.format(f2, this.alpha());
        f.format("%n");
        f.format("%-20s", "Beta-binomial beta: ");
        f.format(f2, this.beta());
        f.format("%n");
        f.format("%-50s", "--------------------------------------------------");
        f.format("%n");
        return f.toString();
    }
	
	
}
