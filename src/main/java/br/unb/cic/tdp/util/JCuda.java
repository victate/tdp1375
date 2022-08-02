package br.unb.cic.tdp.util;

import jcuda.*;

import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyDeviceToHost;
import static jcuda.runtime.JCuda.cudaMemcpy;
import static jcuda.runtime.JCuda.cudaFree;
import static jcuda.runtime.JCuda.cudaMalloc;
import jcuda.runtime.*;

public class JCuda {
    public static void MemoryAllocate(Object obj)
    {
        Pointer pointer = new Pointer();
        cudaMalloc(pointer, 4);
        System.out.println("Pointer: "+pointer);
        cudaMemcpy(Pointer.to(obj), pointer, 4, cudaMemcpyDeviceToHost);
        cudaFree(pointer);
    }
}
