/************************************************************
File Name: PVocoder.java
Cannot use in this version (Performance issue)

This file is Phase Vocoder method that calculate input signal to stretch time without changing the pitch.
Derived from MatLab version.
Edited in order to support processing in parallel - but still cannot work
Parameters n1 and WLen can be changed to find the appropriated value for each file.

Version		Date		Name		Description
------------------------------------------------------------
1			14/06/2014	Patomporn	Create new
/***********************************************************/


package cs.nuim.ssure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.util.Log;
import cs.nuim.ArrayOperations;
import cs.nuim.FFT;
import cs.nuim.utilities;

public class PVocoder {
	
	private static int n1 = 200; //can be changed
	private static int WLen = 2048;
	private static double[] w1 = utilities.hanningz(WLen);
	private static double[] w2 = w1;

	/**
	 * Phase Vocoder 
	 */
	public static double[] doPV(double[] inp, double ratio) {
		Log.d("PVocoder", "pv-func-start");
		int n2, L, Len, pin, pout, pend;
		double max, tstretch_ratio;
		double[] DAFx_in, DAFx_out, inputArray, omega, phi0, psi;
		
		tstretch_ratio = ratio;
		n2 = (int)(ratio * n1);

		DAFx_in = inp;
		L = DAFx_in.length;
		max = utilities.getMaxAbs(DAFx_in);
		for (int i=0; i<L; i++) { DAFx_in[i] *= Math.pow(max, -1); }
		
		Len = (WLen) + (DAFx_in.length) + (WLen - (L%n1));
		inputArray=new double[Len];
		System.arraycopy(DAFx_in, 0, inputArray, WLen, DAFx_in.length);
		System.out.println("Read");
		
		//Initializtions
		DAFx_out = new double[WLen + (int)(Math.ceil(L*tstretch_ratio))];
		omega = new double[WLen];
		for (int i=0; i<WLen; i++) { omega[i] = 2 * Math.PI * n1 * i * Math.pow(WLen, 1); }
		phi0 = new double[WLen];
		psi = new double[WLen];
		pin = 0;
		pout = 0;
		pend = Len-WLen;
		System.out.println("Init");

		//Main
		while (pin<pend) {
			Log.d("PVocoder", "pv-loop-start");
			double[] grain, r, phi, omega_phi, delta_phi;
			double[][] f, ft;
			
			grain = new double[WLen];
			for (int i=0; i<WLen; i++) grain[i] = inputArray[pin+i] * w1[i];
			
			Log.d("PVocoder", "pv-fft-start");
			f = FFT.doFFT(FFT.fftshift(grain), WLen);
			r = utilities.abs(f);
			phi = utilities.angle(f);
			Log.d("PVocoder", "pv-fft-end");
			
			omega_phi = ArrayOperations.SubArray(ArrayOperations.SubArray(phi, phi0),omega);			
			delta_phi = ArrayOperations.AddArray(omega, utilities.princarg(omega_phi));
			phi0 = phi;
			psi = utilities.princarg(ArrayOperations.AddArray(psi, ArrayOperations.RatioArray(delta_phi, tstretch_ratio)));
			ft = utilities.PolarToCartesian(r, psi);			
			grain = ArrayOperations.MultArray(FFT.fftshift(FFT.doIFFT(ft, WLen)), w2);
			
			
			for (int i=0; i<WLen; i++) if (pout+i < DAFx_out.length) DAFx_out[pout+i] = DAFx_out[pout+i] + grain[i];
			pin = pin + n1;
			pout = pout + n2;
			
			Log.d("PVocoder", "pv-loop-end");
		}
		
		Log.d("PVocoder", "pv-func-end");
		return DAFx_out;
		
	}
	
	/**
	 * Phase Vocoder in Parallel
	 */
	public static double[] doAsyncPV(double[] inp, double ratio) {
		Log.d("PVocoder", "pv-start");
		int n2, L, Len, pin, pout, pend;
		double max, tstretch_ratio;
		double[] DAFx_in, DAFx_out, inputArray, omega, phi0, psi;
		
		tstretch_ratio = ratio;
		n2 = (int)(ratio * n1);
	
		DAFx_in = inp;
		L = DAFx_in.length;
		max = utilities.getMaxAbs(DAFx_in);
		for (int i=0; i<L; i++) { DAFx_in[i] *= Math.pow(max, -1); }
		
		Len = (WLen) + (DAFx_in.length) + (WLen - (L%n1));
		inputArray=new double[Len];
		System.arraycopy(DAFx_in, 0, inputArray, WLen, DAFx_in.length);
		System.out.println("Read");
		
		//Initializtions
		DAFx_out = new double[WLen + (int)(Math.ceil(L*tstretch_ratio))];
		omega = new double[WLen];
		for (int i=0; i<WLen; i++) { omega[i] = 2 * Math.PI * n1 * i * Math.pow(WLen, 1); }
		phi0 = new double[WLen];
		psi = new double[WLen];
		pin = 0;
		pout = 0;
		pend = Len-WLen;
		System.out.println("Init");

		List<DAFx> inputs = new ArrayList<DAFx>();
		//Main
		while (pin<pend) {
			inputs.add(new DAFx(pin, pout, n2));

			pin = pin + n1;
			pout = pout + n2;
		}
		
		try {
			List<DAFx> outputs = processInputs(inputs, WLen, tstretch_ratio, inputArray, omega, phi0, psi, w1, w2);
			Log.d("PVocoder", "pv-end-future");
			
			if(outputs!=null) {
				while(outputs.size()!=0) {
				    DAFx output = outputs.get(0);
			        
				    if (output.getPout()+output.getN2() < DAFx_out.length)
						System.arraycopy(output.getDAFx(), 0, DAFx_out, output.getPout(), Math.min(DAFx_out.length-(output.getPout()), (output.getDAFx()).length));
			        
			        outputs.remove(0);
				}
					
			}
			
		} catch (InterruptedException ie) {
			Log.e("PVooder", ie.getMessage());
		} catch (ExecutionException ee) {
			Log.e("PVocoder", ee.getMessage());
		}
		
		Log.d("PVocoder", "pv-wait-complete");
		DAFx_out = setAmp(DAFx_out);
		
		Log.d("PVocoder", "pv-end");
		return DAFx_out;
		
	}

	/**
	 * Phase Vocoder support thread using Future [Cannot work due to its structure]
	 */
	public static List<DAFx> processInputs(List<DAFx> inputs, 
			final int _WLen, final double tstretch_ratio, final double[] inputArray, final double[] omega, 
			final double[] phi0, final double[] psi, final double[] w1, final double[] w2)
			throws InterruptedException, ExecutionException {

	    int threads = Runtime.getRuntime().availableProcessors();
	    ExecutorService service = Executors.newFixedThreadPool(threads);

	    List<Future<DAFx>> futures = new ArrayList<Future<DAFx>>();
	    for (final DAFx input : inputs) {
	        Callable<DAFx> callable = new Callable<DAFx>() {
	            public DAFx call() throws Exception {
	    			int pin = input.getPin();
	    			int n2 = input.getN2();
	    			
	    	        double[] DAFx_out = new double[n2];
	    	        
	    			// Long time comsuming operation
	    			Log.d("PVocoder", "loop-start");
	    			double[] grain, r, phi, omega_phi, delta_phi;
	    			double[][] f, ft;
	    			
	    			grain = new double[WLen];
	    			for (int i=0; i<WLen; i++) grain[i] = inputArray[pin+i] * w1[i];
	    			
	    			f = FFT.doFFT(FFT.fftshift(grain), WLen);
	    			r = utilities.abs(f);
	    			phi = utilities.angle(f);
	    			
	    			omega_phi = ArrayOperations.SubArray(ArrayOperations.SubArray(phi, phi0),omega);			
	    			delta_phi = ArrayOperations.AddArray(omega, utilities.princarg(omega_phi));
	    			//phi0 = phi;
	    			//psi = utilities.princarg(ArrayOperations.AddArray(psi, ArrayOperations.RatioArray(delta_phi, tstretch_ratio)));
	    			ft = utilities.PolarToCartesian(r, psi);			
	    			grain = ArrayOperations.MultArray(FFT.fftshift(FFT.doIFFT(ft, WLen)), w2);
	    			
	    			DAFx output = new DAFx(input.getPin(), input.getPout(), input.getN2());
	    			
	                for (int i=0; i<n2; i++) DAFx_out[i] += grain[i];
	    			output.setDAFx(DAFx_out);
	    			
	    			Log.d("PVocoder", "loop-end : " + output.isComplete());
	            	
	                // process your input here and compute the output
	                return output;
	            }
	        };
	        futures.add(service.submit(callable));
	    }

	    service.shutdown();

	    List<DAFx> outputs = new ArrayList<DAFx>();
	    for (Future<DAFx> future : futures) {
	        outputs.add(future.get());
	    }
	    return outputs;
	}
	
	//--- Support : Public methods
	/**
	 * Set Ampitute for signal
	 */
	public static double[] setAmp(double[] DAFx) {
		double max = utilities.getMaxAbs(DAFx);
		for (int i=WLen; i<DAFx.length; i++) { DAFx[i] *= Math.pow(max, -1); }
		System.out.println("Main");
		
		return DAFx;
	}
	
	/**
	 * Append signal
	 */
	public static double[] append(double[] src, double[] target) {
		double[] out = new double[src.length + target.length];
		System.arraycopy(target, 0, out, 0, target.length);
		System.arraycopy(src, 0, out, target.length, src.length);
		return out;
	}
	
}

//--- Support : Phase Vocoder in Parallel
class DAFx {
	private double[] DAFx;
	private int pin = 0;
	private int pout = 0;
	private int n2;
	private boolean complete;
	
	DAFx(int _pin, int _pout, int _n2) { 
		this.complete = false; 
		this.pin = _pin;
		this.pout = _pout;
		this.n2 = _n2;
	}
	
	public void setDAFx(double[] _DAFx) {
		this.DAFx = _DAFx;
		this.complete = true;
	}
	
	public boolean isComplete() { return complete; }
	public int getPin() { return pin; }
	public int getPout() { return pout; }
	public int getN2() { return n2; }
	public double[] getDAFx() { return DAFx; }
}
