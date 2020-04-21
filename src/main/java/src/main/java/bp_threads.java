package src.main.java;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.util.*;
import java.io.*;
import java.util.Iterator;

class InputLayer{
    double[][][] mat;
    int xSize;
    int ySize;
    int nBandCount;
    //int sizeofkn;
    //Kernel[] KNCount;       //卷积核数量，决定下一隐藏层的通道数
    //double[] bias;

    void rmToMat(String file){
        gdal.AllRegister();
        String fileUrl = file;
        // 读取影像数据
        Dataset dataset = gdal.Open(fileUrl, gdalconstConstants.GA_ReadOnly);
        if (dataset == null) {
            System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());
            System.err.println(gdal.GetLastErrorMsg());
            System.exit(1);
        }
        // 读取影像信息 宽、高、波段数
        int xSize = dataset.getRasterXSize();
        int ySize = dataset.getRasterYSize();
        int nBandCount = dataset.getRasterCount();
        this.xSize=xSize;
        this.ySize=ySize;
        this.nBandCount=nBandCount;
        //int type = dataset.GetRasterBand(1).GetRasterDataType();

        this.mat=new double[nBandCount][ySize][xSize];
        for (int i = 1; i <= nBandCount; i++) {
            Band band = dataset.GetRasterBand(i);
            int[] cache = new int[xSize*ySize];
            band.ReadRaster(0, 0, xSize, ySize, cache);
            for (int j = 0; j < ySize; j++) {
                for (int n = 0; n < xSize; n++) {
                    this.mat[i-1][j][n]=cache[xSize*j+n];
                }
            }
        }
        dataset.delete();
        gdal.GDALDestroyDriverManager();
    }

    void norm(){
        double[] min=new double[nBandCount];
        double[] span=new double[nBandCount];
        for (int band=0;band<nBandCount;band++){
            double mini=9999999;
            double max=0;
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    double k=mat[band][y][x];
                    if (k<mini){mini=k;}
                    if (k>max){max=k;}
                }
            }
            min[band]=mini;
            span[band]=max-mini;
        };
        double[][][] matrix=new double[nBandCount][ySize][xSize];
        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    matrix[band][y][x]=((mat[band][y][x]-min[band])/span[band]);
                }
            }
        }
        mat=matrix;
    }

    InputLayer(String file){
        //String file="H:\\java_wk\\remote_sensing_data\\uv\\uvs\\1.tif";
        this.rmToMat(file);
        this.norm();
    }

    InputLayer(double[][][] mat){
        this.mat=mat;
        this.nBandCount=mat.length;
        this.ySize=mat[0].length;
        this.xSize=mat[0][0].length;
        this.norm();
    }

}

class Array_Kernel{
    double[][][][] AK;
    int length;                         //这一层的band
    //Kernel[] AK;
    int nBandCount;                     //上一层的band
    int size_Kernel;                    //kernel大小，为方型
    Array_Kernel(double[][][][] AK){
        this.length=AK.length;
        this.nBandCount=AK[0].length;
        this.size_Kernel=AK[0][0].length;
        this.AK=AK;
    }

    Array_Kernel(int length,int nBandCount,int size_Kernel){
        this.length=length;
        this.nBandCount=nBandCount;
        this.size_Kernel=size_Kernel;
        this.AK=new double[this.length][this.nBandCount][this.size_Kernel][this.size_Kernel];
        java.util.Random random = new java.util.Random();
        for (int l=0;l<this.length;l++){
            for (int i=0;i<this.nBandCount;i++){
                for (int n=0;n<this.size_Kernel;n++){
                    for (int nn=0;nn<this.size_Kernel;nn++){
                        double ff=0;
                        while(ff<=0 || ff>1){
                            ff=random.nextDouble();
                        }
                        this.AK[l][i][n][nn]=ff;
                    }
                }
            }
        }
    }

    Array_Kernel(int[] AK_size){
        this.length=AK_size[0];
        this.nBandCount=AK_size[1];
        this.size_Kernel=AK_size[2];
        this.AK=new double[this.length][this.nBandCount][this.size_Kernel][this.size_Kernel];
        java.util.Random random = new java.util.Random();
        for (int l=0;l<this.length;l++){
            for (int i=0;i<this.nBandCount;i++){
                for (int n=0;n<this.size_Kernel;n++){
                    for (int nn=0;nn<this.size_Kernel;nn++){
                        double ff=0;
                        while(ff<=0 || ff>1){
                            ff=random.nextDouble();
                        }
                        this.AK[l][i][n][nn]=ff;
                    }
                }
            }
        }
    }
}

class Kernel{
    double[][][] mat;
    int nBandCount;
    int size;
    Kernel(int nBandCount,int size){
        this.nBandCount=nBandCount;
        this.size=size;
        this.mat=new double[nBandCount][size][size];
        java.util.Random random = new java.util.Random();
        for (int i=0;i<nBandCount;i++){
            for (int n=0;n<size;n++){
                for (int nn=0;nn<size;nn++){
                    this.mat[i][n][nn]=random.nextGaussian();
                }
            }
        }
    }
}

class HidenLayer{
    double[][][] mat;
    int xSize;
    int ySize;
    int nBandCount;
    Array_Kernel KernelCount;    //卷积核数量，决定下一隐藏层的通道数
    //int sizeofkn;
    //Kernel[] KNCount;       //卷积核数量，决定下一隐藏层的通道数
    double[] bias;
    int step;

    double[][][] receiveMat(InputLayer IL){
        return IL.mat;
    }

    double[][][] receiveMat(HidenLayer HL){
        return HL.mat;
    }

    double[][][] receiveMat(PoolMat PM){return PM.mat;}

    int receiveYsize(InputLayer IL){
        return IL.ySize;
    }

    int receiveYsize(HidenLayer HL){
        return HL.ySize;
    }

    int receiveYsize(PoolMat PM){ return PM.ySize; }

    int receiveYsize(double[][][] last_mat){ return last_mat[0].length; }

    int receiveXsize(InputLayer IL){
        return IL.xSize;
    }

    int receiveXsize(HidenLayer HL){
        return HL.xSize;
    }

    int receiveXsize(PoolMat PM){ return PM.xSize; }

    int receiveXsize(double[][][] last_mat){
        return last_mat[0][0].length;
    }

    int receiveNband(InputLayer IL){
        return IL.nBandCount;
    }

    int receiveNband(HidenLayer HL){
        return HL.nBandCount;
    }

    int receiveNband(PoolMat PM){
        return PM.nBandCount;
    }

    int receiveNband(double[][][] last_mat){
        return last_mat.length;
    }

    double[][][] define_pa(int L_ySize,int L_xSize,int L_nBandCount, double[][][] L_mat){               //defind past mat
        double[][][] mat;
        int mat_ysize;
        int mat_xsize;
        int yHL;
        int xHL;

        int yremainder=(L_ySize-this.KernelCount.size_Kernel)%step;

        if (yremainder>0){
            yHL=(L_ySize-this.KernelCount.size_Kernel+1)/step+1;
        }
        else{
            yHL=(L_ySize-this.KernelCount.size_Kernel+1)/step;
        }

        mat_ysize=L_ySize+yremainder;

        int xremainder=(L_xSize-this.KernelCount.size_Kernel)%step;

        if (xremainder>0){
            xHL=(L_xSize-this.KernelCount.size_Kernel+1)/step+1;
        }
        else{
            xHL=(L_xSize-this.KernelCount.size_Kernel+1)/step;
        }

        mat_xsize=L_xSize+xremainder;

        if(yremainder>0 && xremainder>0){
            mat=new double[L_nBandCount][mat_ysize][mat_xsize];
            for (int band=0;band<L_nBandCount;band++){
                for (int y=0;y<mat_ysize;y++){
                    for (int x=0;x<mat_xsize;x++){
                        if (x<=L_xSize && y<=L_ySize){
                            mat[band][y][x]=L_mat[band][y][x];
                        }
                        else{
                            mat[band][y][x]=0;
                        }
                    }
                }

            }
        }
        else{
            mat=L_mat;
        }

        this.ySize=yHL;
        this.xSize=xHL;
        this.nBandCount=KernelCount.length;
        this.mat=new double[KernelCount.length][this.ySize][this.xSize];
        return mat;
    }


    void receiveYXBM(int L_ySize,int L_xSize,int L_nBandCount, double[][][] L_mat ){

        double[][][] mat=define_pa(L_ySize,L_xSize,L_nBandCount,L_mat);

        for (int i=0;i<KernelCount.length;i++){
            for (int yhl=0;yhl<this.ySize;yhl++){
                for (int xhl=0;xhl<this.xSize;xhl++){
                    double n=0;
                    for (int band=0;band<KernelCount.nBandCount;band++){
                        for (int y=0;y<KernelCount.size_Kernel;y++){
                            for (int x=0;x<KernelCount.size_Kernel;x++){

                                n+=((KernelCount.AK[i][band][y][x]*mat[band][yhl*step+y][xhl*step+x]));
                            }
                        }
                    }

                    this.mat[i][yhl][xhl]=n+bias[i];                           //有问题？
                }
            }
        }

    }

    void receive(InputLayer IL){
        int L_ySize=receiveYsize(IL);
        int L_xSize=receiveXsize(IL);
        int L_nBandCount=receiveNband(IL);
        double[][][] L_mat=receiveMat(IL);

        receiveYXBM(L_ySize,L_xSize,L_nBandCount,L_mat);
    }

    void receive(double[][][] last_mat){
        int L_ySize=receiveYsize(last_mat);
        int L_xSize=receiveXsize(last_mat);
        int L_nBandCount=receiveNband(last_mat);
        double[][][] L_mat=last_mat;

        receiveYXBM(L_ySize,L_xSize,L_nBandCount,L_mat);
    }

    void receive(HidenLayer HL){
        int L_ySize=receiveYsize(HL);
        int L_xSize=receiveXsize(HL);
        int L_nBandCount=receiveNband(HL);
        double[][][] L_mat=receiveMat(HL);

        receiveYXBM(L_ySize,L_xSize,L_nBandCount,L_mat);
    }

    void receive(PoolMat PM){
        int L_ySize=receiveYsize(PM);
        int L_xSize=receiveXsize(PM);
        int L_nBandCount=receiveNband(PM);
        double[][][] L_mat=receiveMat(PM);

        receiveYXBM(L_ySize,L_xSize,L_nBandCount,L_mat);
    }

    void biasIniting(int bias_size){
        bias=new double[bias_size];
        java.util.Random random = new java.util.Random();
        for (int i=0;i<bias_size;i++){
            double ff=0;
            while(ff<=0 || ff>1){
                ff=random.nextDouble();
            }
            this.bias[i]=ff;
        }
    }

    HidenLayer(){}

    HidenLayer(InputLayer IL, int step,int[] AK_size){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        receive(IL);
    }

    HidenLayer(double[][][] IL_mat, int step,int[] AK_size){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        receive(IL_mat);
    }



    HidenLayer(HidenLayer HL, int step,int[] AK_size){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        receive(HL);
    }

    HidenLayer(PoolMat PM, int step, int[] AK_size ){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        receive(PM);
    }

    HidenLayer(InputLayer IL, int step, double[][][][] nkn, double[] bias){                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        receive(IL);
    }

    HidenLayer(double[][][] IL_mat, int step, double[][][][] nkn, double[] bias){                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        receive(IL_mat);
    }

    HidenLayer(HidenLayer HL, int step, double[][][][] nkn, double[] bias){                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        receive(HL);
    }

    HidenLayer(PoolMat PM, int step, double[][][][] nkn, double[] bias){                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        receive(PM);
    }


    void norm(){
        double[] min=new double[nBandCount];
        double[] span=new double[nBandCount];
        for (int band=0;band<nBandCount;band++){
            double mini=9999999;
            double max=0;
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    double k=mat[band][y][x];
                    if (k<mini){mini=k;}
                    if (k>max){max=k;}
                }
            }
            min[band]=mini;
            span[band]=max-mini;
        };
        double[][][] matrix=new double[nBandCount][ySize][xSize];
        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    matrix[band][y][x]=((mat[band][y][x]-min[band])/span[band]);
                }
            }
        }
        mat=matrix;
    }

    public void relu(){
        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    if (mat[band][y][x]<=0){
                        mat[band][y][x]=0;
                    }
                    else{
                        continue;
                    }
                }
            }
        }
    }
}

class bpAPI {
    static HashMap d_bpmat=new HashMap();               //命名不是特别规范，应该表达bp中的kernel.mat
    static HashMap d_bpbias=new HashMap();
    static HashMap d_kenel=new HashMap<Integer,double[][][][]>();
    static HashMap d_bias=new HashMap<Integer,double[]>();
}

class bpHidenLayer extends HidenLayer{

    //static int nhl=0;

    HashMap d_bpmat;               //命名不是特别规范，应该表达bp中的kernel.mat
    HashMap d_bpbias;

    double[][][][][][][] bpmat;
    double[][][][] bpbias;

    void receiveYXBM_IL(int L_ySize,int L_xSize,int L_nBandCount, double[][][] L_mat ){

        double[][][] mat=define_pa(L_ySize,L_xSize,L_nBandCount,L_mat);

        bpmat=new double[KernelCount.length][KernelCount.nBandCount][KernelCount.size_Kernel][KernelCount.size_Kernel][KernelCount.length][this.ySize][this.xSize];
        bpbias=new double[this.KernelCount.length][this.KernelCount.length][this.ySize][this.xSize];

        for (int i=0;i<KernelCount.length;i++){
            for (int yhl=0;yhl<this.ySize;yhl++){
                for (int xhl=0;xhl<this.xSize;xhl++){
                    double n=0;
                    for (int band=0;band<KernelCount.nBandCount;band++){
                        for (int y=0;y<KernelCount.size_Kernel;y++){
                            for (int x=0;x<KernelCount.size_Kernel;x++){

                                n+=((KernelCount.AK[i][band][y][x]*mat[band][yhl*step+y][xhl*step+x]));

                                bpmat[i][band][y][x][i][yhl][xhl]= mat[band][yhl*step+y][xhl*step+x];                   //通用？
                                bpbias[i][i][yhl][xhl]=1;
                                //bpbias[i][i][yhl][xhl]=bias[i];
                            }
                        }
                    }

                    this.mat[i][yhl][xhl]=n+bias[i];                           //有问题？
                }
            }
        }


        this.d_bpmat.put(0,bpmat);
        this.d_bpbias.put(0,bpbias);

    }

    void receiveYXBM_bpHLorbpPM(int L_ySize,int L_xSize,int L_nBandCount, double[][][] L_mat ){
        double[][][] mat=define_pa(L_ySize,L_xSize,L_nBandCount,L_mat);

        bpmat=new double[KernelCount.length][KernelCount.nBandCount][KernelCount.size_Kernel][KernelCount.size_Kernel][KernelCount.length][this.ySize][this.xSize];
        bpbias=new double[this.KernelCount.length][this.KernelCount.length][this.ySize][this.xSize];

        HashMap new_d_bpmat=new HashMap();
        HashMap new_d_bpbias=new HashMap();
        for(int n=0;n<this.d_bpmat.size();n++){
            double[][][][][][][] bpmatt= (double[][][][][][][]) this.d_bpmat.get(n);
            int len_1=bpmatt.length;
            int len_2=bpmatt[0].length;
            int len_3=bpmatt[0][0].length;
            new_d_bpmat.put(n,new double[len_1][len_2][len_3][len_3][this.KernelCount.length][this.ySize][this.xSize]);
            new_d_bpbias.put(n,new double[len_1][this.KernelCount.length][this.ySize][this.xSize]);

        }

        for (int i=0;i<this.KernelCount.length;i++){
            for (int yhl=0;yhl<this.ySize;yhl++){
                for (int xhl=0;xhl<this.xSize;xhl++){
                    double n=0;
                    for (int band=0;band<KernelCount.nBandCount;band++){
                        for (int y=0;y<KernelCount.size_Kernel;y++){
                            for (int x=0;x<KernelCount.size_Kernel;x++){

                                n+=((KernelCount.AK[i][band][y][x]*mat[band][yhl*step+y][xhl*step+x]));

                                bpmat[i][band][y][x][i][yhl][xhl]= mat[band][yhl*step+y][xhl*step+x];                   //通用？
                                bpbias[i][i][yhl][xhl]=1;
                                //bpbias[i][i][yhl][xhl]=bias[i];
                                //检查纠错
                                for(int nu=0;nu<this.d_bpmat.size();nu++){
                                    double[][][][][][][] new_bpmatt=(double[][][][][][][]) new_d_bpmat.get(nu);
                                    double[][][][][][][] old_bpmatt=(double[][][][][][][]) this.d_bpmat.get(nu);
                                    for (int t=0;t<new_bpmatt.length;t++){
                                        for(int tt=0;tt<new_bpmatt[0].length;tt++){
                                            for(int ttt=0;ttt<new_bpmatt[0][0].length;ttt++){
                                                for(int tttt=0;tttt<new_bpmatt[0][0].length;tttt++){
                                                    new_bpmatt[t][tt][ttt][tttt][i][yhl][xhl]+=(KernelCount.AK[i][band][y][x]*old_bpmatt[t][tt][ttt][tttt][band][yhl*step+y][xhl*step+x]);
                                                }
                                            }
                                        }
                                        ((double[][][][]) new_d_bpbias.get(nu))[t][i][yhl][xhl]+=(((double[][][][]) this.d_bpbias.get(nu))[t][band][yhl*step+y][xhl*step+x])*KernelCount.AK[i][band][y][x];
                                    }
                                }

                            }
                        }
                    }

                    this.mat[i][yhl][xhl]=n+bias[i];                           //有问题？
                }
            }
        }

        this.d_bpmat.clear();
        this.d_bpmat.putAll(new_d_bpmat);
        this.d_bpmat.put(this.d_bpmat.size(),bpmat);
        this.d_bpbias.clear();
        this.d_bpbias.putAll(new_d_bpbias);
        this.d_bpbias.put(this.d_bpbias.size(),bpbias);

        /*this.d_bpmat=new_d_bpmat;
        this.d_bpmat.put(this.d_bpmat.size(),bpmat);
        this.d_bpbias=new_d_bpbias;
        this.d_bpbias.put(this.d_bpbias.size(),bpbias);*/

    }



    void receive(InputLayer IL){
        int L_ySize=receiveYsize(IL);
        int L_xSize=receiveXsize(IL);
        int L_nBandCount=receiveNband(IL);
        double[][][] L_mat=receiveMat(IL);

        receiveYXBM_IL(L_ySize,L_xSize,L_nBandCount,L_mat);

        //bpAPI.d_kenel.put(bpAPI.d_kenel.size(),KernelCount.AK);
        //bpAPI.d_bias.put(bpAPI.d_bias.size(),bias);
    }

    void receive(double[][][] last_mat){
        int L_ySize=receiveYsize(last_mat);
        int L_xSize=receiveXsize(last_mat);
        int L_nBandCount=receiveNband(last_mat);
        double[][][] L_mat=last_mat;

        receiveYXBM_IL(L_ySize,L_xSize,L_nBandCount,L_mat);
    }

    void receive(bpHidenLayer HL){
        int L_ySize=receiveYsize(HL);
        int L_xSize=receiveXsize(HL);
        int L_nBandCount=receiveNband(HL);
        double[][][] L_mat=receiveMat(HL);

        receiveYXBM_bpHLorbpPM(L_ySize,L_xSize,L_nBandCount,L_mat);

        //bpAPI.d_kenel.put(bpAPI.d_kenel.size(),KernelCount.AK);
        //bpAPI.d_bias.put(bpAPI.d_bias.size(),bias);
    }

    void receive(bpPoolMat PM){
        int L_ySize=receiveYsize(PM);
        int L_xSize=receiveXsize(PM);
        int L_nBandCount=receiveNband(PM);
        double[][][] L_mat=receiveMat(PM);

        receiveYXBM_bpHLorbpPM(L_ySize,L_xSize,L_nBandCount,L_mat);

        //bpAPI.d_kenel.put(bpAPI.d_kenel.size(),KernelCount.AK);
        //bpAPI.d_bias.put(bpAPI.d_bias.size(),bias);
    }



    bpHidenLayer(InputLayer IL, int step,int[] AK_size,HashMap d_bpmat,HashMap d_bpbias){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(IL);

    }

    bpHidenLayer(double[][][] IL_mat, int step,int[] AK_size,HashMap d_bpmat,HashMap d_bpbias){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(IL_mat);

    }



    bpHidenLayer(bpHidenLayer HL, int step,int[] AK_size,HashMap d_bpmat,HashMap d_bpbias){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(HL);

    }

    bpHidenLayer(InputLayer IL, int step, double[][][][] nkn, double[] bias,HashMap d_bpmat,HashMap d_bpbias) {                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(IL);
    }

    bpHidenLayer(double[][][] IL_mat, int step, double[][][][] nkn, double[] bias,HashMap d_bpmat,HashMap d_bpbias) {                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(IL_mat);
    }


    bpHidenLayer(bpHidenLayer HL, int step, double[][][][] nkn, double[] bias,HashMap d_bpmat,HashMap d_bpbias){                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(HL);
    }

    bpHidenLayer(bpPoolMat PM, int step, int[] AK_size,HashMap d_bpmat,HashMap d_bpbias ){
        this.step=step;
        this.KernelCount= new Array_Kernel(AK_size);
        biasIniting(KernelCount.length);

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(PM);
    }

    bpHidenLayer(bpPoolMat PM, int step, double[][][][] nkn, double[] bias,HashMap d_bpmat,HashMap d_bpbias){                     //上一层为Inputlayer
        this.step=step;
        this.KernelCount=new Array_Kernel(nkn);
        this.bias=bias;

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        receive(PM);
    }

    void norm(){
        double[] min=new double[nBandCount];
        double[] span=new double[nBandCount];
        for (int band=0;band<nBandCount;band++){
            double mini=9999999;
            double max=0;
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    double k=mat[band][y][x];
                    if (k<mini){mini=k;}
                    if (k>max){max=k;}
                }
            }
            min[band]=mini;
            span[band]=max-mini;
        };

        double[][][] matrix=new double[this.nBandCount][this.ySize][this.xSize];
        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    matrix[band][y][x]=((mat[band][y][x]-min[band])/span[band]);

                    double multiple=matrix[band][y][x]/mat[band][y][x];

                    for(int nu=0;nu<this.d_bpmat.size();nu++){
                        double[][][][][][][] norm_bpmatt=(double[][][][][][][]) this.d_bpmat.get(nu);
                        for (int t=0;t<norm_bpmatt.length;t++){
                            for(int tt=0;tt<norm_bpmatt[0].length;tt++){
                                for(int ttt=0;ttt<norm_bpmatt[0][0].length;ttt++){
                                    for(int tttt=0;tttt<norm_bpmatt[0][0].length;tttt++){
                                        norm_bpmatt[t][tt][ttt][tttt][band][y][x]*=multiple;

                                    }
                                }
                            }
                            ((double[][][][]) this.d_bpbias.get(nu))[t][band][y][x]*=multiple;
                        }
                    }
                }
            }
        }
        mat=matrix;
    }

}

class PoolMat{
    double[][][] mat;
    int xSize;
    int ySize;
    int nBandCount;

    void pm_max(HidenLayer HL, int poolSize){
        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    double n=0;
                    for (int i=0;i<poolSize;i++){
                        for (int ii=0;ii<poolSize;ii++) {
                            if (n < HL.mat[band][y*poolSize+i][x*poolSize+ii]){
                                n=HL.mat[band][y*poolSize+i][x*poolSize+ii];
                            }
                        }
                    }
                    mat[band][y][x]=n;
                }
            }
        }
    }

    void pm_mean(HidenLayer HL, int poolSize){
        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    double n=0;
                    for (int i=0;i<poolSize;i++){
                        for (int ii=0;ii<poolSize;ii++) {
                            n+=HL.mat[band][y*ySize+i][x*xSize+ii];
                        }
                    }
                    mat[band][y][x]=n/(poolSize*poolSize);
                }
            }
        }
    }

    PoolMat(HidenLayer HL, int poolSize, String poolType){
        this.xSize=HL.xSize/poolSize;
        this.ySize=HL.ySize/poolSize;
        this.nBandCount=HL.nBandCount;
        this.mat=new double[nBandCount][ySize][xSize];              //暂时未考虑鲁棒性
        if (poolType=="max"){
            pm_max(HL,poolSize);
        }
        else if(poolType=="mean"){
            pm_mean(HL,poolSize);
        }
        else{
            System.out.println("illegal input,please input 'max' or 'mean'.");
        }
    }
}

class bpPoolMat extends PoolMat{

    HashMap d_bpmat;               //命名不是特别规范，应该表达bp中的kernel.mat
    HashMap d_bpbias;

    HashMap new_d_bpmat=new HashMap();
    HashMap new_d_bpbias=new HashMap();

    void init_new(bpHidenLayer HL){
        for(int t=0;t<this.d_bpmat.size();t++){
            double[][][][][][][] bpmatt= (double[][][][][][][]) this.d_bpmat.get(t);
            int len_1=bpmatt.length;
            int len_2=bpmatt[0].length;
            int len_3=bpmatt[0][0].length;
            new_d_bpmat.put(t,new double[len_1][len_2][len_3][len_3][HL.nBandCount][this.ySize][this.xSize]);
            new_d_bpbias.put(t,new double[len_1][HL.nBandCount][this.ySize][this.xSize]);

        }
    }



    void pm_max(bpHidenLayer HL, int poolSize){

        init_new(HL);

        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    double n=0;
                    int ny=0;
                    int nx=0;
                    for (int i=0;i<poolSize;i++){
                        for (int ii=0;ii<poolSize;ii++) {
                            if (n < HL.mat[band][y*poolSize+i][x*poolSize+ii]){
                                n=HL.mat[band][y*poolSize+i][x*poolSize+ii];
                                ny=i;
                                nx=ii;
                            }
                        }
                    }
                    mat[band][y][x]=n;

                    for(int nu=0;nu<new_d_bpmat.size();nu++){
                        double[][][][][][][] bpmatt=(double[][][][][][][]) new_d_bpmat.get(nu);
                        for (int t=0;t<bpmatt.length;t++){
                            for(int tt=0;tt<bpmatt[0].length;tt++){
                                for(int ttt=0;ttt<bpmatt[0][0].length;ttt++){
                                    for(int tttt=0;tttt<bpmatt[0][0].length;tttt++){
                                        bpmatt[t][tt][ttt][tttt][band][y][x]=((double[][][][][][][])this.d_bpmat.get(nu))[t][tt][ttt][tttt][band][y*poolSize+ny][x*poolSize+nx];

                                    }
                                }
                            }
                            ((double[][][][]) new_d_bpbias.get(nu))[t][band][y][x]=((double[][][][])this.d_bpbias.get(nu))[t][band][y*poolSize+ny][x*poolSize+nx];
                        }
                        //System.out.println(Arrays.deepToString(bpmatt));
                    }

                }
            }
        }

        this.d_bpmat.clear();
        this.d_bpmat.putAll(new_d_bpmat);
        this.d_bpbias.clear();
        this.d_bpbias.putAll(new_d_bpbias);

    }

    void pm_mean(bpHidenLayer HL, int poolSize){

        init_new(HL);

        for (int band=0;band<nBandCount;band++){
            for (int y=0;y<ySize;y++){
                for (int x=0;x<xSize;x++){
                    double n=0;
                    for (int i=0;i<poolSize;i++){
                        for (int ii=0;ii<poolSize;ii++) {
                            n+=HL.mat[band][y*ySize+i][x*xSize+ii];
                        }
                    }
                    int PS_squre=poolSize*poolSize;
                    double mean=n/PS_squre;
                    mat[band][y][x]=mean;
                    double[][] mean_mat=new double[poolSize][poolSize];
                    for (int i=0;i<poolSize;i++){
                        for (int ii=0;ii<poolSize;ii++) {
                            mean_mat[i][ii]=mean/(HL.mat[band][y*ySize+i][x*xSize+ii]);
                        }
                    }

                    for(int nu=0;nu<new_d_bpmat.size();nu++){
                        double[][][][][][][] bpmatt=(double[][][][][][][]) new_d_bpmat.get(nu);
                        for (int t=0;t<bpmatt.length;t++){
                            for(int tt=0;tt<bpmatt[0].length;tt++){
                                for(int ttt=0;ttt<bpmatt[0][0].length;ttt++){
                                    for(int tttt=0;tttt<bpmatt[0][0].length;tttt++){
                                        for (int ps1=0;ps1<poolSize;ps1++){
                                            for (int ps2=0;ps2<poolSize;ps2++){
                                                bpmatt[t][tt][ttt][tttt][band][y][x]+=((((double[][][][][][][])this.d_bpmat.get(nu))[t][tt][ttt][tttt][band][y*poolSize+ps1][x*poolSize+ps2])/mean_mat[ps1][ps2]);
                                            }
                                        }
                                        bpmatt[t][tt][ttt][tttt][band][y][x]/=PS_squre;
                                    }
                                }
                            }
                            for (int ps1=0;ps1<poolSize;ps1++){
                                for (int ps2=0;ps2<poolSize;ps2++){
                                    ((double[][][][]) new_d_bpbias.get(nu))[t][band][y][x]+=((((double[][][][])this.d_bpbias.get(nu))[t][band][y*poolSize+ps1][x*poolSize+ps2])/mean_mat[ps1][ps2]);
                                }
                            }
                            ((double[][][][]) new_d_bpbias.get(nu))[t][band][y][x]/=PS_squre;
                        }
                    }
                }
            }
        }
        this.d_bpmat.clear();
        this.d_bpmat.putAll(new_d_bpmat);
        this.d_bpbias.clear();
        this.d_bpbias.putAll(new_d_bpbias);
    }


    bpPoolMat(bpHidenLayer HL, int poolSize, String poolType,HashMap d_bpmat,HashMap d_bpbias) {

        super(HL,poolSize,poolType);

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        if (poolType=="max"){
            pm_max(HL,poolSize);
        }
        else if(poolType=="mean"){
            pm_mean(HL,poolSize);
        }
        else{
            System.out.println("illegal input,please input 'max' or 'mean'.");
        }
    }

}

class FC{                       //有问题?，没做完?
    double[] vector;
    Array_Kernel KernelCount;    //卷积核数量，决定下一隐藏层的通道数
    double[] bias;


    void biasIniting(int bias_size){
        bias=new double[bias_size];
        java.util.Random random = new java.util.Random();
        for (int i=0;i<bias_size;i++){
            double ff=0;
            while(ff<=0 || ff>1){
                ff=random.nextDouble();
            }
            this.bias[i]=ff;
        }
    }


    FC(){
        //System.out.println("please input PM or FC and proper parameter");
    }

    void progress_PM(PoolMat PM){
        for (int i=0;i<KernelCount.length;i++) {
            double n=0;
            for (int band = 0; band < PM.nBandCount; band++) {
                for (int y = 0; y < PM.ySize; y++) {
                    for (int x = 0; x < PM.xSize; x++) {
                        n += (KernelCount.AK[i][band][y][x] * PM.mat[band][y][x]);

                    }
                }
            }
            vector[i]= n+ bias[i];
        }
    }

    FC(PoolMat PM, int[] AK_size){
        int[] ak_size={AK_size[0],AK_size[1],PM.ySize};
        KernelCount= new Array_Kernel(ak_size);
        biasIniting(KernelCount.length);
        vector=new double[KernelCount.length];

        progress_PM(PM);
    }

    FC(PoolMat PM, double[][][][] AK,double[] bias){
        KernelCount=new Array_Kernel(AK);
        this.bias=bias;
        vector=new double[KernelCount.length];

        progress_PM(PM);
    }

    void progress_FC(FC fc){
        for (int i=0;i<KernelCount.length;i++) {
            double n=0;
            for (int band=0;band<fc.vector.length;band++){
                n +=KernelCount.AK[i][band][0][0]*fc.vector[band];              //bias是否共享，如果共享则直接加共享值
            }
            vector[i]=n+bias[i];
        }
    }

    FC(FC fc,int[] AK_size){
        int[] ak_size={AK_size[0],AK_size[1],1};
        KernelCount= new Array_Kernel(ak_size);
        biasIniting(KernelCount.length);
        vector=new double[KernelCount.length];

        progress_FC(fc);
    }

    FC(FC fc,double[][][][] AK,double[] bias){
        KernelCount=new Array_Kernel(AK);
        this.bias=bias;
        vector=new double[KernelCount.length];

        progress_FC(fc);
    }

    void norm(){
        double min=8192;
        double max=0;
        double span=0;
        for (int band=0;band<vector.length;band++){
            double k=vector[band];
            if (k<min){min=k;}
            if (k>max){max=k;}

        };
        span=max-min;
        double[] vec=new double[vector.length];
        for (int band=0;band<vector.length;band++){
            vec[band]=((vector[band]-min)/span);
        }
        vector=vec;
    }


    void percentify(){
        double sum=0;

        for(int band=0;band<vector.length;band++){
            sum+=vector[band];
        }

        double[] vec=new double[vector.length];
        for (int band=0;band<vector.length;band++){
            vec[band]=vector[band]/sum;
        }

        vector=vec;
    }

    void relu(){
        for (int band=0;band<vector.length;band++){
            if (vector[band]<=0){
                vector[band]=0;
            }
            else{
                continue;
            }
        }
    }

}


class bpFC extends FC{                                  //新的bpmat和bpbias没有考虑

    double[][][][][] bpmat;
    double[][] bpbias;

    HashMap d_bpmat;               //命名不是特别规范，应该表达bp中的kernel.mat
    HashMap d_bpbias;

    HashMap new_d_bpmat=new HashMap();
    HashMap new_d_bpbias=new HashMap();

    void init_new(int sw){
        bpmat=new double[KernelCount.length][KernelCount.nBandCount][KernelCount.size_Kernel][KernelCount.size_Kernel][KernelCount.length];
        bpbias=new double[KernelCount.length][KernelCount.length];
        if (sw==0){
            for(int nu=0;nu<this.d_bpmat.size();nu++){
                double[][][][][][][] bpmatt= (double[][][][][][][]) this.d_bpmat.get(nu);
                int len_1=bpmatt.length;
                int len_2=bpmatt[0].length;
                int len_3=bpmatt[0][0].length;
                new_d_bpmat.put(nu,new double[len_1][len_2][len_3][len_3][KernelCount.length]);
                new_d_bpbias.put(nu,new double[len_1][KernelCount.length]);
            }
        }
        if (sw==1){
            for(int nu=0;nu<this.d_bpmat.size();nu++){
                double[][][][][] bpmatt= (double[][][][][]) this.d_bpmat.get(nu);
                int len_1=bpmatt.length;
                int len_2=bpmatt[0].length;
                int len_3=bpmatt[0][0].length;
                new_d_bpmat.put(nu,new double[len_1][len_2][len_3][len_3][KernelCount.length]);
                new_d_bpbias.put(nu,new double[len_1][KernelCount.length]);
            }
        }
    }
    void progress_PM(bpPoolMat PM){
        init_new(0);
        for (int i=0;i<KernelCount.length;i++) {
            double n=0;
            for (int band = 0; band < PM.nBandCount; band++) {
                for (int y = 0; y < PM.ySize; y++) {
                    for (int x = 0; x < PM.xSize; x++) {
                        n += (KernelCount.AK[i][band][y][x] * PM.mat[band][y][x]);

                        bpmat[i][band][y][x][i]= PM.mat[band][y][x];                   //通用？
                        bpbias[i][i]=1;
                        //bpbias[i][i]=bias[i];

                        for(int nu=0;nu<this.d_bpmat.size();nu++){
                            double[][][][][] new_bpmatt=(double[][][][][]) new_d_bpmat.get(nu);
                            double[][][][][][][] old_bpmatt=(double[][][][][][][]) this.d_bpmat.get(nu);
                            for (int t=0;t<new_bpmatt.length;t++) {
                                for (int tt = 0; tt < new_bpmatt[0].length; tt++) {
                                    for (int ttt = 0; ttt < new_bpmatt[0][0].length; ttt++) {
                                        for (int tttt = 0; tttt < new_bpmatt[0][0].length; tttt++) {
                                            new_bpmatt[t][tt][ttt][tttt][i]+=(old_bpmatt[t][tt][ttt][tttt][band][y][x]*KernelCount.AK[i][band][y][x]);
                                        }
                                    }
                                }
                                ((double[][])new_d_bpbias.get(nu))[t][i]+=(((double[][][][])this.d_bpbias.get(nu))[t][band][y][x]*KernelCount.AK[i][band][y][x]);
                            }
                        }
                    }
                }
            }
            vector[i]= n+ bias[i];
        }

        this.d_bpmat.clear();
        this.d_bpmat.putAll(new_d_bpmat);
        this.d_bpmat.put(this.d_bpmat.size(),bpmat);
        this.d_bpbias.clear();
        this.d_bpbias.putAll(new_d_bpbias);
        this.d_bpbias.put(this.d_bpbias.size(),bpbias);

        /*this.d_bpmat=new_d_bpmat;
        this.d_bpmat.put(this.d_bpmat.size(),bpmat);
        this.d_bpbias=new_d_bpbias;
        this.d_bpbias.put(this.d_bpbias.size(),bpbias);*/

        //bpAPI.d_kenel.put(bpAPI.d_kenel.size(),KernelCount.AK);
        //bpAPI.d_bias.put(bpAPI.d_bias.size(),bias);

    }
    bpFC(bpPoolMat PM, int[] AK_size,HashMap d_bpmat,HashMap d_bpbias){

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        int[] ak_size={AK_size[0],AK_size[1],PM.ySize};
        KernelCount= new Array_Kernel(ak_size);
        biasIniting(KernelCount.length);
        vector=new double[KernelCount.length];

        progress_PM(PM);
    }

    bpFC(bpPoolMat PM, double[][][][] AK,double[] bias,HashMap d_bpmat,HashMap d_bpbias){

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        KernelCount=new Array_Kernel(AK);
        this.bias=bias;
        vector=new double[KernelCount.length];

        progress_PM(PM);
    }

    void progress_FC(bpFC fc){
        init_new(1);
        for (int i=0;i<KernelCount.length;i++) {
            double n=0;
            for (int band=0;band<fc.vector.length;band++){
                n +=KernelCount.AK[i][band][0][0]*fc.vector[band];              //bias是否共享，如果共享则直接加共享值

                bpmat[i][band][0][0][i]= fc.vector[band];                   //通用？
                bpbias[i][i]=1;
                //bpbias[i][i]=bias[i];

                for(int nu=0;nu<this.d_bpmat.size();nu++) {
                    double[][][][][] new_bpmatt = (double[][][][][]) new_d_bpmat.get(nu);
                    double[][][][][] old_bpmatt = (double[][][][][]) this.d_bpmat.get(nu);
                    for (int t = 0; t < new_bpmatt.length; t++) {
                        for (int tt = 0; tt < new_bpmatt[0].length; tt++) {
                            for (int ttt = 0; ttt < new_bpmatt[0][0].length; ttt++) {
                                for (int tttt = 0; tttt < new_bpmatt[0][0].length; tttt++) {
                                    new_bpmatt[t][tt][ttt][tttt][i]+=(old_bpmatt[t][tt][ttt][tttt][band]*KernelCount.AK[i][band][0][0]);
                                }
                            }
                        }
                        ((double[][])new_d_bpbias.get(nu))[t][i]+=(((double[][])this.d_bpbias.get(nu))[t][band]*KernelCount.AK[i][band][0][0]);
                    }
                }
            }
            vector[i]=n+bias[i];
        }

        this.d_bpmat.clear();
        this.d_bpmat.putAll(new_d_bpmat);
        this.d_bpmat.put(this.d_bpmat.size(),bpmat);
        this.d_bpbias.clear();
        this.d_bpbias.putAll(new_d_bpbias);
        this.d_bpbias.put(this.d_bpbias.size(),bpbias);

        /*this.d_bpmat=new_d_bpmat;
        this.d_bpmat.put(this.d_bpmat.size(),bpmat);
        this.d_bpbias=new_d_bpbias;
        this.d_bpbias.put(this.d_bpbias.size(),bpbias);*/

        //bpAPI.d_kenel.put(bpAPI.d_kenel.size(),KernelCount.AK);
        //bpAPI.d_bias.put(bpAPI.d_bias.size(),bias);
    }

    bpFC(bpFC fc,int[] AK_size,HashMap d_bpmat,HashMap d_bpbias){

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        int[] ak_size={AK_size[0],AK_size[1],1};
        KernelCount= new Array_Kernel(ak_size);
        biasIniting(KernelCount.length);
        vector=new double[KernelCount.length];

        progress_FC(fc);
    }

    bpFC(bpFC fc,double[][][][] AK,double[] bias,HashMap d_bpmat,HashMap d_bpbias){

        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;

        KernelCount=new Array_Kernel(AK);
        this.bias=bias;
        vector=new double[KernelCount.length];

        progress_FC(fc);
    }

    void norm(){
        double min=8192;
        double max=0;
        double span=0;
        for (int band=0;band<vector.length;band++){
            double k=vector[band];
            if (k<min){min=k;}
            if (k>max){max=k;}

        };
        span=max-min;
        double[] vec=new double[vector.length];
        for (int band=0;band<vector.length;band++){
            vec[band]=((vector[band]-min)/span);

            double multiple=vec[band]/vector[band];
            for(int nu=0;nu<this.d_bpmat.size();nu++){
                double[][][][][] norm_bpmatt=(double[][][][][]) this.d_bpmat.get(nu);
                for (int t=0;t<norm_bpmatt.length;t++){
                    for(int tt=0;tt<norm_bpmatt[0].length;tt++){
                        for(int ttt=0;ttt<norm_bpmatt[0][0].length;ttt++){
                            for(int tttt=0;tttt<norm_bpmatt[0][0].length;tttt++){
                                norm_bpmatt[t][tt][ttt][tttt][band]*=multiple;
                            }
                        }
                    }
                    ((double[][]) this.d_bpbias.get(nu))[t][band]*=multiple;
                }
            }
        }
        vector=vec;
    }

}


class OutputLayer extends FC{                   //只是多了print

    OutputLayer(PoolMat PM, int[] AK_size){
        super(PM,AK_size);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }

    OutputLayer(PoolMat PM, double[][][][] AK,double[] bias){
        super(PM,AK,bias);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }

    OutputLayer(FC fc,int[] AK_size){
        super(fc,AK_size);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }

    OutputLayer(FC fc,double[][][][] AK,double[] bias){
        super(fc,AK,bias);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }
}


class bpOutputLayer extends bpFC{

    void percentify(){
        double sum=0;

        for(int band=0;band<vector.length;band++){
            sum+=vector[band];
        }

        double[] vec=new double[vector.length];
        for (int band=0;band<vector.length;band++){
            vec[band]=vector[band]/sum;

            double multiple=vec[band]/vector[band];
            for(int nu=0;nu<this.d_bpmat.size();nu++){
                double[][][][][] norm_bpmatt=(double[][][][][]) this.d_bpmat.get(nu);
                for (int t=0;t<norm_bpmatt.length;t++){
                    for(int tt=0;tt<norm_bpmatt[0].length;tt++){
                        for(int ttt=0;ttt<norm_bpmatt[0][0].length;ttt++){
                            for(int tttt=0;tttt<norm_bpmatt[0][0].length;tttt++){
                                norm_bpmatt[t][tt][ttt][tttt][band]*=multiple;
                            }
                        }
                    }
                    ((double[][]) this.d_bpbias.get(nu))[t][band]*=multiple;
                }
            }
        }

        vector=vec;
    }

    bpOutputLayer(bpPoolMat PM, int[] AK_size,HashMap d_bpmat,HashMap d_bpbias) {
        super(PM, AK_size,d_bpmat,d_bpbias);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }

    bpOutputLayer(bpPoolMat PM, double[][][][] AK, double[] bias,HashMap d_bpmat,HashMap d_bpbias) {
        super(PM, AK, bias,d_bpmat,d_bpbias);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }

    bpOutputLayer(bpFC fc, int[] AK_size,HashMap d_bpmat,HashMap d_bpbias) {
        super(fc, AK_size,d_bpmat,d_bpbias);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }

    bpOutputLayer(bpFC fc, double[][][][] AK, double[] bias,HashMap d_bpmat,HashMap d_bpbias) {
        super(fc, AK, bias,d_bpmat,d_bpbias);
        this.percentify();
        System.out.println(Arrays.toString(vector));
    }

}

class bpFinal{

    HashMap d_bpmat;
    HashMap d_bpbias;
    HashMap d_kenel;
    HashMap d_bias;

    double[] l_mse;
    double step;

    HashMap new_d_bpmat=new HashMap();
    HashMap new_d_bpbias=new HashMap();

    void init_new(){
        for(int nu=0;nu<this.d_bpmat.size();nu++){
            double[][][][][] bpmatt= (double[][][][][]) this.d_bpmat.get(nu);
            int len_1=bpmatt.length;
            int len_2=bpmatt[0].length;
            int len_3=bpmatt[0][0].length;
            int len_4=bpmatt[0][0][0][0].length;
            new_d_bpmat.put(nu,new double[len_1][len_2][len_3][len_3]);
            new_d_bpbias.put(nu,new double[len_1]);
        }
    }

    void printmse(bpOutputLayer fc,double[] real_score){
        double countmse=0;
        //System.out.println(Arrays.toString(fc.vector));
        //System.out.println(Arrays.toString(real_score));
        for (int n=0;n<real_score.length;n++){
            //System.out.println("22222222222222222");
            //System.out.println(fc.vector[n]);
            //System.out.println(real_score[n]);
            double mse=(fc.vector[n]-real_score[n]);
            //System.out.println(mse);
            l_mse[n]=mse;
            double mmse=(0.5*mse*mse);
            //System.out.println(mmse);
            countmse+=mmse;
        }
        countmse/=real_score.length;
        //System.out.print("countmse:");
        //System.out.println(countmse);
    }

    bpFinal(bpOutputLayer fc,double[] real_score,double step,HashMap d_bpmat,HashMap d_bpbias,HashMap d_kenel,HashMap d_bias){
        this.d_bpmat=d_bpmat;
        this.d_bpbias=d_bpbias;
        this.d_kenel=d_kenel;
        this.d_bias=d_bias;


        this.step=step;
        init_new();
        l_mse=new double[real_score.length];
        printmse(fc,real_score);
        for(int nu=0;nu<this.d_bpmat.size();nu++){
            double[][][][][] bpmat=(double[][][][][])this.d_bpmat.get(nu);
            for(int t=0;t<fc.vector.length;t++){
                for(int i=0;i<bpmat.length;i++){
                    for(int band=0;band<bpmat[0].length;band++){
                        for(int y=0;y<bpmat[0][0].length;y++){
                            for(int x=0;x<bpmat[0][0].length;x++){
                                //bpmat[i][band][y][x][t]*=l_mse[t];
                                ((double[][][][])new_d_bpmat.get(nu))[i][band][y][x]+=(bpmat[i][band][y][x][t]*l_mse[t]);
                            }

                        }
                    }
                    //((double[][])bpAPI.d_bpbias.get(nu))[i][t] *=l_mse[t];
                    ((double[])new_d_bpbias.get(nu))[i]+=(((double[][])this.d_bpbias.get(nu))[i][t] *l_mse[t]);
                }
            }
        }

        this.d_bpmat.clear();
        this.d_bpmat.putAll(new_d_bpmat);

        this.d_bpbias.clear();
        this.d_bpbias.putAll(new_d_bpbias);

        //this.d_bpmat=new_d_bpmat;
        //this.d_bpbias=new_d_bpbias;
    }

    void update(){
        for(int nu=0;nu<this.d_bpmat.size();nu++){
            double[][][][] bpmat=(double[][][][])this.d_kenel.get(nu);
            double[][][][] kernel=(double[][][][])new_d_bpmat.get(nu);
            for(int i=0;i<bpmat.length;i++){
                for(int band=0;band<bpmat[0].length;band++){
                    for(int y=0;y<bpmat[0][0].length;y++){
                        for(int x=0;x<bpmat[0][0].length;x++){
                            bpmat[i][band][y][x]-=(step*kernel[i][band][y][x]);
                        }
                    }
                }
                ((double[])this.d_bias.get(nu))[i]-=(step*(((double[])this.d_bpbias.get(nu))[i]));
            }
        }
        //bpAPI.d_bpmat=new HashMap();
        //bpAPI.d_bpbias=new HashMap();
    }

    HashMap return_d_kernel(){
        return this.d_kenel;
    }

    HashMap return_d_bias(){
        return this.d_bias;
    }

}

class Net{

    Map d_kenel=new HashMap<Integer,double[][][][]>();
    Map d_bias=new HashMap<Integer,double[]>();

    Net(String namestr) {

        int[][] WC_size = {{10, 3, 5},
                {10, 10, 5},
                {10, 10, 3},
                {6,10,0},
                {3,6,0}
        };
        //long startTime =  System.currentTimeMillis();

        InputLayer IL = new InputLayer(namestr);
        HidenLayer HL_0 = new HidenLayer(IL, 1, WC_size[0]);
        d_kenel.put(d_kenel.size(),HL_0.KernelCount.AK);
        d_bias.put(d_bias.size(),HL_0.bias);
        HL_0.norm();
        //HL_0.relu();
        PoolMat PM_0=new PoolMat(HL_0,2,"max");
        HidenLayer HL_1 = new HidenLayer(PM_0, 1, WC_size[1]);
        d_kenel.put(d_kenel.size(),HL_1.KernelCount.AK);
        d_bias.put(d_bias.size(),HL_1.bias);
        HL_1.norm();
        //HL_1.relu();
        PoolMat PM_1=new PoolMat(HL_1,2,"max");
        HidenLayer HL_2 = new HidenLayer(PM_1, 1, WC_size[2]);
        d_kenel.put(d_kenel.size(),HL_2.KernelCount.AK);
        d_bias.put(d_bias.size(),HL_2.bias);
        HL_2.norm();
        //HL_2.relu();
        PoolMat PM2=new PoolMat(HL_2,2,"max");
        FC FC_0=new FC(PM2,WC_size[3]);
        d_kenel.put(d_kenel.size(),FC_0.KernelCount.AK);
        d_bias.put(d_bias.size(),FC_0.bias);
        FC_0.norm();
        OutputLayer OP=new OutputLayer(FC_0,WC_size[4]);
        d_kenel.put(d_kenel.size(),OP.KernelCount.AK);
        d_bias.put(d_bias.size(),OP.bias);

        //long spanTime =  (System.currentTimeMillis()-startTime);
        //System.out.println(spanTime);
    }

    Map return_d_kernel(){
        return this.d_kenel;
    }

    Map return_d_bias(){
        return this.d_bias;
    }

    /*Net(String namestr) {

        int[][] WC_size = {{10, 3, 5},
                {10, 10, 5},
                {10, 10, 3},
                {6,10,0},
                {3,6,0}
        };
        //long startTime =  System.currentTimeMillis();

        InputLayer IL = new InputLayer(namestr);
        HidenLayer HL_0 = new HidenLayer(IL, 1, WC_size[0]);
        bpAPI.d_kenel.put(bpAPI.d_kenel.size(),HL_0.KernelCount.AK);
        bpAPI.d_bias.put(bpAPI.d_bias.size(),HL_0.bias);
        HL_0.norm();
        //HL_0.relu();
        PoolMat PM_0=new PoolMat(HL_0,2,"max");
        HidenLayer HL_1 = new HidenLayer(PM_0, 1, WC_size[1]);
        bpAPI.d_kenel.put(bpAPI.d_kenel.size(),HL_1.KernelCount.AK);
        bpAPI.d_bias.put(bpAPI.d_bias.size(),HL_1.bias);
        HL_1.norm();
        //HL_1.relu();
        PoolMat PM_1=new PoolMat(HL_1,2,"max");
        HidenLayer HL_2 = new HidenLayer(PM_1, 1, WC_size[2]);
        bpAPI.d_kenel.put(bpAPI.d_kenel.size(),HL_2.KernelCount.AK);
        bpAPI.d_bias.put(bpAPI.d_bias.size(),HL_2.bias);
        HL_2.norm();
        //HL_2.relu();
        PoolMat PM2=new PoolMat(HL_2,2,"max");
        FC FC_0=new FC(PM2,WC_size[3]);
        bpAPI.d_kenel.put(bpAPI.d_kenel.size(),FC_0.KernelCount.AK);
        bpAPI.d_bias.put(bpAPI.d_bias.size(),FC_0.bias);
        FC_0.norm();
        OutputLayer OP=new OutputLayer(FC_0,WC_size[4]);
        bpAPI.d_kenel.put(bpAPI.d_kenel.size(),OP.KernelCount.AK);
        bpAPI.d_bias.put(bpAPI.d_bias.size(),OP.bias);

        //long spanTime =  (System.currentTimeMillis()-startTime);
        //System.out.println(spanTime);
    }*/
}



class bpNet {

    HashMap d_bpmat=new HashMap();
    HashMap d_bpbias=new HashMap();
    HashMap d_kenel=new HashMap();
    HashMap d_bias=new HashMap();
    String bpnet_name;

    int[][] kernel_mode= {{10, 3, 5},
            {10, 10, 5},
            {10, 10, 3},
            {6,10,0},
            {3,6,0}
    };
    double[] real_score={0,1,0};

    bpNet(double[][][] IL_mat,HashMap d_kenel,HashMap d_bias){

        this.d_kenel.putAll(d_kenel);
        this.d_bias.putAll(d_bias);

        bpHidenLayer bpHL_0 = new bpHidenLayer(IL_mat, 1, (double[][][][])this.d_kenel.get(0),(double[])this.d_bias.get(0),this.d_bpmat,this.d_bpbias);
        //System.out.print("0size of d_bpmat is ");
        //System.out.println(this.d_bpmat.size());
        bpHL_0.norm();
        bpPoolMat bpPM_0=new bpPoolMat(bpHL_0,2,"max",this.d_bpmat,this.d_bpbias);
        bpHidenLayer bpHL_1 = new bpHidenLayer(bpPM_0, 1, (double[][][][])this.d_kenel.get(1),(double[])this.d_bias.get(1),this.d_bpmat,this.d_bpbias);
        //System.out.print("1size of d_bpmat is ");
        //System.out.println(this.d_bpmat.size());
        bpHL_1.norm();
        bpPoolMat bpPM_1=new bpPoolMat(bpHL_1,2,"max",this.d_bpmat,this.d_bpbias);
        bpHidenLayer bpHL_2 = new bpHidenLayer(bpPM_1, 1, (double[][][][])this.d_kenel.get(2),(double[])this.d_bias.get(2),this.d_bpmat,this.d_bpbias);
        //System.out.print("2size of d_bpmat is ");
        //System.out.println(this.d_bpmat.size());
        bpHL_2.norm();
        bpPoolMat bpPM_2=new bpPoolMat(bpHL_2,2,"max",this.d_bpmat,this.d_bpbias);
        bpFC bpFC_0=new bpFC(bpPM_2,(double[][][][])this.d_kenel.get(3),(double[])this.d_bias.get(3),this.d_bpmat,this.d_bpbias);
        //System.out.print("3size of d_bpmat is ");
        //System.out.println(this.d_bpmat.size());
        bpFC_0.norm();
        bpOutputLayer bpOP=new bpOutputLayer(bpFC_0,(double[][][][])this.d_kenel.get(4),(double[])this.d_bias.get(4),this.d_bpmat,this.d_bpbias);
        //System.out.print("4size of d_bpmat is ");
        //System.out.println(this.d_bpmat.size());
        bpFinal bf=new bpFinal(bpOP,real_score,0.2,this.d_bpmat,this.d_bpbias,this.d_kenel,this.d_bias);
        bf.update();

        HidenLayer HL_0 = new HidenLayer(IL_mat, 1, (double[][][][])this.d_kenel.get(0),(double[])this.d_bias.get(0));
        HL_0.norm();
        HL_0.relu();
        PoolMat PM_0=new PoolMat(HL_0,2,"max");
        HidenLayer HL_1 = new HidenLayer(PM_0, 1, (double[][][][])this.d_kenel.get(1),(double[])this.d_bias.get(1));
        HL_1.norm();
        HL_1.relu();
        PoolMat PM_1=new PoolMat(HL_1,2,"max");
        HidenLayer HL_2 = new HidenLayer(PM_1, 1, (double[][][][])this.d_kenel.get(2),(double[])this.d_bias.get(2));
        HL_2.norm();
        HL_2.relu();
        PoolMat PM2=new PoolMat(HL_2,2,"max");
        FC FC_0=new FC(PM2,bpFC_0.KernelCount.AK,bpFC_0.bias);
        FC_0.norm();
        OutputLayer OP=new OutputLayer(FC_0,(double[][][][])this.d_kenel.get(4),(double[])this.d_bias.get(4));

    }

    bpNet(String ILnamestr,HashMap d_kenel,HashMap d_bias){

        this.d_kenel=d_kenel;
        this.d_bias=d_bias;

        InputLayer IL = new InputLayer(ILnamestr);

        bpHidenLayer bpHL_0 = new bpHidenLayer(IL, 1, (double[][][][])this.d_kenel.get(0),(double[])this.d_bias.get(0),this.d_bpmat,this.d_bpbias);
        bpHL_0.norm();
        bpPoolMat bpPM_0=new bpPoolMat(bpHL_0,2,"max",this.d_bpmat,this.d_bpbias);
        bpHidenLayer bpHL_1 = new bpHidenLayer(bpPM_0, 1, (double[][][][])this.d_kenel.get(1),(double[])this.d_bias.get(1),this.d_bpmat,this.d_bpbias);
        bpHL_1.norm();
        bpPoolMat bpPM_1=new bpPoolMat(bpHL_1,2,"max",this.d_bpmat,this.d_bpbias);
        bpHidenLayer bpHL_2 = new bpHidenLayer(bpPM_1, 1, (double[][][][])this.d_kenel.get(2),(double[])this.d_bias.get(2),this.d_bpmat,this.d_bpbias);
        bpHL_2.norm();
        bpPoolMat bpPM_2=new bpPoolMat(bpHL_2,2,"max",this.d_bpmat,this.d_bpbias);
        bpFC bpFC_0=new bpFC(bpPM_2,(double[][][][])this.d_kenel.get(3),(double[])this.d_bias.get(3),this.d_bpmat,this.d_bpbias);
        bpFC_0.norm();
        bpOutputLayer bpOP=new bpOutputLayer(bpFC_0,(double[][][][])this.d_kenel.get(4),(double[])this.d_bias.get(4),this.d_bpmat,this.d_bpbias);
        bpFinal bf=new bpFinal(bpOP,real_score,0.2,this.d_bpmat,this.d_bpbias,this.d_kenel,this.d_bias);
        bf.update();

        HidenLayer HL_0 = new HidenLayer(IL, 1, (double[][][][])this.d_kenel.get(0),(double[])this.d_bias.get(0));
        HL_0.norm();
        HL_0.relu();
        PoolMat PM_0=new PoolMat(HL_0,2,"max");
        HidenLayer HL_1 = new HidenLayer(PM_0, 1, (double[][][][])this.d_kenel.get(1),(double[])this.d_bias.get(1));
        HL_1.norm();
        HL_1.relu();
        PoolMat PM_1=new PoolMat(HL_1,2,"max");
        HidenLayer HL_2 = new HidenLayer(PM_1, 1, (double[][][][])this.d_kenel.get(2),(double[])this.d_bias.get(2));
        HL_2.norm();
        HL_2.relu();
        PoolMat PM2=new PoolMat(HL_2,2,"max");
        FC FC_0=new FC(PM2,bpFC_0.KernelCount.AK,bpFC_0.bias);
        FC_0.norm();
        OutputLayer OP=new OutputLayer(FC_0,(double[][][][])this.d_kenel.get(4),(double[])this.d_bias.get(4));

    }




}


public class bp_threads implements Runnable{


    static volatile HashMap d_kernel=new HashMap();
    static volatile HashMap d_bias=new HashMap();
    double[][][] IL_mat;
    String thread_name;
    String IL_name;

    String filename;

    bp_threads(String thread_name, double[][][] IL_mat){
        this.thread_name=thread_name;
        this.IL_mat=IL_mat;
    }

    bp_threads(String thread_name, String IL_name){
        this.thread_name=thread_name;
        this.IL_name=IL_name;
    }


    void set_filename(String filename){
        filename=this.filename;
    }

    synchronized HashMap get_kernel(){
        return bp_threads.d_kernel;
    }

    synchronized HashMap get_bias(){
        return bp_threads.d_bias;
    }


    synchronized void update_kernel_bias(HashMap d_kernel,HashMap d_bias){
        bp_threads.d_kernel=d_kernel;
        bp_threads.d_bias=d_bias;
    }

    public static double[][][] txtToMat(String txtName, int bandCount, int ySize, int xSize) throws IOException{

        double[][][] mat=new double[bandCount][ySize][xSize];
        BufferedReader in = new BufferedReader(new FileReader(txtName));
        String str;
        int band=0;
        while ((str = in.readLine()) != null) {
            System.out.println(str);
            String[] s_i=(str.split("\\]")[0]).split("\\[")[1].split("\\, ");

            for(int y=0;y<50;y++){
                for(int x=0;x<50;x++){
                    mat[band][y][x]=Integer.parseInt(s_i[50*y+x]);
                }
            }
            band+=1;
            //System.out.println(s_i[2499]);
        }

        return mat;

    }

    @Override
    public void run(){
        System.out.println(this.thread_name+" starting");
        //long startTime =  System.currentTimeMillis();
        double[][][] IL_mat = new double[4][50][50];
        try {
            IL_mat=bp_threads.txtToMat(IL_name,4,50,50);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bpNet bpn=new bpNet(IL_mat,d_kernel,d_bias);
        //bpNet bpn=new bpNet(this.IL_name,d_kernel,d_bias);
        update_kernel_bias(d_kernel,d_bias);
        //long spanTime =  (System.currentTimeMillis()-startTime);
        //System.out.print(this.thread_name+" spent ");
        //System.out.println(spanTime);
    }

    public static void main(String[] args) {

        //long startTime =  System.currentTimeMillis();
        /*String filename="H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//reaction//redo//train//jianzhu//fangkuai_657_849.txt";
        double[][][] iinn = new double[4][50][50];
        try {
            iinn=bp_threads.txtToMat(filename,4,50,50);
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        long startTime;
        Net n=new Net("H:\\java_wk\\remote_sensing_data\\uv\\uvs\\1.tif");

        d_kernel= (HashMap) n.return_d_kernel();
        d_bias= (HashMap) n.return_d_bias();

        String namestrr1="H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//reaction//redo//train//jianzhu//fangkuai_657_849.txt";
        InputLayer IL1 = new InputLayer(namestrr1);
        String namestrr2="H:\\java_wk\\remote_sensing_data\\uv\\uvs\\3.tif";
        InputLayer IL2 = new InputLayer(namestrr2);
        String namestrr3="H:\\java_wk\\remote_sensing_data\\uv\\uvs\\4.tif";
        InputLayer IL3 = new InputLayer(namestrr3);

        //bpNet bpn=new bpNet("bpnet1",IL1.mat,d_kernel,d_bias);

        //bp_threads bpthread1=new bp_threads("thread1",IL1.mat);
        //bp_threads bpthread2=new bp_threads("thread2",IL2.mat);
        //bp_threads bpthread3=new bp_threads("thread3",IL3.mat);

        bp_threads bpthread1=new bp_threads("thread1",namestrr1);
        bp_threads bpthread2=new bp_threads("thread2",namestrr2);
        bp_threads bpthread3=new bp_threads("thread3",namestrr3);

        Thread t1 = new Thread(bpthread1);
        Thread t2 = new Thread(bpthread2);
        Thread t3 = new Thread(bpthread3);

        t1.start();
        t2.start();
        t3.start();

        /*int batchsize=10;
        bpNet bpn;

        long spanTime;

        for (int batch=1;batch<=batchsize;batch++){
            startTime =  System.currentTimeMillis();
            String namestrr="H:\\java_wk\\remote_sensing_data\\uv\\uvs\\"+batch+".tif";
            bpn=new bpNet(namestrr);
            System.out.print("lenbpmat:");
            System.out.println(bpAPI.d_bpmat.size());
            System.out.print("lenbpbias:");
            System.out.println(bpAPI.d_bpbias.size());
            System.out.print("lenkernel:");
            System.out.println(bpAPI.d_kenel.size());
            System.out.print("lenbias:");
            System.out.println(bpAPI.d_bias.size());
            spanTime =  (System.currentTimeMillis()-startTime);
            System.out.println(spanTime);
        }*/
    }
}