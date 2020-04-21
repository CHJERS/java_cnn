
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.util.Arrays;

class InputLayer{
    int[][][] mat;
    int xSize;
    int ySize;
    int nBandCount;
    //int sizeofkn;
    //Kernel[] KNCount;       //卷积核数量，决定下一隐藏层的通道数
    //double[] bias;
    int step;

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

        this.mat=new int[nBandCount][ySize][xSize];

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


    InputLayer(String file){
        //String file="H:\\java_wk\\remote_sensing_data\\uv\\uvs\\1.tif";
        this.rmToMat(file);
    }


    /*
    InputLayer(int nnkn,int sizeofkn){
        this.sizeofkn=sizeofkn;
        this.rmToMat();
        this.KNCount=new Kernel[nnkn];
        for (int n=0;n<nnkn;n++){
            this.KNCount[n]=new Kernel(this.nBandCount,sizeofkn);
        }
        bias=new double[nnkn];                              //训练时先初始bias的值
        java.util.Random random = new java.util.Random();
        for (int n=0;n<nnkn;n++){
            bias[n]=random.nextGaussian();
        }
    }
    InputLayer(){
        this(2,3);
    }



    public HidenLayer forwardPropagation(int step){                  //demo网络层前驱，思路改变，原来将kernel信息放在上一层，现今吧kernel挑出来，而后放在下一层，为了后续做训练做准备
        int[][][] mat;
        int mat_ysize;
        int mat_xsize;

        //求出Hidenlayer的xsize和ysize
        int yHL;
        int yremainder=(this.ySize-this.KNCount[0].size)%step;
        if (yremainder>0){
            yHL=(this.ySize-this.KNCount[0].size)/step+1;
        }
        else{
            yHL=(this.ySize-this.KNCount[0].size)/step;
        }
        mat_ysize=this.ySize+yremainder;

        int xHL;
        int xremainder=(this.xSize-this.KNCount[0].size)%step;
        if (xremainder>0){
            xHL=(this.xSize-this.KNCount[0].size)/step+1;
        }
        else{
            xHL=(this.xSize-this.KNCount[0].size)/step;
        }
        mat_xsize=this.ySize+yremainder;
        if(yremainder>0 && xremainder>0){
            mat=new int[nBandCount][mat_ysize][mat_xsize];
            for (int band=0;band<nBandCount;band++){
                for (int y=0;y<mat_ysize;y++){
                    for (int x=0;x<mat_xsize;x++){
                        if (x<=this.xSize && y<=this.ySize){
                            mat[band][y][x]=this.mat[band][y][x];
                        }
                        else{
                            mat[band][y][x]=0;
                        }
                    }
                }

            }
        }
        else{
            mat=this.mat;
        }

        //求下一层的mat
        HidenLayer newHL=new HidenLayer(xHL,yHL,KNCount.length);

        for (int i=0;i<KNCount.length;i++){
            for (int yhl=0;yhl<yHL;yhl++){
                for (int xhl=0;xhl<xHL;xhl++){
                    double n=0;
                    for (int band=0;band<this.nBandCount;band++){
                        for (int y=0;y<this.sizeofkn;y++){
                            for (int x=0;x<this.sizeofkn;x++){
                                n+=((KNCount[i].mat[band][y][x]*(double)mat[band][yhl*step+y][xhl*step+x])+bias[i]);
                            }
                        }
                    }
                    newHL.mat[i][yhl][xhl]=(int)n;
                }
            }
        }

        return newHL;
    }*/

}
/*
class Array_Kernel{
    double[][][][] AK;
    int length;
    Kernel[] AK;
    int nBandCount;
    int size_Kernel;
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
        this.AK=new double[length][nBandCount][size_Kernel][size_Kernel];
        java.util.Random random = new java.util.Random();
        for (int l=0;l<length;l++){
            for (int i=0;i<nBandCount;i++){
                for (int n=0;n<size_Kernel;n++){
                    for (int nn=0;n<size_Kernel;nn++){
                        this.AK[l][i][n][nn]=random.nextGaussian();
                    }
                }
            }
        }
    }
}
*/
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
                for (int nn=0;n<size;nn++){
                    this.mat[i][n][nn]=random.nextGaussian();
                }
            }
        }
    }
}

class HidenLayer{
    int[][][] mat;
    int xSize;
    int ySize;
    int nBandCount;

    Array_Kernel KernelCount;
    int sizeofkn;
    Kernel[] KNCount;       //卷积核数量，决定下一隐藏层的通道数
    double[] bias;
    int step;
    int[][][] poolMat;
    HidenLayer(int nBandCount,int ySize,int xSize){
        this.ySize=ySize;
        this.xSize=xSize;
        this.nBandCount=nBandCount;
        mat=new int[nBandCount][ySize][xSize];
    }





    HidenLayer(InputLayer IL, int step, Kernel[] nkn, double[] bias){

        int[][][] mat;
        int mat_ysize;
        int mat_xsize;

        //求出Hidenlayer的xsize和ysize
        int yHL;
        int yremainder=(IL.ySize-KNCount[0].size)%step;
        if (yremainder>0){
            yHL=(IL.ySize-KNCount[0].size)/step+1;
        }
        else{
            yHL=(IL.ySize-KNCount[0].size)/step;
        }
        mat_ysize=IL.ySize+yremainder;

        int xHL;
        int xremainder=(IL.xSize-KNCount[0].size)%step;
        if (xremainder>0){
            xHL=(IL.xSize-KNCount[0].size)/step+1;
        }
        else{
            xHL=(IL.xSize-KNCount[0].size)/step;
        }
        mat_xsize=IL.ySize+yremainder;
        if(yremainder>0 && xremainder>0){
            mat=new int[IL.nBandCount][mat_ysize][mat_xsize];
            for (int band=0;band<IL.nBandCount;band++){
                for (int y=0;y<mat_ysize;y++){
                    for (int x=0;x<mat_xsize;x++){
                        if (x<=IL.xSize && y<=IL.ySize){
                            mat[band][y][x]=IL.mat[band][y][x];
                        }
                        else{
                            mat[band][y][x]=0;
                        }
                    }
                }

            }
        }
        else{
            mat=IL.mat;
        }

        //求下一层的mat
        //HidenLayer newHL=new HidenLayer(KNCount.length,xHL,yHL);
        this.ySize=yHL;
        this.xSize=xHL;
        KNCount=nkn;
        this.bias=bias;
        this.mat=new int[KNCount.length][yHL][xHL];

        for (int i=0;i<KNCount.length;i++){
            for (int yhl=0;yhl<yHL;yhl++){
                for (int xhl=0;xhl<xHL;xhl++){
                    double n=0;
                    for (int band=0;band<IL.nBandCount;band++){
                        for (int y=0;y<IL.sizeofkn;y++){
                            for (int x=0;x<IL.sizeofkn;x++){
                                n+=((KNCount[i].mat[band][y][x]*(double)mat[band][yhl*step+y][xhl*step+x])+bias[i]);
                            }
                        }
                    }
                    this.mat[i][yhl][xhl]=(int)n;
                }
            }
        }
    }

    void relu(){
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
/*
    public HidenLayer forwardPropagation(int step){
        int[][][] mat;
        int mat_ysize;
        int mat_xsize;

        //求出Hidenlayer的xsize和ysize
        int yHL;
        int yremainder=(this.ySize-this.KNCount[0].size)%step;
        if (yremainder>0){
            yHL=(this.ySize-this.KNCount[0].size)/step+1;
        }
        else{
            yHL=(this.ySize-this.KNCount[0].size)/step;
        }
        mat_ysize=this.ySize+yremainder;

        int xHL;
        int xremainder=(this.xSize-this.KNCount[0].size)%step;
        if (xremainder>0){
            xHL=(this.xSize-this.KNCount[0].size)/step+1;
        }
        else{
            xHL=(this.xSize-this.KNCount[0].size)/step;
        }
        mat_xsize=this.ySize+yremainder;
        if(yremainder>0 && xremainder>0){
            mat=new int[nBandCount][mat_ysize][mat_xsize];
            for (int band=0;band<nBandCount;band++){
                for (int y=0;y<mat_ysize;y++){
                    for (int x=0;x<mat_xsize;x++){
                        if (x<=this.xSize && y<=this.ySize){
                            mat[band][y][x]=this.mat[band][y][x];
                        }
                        else{
                            mat[band][y][x]=0;
                        }
                    }
                }

            }
        }
        else{
            mat=this.mat;
        }

        //求下一层的mat
        HidenLayer newHL=new HidenLayer(xHL,yHL,KNCount.length);

        for (int i=0;i<KNCount.length;i++){
            for (int yhl=0;yhl<yHL;yhl++){
                for (int xhl=0;xhl<xHL;xhl++){
                    double n=0;
                    for (int band=0;band<this.nBandCount;band++){
                        for (int y=0;y<this.sizeofkn;y++){
                            for (int x=0;x<this.sizeofkn;x++){
                                n+=((KNCount[i].mat[band][y][x]*(double)mat[band][yhl*step+y][xhl*step+x])+bias[i]);
                            }
                        }
                    }
                    newHL.mat[i][yhl][xhl]=(int)n;
                }
            }
        }

        return newHL;
    }
*/

}

class PoolMat{
    int[][][] mat;
    int xSize;
    int ySize;
    int nBandCount;
    PoolMat(HidenLayer HL, int poolSize, String poolType) throws Exception{
        this.xSize=HL.xSize/poolSize;
        this.ySize=HL.ySize/poolSize;
        this.nBandCount=HL.nBandCount;
        mat=new int[nBandCount][ySize][xSize];              //暂时未考虑鲁棒性
        if (poolType=="max"){
            for (int band=0;band<nBandCount;band++){
                for (int y=0;y<ySize;y++){
                    for (int x=0;x<xSize;x++){
                        int n=0;
                        for (int i=0;i<poolSize;i++){
                            for (int ii=0;ii<poolSize;ii++) {
                                if (n < mat[band][y*ySize+i][x*xSize+i]){
                                    n=mat[band][y*ySize+i][x*xSize+i];
                                }
                            }
                        }
                        mat[band][y][x]=n;
                    }
                }
            }
        }
        else if(poolType=="mean"){
            for (int band=0;band<nBandCount;band++){
                for (int y=0;y<ySize;y++){
                    for (int x=0;x<xSize;x++){
                        int n=0;
                        for (int i=0;i<poolSize;i++){
                            for (int ii=0;ii<poolSize;ii++) {
                                n+=mat[band][y*ySize+i][x*xSize+i];
                            }
                        }
                        mat[band][y][x]=n/(poolSize*poolSize);
                    }
                }
            }
        }
        else{
            throw new Exception("illegal input,please input 'max' or 'mean'.");
        }
    }
}

class FC{
    double[] vector;
    Kernel[] KNCount;
    int[] bias;
    FC(PoolMat PM,int cap){
        vector=new double[cap];
        double n=0;
        for (int i=0;i<cap;i++) {
            for (int band = 0; band < PM.nBandCount; band++) {
                for (int y = 0; y < PM.ySize; y++) {
                    for (int x = 0; x < PM.xSize; x++) {
                        n += ((KNCount[i].mat[band][y][x] * (double) PM.mat[band][y][x]) + bias[i]);

                    }
                }
            }
            vector[i]= n;
        }
    }

    FC(FC fc,int cap,int[][] weight){
        vector=new double[cap];

        for (int k=0;k<cap;k++) {
            double n=0;
            for (int i=0;i<fc.vector.length;i++){
                n +=weight[k][i]*fc.vector[i]+bias[k];              //bias是否共享，如果共享则直接加共享值
            }
            vector[k]=n;
        }
    }
}

class OutputLayer{                                                  //本质上是全连接层，为了结构单独划分
    double[] vector;
    Kernel[] KNCount;
    int[] bias;
    OutputLayer(PoolMat PM,int cap){
        vector=new double[cap];
        double n=0;
        for (int i=0;i<cap;i++) {
            for (int band = 0; band < PM.nBandCount; band++) {
                for (int y = 0; y < PM.ySize; y++) {
                    for (int x = 0; x < PM.xSize; x++) {
                        n += ((KNCount[i].mat[band][y][x] * (double) PM.mat[band][y][x]) + bias[i]);

                    }
                }
            }
            vector[i]= n;
        }
    }

    OutputLayer(FC fc,int cap,int[][] weight){
        vector=new double[cap];

        for (int k=0;k<cap;k++) {
            double n=0;
            for (int i=0;i<fc.vector.length;i++){
                n +=weight[k][i]*fc.vector[i]+bias[k];              //bias是否共享，如果共享则直接加共享值
            }
            vector[k]=n;
        }
    }
}



public class Net {





    public static void main(String[] args) {
        InputLayer n=new InputLayer();
        n.rmToMat();
        System.out.println(n.nBandCount);
        System.out.println(n.xSize);
        System.out.println(n.ySize);
        for (int i=0;i<1;i++){
            for (int j=0;j<n.ySize;j++){
                System.out.println(Arrays.toString(n.mat[i][j]));
            }
        }
    }

}
