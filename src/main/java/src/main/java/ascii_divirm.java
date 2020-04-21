package src.main.java;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ascii_divirm {
    public static void main(String[] args) {
        gdal.AllRegister();
        String fileUrl = "H:\\java_wk\\remote_sensing_data\\rm\\futian1.tif";
        // ??????
        Dataset dataset = gdal.Open(fileUrl, gdalconstConstants.GA_ReadOnly);
        if (dataset == null) {
            System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());
            System.err.println(gdal.GetLastErrorMsg());
            System.exit(1);
        }
        // ?????? ???????
        int xSize = dataset.getRasterXSize();
        int ySzie = dataset.getRasterYSize();
        int nBandCount = dataset.getRasterCount();
        System.out.println(xSize);
        System.out.println(ySzie);
        System.out.println(nBandCount);

        int type = dataset.GetRasterBand(1).GetRasterDataType();
        System.out.println(type);
        // ????????
        double[] im_geotrans = dataset.GetGeoTransform();
        System.out.println(Arrays.toString(im_geotrans));
        // ????
        String im_proj = dataset.GetProjection();
        System.out.println(im_proj);

        Dataset d2 = gdal.GetDriverByName("GTiff").Create("H:\\java_wk\\java_wk\\readTiff\\createtiff\\r8.tif", 3, 3, nBandCount, type);
        d2.SetGeoTransform(im_geotrans);
        d2.SetProjection(im_proj);


        for (int j = 1; j <= nBandCount; j++) {
            int[] cache = new int[9];
            Band band = dataset.GetRasterBand(j);
            band.ReadRaster(23806, 12462, 3, 3, cache);
            System.out.println(Arrays.toString(cache));
            //band.ReadRaster(4555, 1712, 2, 3, cache);
            Band newBand = d2.GetRasterBand(j);
            newBand.WriteRaster(0, 0, 3, 3, cache);
            newBand.FlushCache();
        }

        dataset.delete();

        d2.delete();
        gdal.GDALDestroyDriverManager();



    }

}
