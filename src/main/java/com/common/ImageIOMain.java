package com.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImageIOMain {

    int imageWidth;
    int imageHeight;
    double ratio;
    int w;
    int h;

    private void bufferedImage(String imageUrl, String saveFile, String fileFormat ){
        try {
//        File file = new File("./pororo.jpg");
            URL url = new URL(imageUrl);
            BufferedImage bi = ImageIO.read(url);

        //자바 1.4 ImageIO를 이용한 이미지 저장
        File file = new File(saveFile);
        ImageIO.write(bi, fileFormat, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void bufferedImage(String imageUrl, String saveFile, String fileFormat ,int newWidth, int newHeight, String mainPosition){
        try {
//        File file = new File("./pororo.jpg");
            URL url = new URL(imageUrl);
            BufferedImage bi = ImageIO.read(url);

            // 원본 이미지 사이즈 가져오기
            imageWidth = bi.getWidth(null);
            imageHeight = bi.getHeight(null);

            if(mainPosition.equals("W")){    // 넓이기준

                ratio = (double)newWidth/(double)imageWidth;
                w = (int)(imageWidth * ratio);
                h = (int)(imageHeight * ratio);

            }else if(mainPosition.equals("H")){ // 높이기준

                ratio = (double)newHeight/(double)imageHeight;
                w = (int)(imageWidth * ratio);
                h = (int)(imageHeight * ratio);

            }else{ //설정값 (비율무시)

                w = newWidth;
                h = newHeight;
            }

            Image resizeImg = bi.getScaledInstance(w,h,Image.SCALE_SMOOTH); //사이즈 변경

            BufferedImage oi = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB ); //저장할 이미지 객체 생성
            Graphics g = oi.getGraphics();
            //g.drawImage(resizeImg,0,0,new Panel()); //스케일링된 이미지 그리기
            g.drawImage(resizeImg,0,0,null); //스케일링된 이미지 그리기
            g.dispose();

            File file = new File(saveFile);
            ImageIO.write(oi, fileFormat, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



     
    public static void main(String[] args){

        String imageUrl = "https://lifepharm.kr/web/product/big/20191226/c2b6707109c86eccbe300d88f20915c7.jpg";
        String filePath = "prod_img/";
        String fileName = "test.png";
        String saveFile = filePath+fileName;
        String fileFormat = "png";

        int newWidth = 600;
        int newHeight = 1000;
        String mainPosition = "W";

        ImageIOMain itm = new ImageIOMain();

        itm.bufferedImage(imageUrl, saveFile, fileFormat, newWidth, newHeight, mainPosition);
//        itm.setSize(330, 420);
//        itm.setVisible(true);



    }
}