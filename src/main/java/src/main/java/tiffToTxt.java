package src.main.java;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.io.*;
import java.util.Arrays;

public class tiffToTxt {

    static void toTxt(String sourcefile,String targetfile) throws IOException{
        gdal.AllRegister();
        String fileUrl = sourcefile;
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

        int[][] txt=new int[nBandCount][xSize*ySize];
        for (int i = 1; i <= nBandCount; i++) {
            Band band = dataset.GetRasterBand(i);
            int[] cache = new int[xSize*ySize];
            band.ReadRaster(0, 0, xSize, ySize, cache);
            txt[i-1]=cache;
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(targetfile));
        for (int i = 0; i < nBandCount; i++) {
            String ss=Arrays.toString(txt[i]);
            out.write(ss);
            out.write("\n");
        }
        out.close();
        dataset.delete();
        gdal.GDALDestroyDriverManager();
    }

    tiffToTxt(){
    }

    public  static void main(String[] args) throws IOException{
        String source_dirname ;
        File f1 ;
        String[] s;
        String target_dirname;

        String source_file;
        String target_file;


        /*source_file=source_dirname+"//fangkuai_425_410.tif";
        String target_filename="fangkuai_425_410.txt";
        target_file=target_dirname+"//"+target_filename;
        tiffToTxt.toTxt(source_file,target_file);*/

        String[] dir_name={"luodi","shuiti","jianzhu","zhibei"};

        for (int dn=0;dn<dir_name.length;dn++){
            source_dirname = "H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//correct//"+dir_name[dn];
            f1 = new File(source_dirname);
            s = f1.list();
            target_dirname="H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//correct//luodi_txt";
            for (int n=0;n<s.length;n++){
                source_file=source_dirname+"//"+s[n];
                String target_filename=((s[n].split("\\."))[0])+".txt";
                target_file=target_dirname+"//"+target_filename;
                tiffToTxt.toTxt(source_file,target_file);
            }
        }
    }

}
