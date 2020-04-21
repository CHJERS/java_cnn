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

public class correct {
    

    correct(){
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

        String[] dir_name={"luodi"};

        for (int dn=0;dn<dir_name.length;dn++){
            source_dirname = "H://java_wk//remote_sensing_data//sz_1m//sz1m//a___cnn//correct//wrong_luodi";
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
