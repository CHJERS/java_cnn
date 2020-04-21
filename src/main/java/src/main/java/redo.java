package src.main.java;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class redo {
    public static void main(String[] args) throws IOException {

        gdal.AllRegister();
        String fileUrl = "H:\\java_wk\\remote_sensing_data\\sz_1m\\sz1m\\sz_1m";
        // 读取影像数据
        Dataset dataset = gdal.Open(fileUrl, gdalconstConstants.GA_ReadOnly);
        if (dataset == null) {
            System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());
            System.err.println(gdal.GetLastErrorMsg());
            System.exit(1);
        }
        // 读取影像信息 宽、高、波段数
        int xSize = dataset.getRasterXSize();
        int ySzie = dataset.getRasterYSize();
        int nBandCount = dataset.getRasterCount();
        System.out.println(xSize);
        System.out.println(ySzie);
        System.out.println(nBandCount);

        int type = dataset.GetRasterBand(1).GetRasterDataType();
        System.out.println(type);
        // 读取仿射变换参数
        double[] im_geotrans = dataset.GetGeoTransform();
        System.out.println(Arrays.toString(im_geotrans));
        // 读取投影
        String im_proj = dataset.GetProjection();
        System.out.println(im_proj);

        Dataset d2;

        String[] dir_name={"luodi","shuiti","jianzhu","zhibei"};

        String source_dirname ;
        File f1 ;
        String[] s;
        String target_dirname="H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//reaction//redo//txt//";

        for (int dn=0;dn<dir_name.length;dn++){
            source_dirname = "H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//reaction//"+dir_name[dn];
            f1 = new File(source_dirname);
            s = f1.list();
            for (int tn=0;tn<s.length;tn++){
                String tif_name=s[tn];
                String[] loc= tif_name.split("\\.")[0].split("_");
                int y=Integer.parseInt(loc[1]);
                int x=Integer.parseInt(loc[2]);
                double[] im_geotrans_=im_geotrans.clone();
                im_geotrans_[0]+=(50*x);
                im_geotrans_[3]-=(50*y);

                String targetfile=target_dirname+dir_name[dn]+"//"+tif_name.split("\\.")[0]+".txt";
                BufferedWriter out = new BufferedWriter(new FileWriter(targetfile));
                d2 = gdal.GetDriverByName("GTiff").Create("H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//reaction//redo//tiff//"+dir_name[dn]+"//"+tif_name, 50, 50, nBandCount, type);
                d2.SetGeoTransform(im_geotrans_);
                d2.SetProjection(im_proj);
                int n_y=y*50;
                int n_x=x*50;
                for (int j = 1; j <= nBandCount; j++) {
                    int[] cache = new int[2500];
                    Band band = dataset.GetRasterBand(j);
                    band.ReadRaster(n_x, n_y, 50, 50, cache);
                    Band newBand = d2.GetRasterBand(j);
                    newBand.WriteRaster(0, 0, 50, 50, cache);
                    newBand.FlushCache();

                    out.write(Arrays.toString(cache));
                    out.write("\n");
                }
                d2.delete();
                out.close();
            }
        }
        dataset.delete();
        gdal.GDALDestroyDriverManager();



    }


}
