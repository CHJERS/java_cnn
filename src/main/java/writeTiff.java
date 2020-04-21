import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.util.Arrays;

public class writeTiff {
    public static void main(String[] args) {
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
        int type = dataset.GetRasterBand(1).GetRasterDataType();
        // 读取仿射变换参数
        double[] im_geotrans = dataset.GetGeoTransform();
        System.out.println(Arrays.toString(im_geotrans));
        // 读取投影
        String im_proj = dataset.GetProjection();
        System.out.println(im_proj);
        /*Dataset d2 = gdal.GetDriverByName("GTiff").Create("H:\\java_wk\\java_wk\\readTiff\\createtiff\\r2.tif", 2, 3, nBandCount, type);
        d2.SetGeoTransform(im_geotrans);
        d2.SetProjection(im_proj);

        for (int j = 1; j <= nBandCount; j++) {
            Band band = dataset.GetRasterBand(j);
            int[] cache = new int[xSize];
            //band.ReadRaster(0, i, xSize, 1, cache);
            band.ReadRaster(4555, 1712, 2, 3, cache);
            Band newBand = d2.GetRasterBand(j);
            newBand.WriteRaster(0, 0, 2, 3, cache);
            newBand.FlushCache();
        }

        dataset.delete();

        /*Band band1 = d2.GetRasterBand(1);
        Band band2 = d2.GetRasterBand(2);
        Band band3 = d2.GetRasterBand(3);

        int[] buf1 = new int[6];
        int[] buf2 = new int[6];
        int[] buf3 = new int[6];
        int xoff=0;
        int yoff=0;
        band1.ReadRaster(xoff, yoff, 2, 3, buf1);
        band2.ReadRaster(xoff, yoff, 2, 3, buf2);
        band3.ReadRaster(xoff, yoff, 2, 3, buf3);
        System.out.println(Arrays.toString(buf1));
        System.out.println(Arrays.toString(buf2));
        System.out.println(Arrays.toString(buf3));*/

        //d2.delete();
        gdal.GDALDestroyDriverManager();



    }

}
